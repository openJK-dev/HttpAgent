package com.sakuqi.httplibrary.request

import java.io.File

/**
 * create: by huangDianHua
 * time: 2020/5/8 15:03:14
 * description: 请求内容
 */
class HttpBody() {
    internal var file: Map<String,File>? = null
    internal var params : Map<String,String>? = null
    internal var body:String? = null
    internal var type: BodyType? = null

    constructor(file: Map<String,File>) : this() {
        this.file = file
    }

    constructor(params:Map<String,String>?=null,file: Map<String,File>?=null):this() {
        this.params = params
        this.file = file
    }

    constructor(body:String,type: BodyType = BodyType.JSON):this(){
        this.body = body
        this.type = type
    }
}

enum class BodyType{
    JSON,XML
}

