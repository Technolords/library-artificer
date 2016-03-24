package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.SignatureAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.annotation.AnnotationsParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.annotation.TypeAnnotationsParser;
import net.technolords.tools.artificer.domain.dotclass.Constant;
import net.technolords.tools.artificer.domain.dotclass.ConstantInfo;
import net.technolords.tools.artificer.domain.resource.Resource;
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
     * @param resource
     *  The resource associated with the attributes.
     * @param location
     *  The location of the attribute (which is: ClassFile, field_info, method_info or Code)
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractAttributes(DataInputStream dataInputStream, int attributesCount, Resource resource, String location) throws IOException {
        for(int index = 0; index < attributesCount; index++) {
            extractAttributeByName(dataInputStream, index, resource, location);
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
     * TODO: implement, field, method and then ClassFile and lastly Code?
     * This specification has 23 predefined attributes (sorted by alphabet):
     * - AnnotationDefault                     [location: method_info]
     * - BootstrapMethods                      [location: ClassFile]
     * v ConstantValue                         [location: field_info]
     * - Code                                  [location: method_info]
     * v Deprecated                            [location: ClassFile, field_info, method_info]
     * - EnclosingMethod                       [location: ClassFile]
     * - Exceptions                            [location: method_info]
     * - InnerClasses                          [location: ClassFile]
     * - LineNumberTable                       [location: Code]
     * - LocalVariableTable                    [location: Code]
     * - LocalVariableTypeTable                [location: Code]
     * - MethodParameters                      [location: method_info]
     * v RuntimeInvisibleAnnotations           [location: ClassFile, field_info, method_info]
     * - RuntimeInvisibleParameterAnnotations  [location: method_info]
     * v RuntimeInvisibleTypeAnnotations       [location: ClassFile, field_info, method_info, Code]
     * v RuntimeVisibleAnnotations             [location: ClassFile, field_info, method_info]
     * - RuntimeVisibleParameterAnnotations    [location: method_info]
     * v RuntimeVisibleTypeAnnotations         [location: ClassFile, field_info, method_info, Code]
     * v Signature                             [location: ClassFile, field_info, method_info]
     * - SourceDebugExtension                  [location: ClassFile]
     * - SourceFile                            [location: ClassFile]
     * - StackMapTable                         [location: Code]
     * v Synthetic                             [location: ClassFile, field_info, method_info]
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param index
     *  The attribute index, used for precise data logging.
     * @param resource
     *  The resource associated with the attribute.
     * @param location
     *  The location of the attribute (which is: ClassFile, field_info, method_info or Code)
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractAttributeByName(DataInputStream dataInputStream, int index, Resource resource, String location) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Attribute (index: ").append(index).append(")");

        // Read the name index
        int attributeNameIndex = dataInputStream.readUnsignedShort();
        String attributeName = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), attributeNameIndex);

        // Read attribute length
        int attributeLength = dataInputStream.readInt();
        buffer.append(", with (index: ").append(attributeNameIndex).append(") of type: ").append(attributeName);
        buffer.append(", with attribute length: ").append(attributeLength);
        LOGGER.debug(buffer.toString());

        // Proceed by attribute name (sorted by alphabet)
        switch (attributeName) {

            // AnnotationDefault                     [location: method_info]
            // BootstrapMethods                      [location: ClassFile]

            case "ConstantValue":                   // [location: field_info]
                // u2                       constantvalue_index;
                //
                // - constantvalue_index:
                //      The value of the 'constantvalue_index' must be a valid index in the 'constant_pool' table. The
                //      'constant_pool' entry at that index gives the constant value represented by this attribute. The
                //      'constant_pool' entry must be of a type appropriate to the field as specified in:
                //
                //      Field type                      Entry type
                //      long                            CONSTANT_Long
                //      float                           CONSTANT_Float
                //      double                          CONSTANT_Double
                //      int,short,char,byte,boolean     CONSTANT_Integer
                //      String                          CONSTANT_String
                int constantValueIndex = dataInputStream.readUnsignedShort();
                Constant constant = ConstantPoolAnalyser.findConstantByIndex(resource.getConstantPool(), constantValueIndex);
                ConstantInfo constantInfo = constant.getConstantInfoList().get(0);
                StringBuilder bufferForConstant = new StringBuilder();
                bufferForConstant.append("Attribute (index: ").append(index).append(")");
                bufferForConstant.append(", is a constant (of type: ").append(constant.getType()).append(") with value: ");
                switch (constant.getType()) {
                    case "Double":
                        bufferForConstant.append(constantInfo.getDoubleValue());
                        break;
                    case "Float":
                        bufferForConstant.append(constantInfo.getFloatValue());
                        break;
                    case "Integer":
                        bufferForConstant.append(constantInfo.getIntValue());
                        break;
                    case "Long":
                        bufferForConstant.append(constantInfo.getLongValue());
                        break;
                    case "String":
                        int stringIndex = constantInfo.getIntValue();
                        String stringValue = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), stringIndex);
                        bufferForConstant.append(stringValue);
                        break;
                    default:
                        bufferForConstant.append("TODO: extract value...");
                }
                LOGGER.debug(bufferForConstant.toString());
                break;

            // Code                                  [location: method_info]

            case "Deprecated":                      // [location: ClassFile, field_info, method_info]
                // Nothing to do, as it is a marker attribute for the Java Compiler
                break;

            // EnclosingMethod                       [location: ClassFile]
            // Exceptions                            [location: method_info]
            // InnerClasses                          [location: ClassFile]
            // LineNumberTable                       [location: Code]
            // LocalVariableTable                    [location: Code]
            // LocalVariableTypeTable                [location: Code]
            // MethodParameters                      [location: method_info]

            case "RuntimeInvisibleAnnotations":     // [location: ClassFile, field_info, method_info]
                // u2                       num_annotations;
                // annotation               annotations[num_annotations];
                //
                // - num_annotations:
                //      The value of 'num_annotations' item gives the number of run-time visible annotations represented
                //      by the structure.
                // - annotation[]:
                //      Each entry in the 'annotation' table represents a single run-time visible annotation on a declaration.

                // Read number of annotations
                int numberOfRuntimeInvisibleAnnotations = dataInputStream.readUnsignedShort();
                LOGGER.debug("Total annotations: " + numberOfRuntimeInvisibleAnnotations);
                AnnotationsParser.extractAnnotations(dataInputStream, numberOfRuntimeInvisibleAnnotations, resource);
                break;

            // RuntimeInvisibleParameterAnnotations  [location: method_info]

            case "RuntimeInvisibleTypeAnnotations": // [location: ClassFile, field_info, method_info, Code]
                // u2                       num_annotations
                // type_annotation          annotations[num_annotations]

                // Read number of invisible type annotations
                int numberOfInvisibleTypeAnnotations = dataInputStream.readUnsignedShort();
                LOGGER.debug("Total type annotations: " + numberOfInvisibleTypeAnnotations);
                TypeAnnotationsParser.extractTypeAnnotations(dataInputStream, numberOfInvisibleTypeAnnotations, resource);
                break;

            case "RuntimeVisibleAnnotations":       // [location: ClassFile, field_info, method_info]
                // u2                       num_annotations
                // annotation               annotations[num_annotations]
                //
                // num_annotations:
                //  The value of 'num_annotations' item gives the number of run-time visible annotations represented
                //  by the structure.
                // annotation:
                //  Each entry in the 'annotation' table represents a single run-time visible annotation on a declaration.

                // Read number of annotations
                int numberOfRuntimeVisibleAnnotations = dataInputStream.readUnsignedShort();
                LOGGER.debug("Total annotations: " + numberOfRuntimeVisibleAnnotations);
                AnnotationsParser.extractAnnotations(dataInputStream, numberOfRuntimeVisibleAnnotations, resource);
                break;

            // RuntimeVisibleParameterAnnotations    [location: method_info]

            case "RuntimeVisibleTypeAnnotations":   // [location: ClassFile, field_info, method_info, Code]
                // u2                       num_annotations;
                // type_annotation          annotations[num_annotations];
                //
                // - num_annotations:
                //      The value of the 'num_annotations' item gives the number of run-time visible type annotations
                //      represented by the structure.
                // - annotations[]:
                //      Each entry in the 'annotations' table represents a single run-time visible annotation on a
                //      type used in a declaration or expression. The 'type_annotation' structure has the following
                //      format:

                // Read number of invisible type annotations
                int numberOfVisibleTypeAnnotations = dataInputStream.readUnsignedShort();
                LOGGER.debug("Total type annotations: " + numberOfVisibleTypeAnnotations);
                TypeAnnotationsParser.extractTypeAnnotations(dataInputStream, numberOfVisibleTypeAnnotations, resource);
                break;

            case "Signature":                       // [location: ClassFile, field_info, method_info]
                // u2                       signature_index
                //
                // signature_index:
                //  The value of the 'signature_index' must be a valid index in the 'constant_pool' table.
                //  The 'constant_pool' entry at that index must be a 'CONSTANT_Utf8_info' structure representing
                //  a class signature if this 'Signature' is an attribute of a 'ClassFile' structure. It is a
                //  method signature of this 'Signature' is an attribute of a 'method_info' structure. It is a
                //  field signature otherwise.

                // Read signature index
                int signatureIndex = dataInputStream.readUnsignedShort();
                String signature = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), signatureIndex);
                LOGGER.debug("Class signature: " + signature + " (location: " + location + ")");

                // Add signature (when applicable) to the referenced classes
                SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), signature);
                break;

            // SourceDebugExtension                  [location: ClassFile]
            // SourceFile                            [location: ClassFile]
            // StackMapTable                         [location: Code]

            case "Synthetic":                       // [location: ClassFile, field_info, method_info]
                // Nothing to do, as it is a marker attribute for the Java Compiler
                break;

            default:
                LOGGER.debug("TODO: extract attribute details of name: " + attributeName + " for now absorbing bytes...");
                for(int i = 0; i < attributeLength; i++) {
                    dataInputStream.readUnsignedByte();
                }
        }
    }

}
