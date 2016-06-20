package net.technolords.tools.test.callable;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import net.technolords.tools.test.domain.Result;
import net.technolords.tools.test.http.HttpUtil;

/**
 * Created by Technolords on 2016-Jun-20.
 */
public class HttpCallableTest {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Test
    public void executeRequest() throws Exception {
        final int POOL_SIZE = 50;
        final String REQUEST = "http://localhost:9000/mapng/traxis-structure-service/v1/pl/pl/vodStructure?categoryId=crid:~~2F~~2Fschange.com~~2F6149894e-c1ac-4deb-8404-d7e944c712e6";

        HttpClient httpClient = HttpUtil.createHttpClientWithConnectionPool(POOL_SIZE);
        HttpCallable httpCallable = new HttpCallable(httpClient, REQUEST);
        Result result = httpCallable.call();
        LOGGER.info("Response: " + result);
    }
}