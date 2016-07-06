package net.technolords.tools.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.test.callable.HttpCallable;
import net.technolords.tools.test.domain.Result;
import net.technolords.tools.test.http.HttpUtil;
import net.technolords.tools.test.report.ResultAnalyser;

/**
 * Created by Technolords on 2016-Jun-17.
 *
 * The purpose of this class is to support load testing, in a simple and 'silent' way. Meaning there is little
 * to no logging and we're certainly not interested in the response (data wise). At most the length of the
 * response (byte wise) and the http code's are used in order to detect or success. This data is stored in a DTO
 * class called Result.
 */
public class SilentLoadGenerator {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private int clientPoolSize;
    private int taskPoolSize;
    private List<String> requestUrls;
    private boolean generateSpike;

    /**
     * Auxiliary constructor, which initializes the task pool and client pool, as wel as the modus operandus. The modus
     * operandus is either spike mode or not.
     *
     * - spike: build of of task pool first, and then immediately use all the client to invoke the tasks.
     * - non spike: TODO: implement, but means it will be time based.
     *
     * @param myClientPoolSize
     *  The size of the client pool.
     * @param myTaskPoolSize
     *  The size of the task pool to create.
     * @param myRequestUrls
     *  The list of URL's to invoke.
     * @param myGenerateSpike
     *  The modus operandus.
     */
    public SilentLoadGenerator(int myClientPoolSize, int myTaskPoolSize, List<String> myRequestUrls, boolean myGenerateSpike) {
        this.clientPoolSize = myClientPoolSize;
        this.taskPoolSize = myTaskPoolSize;
        this.requestUrls = myRequestUrls;
        this.generateSpike = myGenerateSpike;
    }

    private static final String GRID_OPTIONS = "http://localhost:9000/mapng/traxis-context-service/v1/pl/pl/gridOptions?categoryId=crid%3A%7E%7E2F%7E%7E2Fschange.com%7E%7E2F9fe04734-9342-4399-956f-7a8884cdd713&genreCrid=true&profileId=C00366_pl%7E%7E23MasterProfile";
//    private static final String STRUCTURE_REQUEST = "http://localhost:9000/mapng/traxis-structure-service/v1/pl/pl/vodStructure?categoryId=crid:~~2F~~2Fschange.com~~2F6149894e-c1ac-4deb-8404-d7e944c712e6";
    private static final String STRUCTURE_REQUEST = "http://localhost:8405/mapng/traxis-structure-service/v1/pl/pl/vodStructure?categoryId=crid:~~2F~~2Fschange.com~~2F6149894e-c1ac-4deb-8404-d7e944c712e6";

    public void executeLoadGeneration() throws InterruptedException, ExecutionException {
        ResultAnalyser resultAnalyser = new ResultAnalyser();
        if (this.generateSpike) {
            // Build and populate pool
            LOGGER.info("About to create a task pool with size: " + this.taskPoolSize + ", using a client thread pool size: " + this.clientPoolSize);
            ExecutorService executorService = Executors.newFixedThreadPool(this.clientPoolSize);
            List<HttpCallable> tasks = new ArrayList<>();
            HttpClient httpClient = HttpUtil.createHttpClientWithConnectionPool(this.clientPoolSize);
            // Create tasks
            for (long i = 0; i < this.taskPoolSize; i++) {
                HttpCallable task = new HttpCallable(httpClient, STRUCTURE_REQUEST);
                tasks.add(task);
            }
            // Execute tasks all at once
            LOGGER.info("Created a pool with " + tasks.size() + " tasks, and using a client pool with size: " + this.clientPoolSize);
            List<Future<Result>> results = executorService.invokeAll(tasks);
            resultAnalyser.parseResults(results);
            // Done
        } else {
            // Build and populate tool
            ExecutorService executorService = Executors.newWorkStealingPool(this.clientPoolSize);
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
