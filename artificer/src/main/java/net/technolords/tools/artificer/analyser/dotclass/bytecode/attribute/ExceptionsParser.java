package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.domain.dotclass.Constant;
import net.technolords.tools.artificer.domain.dotclass.ConstantInfo;
import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Created by Technolords on 2016-Mar-24.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class ExceptionsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionsParser.class);

    /**
     * Auxiliary method to extract the exceptions associated with the resource. This is fetched from the
     * 'Exceptions_attribute' structure, which has the following format:
     *
     * [java 8]
     * Exceptions_attribute {
     *      u2          attribute_name_index;
     *      u4          attribute_length;
     *      u2          number_of_exceptions;
     *      u2          exception_index_table[number_of_exceptions];
     * }
     *
     * - number_of_exceptions:
     *      The value of the 'number_of_exceptions' item indicates the number of entries in the 'exception_index_table'.
     * - exception_index_table[]:
     *      Each value in the 'exception_index_table' array must be a valid index into the 'constant_pool' table.
     *      The 'constant_pool' entry at that index must be a 'CONSTANT_Class_info' structure representing a class
     *      type that this method is declared to throw.
     *
     * Note that this method deals with the last two parts (as in, delegated from the AttributesParser).
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractExceptions(DataInputStream dataInputStream, Resource resource) throws IOException {
        // Read the number of exceptions
        int numberOfExceptions = dataInputStream.readUnsignedShort();
        LOGGER.debug("Exceptions count: " + numberOfExceptions);

        // Read the exceptions
        for(int index = 0; index < numberOfExceptions; index++) {
            extractException(dataInputStream, index, resource);
        }
    }

    /**
     *     u2           exception_index_table
     *
     * - exception_index_table[]:
     *      Each value in the 'exception_index_table' array must be a valid index into the 'constant_pool' table.
     *      The 'constant_pool' entry at that index must be a 'CONSTANT_Class_info' structure representing a class
     *      type that this method is declared to throw.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param index
     *  The index in the 'exception_index_table' array.
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractException(DataInputStream dataInputStream, int index, Resource resource) throws IOException {
        // Read index
        int constantPoolIndex = dataInputStream.readUnsignedShort();
        StringBuilder buffer = new StringBuilder();
        buffer.append("Exception (index: ").append(index).append(")");

        // Find data in constant pool associated with the index
        Constant constant = ConstantPoolAnalyser.findConstantByIndex(resource.getConstantPool(), constantPoolIndex);
        if(constant != null) {
            List<ConstantInfo> constantInfoList = constant.getConstantInfoList();
            if (constantInfoList != null && constantInfoList.size() > 0) {
                ConstantInfo constantInfo = constantInfoList.get(0);
                int exceptionClassIndex = constantInfo.getIntValue();
                String classDescriptor = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), exceptionClassIndex);
                buffer.append(", with descriptor: ").append(classDescriptor);
                LOGGER.debug(buffer.toString());

                // Skipping analysis of the signature as it is a direct class constant. These are added regardless to
                // the references classes...
            } else {
                buffer.append(", with descriptor: UNKNOWN (failed to get info from the constant fetched from constant pool)");
                LOGGER.debug(buffer.toString());
            }
        } else {
            buffer.append(", with descriptor: UNKNOWN (failed to get a constant from the constant pool)");
            LOGGER.debug(buffer.toString());
        }
    }

}
