package net.technolords.tools.data.attribute.bootstrap;

import java.lang.invoke.CallSite;
import java.lang.invoke.ConstantCallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Created by Technolords on 2016-Apr-06.
 */
public class BootstrapMethodTrigger {

    private static MethodHandle hw;

    private static void hw() {
        System.out.println("Hello, World!");
    }

    public static CallSite bootstrapDynamic(MethodHandles.Lookup caller, String name, MethodType type) throws IllegalAccessException, NoSuchMethodException {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class thisClass = lookup.lookupClass();
        hw = lookup.findStatic(thisClass, "hw", MethodType.methodType(void.class));
        if (!type.equals(hw.type())) {
            hw = hw.asType(type);
        }
        return new ConstantCallSite(hw);
    }
}
