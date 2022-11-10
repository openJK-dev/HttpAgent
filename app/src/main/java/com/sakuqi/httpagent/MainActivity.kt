package com.sakuqi.httpagent

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.sakuqi.httplibrary.*
import com.sakuqi.httplibrary.data.HttpMethod
import com.sakuqi.httplibrary.data.ResponseData
import com.sakuqi.httplibrary.engine.OkHttpEngine
import com.sakuqi.httplibrary.request.HttpBody
import com.sakuqi.httplibrary.request.HttpCallBack
import com.sakuqi.httplibrary.request.executeAsync
import com.sakuqi.httplibrary.request.executeSync
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.HttpURLConnection
import java.net.Proxy

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        HttpAgentClient.newBuilder()
            .setAgent(OkHttpEngine::class.java)
            .setConnectTimeOut(10000)
            .setReadTimeOut(10000)
            .build()
    }

    fun loadNet(view: View) {
        var k = Environment.getExternalStorageDirectory().absolutePath.toString()
        val file = File("$k/DCIM/Camera/粘贴图片.png")
        HttpRequest.newBuilder()
            .setUrl("https://www.example.com")
            .setMethod(HttpMethod.POST)
            //.setBody(HttpBody(mapOf(Pair("keyword","苹果"))))
            //.setBody(HttpBody("{\"keyword\":\"苹果\"}"))
            .setBody(
                HttpBody(
                    mapOf(
                        Pair(
                            "hahha",
                            "value"
                        )
                    ), mapOf(Pair("file111", file))
                )
            )
            .build()
            .executeAsync(object :
                HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                }
            },object :ProgressCallback{
                override fun onProgress(progress: Int) {

                }
            })
    }

    fun downNet(view: View) {
        var k = cacheDir.absolutePath+File.separator+"apk"
        val progressBar = showProgressDialog("下载进度")
        HttpRequest.newBuilder()
            .setUrl("https://fanyi-app.baidu.com/transapp/appdownloadpage?appchannel=webright")
            .setMethod(HttpMethod.GET)
            .setSavePath(k, "simple-manager-debug.apk")
            .build()
            .executeAsync(object :
                HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                    if (result.code == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this@MainActivity, "下载成功", Toast.LENGTH_LONG).show()
                    }
                }
            },object :ProgressCallback{
                override fun onProgress(progress: Int) {
                    Log.d("HttpAgent", "progress = $progress")
                    val current = progress
                    progressBar.setProgress(current.toInt(), true)
                }
            })
    }

    fun showProgressDialog(title: String): ProgressBar {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_view, null, false)
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

    fun downGet(view: View) {
        GlobalScope.launch {
            val result = HttpRequest.newBuilder()
                .setUrl("http://www.baidu.com")
                .setMethod(HttpMethod.GET)
                .build()
                .executeSync<ResponseData>()
            Log.d("TAG", result.toString())
        }
    }

    fun downPost(view: View) {
        var cancel = HttpRequest.newBuilder()
            .setUrl("https://fanyi.baidu.com/v2transapi?from=zh&to=en")
            .setMethod(HttpMethod.POST)
            .setBody(HttpBody(params = HashMap<String, String>().apply {
                put("from", "zh")
                put("to", "en")
                put("query", "今天天气真的很好")
                put("transtype", "realtime")
                put("simple_means_flag", "3")
                put("sign", "404035.182642")
                put("token", "a0631086b7d2f78a163c758f9cf")
                put("domain", "common")
            }))
            .build()
            .executeAsync(object :
                HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                }
            })
    }
}
