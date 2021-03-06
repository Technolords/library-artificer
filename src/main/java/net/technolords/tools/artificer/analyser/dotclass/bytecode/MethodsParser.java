package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.SignatureAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.specification.JavaSpecification;
import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Created by Technolords on 2016-Mar-22.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class MethodsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodsParser.class);

    /**
     * Auxiliary method to extract the methods associated to the resource. From the 'ClassFile'
     * structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   methods_count;
     *     method_info          methods[methods_count];
     *     ...
     * }
     *
     * - methods_count:
     *      The value of the 'methods_count' item gives the number of 'method_info' structures in the methods table.
     * - methods[]:
     *      Each value in the 'methods' table must be a 'method_info' structure giving a complete description of
     *      a method in this class or interface. If neither of the ACC_NATIVE and ACC_ABSTRACT flags are set in
     *      the 'access_flags' item of a 'method_info' structure, the Java Virtual Machine instructions implementing
     *      the method are also supplied.
     *
     *      The 'method_info' structures represent all methods declared by this class or interface type, including
     *      instance methods, class methods, instance initialization methods, and any class or interface
     *      initialization method. The methods table does not include items representing methods that are inherited
     *      from superclasses or superinterfaces.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param javaSpecification
     *  The Java specification associated with the compiled version associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractMethods(DataInputStream dataInputStream, JavaSpecification javaSpecification, Resource resource) throws IOException {
        int methodsCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("MethodsCount: " + methodsCount);
        for(int index = 0; index < methodsCount; index++) {
            extractMethod(dataInputStream, index, javaSpecification, resource);
        }
    }

    /**
     *
     * method_info {
     *     u2                   access_flags;
     *     u2                   name_index;
     *     u2                   descriptor_index;
     *     u2                   attributes_count;
     *     attribute_info       attributes[attributes_count];
     * }
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param index
     *  The attribute index, used for precise data logging.
     * @param javaSpecification
     *  The Java specification associated with the compiled version associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     */
    protected static void extractMethod(DataInputStream dataInputStream, int index, JavaSpecification javaSpecification, Resource resource) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Method (index: ").append(index).append(")");

        // Read the access flags
        AccessFlagsParser.extractAccessFlags(dataInputStream, AccessFlagsParser.LOCATION_METHOD_INFO);

        // Read name index
        int nameIndex = dataInputStream.readUnsignedShort();
        String methodName = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), nameIndex);
        buffer.append(", with name: ").append(methodName);

        // Read descriptor index
        int descriptorIndex = dataInputStream.readUnsignedShort();
        String descriptor = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), descriptorIndex);
        buffer.append(", with descriptor (index: ").append(descriptorIndex).append("): ").append(descriptor);

        // Read attributes count
        int attributesCount = dataInputStream.readUnsignedShort();
        buffer.append(" and total attributes: ").append(attributesCount);
        LOGGER.debug(buffer.toString());

        // Add signature (when applicable) to the referenced classes
        SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), descriptor);

        AttributesParser.extractAttributes(dataInputStream, attributesCount, javaSpecification, resource, AttributesParser.LOCATION_METHOD_INFO);
    }
}
