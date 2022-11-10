package com.sakuqi.httplibrary.engine;

import com.sakuqi.httplibrary.ProgressCallback
import com.sakuqi.httplibrary.HttpRequest
import com.sakuqi.httplibrary.data.HttpMethod
import com.sakuqi.httplibrary.data.ResponseData
import com.sakuqi.httplibrary.request.FilRequestBody
import com.sakuqi.httplibrary.utils.RequestConfig
import com.sakuqi.httplibrary.utils.SslHandler
import com.sakuqi.httplibrary.utils.UNKNOWN_EXCEPTION_CODE
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
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
            val build = OkHttpClient.Builder()
                .connectTimeout(
                    RequestConfig.connectTimeout.toLong(),
                    TimeUnit.MILLISECONDS
                )
                .readTimeout(
                    RequestConfig.readTimeout.toLong(),
                    TimeUnit.MILLISECONDS
                )
            val sslParams = SslHandler.getSslSocketFactory(null, null, null)
            build.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager)
            client = build.build()
        }
    }

    override fun execute(callback: ProgressCallback?): ResponseData {

        var requestBody: RequestBody? = null
        if (builder.method === HttpMethod.POST) {
            if (builder.body?.file != null) {
                var build = MultipartBody.Builder().setType(MultipartBody.FORM)
                builder.body?.file!!.forEach { (k, v) ->
                    build.addFormDataPart(
                        "file",
                        v.name,
                        v.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    )
                }
                if (builder.body?.params != null) {
                    builder.body?.params!!.forEach { (t, u) ->
                        build.addFormDataPart(t, u)
                    }
                }
                requestBody = build.build()
                requestBody = FilRequestBody(
                    requestBody,
                    callback
                )
            } else if (builder.body?.params != null) {
                var build = MultipartBody.Builder().setType(MultipartBody.FORM)
                if (builder.body?.params != null) {
                    builder.body?.params!!.forEach { (t, u) ->
                        build.addFormDataPart(t, u)
                    }
                }
                requestBody = build.build()
            } else if (builder.body?.body != null) {
                requestBody = builder.body?.body!!.toRequestBody()
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
                if (builder.isDownFile) {
                    writeFileFromNetStream(response, callback)
                } else {
                    string = responseBody.string()
                }
            }
            ResponseData(
                response?.code ?: UNKNOWN_EXCEPTION_CODE,
                string
            )
        } catch (e: IOException) {
            e.printStackTrace()
            ResponseData(
                UNKNOWN_EXCEPTION_CODE,
                e.message ?: ""
            )
        }
    }

    private fun writeFileFromNetStream(response: Response, callback: ProgressCallback?) {
        var file: File? = null
        if (builder.savePath != null) {
            val filePath = File(builder.savePath!!)
            if (!filePath.exists()) {
                filePath.mkdirs()
            }
            if (builder.saveName != null) {
                file = File(builder.savePath + File.separator + builder.saveName)
            }
        }

        val buf = ByteArray(2048)
        var len = 0
        val total = response.body?.contentLength() ?: 1
        var current = 0L
        val inputStream = response.body?.byteStream()
        var fos = FileOutputStream(file)
        len = inputStream?.read(buf) ?: -1
        while (len != -1) {
            current += len
            fos.write(buf, 0, len)
            val progress = current * 100f / total
            if (progress <= 100) {
                callback?.onProgress(progress.toInt())
            }
            len = inputStream?.read(buf) ?: -1
        }
        fos.flush()
        inputStream?.close()
        fos.close()
    }

    override fun cancel() {
        call?.cancel()
    }

    companion object {
        private var client: OkHttpClient? = null
    }
}

