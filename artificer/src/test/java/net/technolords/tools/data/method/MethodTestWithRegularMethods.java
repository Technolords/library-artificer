package net.technolords.tools.data.method;

import java.util.List;

/**
 * Created by Technolords on 2016-Mar-24.
 */
public class MethodTestWithRegularMethods {

//    public void simple() { }

//    public void singleParameter(final String param1) { }

//    public void dualParameters(String param1, int param2) { }

//    public int multiParameters(String... parameters) {
//        int totalCharlength = 0;
//        for(String parameter : parameters) {
//            totalCharlength += parameter.length();
//        }
//        return totalCharlength;
//    }

//    public static void main(String[] args) {
//        MethodTestWithRegularMethods m = new MethodTestWithRegularMethods();
//        m.multiParameters("a", "bb", "ccc");
//    }

    void totalFuel(List<? super MethodTestWithRegularMethods> list) { }

}
