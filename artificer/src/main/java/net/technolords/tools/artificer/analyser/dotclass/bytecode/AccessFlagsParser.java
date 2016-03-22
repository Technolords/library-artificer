package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class AccessFlagsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AccessFlagsParser.class);

    /**
     * Access flags can have a different location, namely:
     * - ClassFile
     * - field_info
     * - method_info
     *
     * And depending on the location, the value or info has different semantics.
     */
    public static final String LOCATION_CLASS_FILE = "LOCATION_CLASS_FILE";
    public static final String LOCATION_NESTED_CLASS_FILE = "LOCATION_NESTED_CLASS_FILE";
    public static final String LOCATION_FIELD_INFO = "LOCATION_FIELD_INFO";
    public static final String LOCATION_METHOD_INFO = "LOCATION_METHOD_INFO";

    public static void extractAccessFlags(DataInputStream dataInputStream, String location) throws IOException {
        int accessFlags = dataInputStream.readUnsignedShort();
        LOGGER.debug("accessFlags: " + accessFlags);
    }
}
