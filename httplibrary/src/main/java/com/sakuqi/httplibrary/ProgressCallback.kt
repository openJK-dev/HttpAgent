package com.sakuqi.httplibrary

/**
 * @author : DianHua huang
 * date : 2022/11/10 16:32
 * description :下载回调
 */
interface ProgressCallback {
    fun onProgress(progress: Int)
}