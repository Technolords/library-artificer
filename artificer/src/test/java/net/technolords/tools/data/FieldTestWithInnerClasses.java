package net.technolords.tools.data;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Technolords on 2016-Mar-10.
 */
public class FieldTestWithInnerClasses {
    private Integer base;

    class NestedClass1 {
        private Double nestedDouble;
    }

    class NestedClass2 {
        ConcurrentHashMap<Long, NestedClass1> nestedMap;
    }
}
