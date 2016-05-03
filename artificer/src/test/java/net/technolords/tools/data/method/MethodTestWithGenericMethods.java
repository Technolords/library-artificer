package net.technolords.tools.data.method;

import java.util.Comparator;
import java.util.List;

/**
 * Created by Technolords on 2016-May-03.
 */
public class MethodTestWithGenericMethods {

    public <T> T max(List<T> list, Comparator<T> comp) {
        T biggestSoFar = list.get(0);
        for ( T t : list ) {
            if (comp.compare(t, biggestSoFar) > 0) {
                biggestSoFar = t;
            }
        }
        return biggestSoFar;
    }
}
