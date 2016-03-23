package net.technolords.tools.artificer.analyser.dotclass.bytecode.annotation;

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

    public static void extractElementValuePairs(DataInputStream dataInputStream, int numberOfElementValuePairs, Resource resource) throws IOException {
        LOGGER.debug("Number of element-value pairs: " + numberOfElementValuePairs);
        for(int index = 0; index < numberOfElementValuePairs; index++) {
            extractElementValuePair(dataInputStream, index, resource);
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
     *  The byte stream associated with the resource (aka .class file).
     * @param index
     *  The index of the element value pair.
     * @param resource
     *  The resource associated with the attribute.
     *
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractElementValuePair(DataInputStream dataInputStream, int index, Resource resource) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Element-value pair (index: ").append(index).append(")");

        // Read the element key
        int elementNameIndex = dataInputStream.readUnsignedShort();
        String element = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), elementNameIndex);
        buffer.append(", with key (index: ").append(elementNameIndex).append("): ").append(element);

        // Extract the value
        extractElementValue(dataInputStream, buffer, resource);
    }

    /**
     * Auxiliary method to extract an 'element_value' from the data input stream. Note that this method can be called
     * recursively (in case the value of the element_value is an array with more element_value's).
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param buffer
     *  The buffer with relevant text to support (deep and semantic) logging.
     * @param resource
     *  The resource associated with the attribute.
     *
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractElementValue(DataInputStream dataInputStream, StringBuilder buffer, Resource resource) throws IOException {
        // Read the tag
        int tag = dataInputStream.readUnsignedByte();

        // Read the element value
        switch ((char) tag) {

            case 'B':
                int constantByteValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo byteValue = findConstantByIndex(constantByteValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantByteValueIndex).append(", type: byte): ").append(byteValue.getIntValue());
                LOGGER.debug(buffer.toString());
                break;

            case 'C':
                int constantCharValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo charValue = findConstantByIndex(constantCharValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantCharValueIndex).append(", type: char): ").append((char) charValue.getIntValue());
                LOGGER.debug(buffer.toString());
                break;

            case 'D':
                int constantDoubleValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo doubleValue = findConstantByIndex(constantDoubleValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantDoubleValueIndex).append(", type: double): ").append(doubleValue.getIntValue());
                LOGGER.debug(buffer.toString());
                break;

            case 'F':
                int constantFloatValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo floatValue = findConstantByIndex(constantFloatValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantFloatValueIndex).append(", type: float): ").append(floatValue.getIntValue());
                LOGGER.debug(buffer.toString());
                break;

            case 'I':
                // Read the value index
                int constantIntegerValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo integerValue = findConstantByIndex(constantIntegerValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantIntegerValueIndex).append(", type: integer): ").append(integerValue.getIntValue());
                LOGGER.debug(buffer.toString());
                break;

            case 'J':
                // Read the value index
                int constantLongValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo longValue = findConstantByIndex(constantLongValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantLongValueIndex).append(", type: long): ").append(longValue.getIntValue());
                LOGGER.debug(buffer.toString());
                break;

            case 'S':
                // Read the value index
                int constantShortValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo shortValue = findConstantByIndex(constantShortValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantShortValueIndex).append(", type: short): ").append(shortValue.getIntValue());
                LOGGER.debug(buffer.toString());
                break;

            case 'Z':
                // Read the value index
                int constantBooleanValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo booleanValue = findConstantByIndex(constantBooleanValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantBooleanValueIndex).append(", type: boolean): ").append(booleanValue.getIntValue());
                LOGGER.debug(buffer.toString());
                break;

            case 's':
                // Read the value index
                int constantStringValueIndex = dataInputStream.readUnsignedShort();
                ConstantInfo stringValue = findConstantByIndex(constantStringValueIndex, resource.getConstantPool());
                buffer.append(", with value (index: ").append(constantStringValueIndex).append(", type: String): ").append(stringValue.getStringValue());
                LOGGER.debug(buffer.toString());
                break;

            case 'e':
                // Read the enum type index
                int typeNameIndex = dataInputStream.readUnsignedShort();

                // The value of the type_name_index item must be a valid index in the 'constant_pool' table. The 'constant_pool'
                // entry at that index must be a 'CONSTANT_Utf8_info' structure representing a field descriptor.
                String typeNameDescriptor = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), typeNameIndex);

                // Read the constant name index
                int constantNameIndex = dataInputStream.readUnsignedShort();

                // The value of constant_name_index item must be a valid index in the 'constant_pool' table. The 'constant_pool'
                // entry at that index must be a 'CONSTANT_Utf8_info' structure.
                String enumValue = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), constantNameIndex);
                buffer.append(", with type name (index: ").append(typeNameIndex).append(", type: enum): ").append(typeNameDescriptor);
                buffer.append(" and value (index: ").append(constantNameIndex).append("): ").append(enumValue);
                LOGGER.debug(buffer.toString());

                // Add signature (when applicable) to the referenced classes
                SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), typeNameDescriptor);
                break;

            case 'c':
                // Read the class index
                int classInfoIndex = dataInputStream.readUnsignedShort();

                // The class_info_index denotes a class literal as the value of this element-value pair. The class_info_index
                // must be a valid index into the 'constant_pool' table. The 'constant_pool' entry at that index must be a
                // 'CONSTANT_Utf8_info' structure representing a return descriptor. The return descriptor give the type
                // corresponding to the class literal represented by this 'element_value' structure. Types correspond to class
                // literals as follows:
                //
                // - For a class literal C.class, where C is the name of the class, interface, or array type, the corresponding
                //   type is C. The return descriptor in the 'constant_pool' will be an ObjectType or an ArrayType.
                //   Example: the class literal Object.class corresponds to Object, so the 'constant_pool' entry is Ljava/lang/Object;
                // - For a class literal p.class, where p is the name of a primitive type, the corresponding type is p. The
                //   return descriptor in the 'constant_pool' will be a BaseType character.
                //   Example: the literal int.class corresponds to the type int, so the 'constant_pool' entry is I.
                // - For a class literal void.class, the corresponding type is void. The return descriptor in the 'constant_pool'
                //   will be V.
                //   Example: the literal void.class corresponds to void, so the 'constant_pool' entry is V, whereas
                //   the class literal Void.class corresponds to the type Void, so the 'constant_pool' entry is Ljava/lang/Void;
                String descriptor = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), classInfoIndex);
                buffer.append(", with value (index: ").append(classInfoIndex).append(", type: Class) with descriptor: ").append(descriptor);
                LOGGER.debug(buffer.toString());

                // Add signature (when applicable) to the referenced classes
                SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), descriptor);
                break;

            case '@':
                buffer.append(", with type annotation -> delegating to extract annotation (with index: -1)...");
                LOGGER.debug(buffer.toString());

                // Read annotation
                extractAnnotation(dataInputStream, -1, resource);
                break;

            case '[':
                int numberOfValues = dataInputStream.readUnsignedShort();
                buffer.append(", with type array (size: ").append(numberOfValues).append(") entering recursion...");
                LOGGER.debug(buffer.toString());

                // Read array members, delegate by recursion
                LOGGER.debug("Number of nested element-value pairs: " + numberOfValues);
                for(int index = 0; index < numberOfValues; index++) {
                    StringBuilder nestedBuffer = new StringBuilder();
                    nestedBuffer.append("Nested element-value pair (index: ").append(index).append(")");
                    extractElementValue(dataInputStream, nestedBuffer, resource);
                }
                break;

            default:
                buffer.append(", but tag is unsupported: ").append(tag);
                LOGGER.warn(buffer.toString());
        }
    }

    /**
     * Auxiliary method to find a Constant by index (in the ConstantPool). Note that the ConstantPool
     * has a list, but the index will not be *that* index. Each Constant reference in that list has a
     * field member with an index which represents the semantic index in th ConstantPool. If that value
     * matches that of the parameter, the Constant is found.
     *
     * @param index
     *  The index associated with the Constant in the ConstantPool.
     * @param constantPool
     *  The ConstantPool associated with the Constants.
     *
     * @return
     *  A reference of a ConstantInfo
     */
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
