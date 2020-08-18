package com.sakuqi.httplibrary

import com.sakuqi.httplibrary.data.HttpMethod
import com.sakuqi.httplibrary.data.ResponseData
import com.sakuqi.httplibrary.request.HttpBody
import com.sakuqi.httplibrary.request.HttpCallBack
import com.sakuqi.httplibrary.request.HttpProxy
import com.sakuqi.httplibrary.request.RequestExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.reflect.KClass

/**
 * create: by huangDianHua
 * time: 2020/5/8 14:43:49
 * description: 网络请求入口
 */

typealias ProgressCallback = (current: Long, total: Long) -> Unit

class HttpRequest internal constructor(var builder: Builder) :
    RequestExecutor {
    override fun <T : ResponseData> executeSync(tClass: KClass<T>): T {
        return HttpProxy(builder)
            .execute(tClass, null, null)
    }

    override fun <T : ResponseData> executeAsync(
        tClass: KClass<T>,
        httpCallBack: HttpCallBack<T>,
        progressCallback: ProgressCallback?
    ): HttpRequestCancel? {
        var httpRequestCancelSub: HttpRequestCancel? = null
        var jobMain: Job? = null
        var jobIO: Job? = null
        var httpRequestCancel: HttpRequestCancel = object : HttpRequestCancel {
            override fun cancel() {
                httpRequestCancelSub?.cancel()
                jobMain?.cancel()
                jobIO?.cancel()
            }
        }
        jobIO = CoroutineScope(Dispatchers.IO).launch {
            val result = HttpProxy(builder)
                .execute(tClass, getCancel = {
                httpRequestCancelSub = it
            }, uploadCallback = { current, total ->
                CoroutineScope(Dispatchers.Main).launch {
                    progressCallback?.invoke(current, total)
                }
            })
            jobMain = CoroutineScope(Dispatchers.Main).launch {
                httpCallBack.onReceivedData(result)
            }
        }
        return httpRequestCancel
    }

    companion object {
        @JvmStatic
        fun newBuilder(): Builder {
            return Builder()
        }
    }

    class Builder {
        internal var url: String? = null
        internal var method: HttpMethod = HttpMethod.GET
        internal var header: Map<String, String> = mapOf()
        internal var body: HttpBody? = null
        internal var urlParams: Map<String, Any>? = null
        internal var savePath: String? = null
        internal var saveName: String? = null
        internal var isDownFile = false

        fun setUrl(url: String): Builder {
            this.url = url
            return this
        }

        fun setMethod(method: HttpMethod): Builder {
            this.method = method
            return this
        }

        fun setHeader(header: Map<String, String>): Builder {
            this.header = header
            return this
        }

        fun setBody(body: HttpBody): Builder {
            this.body = body
            return this
        }

        fun setUrlParams(urlParams: Map<String, Any>?): Builder {
            this.urlParams = urlParams
            return this
        }

        fun setSavePath(savePath: String, saveName: String): Builder {
            this.saveName = saveName
            this.savePath = savePath
            isDownFile = true
            return this
        }

        fun build(): HttpRequest {
            return HttpRequest(this)
        }
    }
}