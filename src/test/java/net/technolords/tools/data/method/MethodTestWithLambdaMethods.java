package net.technolords.tools.data.method;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created by Technolords on 2016-May-11.
 */
public class MethodTestWithLambdaMethods {

    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap1() {
        return Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue());
    }

    public static <K, U> Collector<Map.Entry<K, U>, ?, Map<K, U>> entriesToMap2() {
        return Collectors.toMap((e) -> (e).getKey(), (e) -> (e).getValue());
    }
}
