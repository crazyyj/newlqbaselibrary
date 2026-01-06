一个开发基类库(工具类, 父类, 常用View)

lib_jvmti 
这个模块主要是使用 jvmti 接口,在 Android app 上实现方法的入栈与出栈,预计变量修改的监控,让输出到 UI 上.
首先,这个DebugStackMotion.java 提供记录 类,与方法 Method的方法,讲对象设置到 jvmti.cpp 中进行记录,在 Android app 中,启动 agent 启动后,执行方法监听.
在设置记录类 和 对应的方法后, 然后还有设置回调,这个回调的回调方法,是每个 class 与 method 对象. 
要有 release 方法,主要是对设置进来的 callback 进行释放,防止内存泄漏.
- 对添加的方法添加注释
- 在修改之后,执行编译,并修改编译失败的位置