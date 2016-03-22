package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Mar-22.
 */
public class MethodsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodsParser.class);

    /**
     * Auxiliary method to extract the methods associated to the resource. From the 'ClassFile'
     * structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   methods_count;
     *     method_info          methods[methods_count];
     *     ...
     * }
     *
     * - methods_count:
     *      The value of the 'methods_count' item gives the number of 'method_info' structures in the methods table.
     * - methods[]:
     *      Each value in the 'methods' table must be a 'method_info' structure giving a complete description of
     *      a method in this class or interface. If neither of the ACC_NATIVE and ACC_ABSTRACT flags are set in
     *      the 'access_flags' item of a 'method_info' structure, the Java Virtual Machine instructions implementing
     *      the method are also supplied.
     *
     *      The 'method_info' structures represent all methods declared by this class or interface type, including
     *      instance methods, class methods, instance initialization methods, and any class or interface
     *      initialization method. The methods table does not include items representing methods that are inherited
     *      from superclasses or superinterfaces.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractMethods(DataInputStream dataInputStream) throws IOException {
        int methodsCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("MethodsCount: " + methodsCount);
    }
}
