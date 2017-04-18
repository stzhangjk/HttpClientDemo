import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zhjk on 2017/4/10.
 */
public class TestHttpEntity {
    @Test
    public void testStringEntity() throws IOException {
        StringEntity myEntity = new StringEntity("important message", ContentType.create("text/plain", "UTF-8"));

        System.out.println(myEntity.getContentType());
        System.out.println(myEntity.getContentLength());
        System.out.println(EntityUtils.toString(myEntity));
        System.out.println(EntityUtils.toByteArray(myEntity).length);
    }

    /**
     * 要关闭底层资源
     * 关闭内容流和关闭响应之间的区别是前者试图通过消耗实体内容而保持底层连接活着，而后者则立即关闭并丢弃连接。
     * When working with streaming entities, one can use the EntityUtils#consume(HttpEntity) method to ensure that the entity content has been fully consumed and the underlying stream has been closed.
     * @throws IOException
     */
    @Test
    public void test() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://localhost/");
        CloseableHttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            InputStream instream = entity.getContent();
            try {
                // do something useful
            } finally {
                instream.close();
            }
        }
        response.close();
    }

    /**
     * EntityUtils工具类
     * @throws IOException
     */
    @Test
    public void testEntityUtils() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://localhost/");
        CloseableHttpResponse response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                long len = entity.getContentLength();
                if (len != -1 && len < 2048) {
                    System.out.println(EntityUtils.toString(entity));
                } else {
                    // Stream content out
                }
            }
            response.close();
    }

    @Test
    public void testBufferedHttpEntity() throws IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpGet httpget = new HttpGet("http://localhost/");
        CloseableHttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            entity = new BufferedHttpEntity(entity);
        }

    }

    @Test
    public void testNameValuePair() throws UnsupportedEncodingException {
        CloseableHttpClient client = HttpClients.createDefault();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username","admin "));
        params.add(new BasicNameValuePair("password","123456"));
        HttpPost post = new HttpPost("http://localhost/");
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params,"UTF-8");

    }
}
