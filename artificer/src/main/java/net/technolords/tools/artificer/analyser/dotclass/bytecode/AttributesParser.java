package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.AnnotationDefaultParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.AnnotationsParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.BootstrapMethodsParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.CodeParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.EnclosingMethodParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.InnerClassesParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.LineNumberTableParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.MethodParametersParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.ParameterAnnotationsParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.SignatureParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.SourceDebugExtensionParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.SourceFileParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.TypeAnnotationsParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.ConstantValueParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute.ExceptionsParser;
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
    private static final String ANNOTATION_DEFAULT = "AnnotationDefault";
    private static final String BOOTSTRAP_METHODS = "BootstrapMethods";
    private static final String CONSTANT_VALUE = "ConstantValue";
    private static final String CODE = "Code";
    private static final String DEPRECATED = "Deprecated";
    private static final String ENCLOSING_METHOD = "EnclosingMethod";
    private static final String EXCEPTIONS = "Exceptions";
    private static final String INNER_CLASSES = "InnerClasses";
    private static final String LINE_NUMBER_TABLE = "LineNumberTable";
    private static final String LOCAL_VARIABLE_TABLE = "LocalVariableTable";
    private static final String LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable";
    private static final String METHOD_PARAMETERS = "MethodParameters";
    private static final String RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations";
    private static final String RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations";
    private static final String RUNTIME_INVISIBLE_TYPE_ANNOTATIONS = "RuntimeInvisibleTypeAnnotations";
    private static final String RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations";
    private static final String RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations";
    private static final String RUNTIME_VISIBLE_TYPE_ANNOTATIONS = "RuntimeVisibleTypeAnnotations";
    private static final String SIGNATURE = "Signature";
    private static final String SOURCE_DEBUG_EXTENSION = "SourceDebugExtension";
    private static final String SOURCE_FILE = "SourceFile";
    private static final String STACK_MAP_TABLE = "StackMapTable";
    private static final String SYNTHETIC = "Synthetic";

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

    public static void extractAttributesFromClassFile(DataInputStream dataInputStream, Resource resource) throws IOException {
        int attributesCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("Attributes count (ClassFile): " + attributesCount);
        extractAttributes(dataInputStream, attributesCount, resource, AttributesParser.LOCATION_CLASS_FILE);
    }

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
     * This specification has 23 predefined attributes (sorted by alphabet):
     * v AnnotationDefault                     [location: method_info]
     * v BootstrapMethods                      [location: ClassFile]
     * v ConstantValue                         [location: field_info]
     * - Code                                  [location: method_info]
     * v Deprecated                            [location: ClassFile, field_info, method_info]
     * v EnclosingMethod                       [location: ClassFile]
     * v Exceptions                            [location: method_info]
     * v InnerClasses                          [location: ClassFile]
     * - LineNumberTable                       [location: Code]
     * - LocalVariableTable                    [location: Code]
     * - LocalVariableTypeTable                [location: Code]
     * v MethodParameters                      [location: method_info]
     * v RuntimeInvisibleAnnotations           [location: ClassFile, field_info, method_info]
     * v RuntimeInvisibleParameterAnnotations  [location: method_info]
     * v RuntimeInvisibleTypeAnnotations       [location: ClassFile, field_info, method_info, Code]
     * v RuntimeVisibleAnnotations             [location: ClassFile, field_info, method_info]
     * v RuntimeVisibleParameterAnnotations    [location: method_info]
     * v RuntimeVisibleTypeAnnotations         [location: ClassFile, field_info, method_info, Code]
     * v Signature                             [location: ClassFile, field_info, method_info]
     * v SourceDebugExtension                  [location: ClassFile]
     * v SourceFile                            [location: ClassFile]
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

            case ANNOTATION_DEFAULT:                        // [location: method_info]
                // Parse the annotation default (delegated)
                AnnotationDefaultParser.extractAnnotationDefault(dataInputStream, resource);
                break;

            case BOOTSTRAP_METHODS:                         // [location: ClassFile]
                // Parse the bootstrap arguments (delegated)
                BootstrapMethodsParser.extractBootstrapMethods(dataInputStream, resource);
                break;

            case CONSTANT_VALUE:                            // [location: field_info]
                // Parse the constant (delegated)
                ConstantValueParser.extractConstantValue(dataInputStream, resource);
                break;

            case CODE:                                      // [location: method_info]
                // Parse the code (delegated)
                CodeParser.extractCode(dataInputStream, resource);
                break;

            case DEPRECATED:                                // [location: ClassFile, field_info, method_info]
                // Nothing to do, as it is a marker attribute for the Java Compiler
                break;

            case ENCLOSING_METHOD:                          // [location: ClassFile]
                // Parse the enclosing method (delegated)
                EnclosingMethodParser.extractEnclosingMethod(dataInputStream, resource);
                break;

            case EXCEPTIONS:                                // [location: method_info]
                // Parse the exceptions (delegated)
                ExceptionsParser.extractExceptions(dataInputStream, resource);
                break;

            case INNER_CLASSES:                             // [location: ClassFile]
                // Parse the inner classes (delegated)
                InnerClassesParser.extractInnerClasses(dataInputStream, resource);
                break;

            case LINE_NUMBER_TABLE:                         // [location: Code]
                LineNumberTableParser.extractLineNumberTable(dataInputStream, resource);
                // TODO
//                LOGGER.debug("TODO: extract attribute details of name: " + attributeName + " for now absorbing bytes...");
//                for(int i = 0; i < attributeLength; i++) {
//                    dataInputStream.readUnsignedByte();
//                }
                break;

            case LOCAL_VARIABLE_TABLE:                      // [location: Code]
                // TODO
                LOGGER.debug("TODO: extract attribute details of name: " + attributeName + " for now absorbing bytes...");
                for(int i = 0; i < attributeLength; i++) {
                    dataInputStream.readUnsignedByte();
                }
                break;

            case LOCAL_VARIABLE_TYPE_TABLE:                 // [location: Code]
                // TODO
                LOGGER.debug("TODO: extract attribute details of name: " + attributeName + " for now absorbing bytes...");
                for(int i = 0; i < attributeLength; i++) {
                    dataInputStream.readUnsignedByte();
                }
                break;

            case METHOD_PARAMETERS:                         // [location: method_info]
                // Parse the method parameters (delegated)
                MethodParametersParser.extractMethodParameters(dataInputStream, resource);
                break;

            case RUNTIME_INVISIBLE_ANNOTATIONS:             // [location: ClassFile, field_info, method_info]
                // Parse the annotations (delegated)
                AnnotationsParser.extractAnnotations(dataInputStream, resource);
                break;

            case RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS:   // [location: method_info]
                // Parse the parameter annotations (delegated)
                ParameterAnnotationsParser.extractParameterAnnotations(dataInputStream, resource);
                break;

            case RUNTIME_INVISIBLE_TYPE_ANNOTATIONS:        // [location: ClassFile, field_info, method_info, Code]
                // Parse the type annotations (delegated)
                TypeAnnotationsParser.extractTypeAnnotations(dataInputStream, resource);
                break;

            case RUNTIME_VISIBLE_ANNOTATIONS:               // [location: ClassFile, field_info, method_info]
                // Parse the annotations (delegated)
                AnnotationsParser.extractAnnotations(dataInputStream, resource);
                break;

            case RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS:     // [location: method_info]
                // Parse the parameter annotations (delegated)
                ParameterAnnotationsParser.extractParameterAnnotations(dataInputStream, resource);
                break;

            case RUNTIME_VISIBLE_TYPE_ANNOTATIONS:          // [location: ClassFile, field_info, method_info, Code]
                // Parse the type annotations (delegated)
                TypeAnnotationsParser.extractTypeAnnotations(dataInputStream, resource);
                break;

            case SIGNATURE:                                 // [location: ClassFile, field_info, method_info]
                // Parse the signature (delegated)
                SignatureParser.extractSignature(dataInputStream, location, resource);
                break;

            case SOURCE_DEBUG_EXTENSION:                    // [location: ClassFile]
                // Parse the source debug extension (delegated)
                SourceDebugExtensionParser.extractSourceDebugExtension(dataInputStream, attributeLength, resource);
                break;

            case SOURCE_FILE:                               // [location: ClassFile]
                // Parse the source file (delegated)
                SourceFileParser.extractSourceFile(dataInputStream, resource);
                break;

            case STACK_MAP_TABLE:                           // [location: Code]
                // TODO
                LOGGER.debug("TODO: extract attribute details of name: " + attributeName + " for now absorbing bytes...");
                for(int i = 0; i < attributeLength; i++) {
                    dataInputStream.readUnsignedByte();
                }
                break;

            case SYNTHETIC:                                 // [location: ClassFile, field_info, method_info]
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
