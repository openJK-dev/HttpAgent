package com.sakuqi.httplibrary.engine

import com.sakuqi.httplibrary.ProgressCallback
import com.sakuqi.httplibrary.HttpRequest
import com.sakuqi.httplibrary.HttpRequestCancel
import com.sakuqi.httplibrary.data.ResponseData

/**
 * create: by huangDianHua
 * time: 2020/5/8 15:43:41
 * description: 网络引擎功能接口类
 */
interface IHttpEngine : HttpRequestCancel {
    fun initConfig(builder: HttpRequest.Builder)
    fun execute(callback: ProgressCallback?): ResponseData
}