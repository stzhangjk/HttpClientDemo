#HttpClient

##一、基础知识
###1. 上下文Http Context###

###2. 拦截器interceptprs###
1. 拦截器之间可通过context共享信息；
2. 典型应用--解密加密；
3. 执行顺序与添加顺序相同；
4. 必须做到线程安全；

###3. 异常###
1. ``HttpClient``会抛出两种异常:``HttpException``和``IOException``；
2. ``HttpClient``把``HttpException``重新throw为``ClientProtocolException extends IOException``以便用户可以在一个``catch``块处理``HttpException``和``IOException``；

###4. 出错重试机制###
1. 默认情况下``IOException``会自动恢复，``HttpException``不做尝试；
2. 会重试那些被假定为幂等性的方法
3. HttpClient will automatically retry those methods that fail with a transport exception while the HTTP request is still being transmitted to the target server (i.e. the request has not been fully transmitted to the server).

###5. 自定义出错重试处理###
1. 实现``HttpRequestRetryHandler``接口

```
	HttpRequestRetryHandler myRetryHandler = new HttpRequestRetryHandler() {
	    public boolean retryRequest(
	            IOException exception,
	            int executionCount,
	            HttpContext context) {
	        if (executionCount >= 5) {
	            // Do not retry if over max retry count
	            return false;
	        }
	        if (exception instanceof InterruptedIOException) {
	            // Timeout
	            return false;
	        }
	        if (exception instanceof UnknownHostException) {
	            // Unknown host
	            return false;
	        }
	        if (exception instanceof ConnectTimeoutException) {
	            // Connection refused
	            return false;
	        }
	        if (exception instanceof SSLException) {
	            // SSL handshake exception
	            return false;
	        }
	        HttpClientContext clientContext = HttpClientContext.adapt(context);
	        HttpRequest request = clientContext.getRequest();
	        boolean idempotent = !(request instanceof HttpEntityEnclosingRequest);
	        if (idempotent) {
	            // Retry if the request is considered idempotent
	            return true;
	        }
	        return false;
	    }
	};
	CloseableHttpClient httpclient = HttpClients.custom()
	        .setRetryHandler(myRetryHandler)
	        .build();
```

>Please note that one can use StandardHttpRequestRetryHandler instead of the one used by default in order to treat >those request methods defined as idempotent by RFC-2616 as safe to retry automatically: GET, HEAD, PUT, DELETE, >OPTIONS, and TRACE.

###6.  重定向处理###
1.   HttpClient自动处理所有重定向，除了HTTP规范有命令禁止的且需要用户干预的。
2.   工具类**``URIUtils#resolve``**可以从重定向路径集合``HttpClientContext#getRedirectLocations()``中解析出定向到最后的绝对路径


```
CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpClientContext context = HttpClientContext.create();
        HttpGet httpget = new HttpGet("http://localhost:8080/003/redirect1Servlet");
        CloseableHttpResponse response = null;
        try {
            response = httpclient.execute(httpget, context);
            HttpHost target = context.getTargetHost();
            List<URI> redirectLocations = context.getRedirectLocations();
            redirectLocations.forEach((e)->{
                System.out.println(e);
            });
            URI location = URIUtils.resolve(httpget.getURI(), target, redirectLocations);           //获得重定向最后的绝对路径
            System.out.println("Final HTTP location: " + location.toASCIIString());
            // Expected to be an absolute URI
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
```
>输出：
>http://localhost:8080/003/redirect2Servlet
>http://localhost:8080/003/redirect3Servlet
>http://localhost:8080/003/index.jsp
>Final HTTP location: http://localhost:8080/003/index.jsp


##二、连接管理
###1. 连接管理

1.  Http连接是有状态的且线程不安全的

2.   HttpClient使用``HttpClientConnectionManager``来管理连接
>* 使用工厂产生新连接，并且管理连接的生命周期，确保一次只有一个线程操作连接；
>* 内部用``ManagedHttpClientConnection``代理真正的连接对象，用户显式或隐式关闭连接后，真正的连接会脱离代理，并回到管理对象中，不管用户是否还持有代理的引用，这时用户通过该代理进行IO操作或是显式/隐式地改变连接的状态都是不可行的。

3.    **``BasicHttpClientConnectionManager``**
-  一次只维护一个Connection；
-  尽管这个类是线程安全的，也应该一次只有一个线程运行它；
-  该类会竟可能重用同一个host的连接，如果新请求的连接的``route``与当前连接不相同，则会关闭现有连接，然后重新开启一个新连接（如果连接已经存在，则抛出``java.lang.IllegalStateException``异常）；

4.     **``PoolingHttpClientConnectionManager``**
- 可于多线程环境下提供连接池服务；
- 连接池按``route``组织，每次新连接的请求从连接池中获取，而不是创建一个新连接；
- 默认每个``route``上可以同时创建小于等于2的连接，而所有``route``的总的连接不能超过20个；自定义的方法如下：
```
	PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
	// Increase max total connection to 200
	cm.setMaxTotal(200);
	// Increase default max connection per route to 20
	cm.setDefaultMaxPerRoute(20);
	// Increase max connections for localhost:80 to 50
	HttpHost localhost = new HttpHost("locahost", 80);
	cm.setMaxPerRoute(new HttpRoute(localhost), 50);
	
	CloseableHttpClient httpClient = HttpClients.custom()
	        .setConnectionManager(cm)
	        .build();
```
- 当一个``HttpClient``不在使用而别关闭时，它的Conntection Manager也应该被关闭，以关闭所有的连接，并且释放资源。
- 当一个``route``的所有连接已经分配满了，这时新的连接请求将会阻塞，直到有连接返回到连接池。另外，可以通过``http.conn-manager.timeout``设置新请求的阻塞超时时间，如果时间到了还分配不到连接，则抛出``ConnectionPoolTimeoutException``异常。

5. 自定义保持活动连接的周期``ConnectionKeepAliveStrategy``
```
ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {
    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
    // Honor 'keep-alive' header
    HeaderElementIterator it = new BasicHeaderElementIterator(
    response.headerIterator(HTTP.CONN_KEEP_ALIVE));
    while (it.hasNext()) {
    HeaderElement he = it.nextElement();
    String param = he.getName();
    String value = he.getValue();
    if (value != null && param.equalsIgnoreCase("timeout")) {
        try {
        return Long.parseLong(value) * 1000;
        } catch(NumberFormatException ignore) {
        }
    }
}
HttpHost target = (HttpHost) context.getAttribute(
HttpClientContext.HTTP_TARGET_HOST);
if ("www.naughty-server.com".equalsIgnoreCase(target.getHostName())) {
// Keep alive for 5 seconds only
return 5 * 1000;
} else {
// otherwise
return 30 * 1000;
}
}
};
CloseableHttpClient client = HttpClients.custom()
.setKeepAliveStrategy(myStrategy)
.build();
```


