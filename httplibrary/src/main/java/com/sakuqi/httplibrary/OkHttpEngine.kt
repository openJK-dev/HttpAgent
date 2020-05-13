package com.sakuqi.httplibrary;

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
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

    override fun execute(uploadCallback:((current:Long,total:Long)->Unit)?): ResponseData {

        var requestBody: RequestBody? = null
        if (builder.method === HttpMethod.POST) {
            if(builder.body?.file != null){
                var build = MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.body?.file!!.forEach { (k, v) ->
                    build.addFormDataPart("file",v.name,v.asRequestBody("multipart/form-data".toMediaTypeOrNull()))
                }
                if(builder.body?.params != null){
                    builder.body?.params!!.forEach { (t, u) ->
                        build.addFormDataPart(t,u)
                    }
                }
                requestBody = build.build()
                requestBody = FilRequestBody(requestBody,uploadCallback)
            }else{
                if(builder.body?.body!=null){
                    requestBody = builder.body?.body!!.toRequestBody()
                }
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

