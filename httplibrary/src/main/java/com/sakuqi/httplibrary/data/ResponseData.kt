package com.sakuqi.httplibrary.data

/**
 * create: by huangDianHua
 * time: 2020/5/8 15:34:52
 * description: 网络请求结果
 */
open class ResponseData(var code:Int,var data:String){
    override fun toString(): String {
        return "ResponseData(code=$code, data='$data')"
    }
}