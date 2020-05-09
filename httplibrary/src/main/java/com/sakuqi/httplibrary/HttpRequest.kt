package com.sakuqi.httplibrary

import kotlinx.coroutines.*
import kotlin.reflect.KClass

/**
 * create: by huangDianHua
 * time: 2020/5/8 14:43:49
 * description: 网络请求入口
 */

class HttpRequest internal constructor(var builder: Builder) : RequestExecutor {

    override fun <T : ResponseData> executeSync(tClass: KClass<T>): T {
        return HttpProxy(builder).execute(tClass, null)
    }

    override fun <T : ResponseData> executeAsync(
        tClass: KClass<T>,
        httpCallBack: HttpCallBack<T>
    ): HttpRequestCancel? {
        var httpRequestCancelSub:HttpRequestCancel?=null
        var jobMain: Job? = null
        var jobIO: Job? = null
        var httpRequestCancel: HttpRequestCancel = object : HttpRequestCancel {
            override fun cancel() {
                httpRequestCancelSub?.cancel()
                jobMain?.cancel()
                jobIO?.cancel()
            }
        }
        jobIO = GlobalScope.launch(Dispatchers.IO) {
            delay(5*1000)
            val result = HttpProxy(builder).execute(tClass) {
                httpRequestCancelSub = it
            }
            jobMain = GlobalScope.launch(Dispatchers.Main) {
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

        fun build(): HttpRequest {
            return HttpRequest(this)
        }
    }
}