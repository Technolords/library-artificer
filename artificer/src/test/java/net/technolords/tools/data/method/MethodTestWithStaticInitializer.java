package net.technolords.tools.data.method;

import java.util.Date;

/**
 * Created by Technolords on 2016-May-11.
 */
public class MethodTestWithStaticInitializer {
    private static String CONSTANT = "constant";
    private String constructorArg;
    private static Date constructedDate;

    static {
        constructedDate = new Date();
    }

    public MethodTestWithStaticInitializer(String argument) {
        constructorArg = argument;
    }

}
