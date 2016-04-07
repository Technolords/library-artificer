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
public class SourceDebugExtensionParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceDebugExtensionParser.class);

    /**
     * Auxiliary method to extract the bootstrap methods associated with the resource. This is fetched from the
     * 'SourceDebugExtension_attribute' structure, which has the following format:
     *
     * [java 8]
     * SourceDebugExtension_attribute {
     *      u2 attribute_name_index;
     *      u4 attribute_length;
     *      u1 debug_extension[attribute_length];
     * }
     *
     * - debug_extension[]:
     *      The 'debug_extension' array holds extended debugging information which has no semantic effect on the
     *      Java Virtual Machine. The information is represented using a modified UTF-8 string with no terminating
     *      zero byte. Note that the 'debug_extension' array may denote a string longer than that which can be
     *      represented with an instance of class String.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param attributeLength
     *  The length of the debug extension.
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractSourceDebugExtension(DataInputStream dataInputStream, int attributeLength, Resource resource) throws IOException {
        int debug;
        for(int index = 0; index < attributeLength; index++) {
            debug = dataInputStream.readUnsignedByte();
        }
    }
}
