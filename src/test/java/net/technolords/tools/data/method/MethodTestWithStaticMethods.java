package net.technolords.tools.data.method;

import java.util.ArrayList;

/**
 * Created by Technolords on 2016-Mar-31.
 */
public class MethodTestWithStaticMethods {

    public static <T> ArrayList<T> createArrayList(T ... elements) {
        ArrayList<T> list = new ArrayList<T>();
        for (T element : elements) {
            list.add(element);
        }
        return list;
    }

}
