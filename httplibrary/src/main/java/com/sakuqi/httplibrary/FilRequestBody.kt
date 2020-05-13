package com.sakuqi.httplibrary

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*

/**
 * create: by huangDianHua
 * time: 2020/5/13 18:34:48
 * description:
 */
class FilRequestBody(private val mRequestBody: RequestBody, val uploadCallback:((current:Long, total:Long)->Unit)?) :RequestBody(){
    private var mContentLength = 0L

    override fun contentLength(): Long {
        try{
            if(mContentLength == 0L){
                mContentLength = mRequestBody.contentLength()
            }
            return mContentLength
        }catch (e:IOException){
            e.printStackTrace()
        }
        return -1
    }
    override fun contentType(): MediaType? {
        return mRequestBody.contentType()
    }

    override fun writeTo(sink: BufferedSink) {
        val byteSink = ByteSink(sink)
        val mBufferedSink = byteSink.buffer()
        mRequestBody.writeTo(mBufferedSink)
        mBufferedSink.flush()
    }

    inner class ByteSink(delegate:Sink) : ForwardingSink(delegate) {
        private var mByteLength = 0L

        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            mByteLength += byteCount
            uploadCallback?.invoke(mByteLength,contentLength())
        }
    }
}