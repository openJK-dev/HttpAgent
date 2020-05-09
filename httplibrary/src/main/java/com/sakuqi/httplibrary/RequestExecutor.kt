package com.sakuqi.httplibrary

import kotlin.reflect.KClass

/**
 * create: by huangDianHua
 * time: 2020/5/9 14:22:34
 * description: 网络请求执行接口
 */
interface RequestExecutor {
    fun <T : ResponseData> executeSync(tClass: KClass<T>): T

    fun <T : ResponseData> executeAsync(tClass: KClass<T>, httpCallBack: HttpCallBack<T>):HttpRequestCancel?
}

inline fun <reified T : ResponseData> RequestExecutor.executeSync():T{
    return executeSync(T::class)
}

inline fun <reified T : ResponseData> RequestExecutor.executeAsync(httpCallBack: HttpCallBack<T>):HttpRequestCancel?{
    return executeAsync(T::class,httpCallBack)
}

interface HttpCallBack<T>{
    fun onReceivedData(result:T)
}