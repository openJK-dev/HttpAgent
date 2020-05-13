package com.sakuqi.httplibrary

/**
 * create: by huangDianHua
 * time: 2020/5/8 15:43:41
 * description: 网络引擎功能接口类
 */
interface IHttpEngine :HttpRequestCancel{
    fun initConfig(builder:HttpRequest.Builder)
    fun createRequest()
    fun execute(uploadCallback:((current:Long,total:Long)->Unit)? = null):ResponseData
}