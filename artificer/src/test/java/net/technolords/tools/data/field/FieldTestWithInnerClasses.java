package net.technolords.tools.data.field;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Technolords on 2016-Mar-10.
 */
public class FieldTestWithInnerClasses {
    private Integer base;

    class NestedClass1 {
        private Double nestedDouble;

        NestedClass1(double value) {
            nestedDouble = value;
        }
    }

    class NestedClass2 {
        ConcurrentHashMap<Long, NestedClass1> nestedMap;

        NestedClass2() {
            nestedMap.put(3L, new NestedClass1(2.0));
        }

        Map<Long, NestedClass1> result() {
            return this.nestedMap;
        }
    }
}
