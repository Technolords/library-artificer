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

    public static void extractAccessFlags(DataInputStream dataInputStream) throws IOException {
        int accessFlags = dataInputStream.readUnsignedShort();
        LOGGER.debug("accessFlags: " + accessFlags);
    }
}
