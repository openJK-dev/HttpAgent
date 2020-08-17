package com.sakuqi.httpagent

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sakuqi.httplibrary.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

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
            //.setBody(HttpBody("{\"keyword\":\"苹果\"}"))
            .setBody(HttpBody(mapOf(Pair("hahha", "value")), mapOf(Pair("file111", file))))
            .build()
            .executeAsync(object : HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                }
            }) { current, total ->
                Log.d("HttpAgent", "current = $current,total = $total")
            }
    }

    fun downNet(view: View) {
        var k = Environment.getExternalStorageDirectory().absolutePath.toString()
        val progressBar = showProgressDialog("下载进度")
        HttpRequest.newBuilder()
            .setUrl("https://github.com/AndroidHdh/HttpAgent/archive/master.zip")
            .setMethod(HttpMethod.GET)
            .setSavePath(k,"HttpAgent.zip")
            .build()
            .executeAsync(object : HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                }
            }) { current, total ->
                Log.d("HttpAgent", "current = $current,total = $total")
                Log.w("HttpAgent","currentThread:"+Thread.currentThread().name)
                val current = (current.toInt()/202779.0)*100
                progressBar.setProgress(current.toInt(),true)
            }
    }

    fun showProgressDialog(title:String):ProgressBar{
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_view,null,false)
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .create()
        dialog.show()
        return view.findViewById<ProgressBar>(R.id.progressBar)
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
