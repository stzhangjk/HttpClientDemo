import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

/**
 * @author zhjk on 2017/4/14.
 */
public class Test003 {
    @Test
    public void test() {
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
    }
}
