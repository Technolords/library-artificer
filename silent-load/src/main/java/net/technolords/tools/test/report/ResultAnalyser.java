package net.technolords.tools.test.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.test.domain.Result;

/**
 * Created by Technolords on 2016-Jun-20.
 */
public class ResultAnalyser {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    public void parseResults(List<Future<Result>> results) throws ExecutionException, InterruptedException {
        Map<String, List<Result>> parsedResults = new HashMap<>();
        for(Future<Result> future : results) {
            Result result = future.get();
            LOGGER.trace("Parsing result: " + result);
            this.updateParsedResults(parsedResults, result);
        }
        this.reportResults(parsedResults);

    }

    protected void reportResults(Map<String, List<Result>> parsedResults) {
        LOGGER.info("Analysis of result, number of different results: " + parsedResults.size());
        for (String result : parsedResults.keySet()) {
            LOGGER.info("Result: " + result + ", frequency: " + parsedResults.get(result).size() + ", average: " + this.calculateAverage(parsedResults.get(result)));
        }
    }

    protected double calculateAverage(List<Result> results) {
        if (results != null) {
            LongSummaryStatistics stats = results.stream().collect(Collectors.summarizingLong(Result::getExecutionTime));
            return stats.getAverage();
        }
        return -1L;
    }

    protected void updateParsedResults(Map<String, List<Result>> parsedResults, Result result) {
        String key = this.generateKey(result);
        if (parsedResults.containsKey(key)) {
            List<Result> resultList = parsedResults.get(key);
            resultList.add(result);
        } else {
            List<Result> resultList = new ArrayList<>();
            resultList.add(result);
            parsedResults.put(key, resultList);
        }
    }

    protected String generateKey(Result result) {
        if (result != null) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(result.getStatusCode());
            buffer.append("-");
            buffer.append(result.getSize());
            return buffer.toString();
        }
        return "No result object";
    }

}
