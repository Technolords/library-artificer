package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Mar-29.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class SourceFileParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceFileParser.class);

    /**
     * Auxiliary method to extract the source file associated with the resource. This is fetched from the
     * 'SourceFile_attribute' structure, which has the following format:
     *
     * [java 8]
     * SourceFile_attribute {
     *      u2          attribute_name_index;
     *      u4          attribute_length;
     *      u2          sourcefile_index;
     * }
     *
     * - sourcefile_index:
     *      The value of the 'sourcefile_index' item must be a valid index into the 'constant_pool' table. The
     *      'constant_pool' entry at that index must be a 'CONSTANT_Utf8_info' structure representing a string.
     *      The string referenced by the sourcefile_index item will be interpreted as indicating the name of the
     *      source file from which this class file was compiled. It will not be interpreted as indicating the name
     *      of a directory containing the file or an absolute path name for the file; such platform-specific additional
     *      information must be supplied by the run-time interpreter or development tool at the time the file name
     *      is actually used.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractSourceFile(DataInputStream dataInputStream, Resource resource) throws IOException {
        // Read index
        int constantPoolIndex = dataInputStream.readUnsignedShort();
        String sourceFile = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), constantPoolIndex);
        LOGGER.debug("SourceFile: " + sourceFile);
    }
}
