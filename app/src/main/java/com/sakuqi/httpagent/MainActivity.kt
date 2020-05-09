package com.sakuqi.httpagent

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.sakuqi.httplibrary.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HttpAgentClient.newBuilder()
            .setAgent(NativeHttpEngine::class.java)
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
        var cancel =HttpRequest.newBuilder()
                .setUrl("http://www.baidu.com")
                .setMethod(HttpMethod.GET)
                .build()
                .executeAsync(object :HttpCallBack<ResponseData>{
                    override fun onReceivedData(result: ResponseData) {
                        Log.d("TAG", result.toString())
                    }
                })

        GlobalScope.launch {
            delay(8*1000)
            cancel?.cancel()
        }
    }
}
