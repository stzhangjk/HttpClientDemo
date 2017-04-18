import org.apache.http.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhjk on 2017/4/17.
 */
public class Test005 {
    @Test
    public void test() throws IOException {

        HttpHost httpHost = new HttpHost("localhost",8080);
        HttpRoute route = new HttpRoute(httpHost);
        HttpClientContext context = HttpClientContext.create();

        /*创建连接管理对象*/
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(200);
        manager.setDefaultMaxPerRoute(20);
        manager.setMaxPerRoute(route,50);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(manager).build();

        /*获取连接并打开*/
        ConnectionRequest connRequest = manager.requestConnection(route,null);
        HttpClientConnection conn = null;
        try {

            conn = connRequest.get(10, TimeUnit.SECONDS);
            if(!conn.isOpen()){
                manager.connect(conn,route,1000,context);
                manager.routeComplete(conn,route,context);
            }
            System.out.println(conn.isOpen());
//            HttpGet get = new HttpGet("http://localhost:8080/server");//不加“http:”访问不到
//            conn.sendRequestHeader(get);
//            HttpResponse response = conn.receiveResponseHeader();
//            conn.receiveResponseEntity(response);
//            HttpEntity entity = response.getEntity();
//            System.out.println(EntityUtils.toString(entity));
        } catch (InterruptedException|ExecutionException e) {
            e.printStackTrace();
        } finally {
            manager.releaseConnection(conn,null,1,TimeUnit.MINUTES);
        }
    }

    @Test
    public void test2(){
        /*创建连接管理对象*/
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(200);
        manager.setDefaultMaxPerRoute(20);
        CloseableHttpClient httpClient = HttpClients.custom().setConnectionManager(manager).build();

        HttpGet get = new HttpGet("http://localhost:8080/server");//不加“http://”访问不到
        CloseableHttpResponse response = null;
        HttpClientContext context = HttpClientContext.create();
        try {
            response = httpClient.execute(get,context);
            HttpEntity entity = response.getEntity();
            System.out.println(EntityUtils.toString(entity));
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if(response != null){
                    response.close();
                }
                if(httpClient != null){
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

