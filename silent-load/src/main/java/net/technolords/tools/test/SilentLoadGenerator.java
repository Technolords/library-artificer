package net.technolords.tools.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.test.callable.HttpCallable;
import net.technolords.tools.test.domain.Result;
import net.technolords.tools.test.http.HttpUtil;

/**
 * Created by Technolords on 2016-Jun-17.
 *
 * The purpose of this class is to support load testing, in a simple and 'silent' way. Meaning there is little
 * to no logging and we're certainly not interested in the response (data wise). At most the length of the
 * response (byte wise) and the http code's are used in order to detect or success.
 */
public class SilentLoadGenerator {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private int clientPoolSize;
    private int taskFactor;
    private List<String> requestUrls;
    private boolean generateSpike;

    public SilentLoadGenerator(int myClientPoolSize, int myTaskFactor, List<String> myRequestUrls, boolean myGenerateSpike) {
        this.clientPoolSize = myClientPoolSize;
        this.taskFactor = myTaskFactor;
        this.requestUrls = myRequestUrls;
        this.generateSpike = myGenerateSpike;
    }

    private static final String GRID_OPTIONS = "http://localhost:9000/mapng/traxis-context-service/v1/pl/pl/gridOptions?categoryId=crid%3A%7E%7E2F%7E%7E2Fschange.com%7E%7E2F9fe04734-9342-4399-956f-7a8884cdd713&genreCrid=true&profileId=C00366_pl%7E%7E23MasterProfile";
    private static final String STRUCTURES = "http://localhost:9000/mapng/traxis-structure-service/v1/pl/pl/vodStructure?categoryId=crid:~~2F~~2Fschange.com~~2F6149894e-c1ac-4deb-8404-d7e944c712e6";

    public void executeLoadGeneration() throws InterruptedException {
        if (this.generateSpike) {
            // Build and populate pool
            ExecutorService executorService = Executors.newFixedThreadPool(this.clientPoolSize);
            List<HttpCallable> tasks = new ArrayList<>();
            HttpClient httpClient = HttpUtil.createHttpClientWithConnectionPool(this.clientPoolSize);
            // Create tasks
            long totalTasks = this.clientPoolSize * this.taskFactor;
            for (long i = 0; i < totalTasks; i++) {
                HttpCallable task = new HttpCallable(httpClient, GRID_OPTIONS);
                tasks.add(task);
            }
            // Execute tasks all at once
            LOGGER.info("About to fire tasks (" + tasks.size() + "), for client pool size: " + this.clientPoolSize);
            List<Future<Result>> results = executorService.invokeAll(tasks);
            // Done
        } else {
            // Create task pool (create more tasks than thread pool size)
            // Execute task
            // Add task to task pool again
            // Run for x seconds and then terminate
        }
    }

    /*
        Simple main class that generates a load for a period of time
        by creating a threadpool of clients.
        These clients then perform a request and discard the respond (body wise)
     */
}
