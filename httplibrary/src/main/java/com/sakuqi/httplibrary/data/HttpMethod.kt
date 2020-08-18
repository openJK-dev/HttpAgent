package com.sakuqi.httplibrary.data

/**
 * create: by huangDianHua
 * time: 2020/5/8 14:27:35
 * description: 网络请求方式
 */
enum class HttpMethod(val method:String) {
    POST("POST"),GET("GET"),HEAD("HEAD"),PUT("PUT"),
    DELETE("DELETE"),OPTIONS("OPTIONS"),TRACE("TRACE")
}