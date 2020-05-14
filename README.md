# HttpAgent
Android 网络请求框架，可以自定义网络引擎，支持 GET、POST、文件上传，可以监听文件上传进度


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