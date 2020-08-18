# HttpAgent
Android 网络请求框架，可以自定义网络引擎，支持 GET、POST、文件上传、文件下载，可以监听文件下载上传进度。本库内部基于 OkHttp 和
HttpUrlConnection 实现了两套网络请求逻辑，使用者可以根据自己的需要使用其中的某一种类型，或者自己按提供的接口自行实现。


## 使用方法

### 1、全局配置
```kotlin
HttpAgentClient.newBuilder()
            .setAgent(OkHttpEngine::class.java)
            .setConnectTimeOut(10000)
            .setReadTimeOut(10000)
            .build()
```

### 2、同步网络请求
```kotlin
val result = HttpRequest.newBuilder()
                .setUrl("http://www.example.com")
                .setMethod(HttpMethod.GET)
                .build()
                .executeSync<ResponseData>()
            Log.d("TAG", result.toString())
```

### 3、异步网络请求
```kotlin
HttpRequest.newBuilder()
            .setUrl("http://www.baidu.com")
            .setMethod(HttpMethod.GET)
            .build()
            .executeAsync(object : HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                }
            })
```

### 4、异步网络请求可以取消
```kotlin
var cancel = HttpRequest.newBuilder()
            .setUrl("http://www.example.com")
            .setMethod(HttpMethod.GET)
            .build()
            .executeAsync(object : HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                }
            })
cancel?.cancel()
```

### 5、上传文件，监听上传进度
```kotlin
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
```

### 6、下载文件，监听下载进度
```kotlin
var k = Environment.getExternalStorageDirectory().absolutePath.toString()
        val progressBar = showProgressDialog("下载进度")
        HttpRequest.newBuilder()
            .setUrl("https://www.wandoujia.com/apps/7965171/download/dot?ch=detail_normal_dl")
            .setMethod(HttpMethod.GET)
            .setSavePath(k,"王教授.apk")
            .build()
            .executeAsync(object :
                HttpCallBack<ResponseData> {
                override fun onReceivedData(result: ResponseData) {
                    Log.d("TAG", result.toString())
                    if(result.code == HttpURLConnection.HTTP_OK){
                        Toast.makeText(this@MainActivity,"下载成功",Toast.LENGTH_LONG).show()
                    }
                }
            }) { current, total ->
                Log.d("HttpAgent", "current = $current,total = $total")
                val current = (current.toInt()/total.toDouble())*100
                progressBar.setProgress(current.toInt(),true)
            }
```