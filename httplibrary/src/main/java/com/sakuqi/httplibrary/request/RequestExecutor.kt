package com.sakuqi.httplibrary.request

import com.sakuqi.httplibrary.HttpRequestCancel
import com.sakuqi.httplibrary.ProgressCallback
import com.sakuqi.httplibrary.data.ResponseData
import kotlin.reflect.KClass

/**
 * create: by huangDianHua
 * time: 2020/5/9 14:22:34
 * description: 网络请求执行接口
 */
interface RequestExecutor {
    fun <T : ResponseData> executeSync(tClass: KClass<T>): T

    fun <T : ResponseData> executeAsync(tClass: KClass<T>, httpCallBack: HttpCallBack<T>, uploadCallback:ProgressCallback? = null): HttpRequestCancel?
}

inline fun <reified T : ResponseData> RequestExecutor.executeSync():T{
    return executeSync(T::class)
}

inline fun <reified T : ResponseData> RequestExecutor.executeAsync(httpCallBack: HttpCallBack<T>, uploadCallback:ProgressCallback? = null): HttpRequestCancel?{
    return executeAsync(T::class,httpCallBack,uploadCallback)
}

interface HttpCallBack<T>{
    fun onReceivedData(result:T)
}