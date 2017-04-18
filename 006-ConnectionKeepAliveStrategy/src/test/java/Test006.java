import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.message.BasicHeaderIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

import java.io.IOException;

/**
 * @author zhjk on 2017/4/18.
 */
public class Test006 {
    @Test
    public void test() throws IOException {
        ConnectionKeepAliveStrategy strategy = new ConnectionKeepAliveStrategy() {
            @Override
            public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                HeaderElementIterator iterator = new BasicHeaderElementIterator(response.headerIterator(HTTP.CONN_KEEP_ALIVE));
                while(iterator.hasNext()){
                    HeaderElement element = iterator.nextElement();
                    String name = element.getName();
                    String value = element.getValue();
                    System.out.println(name + ":" + value);
                }
                return 1000;
            }
        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setKeepAliveStrategy(strategy).build();

        HttpGet get = new HttpGet("http://localhost:8080/server/");
        HttpResponse response = httpClient.execute(get);
        httpClient.close();
    }
}
