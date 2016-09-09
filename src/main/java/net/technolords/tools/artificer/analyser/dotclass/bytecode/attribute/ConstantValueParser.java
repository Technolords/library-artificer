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
public class ConstantValueParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantValueParser.class);
    private static final String DOUBLE = "Double";
    private static final String FLOAT = "Float";
    private static final String INTEGER = "Integer";
    private static final String LONG = "Long";
    private static final String STRING = "String";

    /**
     * Auxiliary method to extract the constant value associated with the resource. This is fetched from the
     * 'ConstantValue_attribute' structure, which has the following format:
     *
     * [java 8]
     * ConstantValue_attribute {
     *      u2          attribute_name_index;
     *      u4          attribute_length;
     *      u2          constantvalue_index;
     * }
     *
     * - constantvalue_index:
     *      The value of the 'constantvalue_index' must be a valid index in the 'constant_pool' table. The
     *      'constant_pool' entry at that index gives the constant value represented by this attribute. The
     *      'constant_pool' entry must be of a type appropriate to the field as specified in:
     *
     *      Field type                      Entry type
     *      ----------                      -----------
     *      long                            CONSTANT_Long
     *      float                           CONSTANT_Float
     *      double                          CONSTANT_Double
     *      int,short,char,byte,boolean     CONSTANT_Integer
     *      String                          CONSTANT_String
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractConstantValue(DataInputStream dataInputStream, Resource resource) throws IOException {
        // Read index
        int constantPoolIndex = dataInputStream.readUnsignedShort();
        StringBuilder buffer = new StringBuilder();
        buffer.append("Constant ");

        // Find data in constant pool associated with the index
        Constant constant = ConstantPoolAnalyser.findConstantByIndex(resource.getConstantPool(), constantPoolIndex);
        if(constant != null) {
            List<ConstantInfo> constantInfoList = constant.getConstantInfoList();
            if (constantInfoList != null && constantInfoList.size() > 0) {
                ConstantInfo constantInfo = constantInfoList.get(0);
                buffer.append("(of type: ").append(constant.getType()).append(") with value: ");
                switch (constant.getType()) {
                    case DOUBLE:
                        buffer.append(constantInfo.getDoubleValue());
                        break;
                    case FLOAT:
                        buffer.append(constantInfo.getFloatValue());
                        break;
                    case INTEGER:
                        buffer.append(constantInfo.getIntValue());
                        break;
                    case LONG:
                        buffer.append(constantInfo.getLongValue());
                        break;
                    case STRING:
                        int stringIndex = constantInfo.getIntValue();
                        String stringValue = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), stringIndex);
                        buffer.append(stringValue);
                        break;
                    default:
                        buffer.append("TODO (unsupported type)");
                }
                LOGGER.debug(buffer.toString());
            } else {
                buffer.append("of type: UNKNOWN (failed to get info from the constant fetched from constant pool)");
                LOGGER.debug(buffer.toString());
            }
        } else {
            buffer.append("of type: UNKNOWN (failed to get a constant from the constant pool)");
            LOGGER.debug(buffer.toString());
        }
    }
}
