package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Apr-07.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class BootstrapMethodsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapMethodsParser.class);

    /**
     * Auxiliary method to extract the bootstrap methods associated with the resource. This is fetched from the
     * 'BootstrapMethods_attribute' structure, which has the following format:
     *
     * [java 8]
     * BootstrapMethods_attribute {
     *      u2          attribute_name_index;
     *      u4          attribute_length;
     *      u2          num_bootstrap_methods;
     *      {
     *          u2      bootstrap_method_ref;
     *          u2      num_bootstrap_arguments;
     *          u2      bootstrap_arguments[num_bootstrap_arguments];
     *      }           bootstrap_methods[num_bootstrap_methods];
     * }
     *
     * - num_bootstrap_methods:
     *      The value of the 'num_bootstrap_methods' item determines the number of bootstrap method specifiers in
     *      the 'bootstrap_methods' array.
     * - bootstrap_methods[]:
     *      Each entry in the 'bootstrap_methods' table contains an index to a 'CONSTANT_MethodHandle_info' structure
     *      which specifies a bootstrap method, and a sequence (perhaps empty) of indexes to static arguments for
     *      the bootstrap method. Each 'bootstrap_methods' entry must contain the following three items:
     *
     *      - bootstrap_method_ref:
     *          The value of the 'bootstrap_method_ref' item must be a valid index into the constant_pool table.
     *          The 'constant_pool' entry at that index must be a 'CONSTANT_MethodHandle_info' structure.
     *          The form of the method handle is driven by the continuing resolution of the call site specifier
     *          in §invokedynamic, where execution of invoke in java.lang.invoke.MethodHandle requires that the
     *          bootstrap method handle be adjustable to the actual arguments being passed, as if by a call to
     *          java.lang.invoke.MethodHandle.asType. Accordingly, the 'reference_kind' item of the
     *          'CONSTANT_MethodHandle_info' structure should have the value 6 or 8, and the 'reference_index' item
     *          should specify a static method or constructor that takes three arguments of type
     *          java.lang.invoke.MethodHandles.Lookup, String, and java.lang.invoke.MethodType, in that order.
     *          Otherwise, invocation of the bootstrap method handle during call site specifier resolution will
     *          complete abruptly.
     *      - num_bootstrap_arguments:
     *          The value of the 'num_bootstrap_arguments' item gives the number of items in the
     *          'bootstrap_arguments' array.
     *      - bootstrap_arguments[]:
     *          Each entry in the 'bootstrap_arguments' array must be a valid index into the 'constant_pool' table.
     *          The 'constant_pool' entry at that index must be a 'CONSTANT_String_info', 'CONSTANT_Class_info',
     *          'CONSTANT_Integer_info', 'CONSTANT_Long_info', 'CONSTANT_Float_info', 'CONSTANT_Double_info',
     *          'CONSTANT_MethodHandle_info', or 'CONSTANT_MethodType_info' structure.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractBootstrapMethods(DataInputStream dataInputStream, Resource resource) throws IOException {
        int bootstrapMethodsCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("Bootstrap methods count: " + bootstrapMethodsCount);
        for(int index = 0; index < bootstrapMethodsCount; index++) {
            extractBootstrapMethod(dataInputStream, index, resource);
        }
    }

    protected static void extractBootstrapMethod(DataInputStream dataInputStream, int index, Resource resource) throws IOException {
        int bootstrapMethodReference = dataInputStream.readUnsignedShort();
        int bootstrapArgumentsCount = dataInputStream.readUnsignedShort();
        int bootstrapArgument;
        for(int argument = 0; argument < bootstrapArgumentsCount; argument++) {
            bootstrapArgument = dataInputStream.readUnsignedShort();
        }
    }

}
