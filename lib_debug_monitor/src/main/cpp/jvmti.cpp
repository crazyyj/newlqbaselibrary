#include <jni.h>
#include <jvmti.h>
#include <android/log.h>
#include <string>
#include <vector>
#include <deque>
#include <mutex>
#include <condition_variable>
#include <thread>
#include <atomic>
#include <algorithm>

#define LOG_TAG "JVMTI_AGENT"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

struct MethodEvent {
    bool isEntry = true;
    std::string className;
    std::string methodName;
    std::string methodDesc;
    jclass declaringClassGlobal = nullptr;
};

struct MethodInterest {
    jclass baseClassGlobal = nullptr;
    jobject methodRefGlobal = nullptr;
    std::string methodName;
    std::string methodDesc;
    bool includeSubclasses = false;
};

static JavaVM* g_vm = nullptr;
static jvmtiEnv* g_jvmti = nullptr;
static jclass g_debugStackMotionClass = nullptr;
static jmethodID g_onMethodVisitMethod = nullptr;
static jmethodID g_onVariableVisitMethod = nullptr;

static std::mutex g_interestMutex;
static std::vector<MethodInterest> g_interestedMethods;

static std::mutex g_queueMutex;
static std::condition_variable g_queueCv;
static std::deque<MethodEvent> g_eventQueue;
static std::atomic<bool> g_consumerShouldRun(false);
static std::thread g_consumerThread;

static std::string NormalizeClassName(const char* signature) {
    if (signature == nullptr) {
        return "";
    }
    std::string result(signature);
    if (!result.empty()) {
        if (result[0] == 'L' && result.back() == ';') {
            result = result.substr(1, result.size() - 2);
        } else if (result[0] == '[' && result.size() > 2 && result[1] == 'L' && result.back() == ';') {
            std::string component = result.substr(2, result.size() - 3);
            std::replace(component.begin(), component.end(), '/', '.');
            return "[L" + component + ";";
        }
    }
    std::replace(result.begin(), result.end(), '/', '.');
    return result;
}

static bool ensureDebugStackMotionClass(JNIEnv* env) {
    if (g_debugStackMotionClass != nullptr &&
        g_onMethodVisitMethod != nullptr &&
        g_onVariableVisitMethod != nullptr) {
        return true;
    }
    jclass localClass = env->FindClass("com/newchar/jvmti/DebugStackMotion");
    if (localClass == nullptr) {
        if (env->ExceptionCheck()) {
            env->ExceptionClear();
        }
        LOGE("Failed to find DebugStackMotion class");
        return false;
    }
    g_debugStackMotionClass = reinterpret_cast<jclass>(env->NewGlobalRef(localClass));
    env->DeleteLocalRef(localClass);
    if (g_debugStackMotionClass == nullptr) {
        LOGE("Failed to create global ref for DebugStackMotion class");
        return false;
    }
    g_onMethodVisitMethod = env->GetStaticMethodID(g_debugStackMotionClass,
            "onMethodVisit", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Z)V");
    g_onVariableVisitMethod = env->GetStaticMethodID(g_debugStackMotionClass,
            "onVariableVisit",
            "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Z)V");
    if (g_onMethodVisitMethod == nullptr || g_onVariableVisitMethod == nullptr) {
        LOGE("Failed to cache DebugStackMotion methods");
        return false;
    }
    return true;
}

static void releaseDebugStackMotionClass(JNIEnv* env) {
    if (g_debugStackMotionClass != nullptr) {
        env->DeleteGlobalRef(g_debugStackMotionClass);
        g_debugStackMotionClass = nullptr;
    }
    g_onMethodVisitMethod = nullptr;
    g_onVariableVisitMethod = nullptr;
}

static void dispatchEvent(JNIEnv* env, const MethodEvent& event) {
    if (!ensureDebugStackMotionClass(env)) {
        return;
    }
    jstring className = env->NewStringUTF(event.className.c_str());
    jstring methodName = env->NewStringUTF(event.methodName.c_str());
    jstring methodDesc = env->NewStringUTF(event.methodDesc.c_str());
    env->CallStaticVoidMethod(g_debugStackMotionClass, g_onMethodVisitMethod,
                              className, methodName, methodDesc, event.isEntry ? JNI_TRUE : JNI_FALSE);
    env->DeleteLocalRef(className);
    env->DeleteLocalRef(methodName);
    env->DeleteLocalRef(methodDesc);
}

static bool isInterested(JNIEnv* env, const MethodEvent& event) {
    std::lock_guard<std::mutex> lock(g_interestMutex);
    for (const MethodInterest& interest : g_interestedMethods) {
        if (interest.baseClassGlobal == nullptr) {
            continue;
        }
        bool classMatch = false;
        if (interest.includeSubclasses) {
            classMatch = env->IsAssignableFrom(event.declaringClassGlobal, interest.baseClassGlobal);
        } else {
            classMatch = env->IsSameObject(event.declaringClassGlobal, interest.baseClassGlobal);
        }
        if (!classMatch) {
            continue;
        }
        if (event.methodName == interest.methodName && event.methodDesc == interest.methodDesc) {
            return true;
        }
    }
    return false;
}

static void enqueueMethodEvent(JNIEnv* env,
                               bool isEntry,
                               jclass declaringClass,
                               const char* classSignature,
                               const char* methodName,
                               const char* methodDesc) {
    if (declaringClass == nullptr || methodName == nullptr || methodDesc == nullptr) {
        return;
    }
    MethodEvent event;
    event.isEntry = isEntry;
    event.className = NormalizeClassName(classSignature);
    event.methodName = methodName;
    event.methodDesc = methodDesc;
    event.declaringClassGlobal = reinterpret_cast<jclass>(env->NewGlobalRef(declaringClass));
    {
        std::lock_guard<std::mutex> lock(g_queueMutex);
        g_eventQueue.emplace_back(std::move(event));
    }
    g_queueCv.notify_one();
}

static void clearEventQueue(JNIEnv* env) {
    std::lock_guard<std::mutex> lock(g_queueMutex);
    for (MethodEvent& event : g_eventQueue) {
        if (event.declaringClassGlobal != nullptr) {
            env->DeleteGlobalRef(event.declaringClassGlobal);
            event.declaringClassGlobal = nullptr;
        }
    }
    g_eventQueue.clear();
}

static void consumerLoop() {
    JNIEnv* env = nullptr;
    if (g_vm == nullptr ||
        g_vm->AttachCurrentThread(reinterpret_cast<void**>(&env), nullptr) != JNI_OK) {
        LOGE("Failed to attach consumer thread to JVM");
        g_consumerShouldRun.store(false);
        return;
    }
    while (true) {
        MethodEvent event;
        {
            std::unique_lock<std::mutex> lock(g_queueMutex);
            g_queueCv.wait(lock, [] {
                return !g_eventQueue.empty() || !g_consumerShouldRun.load();
            });
            if (!g_consumerShouldRun.load() && g_eventQueue.empty()) {
                break;
            }
            event = std::move(g_eventQueue.front());
            g_eventQueue.pop_front();
        }
        if (event.declaringClassGlobal == nullptr) {
            continue;
        }
        if (isInterested(env, event)) {
            dispatchEvent(env, event);
        }
        env->DeleteGlobalRef(event.declaringClassGlobal);
    }
    g_vm->DetachCurrentThread();
}

static void startConsumerThread() {
    bool expected = false;
    if (!g_consumerShouldRun.compare_exchange_strong(expected, true)) {
        return;
    }
    g_consumerThread = std::thread(consumerLoop);
}

static void stopConsumerThread(JNIEnv* env) {
    if (!g_consumerShouldRun.exchange(false)) {
        clearEventQueue(env);
        return;
    }
    g_queueCv.notify_all();
    if (g_consumerThread.joinable()) {
        g_consumerThread.join();
    }
    clearEventQueue(env);
}

static void releaseInterestedMethods(JNIEnv* env) {
    std::lock_guard<std::mutex> lock(g_interestMutex);
    for (MethodInterest& interest : g_interestedMethods) {
        if (interest.baseClassGlobal != nullptr) {
            env->DeleteGlobalRef(interest.baseClassGlobal);
            interest.baseClassGlobal = nullptr;
        }
        if (interest.methodRefGlobal != nullptr) {
            env->DeleteGlobalRef(interest.methodRefGlobal);
            interest.methodRefGlobal = nullptr;
        }
    }
    g_interestedMethods.clear();
}

static void disableJvmtiEvents() {
    if (g_jvmti == nullptr) {
        return;
    }
    g_jvmti->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_METHOD_ENTRY, nullptr);
    g_jvmti->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_METHOD_EXIT, nullptr);
    g_jvmti->SetEventNotificationMode(JVMTI_DISABLE, JVMTI_EVENT_FIELD_MODIFICATION, nullptr);
}

// JVMTI Event Callbacks
void JNICALL onMethodEntry(jvmtiEnv* jvmti, JNIEnv* env, jthread /*thread*/, jmethodID method) {
    char* methodName = nullptr;
    char* methodDesc = nullptr;
    char* classSignature = nullptr;
    jclass declaringClass = nullptr;

    if (jvmti->GetMethodName(method, &methodName, &methodDesc, nullptr) != JVMTI_ERROR_NONE) {
        return;
    }
    if (jvmti->GetMethodDeclaringClass(method, &declaringClass) != JVMTI_ERROR_NONE) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
        return;
    }
    if (jvmti->GetClassSignature(declaringClass, &classSignature, nullptr) != JVMTI_ERROR_NONE) {
        env->DeleteLocalRef(declaringClass);
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
        return;
    }

    enqueueMethodEvent(env, true, declaringClass, classSignature, methodName, methodDesc == nullptr ? "" : methodDesc);

    env->DeleteLocalRef(declaringClass);
    if (methodName != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
    }
    if (methodDesc != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
    }
    if (classSignature != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(classSignature));
    }
}

void JNICALL onMethodExit(jvmtiEnv* jvmti, JNIEnv* env, jthread /*thread*/, jmethodID method,
                          jboolean /*wasPoppedByException*/, jvalue /*return_value*/) {
    char* methodName = nullptr;
    char* methodDesc = nullptr;
    char* classSignature = nullptr;
    jclass declaringClass = nullptr;

    if (jvmti->GetMethodName(method, &methodName, &methodDesc, nullptr) != JVMTI_ERROR_NONE) {
        return;
    }
    if (jvmti->GetMethodDeclaringClass(method, &declaringClass) != JVMTI_ERROR_NONE) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
        return;
    }
    if (jvmti->GetClassSignature(declaringClass, &classSignature, nullptr) != JVMTI_ERROR_NONE) {
        env->DeleteLocalRef(declaringClass);
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
        return;
    }

    enqueueMethodEvent(env, false, declaringClass, classSignature, methodName, methodDesc == nullptr ? "" : methodDesc);

    env->DeleteLocalRef(declaringClass);
    if (methodName != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
    }
    if (methodDesc != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
    }
    if (classSignature != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(classSignature));
    }
}

void JNICALL onFieldModification(jvmtiEnv* jvmti, JNIEnv* env, jthread /*thread*/, jmethodID method,
                                 jlocation /*location*/, jclass field_klass, jobject /*object*/, jfieldID field,
                                 char signature_type, jvalue new_value) {
    char* methodName = nullptr;
    char* classSignature = nullptr;
    char* methodDesc = nullptr;
    char* fieldName = nullptr;

    if (jvmti->GetMethodName(method, &methodName, &methodDesc, nullptr) != JVMTI_ERROR_NONE) {
        return;
    }
    if (jvmti->GetMethodDeclaringClass(method, &field_klass) != JVMTI_ERROR_NONE) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
        return;
    }
    if (jvmti->GetClassSignature(field_klass, &classSignature, nullptr) != JVMTI_ERROR_NONE) {
        env->DeleteLocalRef(field_klass);
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
        return;
    }
    if (jvmti->GetFieldName(field_klass, field, &fieldName, nullptr, nullptr) != JVMTI_ERROR_NONE) {
        env->DeleteLocalRef(field_klass);
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(classSignature));
        return;
    }

    if (ensureDebugStackMotionClass(env)) {
        std::string normalizedClass = NormalizeClassName(classSignature);
        jstring classNameStr = env->NewStringUTF(normalizedClass.c_str());
        jstring methodNameStr = env->NewStringUTF(methodName);
        jstring methodDescStr = env->NewStringUTF(methodDesc == nullptr ? "" : methodDesc);
        jstring fieldNameStr = env->NewStringUTF(fieldName);
        jobject valueObj = nullptr;
        if (signature_type == 'I') {
            jclass integerClass = env->FindClass("java/lang/Integer");
            if (integerClass != nullptr) {
                jmethodID intCtor = env->GetMethodID(integerClass, "<init>", "(I)V");
                if (intCtor != nullptr) {
                    valueObj = env->NewObject(integerClass, intCtor, new_value.i);
                }
                env->DeleteLocalRef(integerClass);
            }
        }
        env->CallStaticVoidMethod(g_debugStackMotionClass, g_onVariableVisitMethod,
                                  classNameStr, methodNameStr, methodDescStr, fieldNameStr, valueObj, JNI_TRUE);
        env->DeleteLocalRef(classNameStr);
        env->DeleteLocalRef(methodNameStr);
        env->DeleteLocalRef(methodDescStr);
        env->DeleteLocalRef(fieldNameStr);
        if (valueObj != nullptr) {
            env->DeleteLocalRef(valueObj);
        }
    }

    env->DeleteLocalRef(field_klass);
    if (methodName != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodName));
    }
    if (methodDesc != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(methodDesc));
    }
    if (classSignature != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(classSignature));
    }
    if (fieldName != nullptr) {
        jvmti->Deallocate(reinterpret_cast<unsigned char*>(fieldName));
    }
}

// JVMTI Agent Entry
JNIEXPORT jint JNICALL Agent_OnAttach(JavaVM* vm, char* /*options*/, void* /*reserved*/) {
    g_vm = vm;
    JNIEnv* env = nullptr;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK || env == nullptr) {
        LOGE("Failed to get JNIEnv");
        return JNI_ERR;
    }
    if (vm->GetEnv(reinterpret_cast<void**>(&g_jvmti), JVMTI_VERSION_1_2) != JNI_OK || g_jvmti == nullptr) {
        LOGE("Failed to get jvmtiEnv");
        return JNI_ERR;
    }

    jvmtiCapabilities caps = {0};
    caps.can_generate_method_entry_events = 1;
    caps.can_generate_method_exit_events = 1;
    caps.can_generate_field_modification_events = 1;
    jvmtiError err = g_jvmti->AddCapabilities(&caps);
    if (err != JVMTI_ERROR_NONE) {
        LOGE("AddCapabilities failed: %d", err);
        return JNI_ERR;
    }

    jvmtiEventCallbacks callbacks = {0};
    callbacks.MethodEntry = &onMethodEntry;
    callbacks.MethodExit = &onMethodExit;
    callbacks.FieldModification = &onFieldModification;
    err = g_jvmti->SetEventCallbacks(&callbacks, sizeof(callbacks));
    if (err != JVMTI_ERROR_NONE) {
        LOGE("SetEventCallbacks failed: %d", err);
        return JNI_ERR;
    }

    err = g_jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_ENTRY, nullptr);
    if (err != JVMTI_ERROR_NONE) {
        LOGE("Enable METHOD_ENTRY failed: %d", err);
        return JNI_ERR;
    }

    err = g_jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_METHOD_EXIT, nullptr);
    if (err != JVMTI_ERROR_NONE) {
        LOGE("Enable METHOD_EXIT failed: %d", err);
        return JNI_ERR;
    }

    err = g_jvmti->SetEventNotificationMode(JVMTI_ENABLE, JVMTI_EVENT_FIELD_MODIFICATION, nullptr);
    if (err != JVMTI_ERROR_NONE) {
        LOGE("Enable FIELD_MODIFICATION failed: %d", err);
        return JNI_ERR;
    }

    startConsumerThread();
    LOGI("JVMTI Agent attached");
    return JNI_OK;
}

// JNI 方法实现
extern "C" JNIEXPORT void JNICALL
Java_com_newchar_jvmti_DebugStackMotion_registerMethodNative(JNIEnv* env, jclass /*clazz*/,
                                                                jclass baseClass, jobject methodObj,
                                                                jboolean includeSubclasses) {
    if (baseClass == nullptr || methodObj == nullptr || g_jvmti == nullptr) {
        return;
    }

    jmethodID methodId = env->FromReflectedMethod(methodObj);
    if (methodId == nullptr) {
        return;
    }

    char* name = nullptr;
    char* desc = nullptr;
    if (g_jvmti->GetMethodName(methodId, &name, &desc, nullptr) != JVMTI_ERROR_NONE) {
        return;
    }

    MethodInterest interest;
    interest.baseClassGlobal = reinterpret_cast<jclass>(env->NewGlobalRef(baseClass));
    interest.methodRefGlobal = env->NewGlobalRef(methodObj);
    interest.methodName = name == nullptr ? "" : name;
    interest.methodDesc = desc == nullptr ? "" : desc;
    interest.includeSubclasses = includeSubclasses == JNI_TRUE;

    {
        std::lock_guard<std::mutex> lock(g_interestMutex);
        g_interestedMethods.emplace_back(std::move(interest));
    }

    if (name != nullptr) {
        g_jvmti->Deallocate(reinterpret_cast<unsigned char*>(name));
    }
    if (desc != nullptr) {
        g_jvmti->Deallocate(reinterpret_cast<unsigned char*>(desc));
    }
}

extern "C" JNIEXPORT void JNICALL
Java_com_newchar_jvmti_DebugStackMotion_clearRegisteredMethodsNative(JNIEnv* env, jclass /*clazz*/) {
    releaseInterestedMethods(env);
}

extern "C" JNIEXPORT void JNICALL
Java_com_newchar_jvmti_DebugStackMotionAgent_startAgentNative(JNIEnv* /*env*/, jclass /*clazz*/) {
    LOGI("startAgentNative called");
}

extern "C" JNIEXPORT void JNICALL
Java_com_newchar_jvmti_DebugStackMotionAgent_stopAgentNative(JNIEnv* /*env*/, jclass /*clazz*/) {
    LOGI("stopAgentNative called");
}

extern "C" JNIEXPORT void JNICALL
Java_com_newchar_jvmti_DebugStackMotion_releaseNative(JNIEnv* env, jclass /*clazz*/) {
    stopConsumerThread(env);
    releaseInterestedMethods(env);
    releaseDebugStackMotionClass(env);
    disableJvmtiEvents();
    LOGI("releaseNative called");
}
