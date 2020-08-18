package com.sakuqi.httplibrary.engine;

import com.sakuqi.httplibrary.*
import com.sakuqi.httplibrary.data.HttpMethod
import com.sakuqi.httplibrary.data.ResponseData
import com.sakuqi.httplibrary.request.FilRequestBody
import com.sakuqi.httplibrary.utils.RequestConfig
import com.sakuqi.httplibrary.utils.UNKNOWN_EXCEPTION_CODE
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.*
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

    override fun execute(progressCallback: ProgressCallback?): ResponseData {

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
                    progressCallback
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
                    writeFileFromNetStream(response, progressCallback)
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

    private fun writeFileFromNetStream(response: Response?, progressCallback: ProgressCallback?) {
        val requestBody = response?.body ?: return
        val inputStream = requestBody.byteStream()
        val buffer = ByteArray(1024)
        var len = 0
        val bos = ByteArrayOutputStream()
        len = inputStream.read(buffer)
        val total = requestBody.contentLength()
        var current = len.toLong()
        while (len != -1) {
            bos.write(buffer, 0, len)
            len = inputStream.read(buffer)
            progressCallback?.invoke(current, total)
            current += len
        }
        bos.close()
        if (builder.savePath != null) {
            val file = File(builder.savePath!!)
            if (!file.exists()) {
                file.mkdirs()
            }
            if (builder.saveName != null) {
                val file = File(builder.savePath + File.separator + builder.saveName)
                val fos = FileOutputStream(file)
                fos.write(bos.toByteArray())
                fos.close()
            }
        }
    }

    override fun cancel() {
        call?.cancel()
    }

    companion object {
        private var client: OkHttpClient? = null
    }
}

