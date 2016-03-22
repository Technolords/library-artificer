package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Mar-22.
 */
public class InterfaceParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterfaceParser.class);

    /**
     * Auxiliary method to extract the interfaces associated to the resource. From the 'ClassFile'
     * structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   interfaces_count;
     *     u2                   interfaces[interfaces_count];
     *     ...
     * }
     *
     * - interfaces_count:
     *      The value of the 'interfaces_count' item gives the number of direct superinterfaces of this class or
     *      interface type.
     * - interfaces[]:
     *      Each value in the 'interfaces' array must be a valid index into the 'constant_pool' table. The
     *      'constant_pool' entry at each value of interfaces[i], where 0 ≤ i < interfaces_count, must be a
     *      'CONSTANT_Class_info' structure representing an interface that is a direct superinterface of this
     *      class or interface type, in the left-to-right order given in the source for the type.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractInterfaces(DataInputStream dataInputStream) throws IOException {
        int interfacesCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("InterfacesCount: " + interfacesCount);
        if(interfacesCount != 0) {
            int interfaces = dataInputStream.readUnsignedShort();
            LOGGER.debug("Interfaces: " + interfaces);
        }
    }
}
