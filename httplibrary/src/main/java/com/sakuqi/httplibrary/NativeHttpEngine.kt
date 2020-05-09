package com.sakuqi.httplibrary

import java.io.*
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

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

    override fun createRequest() {
        openConnection()
        commonConfig()
        headerConfig()
    }

    override fun execute(): ResponseData {
        return request()
    }

    private fun openConnection() {
        try {
            val url = URL(builder.url+getUrlParams())
            mConnection = url.openConnection() as HttpURLConnection?
            if(mConnection == null){
                responseCode = CONNECTION_FAIL_CODE
            }else{
                connSuccess = true
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun getUrlParams():String{
        return builder.urlParams?.map {
            "${it.key}=${it.value}"
        }?.fold("&",{first,second->
            "$first&$second"
        })?:""
    }

    private fun commonConfig(){
        if(connSuccess){
            mConnection?.requestMethod = builder.method.method
            mConnection?.connectTimeout = RequestConfig.connectTimeout
            mConnection?.readTimeout = RequestConfig.readTimeout
        }
    }
    
    private fun headerConfig(){
        if(connSuccess){
            builder.header.forEach { (k, v) -> 
                mConnection?.addRequestProperty(k,v)
            }
        }
    }

    private fun request(): ResponseData {
        if (!connSuccess) {
            //如果连接失败，直接返回相应提示
            return getResponseData(
                responseCode,
                ""
            )
        }
        val method: HttpMethod = builder.method
        val isDoInput = method === HttpMethod.POST
        var outputStream: OutputStream? = null
        var inputStream: InputStream? = null
        var inputStreamReader: InputStreamReader? = null
        var reader: BufferedReader? = null
        var tempLine: String? = null
        val resultBuffer = StringBuilder()
        try {
            if (isDoInput) {
                mConnection!!.doOutput = true
            }
            mConnection!!.doInput = true
            mConnection!!.useCaches = false
            mConnection!!.connect()
            if (isDoInput && builder.body?.getByteArray() != null) {
                outputStream = mConnection!!.outputStream
                outputStream.write(builder.body?.getByteArray()!!)
                outputStream.flush()
            }
            responseCode = mConnection!!.responseCode
            if (responseCode >= 300) {
                inputStream = mConnection!!.errorStream
                inputStreamReader = InputStreamReader(inputStream, CHARSET)
                reader = BufferedReader(inputStreamReader)
                while (reader.readLine().also { tempLine = it } != null) {
                    resultBuffer.append(tempLine)
                }
            } else {
                inputStream = mConnection!!.inputStream
                inputStreamReader = InputStreamReader(inputStream, CHARSET)
                reader = BufferedReader(inputStreamReader)
                while (reader.readLine().also { tempLine = it } != null) {
                    resultBuffer.append(tempLine)
                }
            }
            responseMessage = resultBuffer.toString()
        } catch (e: SocketTimeoutException) {
            //连接超时提示
            responseCode = HttpURLConnection.HTTP_CLIENT_TIMEOUT
            responseMessage = e.message?:""
        } catch (e: IOException) {
            //IO异常提示
            responseCode = FILE_IO_EXCEPTION_CODE
            responseMessage = e.message?:""
        } catch (e: Exception) {
            //其他异常未知错误
            e.printStackTrace()
            responseMessage = e.message?:""
        } finally {
            close(outputStream, reader, inputStreamReader, inputStream)
        }
        return getResponseData(responseCode, responseMessage)
    }

    private fun close(
        outputStream: OutputStream?,
        reader: BufferedReader?,
        inputStreamReader: InputStreamReader?,
        inputStream: InputStream?
    ) {
        try {
            outputStream?.close()
        } catch (e: IOException) {
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
}