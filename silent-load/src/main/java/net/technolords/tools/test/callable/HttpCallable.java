package net.technolords.tools.test.callable;

import java.nio.charset.Charset;
import java.util.concurrent.Callable;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.test.domain.Result;

/**
 * Created by Technolords on 2016-Jun-17.
 */
public class HttpCallable implements Callable<Result> {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private HttpClient httpClient;
    private HttpUriRequest uriRequest;

    public HttpCallable(HttpClient myHttpClient, String myRequestUrl) {
        this.httpClient = myHttpClient;
        this.uriRequest = new HttpGet(myRequestUrl);
    }

    @Override
    public Result call() throws Exception {
        long timeBefore = System.currentTimeMillis();
        HttpResponse response = httpClient.execute(this.uriRequest);
        long timeAfter = System.currentTimeMillis();
        StatusLine statusLine = response.getStatusLine();
        HttpEntity entity = response.getEntity();
        Result result = new Result();
        String stringResult = EntityUtils.toString(entity, Charset.defaultCharset());
        result.setResponse(stringResult);
        result.setStatusCode(statusLine.getStatusCode());
        result.setSize(stringResult.length());
        result.setExecutionTime(timeAfter - timeBefore);
        LOGGER.trace("Got result: " + result.getStatusCode() + ", size: " + result.getSize());
        return result;
    }
}
