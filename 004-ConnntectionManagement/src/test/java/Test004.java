import org.apache.http.HttpClientConnection;
import org.apache.http.HttpHost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.ConnectionRequest;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.junit.Test;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author zhjk on 2017/4/14.
 */
public class Test004 {
    @Test
    public void test() throws InterruptedException, ExecutionException, ConnectionPoolTimeoutException {
        HttpClientContext context = HttpClientContext.create();
        HttpClientConnectionManager connMrg = new BasicHttpClientConnectionManager();
        HttpRoute route = new HttpRoute(new HttpHost("localhost", 8080));
// Request new connection. This can be a long process
        ConnectionRequest connRequest = connMrg.requestConnection(route, null);
// Wait for connection up to 10 sec,可通过ConnectionRequest#cancel()强制关闭
        HttpClientConnection conn = connRequest.get(10, TimeUnit.SECONDS);

        try {
            // If not open
            if (!conn.isOpen()) {
                // establish connection based on its route info
                connMrg.connect(conn, route, 1000, context);
                // and mark it as route complete
                connMrg.routeComplete(conn, route, context);
            }
            // Do useful things with the connection.
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            connMrg.releaseConnection(conn, null, 1, TimeUnit.MINUTES);
        }
    }
}
