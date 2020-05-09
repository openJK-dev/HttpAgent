package com.sakuqi.httplibrary

import java.nio.charset.Charset

/**
 * create: by huangDianHua
 * time: 2020/5/8 15:03:14
 * description: 请求内容
 */
class HttpBody() {
    private var byteArray: ByteArray? = null

    constructor(dataByte:ByteArray):this(){
        this.byteArray = dataByte
    }

    constructor(dataStr:String):this(){
        this.byteArray = dataStr.toByteArray(Charset.forName(CHARSET))
    }

    /**
     * 返回请求内容
     */
    fun getByteArray():ByteArray{
        if(byteArray == null){
            byteArray = byteArrayOf()
        }
        return byteArray!!
    }
}