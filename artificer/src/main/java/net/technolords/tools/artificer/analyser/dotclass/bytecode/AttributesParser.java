package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.domain.dotclass.ConstantPool;
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
public class AttributesParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AttributesParser.class);
    /**
     * An attribute can have a different location, namely:
     * - ClassFile
     * - field_info
     * - method_info
     * - Code
     *
     * And depending on the location, the value or info has different semantics.
     */
    public static final String LOCATION_CLASS_FILE = "LOCATION_CLASS_FILE";
    public static final String LOCATION_FIELD_INFO = "LOCATION_FIELD_INFO";
    public static final String LOCATION_METHOD_INFO = "LOCATION_METHOD_INFO";
    public static final String LOCATION_CODE = "LOCATION_CODE";

    /**
     * Attributes occur in the following four structures:
     * - ClassFile
     * - field_info
     * - method_info
     * - Code
     *
     * From the 'ClassFile' structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   attributes_count;
     *     attributes_info      attributes[attributes_count];
     * }
     *
     * [java 8]
     * field_info {
     *     ...
     *     u2                   attributes_count;
     *     attributes_info      attributes[attributes_count];
     * }
     *
     * [java 8]
     * method_info {
     *      ...
     *      u2                  attributes_count;
     *      attributes_info     attributes[attributes_count];
     * }
     *
     * [java 8]
     * TODO: Code
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param attributesCount
     *  The total attributes to parse.
     * @param constantPool
     *  The constant pool associated with the resource.
     * @param location
     *  The location of the attribute (which is: ClassFile, field_info, method_info or Code)
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractAttributes(DataInputStream dataInputStream, int attributesCount, ConstantPool constantPool, String location) throws IOException {
        for(int i = 0; i < attributesCount; i++) {
            LOGGER.trace("extracting attribute: " + i);
            extractAttributeByName(dataInputStream, constantPool, location);
        }
    }

    /**
     * The structure has the following (general) format:
     *
     * [java 8]
     * attribute_info {
     *      u2                  attribute_name_index;
     *      u4                  attribute_length;
     *      u1 *                info[attribute_length]
     * }
     *
     * - attribute_name_index:
     *      For all attributes, the 'attribute_name_index' must be a valid unsigned 16-bit index into the
     *      constant pool of the class. The 'constant_pool' entry at 'attribute_name_index' must be a 'CONSTANT_Utf8_info'
     *      structure representing the name of the attribute.
     * - attribute_length:
     *      The value of the 'attribute_length' item indicates the length of the subsequent information in bytes. The
     *      length does not include the initial six bytes the 'attribute_name_index' and 'attribute_length' items.
     *
     * This specification has 23 predefined attributes (sorted by alphabet):
     * - AnnotationDefault                     [location: method_info]
     * - BootstrapMethods                      [location: ClassFile]
     * - ConstantValue                         [location: field_info]
     * - Code                                  [location: method_info]
     * - Deprecated                            [location: ClassFile, field_info, method_info]
     * - EnclosingMethod                       [location: ClassFile]
     * - Exceptions                            [location: method_info]
     * - InnerClasses                          [location: ClassFile]
     * - LineNumberTable                       [location: Code]
     * - LocalVariableTable                    [location: Code]
     * - LocalVariableTypeTable                [location: Code]
     * - MethodParameters                      [location: method_info]
     * - RuntimeInvisibleAnnotations           [location: ClassFile, field_info, method_info]
     * - RuntimeInvisibleParameterAnnotations  [location: method_info]
     * - RuntimeInvisibleTypeAnnotations       [location: ClassFile, field_info, method_info, Code]
     * - RuntimeVisibleAnnotations             [location: ClassFile, field_info, method_info]
     * - RuntimeVisibleParameterAnnotations    [location: method_info]
     * - RuntimeVisibleTypeAnnotations         [location: ClassFile, field_info, method_info, Code]
     * - Signature                             [location: ClassFile, field_info, method_info]
     * - SourceDebugExtension                  [location: ClassFile]
     * - SourceFile                            [location: ClassFile]
     * - StackMapTable                         [location: Code]
     * - Synthetic                             [location: ClassFile, field_info, method_info]
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param constantPool
     *  The constant pool associated with the resource.
     * @param location
     *  The location of the attribute (which is: ClassFile, field_info, method_info or Code)
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractAttributeByName(DataInputStream dataInputStream, ConstantPool constantPool, String location) throws IOException {
        int attributeNameIndex = dataInputStream.readUnsignedShort();
        String attributeName = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(constantPool, attributeNameIndex);
        int attributeLength = dataInputStream.readInt();
        LOGGER.debug("About to extract attribute by name, with name: " + attributeName + ", with attribute length: " + attributeLength);
        switch (attributeName) {
            // ClassFile attributes
            // field_info attributes
            case "Signature":                       // [location: ClassFile, field_info, method_info]
                // u2 signature_index
                // The value of the 'signature_index' must be a valid index in the 'constant_pool' table.
                // The 'constant_pool' entry at that index must be a 'CONSTANT_Utf8_info' structure representing
                // a class signature if this 'Signature' is an attribute of a 'ClassFile' structure. It is a
                // method signature of this 'Signature' is an attribute of a 'method_info' structure. It is a
                // field signature otherwise.
                int signature_index = dataInputStream.readUnsignedShort();
                String signature = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(constantPool, signature_index);
                LOGGER.debug("Class signature: " + signature + " (location: " + location + ")");
                break;
            // method_info attributes
            // Code attributes
            default:
                LOGGER.debug("TODO: extract attribute details of name: " + attributeName);
                for(int i = 0; i < attributeLength; i++) {
                    dataInputStream.readUnsignedByte();
                }
        }

    }

    // ClassFile attributes:
    //  - BootstrapMethods
    //  - Deprecated
    //  - EnclosingMethod
    //  - InnerClasses
    //  - RuntimeInvisibleAnnotations
    //  - RuntimeInvisibleTypeAnnotations
    //  - RuntimeVisibleAnnotations
    //  - RuntimeVisibleTypeAnnotations
    //  - Signature
    //  - SourceDebugExtension
    //  - SourceFile
    //  - Synthetic

    // field_info attributes
    //  - ConstantValue
    //  - Deprecated
    //  - RuntimeInvisibleAnnotations
    //  - RuntimeInvisibleTypeAnnotations
    //  - RuntimeVisibleAnnotations
    //  - RuntimeVisibleTypeAnnotations
    //  - Signature
    //  - Synthetic

    // method_info attributes
    //  - AnnotationDefault
    //  - Code
    //  - Deprecated
    //  - Exceptions
    //  - MethodParameters
    //  - RuntimeInvisibleAnnotations
    //  - RuntimeInvisibleParameterAnnotations
    //  - RuntimeInvisibleTypeAnnotations
    //  - RuntimeVisibleAnnotations
    //  - RuntimeVisibleParameterAnnotations
    //  - RuntimeVisibleTypeAnnotations
    //  - Signature
    //  - Synthetic

    // Code attributes
    //  - LineNumberTable
    //  - LocalVariableTable
    //  - LocalVariableTypeTable
    //  - RuntimeInvisibleTypeAnnotations
    //  - RuntimeVisibleTypeAnnotations
    //  - StackMapTable
}
