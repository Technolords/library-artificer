package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Apr-11.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class LineNumberTableParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LineNumberTableParser.class);

    /**
     * Auxiliary method to extract the code associated with the resource. This is fetched from the
     * 'LineNumberTable_attribute' structure, which has the following format:
     *
     * [java 8]
     * LineNumberTable_attribute {
     *      u2              attribute_name_index;
     *      u4              attribute_length;
     *      u2              line_number_table_length;
     *      {
     *          u2          start_pc;
     *          u2          line_number;
     *      }               line_number_table[line_number_table_length];
     * }
     *
     * - line_number_table_length:
     *      The value of the 'line_number_table_length' item indicates the number of entries in the 'line_number_table'
     *      array.
     * - line_number_table[]:
     *      Each entry in the 'line_number_table' array indicates that the line number in the original source file
     *      changes at a given point in the code array. Each 'line_number_table' entry must contain the following
     *      two items:
     *
     *      - start_pc:
     *          The value of the 'start_pc' item must indicate the index into the code array at which the code for
     *          a new line in the original source file begins. The value of 'start_pc' must be less than the value of
     *          the 'code_length' item of the Code attribute of which this LineNumberTable is an attribute.
     *      - line_number:
     *          The value of the 'line_number' item must give the corresponding line number in the original source file.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractLineNumberTable(DataInputStream dataInputStream, Resource resource) throws IOException {
        int lineNumberTableLength = dataInputStream.readUnsignedShort();
        LOGGER.debug("Line number table length: " + lineNumberTableLength);
        for(int lineNumberIndex = 0; lineNumberIndex < lineNumberTableLength; lineNumberIndex++) {
            int startPc = dataInputStream.readUnsignedShort();
            int lineNumber = dataInputStream.readUnsignedShort();
            LOGGER.debug("Line number: " + lineNumber + ", start pc: " + startPc);
        }
    }
}
