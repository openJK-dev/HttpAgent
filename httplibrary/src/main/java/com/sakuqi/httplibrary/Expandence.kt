package com.sakuqi.httplibrary

import android.util.Log

/**
 * create: by huangDianHua
 * time: 2020/5/9 16:59:27
 * description:
 */

fun Log.debug(tag:String = "HttpAgent",message:String){
    if(BuildConfig.DEBUG){
        Log.d(tag,message)
    }
}