package com.sakuqi.httplibrary.engine

/**
 * create: by huangDianHua
 * time: 2020/5/8 15:46:57
 * description:引擎工厂
 */
object EngineFactory {
    private var engineClass: Class<out IHttpEngine>? = null
    fun setEngineClass(engineClass: Class<out IHttpEngine>?) {
        EngineFactory.engineClass = engineClass
    }

    fun createEngine(): IHttpEngine {
        if (engineClass != null) {
            try {
                return engineClass!!.newInstance()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            } catch (e: InstantiationException) {
                e.printStackTrace()
            }
        }
        return NativeHttpEngine()
    }
}
