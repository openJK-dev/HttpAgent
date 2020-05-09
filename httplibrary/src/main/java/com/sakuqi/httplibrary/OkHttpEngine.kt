package com.sakuqi.httplibrary;

import okhttp3.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * create: by huangDianHua
 * time: 2020/4/21 17:51:48
 * description:
 */

class OkHttpEngine : IHttpEngine {
    private var request: Request? = null
    private lateinit var builder: HttpRequest.Builder
    private var call: Call? = null

    override fun initConfig(httpBuilder: HttpRequest.Builder) {
        this.builder = httpBuilder
        if (client == null) {
            client = OkHttpClient.Builder()
                .connectTimeout(
                    RequestConfig.connectTimeout.toLong(),
                    TimeUnit.MILLISECONDS
                )
                .readTimeout(
                    RequestConfig.readTimeout.toLong(),
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
    }

    override fun createRequest() {
        var requestBody: RequestBody? = null
        if (builder.method === HttpMethod.POST) {
            builder.body?.let {
                requestBody = RequestBody.create(null, it.getByteArray())
            }
        }
        val headers = Headers.Builder()
        val hashMap: Map<String, String> = builder.header
        for ((key, value) in hashMap) {
            headers.add(key, value)
        }
        request = Request.Builder()
            .url(builder.url ?: "")
            .method(builder.method.method, requestBody)
            .headers(headers.build())
            .build()
    }

    override fun execute(): ResponseData {
        return try {
            call = client!!.newCall(request!!)
            val response = call?.execute()
            val responseBody = response?.body
            var string = ""
            if (responseBody != null) {
                string = responseBody.string()
            }
            ResponseData(response?.code ?: UNKNOWN_EXCEPTION_CODE, string)
        } catch (e: IOException) {
            e.printStackTrace()
            ResponseData(UNKNOWN_EXCEPTION_CODE, e.message ?: "")
        }
    }

    override fun cancel() {
        call?.cancel()
    }

    companion object {
        private var client: OkHttpClient? = null
    }
}

