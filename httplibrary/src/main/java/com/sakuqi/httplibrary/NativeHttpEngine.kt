package com.sakuqi.httplibrary

import android.util.Log
import java.io.*
import java.lang.IllegalArgumentException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*
import javax.activation.MimetypesFileTypeMap

/**
 * create: by huangDianHua
 * time: 2020/5/8 15:51:51
 * description:Android 原生网络请求引擎
 */
class NativeHttpEngine : IHttpEngine {
    private var mConnection: HttpURLConnection? = null
    private lateinit var builder: HttpRequest.Builder
    private var responseCode = UNKNOWN_EXCEPTION_CODE
    private var responseMessage = ""
    private var connSuccess = false

    override fun initConfig(builder: HttpRequest.Builder) {
        this.builder = builder
    }

    override fun execute(uploadCallback: ((current: Long, total: Long) -> Unit)?): ResponseData {
        openConnection()
        commonConfig()
        headerConfig()
        return request(uploadCallback)
    }

    private fun openConnection() {
        try {
            val url = URL(builder.url + getUrlParams())
            mConnection = url.openConnection() as HttpURLConnection?
            if (mConnection == null) {
                responseCode = CONNECTION_FAIL_CODE
            } else {
                connSuccess = true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUrlParams(): String {
        return builder.urlParams?.map {
            "${it.key}=${it.value}"
        }?.fold("&", { first, second ->
            "$first&$second"
        }) ?: ""
    }

    private fun commonConfig() {
        if (connSuccess) {
            mConnection?.requestMethod = builder.method.method
            mConnection?.connectTimeout = RequestConfig.connectTimeout
            mConnection?.readTimeout = RequestConfig.readTimeout
        }
    }

    private fun headerConfig() {
        if (connSuccess) {
            builder.header.forEach { (k, v) ->
                mConnection?.setRequestProperty(k, v)
            }
            if(builder.isDownFile){
                mConnection?.setRequestProperty("Accept-Encoding", "identity");
            }
        }
    }

    private fun request(progressCallback: ((current: Long, total: Long) -> Unit)? = null): ResponseData {
        if (!connSuccess) {
            //如果连接失败，直接返回相应提示
            return getResponseData(
                responseCode,
                ""
            )
        }
        var inputStream: InputStream? = null
        var inputStreamReader: InputStreamReader? = null
        var reader: BufferedReader? = null
        var tempLine: String? = null
        val resultBuffer = StringBuilder()
        try {
            mConnection?.apply {
                useCaches = false
                doInput = true
                setRequestProperty("Connection", "keep-alive")
            }
            when (builder.method) {
                HttpMethod.POST -> {
                    doPost(mConnection!!, progressCallback)
                }
                HttpMethod.GET -> {
                    doGet(mConnection!!)
                }
            }

            responseCode = mConnection!!.responseCode
            if (responseCode >= 300) {
                inputStream = mConnection!!.errorStream
                if (inputStream != null) {
                    inputStreamReader = InputStreamReader(inputStream, CHARSET)
                    reader = BufferedReader(inputStreamReader)
                    while (reader.readLine().also { tempLine = it } != null) {
                        resultBuffer.append(tempLine)
                    }
                } else {
                    resultBuffer.append(mConnection!!.responseMessage)
                }
            } else {
                inputStream = mConnection!!.inputStream
                if (builder.isDownFile) {
                    if(builder.savePath == null || builder.saveName == null){
                        throw IllegalArgumentException("下载文件必须设置文件保存路径和文件名称")
                    }
                    val buffer = ByteArray(1024)
                    var len = 0
                    val bos = ByteArrayOutputStream()
                    len = inputStream.read(buffer)
                    val total = mConnection!!.contentLengthLong
                    var current = len.toLong()
                    while (len != -1){
                        bos.write(buffer,0,len)
                        len = inputStream.read(buffer)
                        progressCallback?.invoke(current,total)
                        current +=len
                    }
                    bos.close()
                    val file = File(builder.savePath+File.separator+builder.saveName)
                    if(!file.parentFile.exists()){
                        file?.parentFile?.mkdirs()
                    }
                    val fos = FileOutputStream(file)
                    fos.write(bos.toByteArray())
                    fos.close()
                } else {
                    inputStreamReader = InputStreamReader(inputStream, CHARSET)
                    reader = BufferedReader(inputStreamReader)
                    while (reader.readLine().also { tempLine = it } != null) {
                        resultBuffer.append(tempLine)
                    }
                }
            }
            responseMessage = resultBuffer.toString()
        } catch (e: SocketTimeoutException) {
            //连接超时提示
            responseCode = HttpURLConnection.HTTP_CLIENT_TIMEOUT
            responseMessage = e.message ?: ""
        } catch (e: IOException) {
            //IO异常提示
            responseCode = FILE_IO_EXCEPTION_CODE
            responseMessage = e.message ?: ""
        } catch (e: Exception) {
            //其他异常未知错误
            e.printStackTrace()
            responseMessage = e.message ?: ""
        } finally {
            close(mConnection, reader, inputStreamReader, inputStream)
        }
        return getResponseData(responseCode, responseMessage)
    }

    private fun doGet(connection: HttpURLConnection) {
        connection.apply {
            requestMethod = "GET"
            connect()
        }
    }

    private fun doPost(
        connection: HttpURLConnection,
        uploadCallback: ((current: Long, total: Long) -> Unit)? = null
    ) {
        connection.apply {
            doOutput = true
            useCaches = false
            requestMethod = "POST"
            setRequestProperty("Charset", "UTF-8")
            if (builder.body?.file != null) {
                setRequestProperty("Content-Type", "multipart/form-data; boundary=$BOUNDARY")
            } else if (builder.body?.params != null) {
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            } else if (builder.body?.body != null && builder.body?.type == BodyType.JSON) {
                setRequestProperty("Content-Type", "application/json")
            } else if (builder.body?.body != null && builder.body?.type == BodyType.XML) {
                setRequestProperty("Content-Type", "text/xml")
            } else {
                setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            }
            connect()
        }
        var outputStream = DataOutputStream(mConnection!!.outputStream)
        if (builder.body?.file == null) {
            var body = builder.body?.params?.map { "${it.key}=${it.value}" }?.fold("", { acc, t ->
                "$acc&$t"
            })
            if (body.isNullOrBlank()) {
                body = builder.body?.body
            }
            if (!body.isNullOrBlank()) {
                writeString(body, outputStream)
            }
        } else {
            if (builder.body?.params != null) {
                writeParams(builder.body?.params!!, outputStream)
            }
            writeFile(outputStream, uploadCallback)
        }
        outputStream.flush()
        outputStream.close()
    }

    private fun writeString(body: String, outputStream: OutputStream) {
        try {
            outputStream.write(body.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun writeParams(params: Map<String, String>, outputStream: OutputStream) {
        try {
            var paramSB = StringBuffer()
            params.forEach { (k, v) ->
                paramSB.append(PREFIX).append(BOUNDARY).append(LINE_END)
                paramSB.append("Content-Disposition: form-data; name=\"")
                    .append(k).append("\"").append(LINE_END)
                paramSB.append("Content-Type: text/plain; charset=utf-8").append(LINE_END)
                paramSB.append("Content-Transfer-Encoding: 8bit").append(LINE_END)
                paramSB.append(LINE_END)
                paramSB.append(v)
                paramSB.append(LINE_END)
            }
            outputStream.write(paramSB.toString().toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 上传文件
     */
    private fun writeFile(
        outputStream: OutputStream,
        uploadCallback: ((current: Long, total: Long) -> Unit)? = null
    ) {
        var total = 0L
        var current = 0L
        val file = builder.body?.file
        val stringBuffer = StringBuffer()
        file?.values?.forEach {
            total += it.length()
        }
        file?.forEach { k, v ->
            stringBuffer.append(PREFIX).append(BOUNDARY).append(LINE_END)
            stringBuffer.append("Content-Disposition: form-data; name=\"")
                .append(k).append("\"; filename=\"")
                .append(v.name)
                .append("\"").append(LINE_END)
            stringBuffer.append("Content-Type: ")
                .append(getContentType(v))
                .append(LINE_END)
            stringBuffer.append("Content-Transfer-Encoding: 8bit")
                .append(LINE_END)
            stringBuffer.append(LINE_END)
            outputStream.write(stringBuffer.toString().toByteArray())

            val fileInput = FileInputStream(v)
            val bufferSize = 1024
            var buffer = ByteArray(bufferSize)
            var length = fileInput.read(buffer)
            while (length != -1) {
                outputStream.write(buffer, 0, length)
                current += length
                uploadCallback?.invoke(current, total)
                length = fileInput.read(buffer)
            }
            outputStream.write(LINE_END.toByteArray())
            fileInput.close()
        }
    }

    private fun getContentType(file: File): String {
        return MimetypesFileTypeMap().getContentType(file)
    }

    private fun close(
        connection: HttpURLConnection?,
        reader: BufferedReader?,
        inputStreamReader: InputStreamReader?,
        inputStream: InputStream?
    ) {
        try {
            connection?.disconnect()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        try {
            reader?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            inputStreamReader?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        try {
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun cancel() {
        mConnection?.disconnect()
    }

    /**
     * 构建网络请求返回对象
     *
     * @param code
     * @param message
     * @return
     */
    private fun getResponseData(code: Int, message: String): ResponseData {
        return ResponseData(code, message)
    }

    companion object {
        val BOUNDARY = UUID.randomUUID().toString().toLowerCase().replace("-", "")
        val PREFIX = "--"
        val LINE_END = "\r\n"
    }
}