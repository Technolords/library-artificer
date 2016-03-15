package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.SignatureAnalyser;
import net.technolords.tools.artificer.domain.dotclass.Constant;
import net.technolords.tools.artificer.domain.dotclass.ConstantInfo;
import net.technolords.tools.artificer.domain.dotclass.ConstantPool;
import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class AnnotationsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationsParser.class);

    /**
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param annotationsCount
     *  The number of annotations.
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractAnnotations(DataInputStream dataInputStream, int annotationsCount, Resource resource) throws IOException {
        for(int index = 0; index < annotationsCount; index++) {
            extractAnnotation(dataInputStream, index, resource);
        }
    }

    /**
     * The structure has the following (general) format:
     *
     * [java 8]
     * annotation {
     *      u2                      type_index;
     *      u2                      num_element_value_pairs;
     *      element_value_pairs     element_value_pair[num_element_value_pairs]
     * }
     *
     * - type_index:
     *      The value of the 'type_index' item must be a valid index in the 'constant_pool' table. The 'constant_pool'
     *      table entry at that index must be a 'CONSTANT_Utf8_info' structure representing a field descriptor. The
     *      field descriptor denotes the type of the annotation represented by this 'annotation' structure.
     * - num_element_value_pairs:
     *      The value of 'num_element_value_pairs' item gives the number of element-value pairs of the annotation
     *      represented by this 'annotation' structure.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param index
     *  The annotation index, used for precise data logging.
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractAnnotation(DataInputStream dataInputStream, int index, Resource resource) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Annotation (index: ").append(index).append(")");

        // Read the type index
        int typeIndex = dataInputStream.readUnsignedShort();
        String descriptor = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), typeIndex);
        buffer.append(", of type (index: ").append(typeIndex).append("): ").append(descriptor);
        LOGGER.debug(buffer.toString());

        // Add signature (when applicable) to the referenced classes
        SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), descriptor);

        // Read the element value pairs
        int numberOfElementValuePairs = dataInputStream.readUnsignedShort();
        extractElementValuePairs(dataInputStream, numberOfElementValuePairs, resource);
    }

    protected static void extractElementValuePairs(DataInputStream dataInputStream, int numberOfElementValuePairs, Resource resource) throws IOException {
        LOGGER.debug("Number of element-value pairs: " + numberOfElementValuePairs);
        for(int index = 0; index < numberOfElementValuePairs; index++) {
            extractElementValuePair(index, dataInputStream, resource);
        }
    }

    /**
     * The structure has the following (general) format:
     *
     * [java 8]
     * element_value_pair {
     *      u2                      element_name_index
     *      element_value           value
     * }
     *
     * - element_name_index:
     *      The value of the 'element_name_index' must be a valid index into the 'constant_pool' table. The
     *      'constant_pool' entry at that index must be a 'CONSTANT_Utf8_info' structure. The 'constant_pool'
     *      entry denotes the name of the element of the element-value pair represented by this 'element_value_pairs'
     *      entry. In other words, the entry denotes an element of the annotation type specified by 'type_index'.
     * - value:
     *      The value of the 'value' item represents the value of the element-value pair represented by this
     *      'element_value_pairs' entry.
     *
     * The 'element_value' structure is a discriminated union representing the value of an element-value pair. It has
     * the following format:
     *
     * element_value {
     *      u1                      tag
     *      union {
     *          u2                  const_value_index;
     *
     *          {
     *              u2              type_name_index;
     *              u2              constant_name_index;
     *          } enum_const_value;
     *
     *          u2                  class_info_index;
     *
     *          annotation          annotation_value;
     *
     *          {
     *              u2              num_value;
     *              element_value   values[num_value]
     *          } array_value
     *      } value
     * }
     *
     * In other words, the union describes it is one of the following types:
     * - const_value_index
     * - enum_const_value
     * - class_info_index
     * - annotation_value
     * - array_value
     *
     * @param dataInputStream
     * @param resource
     */
    protected static void extractElementValuePair(int index, DataInputStream dataInputStream, Resource resource) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Element-value pair (index: ").append(index).append(")");

        // Read the element key
        int elementNameIndex = dataInputStream.readUnsignedShort();
        String element = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), elementNameIndex);
        buffer.append(", with key (index: ").append(elementNameIndex).append("): ").append(element);

        // Read the tag
        int tag = dataInputStream.readUnsignedByte();
        int constantValueIndex;
        ConstantInfo value;

        // Read the element value
        switch ((char) tag) {
            case 'B':
                constantValueIndex = dataInputStream.readUnsignedShort();
                LOGGER.debug("Associated value (byte constant), constant value index: " + constantValueIndex);
                break;
            case 'C':
                constantValueIndex = dataInputStream.readUnsignedShort();
                LOGGER.debug("Associated value (char constant), constant value index: " + constantValueIndex);
                break;
            case 'D':
                constantValueIndex = dataInputStream.readUnsignedShort();
                LOGGER.debug("Associated value (double constant), constant value index: " + constantValueIndex);
                break;
            case 'F':
                constantValueIndex = dataInputStream.readUnsignedShort();
                LOGGER.debug("Associated value (float constant), constant value index: " + constantValueIndex);
                break;
            case 'I':
                constantValueIndex = dataInputStream.readUnsignedShort();
                LOGGER.debug("Associated value (integer constant), constant value index: " + constantValueIndex);
                break;
            case 'J':
                constantValueIndex = dataInputStream.readUnsignedShort();
                LOGGER.debug("Associated value (long constant), constant value index: " + constantValueIndex);
                break;
            case 'S':
                constantValueIndex = dataInputStream.readUnsignedShort();
                LOGGER.debug("Associated value (short constant), constant value index: " + constantValueIndex);
                break;
            case 'Z':
                constantValueIndex = dataInputStream.readUnsignedShort();
                value = findConstantByIndex(constantValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantValueIndex).append(", type: boolean): ").append(value.getIntValue());
                LOGGER.debug(buffer.toString());
                break;
            case 's':
                constantValueIndex = dataInputStream.readUnsignedShort();
                value = findConstantByIndex(constantValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantValueIndex).append(", type: String): ").append(value.getStringValue());
                LOGGER.debug(buffer.toString());
                break;
            case 'e':
                LOGGER.debug("Got an enum constant...TODO!");
                // TODO: parse enum
                break;
            case 'c':
                LOGGER.debug("Got a class...TODO!");
                // TODO: parse class
                break;
            case '@':
                LOGGER.debug("Got an annotation...TODO!");
                // TODO: parse annotation
                break;
            case '[':
                LOGGER.debug("Got an array...TODO!");
                // TODO: parse array
                break;
            default:
                LOGGER.warn("Unsupported tag: " + tag);
        }
    }

    protected static ConstantInfo findConstantByIndex(int index, ConstantPool constantPool) {
        if(constantPool != null) {
            for (Constant constant : constantPool.getConstants()) {
                if(index == constant.getConstantPoolIndex()) {
                    // Get first item
                    List<ConstantInfo> constantInfoList = constant.getConstantInfoList();
                    if(constantInfoList != null && constantInfoList.size() > 0) {
                        return constant.getConstantInfoList().get(0);
                    }
                }
            }
        }
        return null;
    }

}
