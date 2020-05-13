package com.sakuqi.httpagent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import com.sakuqi.httplibrary.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HttpAgentClient.newBuilder()
            .setAgent(OkHttpEngine::class.java)
            .setConnectTimeOut(10000)
            .setReadTimeOut(10000)
            .build()

        GlobalScope.launch {
            val result = HttpRequest.newBuilder()
                .setUrl("http://www.baidu.com")
                .setMethod(HttpMethod.GET)
                .build()
                .executeSync<ResponseData>()
            Log.d("TAG", result.toString())
        }
        var cancel = HttpRequest.newBuilder()
            .setUrl("http://www.baidu.com")
            .setMethod(HttpMethod.GET)
            .build()
            .executeAsync(object : HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                }
            })
    }

    fun loadNet(view: View) {
        var k = Environment.getExternalStorageDirectory().absolutePath.toString()
        val file = File("$k/DCIM/Camera/粘贴图片.png")
        HttpRequest.newBuilder()
            .setUrl("https://www.example.com")
            .setMethod(HttpMethod.POST)
            //.setBody(HttpBody(mapOf(Pair("keyword","苹果"))))
            .setBody(HttpBody("{\"keyword\":\"苹果\"}"))
            //.setBody(HttpBody(mapOf(Pair("hahha","value")), mapOf(Pair("file111",file))))
            .build()
            .executeAsync(object : HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                }
            }) { current, total ->
                Log.d("HttpAgent", "current = $current,total = $total")
            }
    }
}
