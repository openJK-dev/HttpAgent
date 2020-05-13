package com.sakuqi.httplibrary

import kotlin.reflect.KClass

/**
 * create: by huangDianHua
 * time: 2020/5/9 14:22:34
 * description: 网络请求执行接口
 */
interface RequestExecutor {
    fun <T : ResponseData> executeSync(tClass: KClass<T>): T

    fun <T : ResponseData> executeAsync(tClass: KClass<T>, httpCallBack: HttpCallBack<T>,uploadCallback:((current:Long,total:Long)->Unit)? = null):HttpRequestCancel?
}

inline fun <reified T : ResponseData> RequestExecutor.executeSync():T{
    return executeSync(T::class)
}

inline fun <reified T : ResponseData> RequestExecutor.executeAsync(httpCallBack: HttpCallBack<T>,noinline uploadCallback:((current:Long,total:Long)->Unit)? = null):HttpRequestCancel?{
    return executeAsync(T::class,httpCallBack,uploadCallback)
}

interface HttpCallBack<T>{
    fun onReceivedData(result:T)
}