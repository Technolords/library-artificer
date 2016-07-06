package net.technolords.tools.test;

import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Created by Technolords on 2016-Jun-17.
 */
public class SilentLoadGeneratorTest {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static final int TASKS_1M = 1000000;
    private static final int TASKS_5M = 5000000;

    @Test(enabled = false)
    public void testSmallSpike() throws InterruptedException, ExecutionException {
        // Tasks
        long startTime = System.currentTimeMillis();
        SilentLoadGenerator silentLoadGenerator = new SilentLoadGenerator(500, 5000, null, true);
        silentLoadGenerator.executeLoadGeneration();
        long endTime = System.currentTimeMillis();
        LOGGER.info("Total execution time: " + ((endTime - startTime) / 1000) + " s");
    }

    @Test
    public void testTimedLoad() throws ExecutionException, InterruptedException {
        // Tasks
        long startTime = System.currentTimeMillis();
        SilentLoadGenerator silentLoadGenerator = new SilentLoadGenerator(50, 50, null, false);
        silentLoadGenerator.executeLoadGeneration();
        long endTime = System.currentTimeMillis();
        LOGGER.info("Total execution time: " + ((endTime - startTime) / 1000) + " s");
    }
}