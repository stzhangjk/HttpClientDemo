import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;


/**
 * @author zhjk on 2017/4/10.
 */
public class Test001 {

    /**
     * 简单的get请求
     */
    @Test
    public void test(){
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet get = new HttpGet("http://localhost:8080/hello");
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(get);
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(response != null){
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * 工具类URIBuilder
     */
    @Test
    public void testURIBuilder(){
        try {
            URI uri = new URIBuilder()
                    .setScheme("http")
                    .setCharset(Charset.forName("UTF-8"))
                    .setHost("localhost")
                    .setPath("/test")
                    .setParameter("username","admin")
                    .setParameter("password","123456")
                    .build();
            System.out.println(uri);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    /**
     * HttpResponse
     */
    @Test
    public void testHttpResponse(){
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK,"OK");
        System.out.println(response.getProtocolVersion());
        System.out.println(response.getStatusLine().getStatusCode());
        System.out.println(response.getStatusLine().getReasonPhrase());
        System.out.println(response.getStatusLine().toString());

        HttpResponse response2 = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        response2.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
        response2.addHeader("Set-Cookie", "c2=b; path=\"/\", c3=c; domain=\"localhost\"");
        Header h1 = response2.getFirstHeader("Set-Cookie");
        Header h2 = response2.getLastHeader("Set-Cookie");
        Header[] hs = response2.getHeaders("Set-Cookie");
        for(Header h : hs){
            System.out.println(h);
        }
    }


    @Test
    public void testHeaderIterator(){
        HttpResponse response = new BasicHttpResponse(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK");
        response.addHeader("Set-Cookie", "c1=a; path=/; domain=localhost");
        response.addHeader("Set-Cookie", "c2=b; path=\"/\", c3=c; domain=\"localhost\"");

        HeaderIterator it = response.headerIterator("Set-Cookie");

        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }
}
