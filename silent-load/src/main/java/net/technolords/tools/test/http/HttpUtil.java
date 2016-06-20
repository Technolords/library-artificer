package net.technolords.tools.test.http;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

/**
 * Created by Technolords on 2016-Jun-17.
 *
 * The Http utility class offers simple creation of Http clients.
 */
public class HttpUtil {

    /**
     * Auxiliary method to create a http client using a connection manager with a thread pool size.
     *
     * @param threadPoolSize
     *  The size associated with the thread pool.
     *
     * @return
     *  An instance of a http client.
     */
    public static HttpClient createHttpClientWithConnectionPool(int threadPoolSize) {
        HttpClientConnectionManager httpClientConnectionManager = createPoolingHttpClientConnectionManager(threadPoolSize);
        return HttpClients.custom().setConnectionManager(httpClientConnectionManager).build();
    }

    /**
     * Auxiliary method to create a http client connection manager with a thread pool size.
     *
     * @param threadPoolSize
     *  The size associated with the thread pool.
     *
     * @return
     *  An instance of a http client connection manager.
     */
    public static HttpClientConnectionManager createPoolingHttpClientConnectionManager(int threadPoolSize) {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        poolingHttpClientConnectionManager.setMaxTotal(threadPoolSize);
        return poolingHttpClientConnectionManager;
    }
}
