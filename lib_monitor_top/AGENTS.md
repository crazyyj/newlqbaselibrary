实现兼容 Activity/Fragment/Dialog/PopupWindow 等组件的打开与关闭,
希望通过系统组件来实现,目前实现了 Activity 通过注册 ActivityLifecycleCallbacks 来监听 Activity 的打开与关闭,在注入 Activity 这个 callback 中,进行是否属于某个 Fragment 的宿主,然后来注册 FragmentLifecycleCallbacks 来监听 Fragment 的打开与关闭, 对 Activity,与 Fragment 注册另外的 Dialog/PopupWindow 的打开关闭,这部分的实现依赖与 debug 环境下的 jvmti.
目前想完美环境下实现 Activity 的打开关闭,与 Fragment 显示隐藏,与销毁的监听.

[//]: # (如果 Fragment 或者 Dialog 等依附 Activity 的容器被销毁后,将字体变小为 6sp,并且变为灰色,增加中划线, 如果 Activity 等组件被销毁,也同样处理,但是更多一点的是,对应 Activity 的 Item 要保持收起的状态.)
