package com.sakuqi.httplibrary

import android.annotation.SuppressLint
import kotlin.reflect.KClass

/**
 * create: by huangDianHua
 * time: 2020/5/8 15:41:48
 * description: 网络请求代理
 */
class HttpProxy(var builder: HttpRequest.Builder) {
    private var engine: IHttpEngine = EngineFactory.createEngine()

    init {
        engine.initConfig(builder)
    }

    fun <T : ResponseData> execute(
        tClass: KClass<T>,
        getCancel: ((HttpRequestCancel) -> Unit)?,
        uploadCallback: ((current: Long, total: Long) -> Unit)? = null
    ): T {
        if (builder.url.isNullOrEmpty()) {
            return getResponseData(tClass, UNKNOWN_HOST_CODE, "url is null or empty")
        } else if (!builder.url!!.startsWith("http://") and !builder.url!!.startsWith("https://")) {
            return getResponseData(tClass, UNKNOWN_HOST_CODE, "url is error")
        }
        engine.createRequest()
        getCancel?.invoke(engine)
        val responseData = engine.execute(uploadCallback)
        return if (responseData != null) {
            getResponseData(tClass, responseData.code, responseData.data)
        } else {
            getResponseData(tClass, UNKNOWN_EXCEPTION_CODE, "未知错误")
        }
    }

    /**
     * 构建网络请求返回对象
     *
     * @param tClass
     * @param code
     * @param message
     * @param <T>
     * @return
    </T> */
    @SuppressLint("NewApi")
    private fun <T : ResponseData> getResponseData(
        tClass: KClass<T>,
        code: Int,
        message: String
    ): T {
        val constructor = tClass.constructors
        for (con in constructor) {
            val classes = con.parameters
            if (classes.size == 2) {
                try {
                    return con.call(code, message)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return tClass.objectInstance!!
    }
}