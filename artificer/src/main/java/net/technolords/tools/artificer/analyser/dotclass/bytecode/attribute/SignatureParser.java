package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.SignatureAnalyser;
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
public class SignatureParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignatureParser.class);

    /**
     * Auxiliary method to extract the signature associated with the resource. This is fetched from the
     * 'Exceptions_attribute' structure, which has the following format:
     *
     * [java 8]
     * Signature_attribute {
     *      u2          attribute_name_index;
     *      u4          attribute_length;
     *      u2          signature_index;
     * }
     *
     * - signature_index:
     *      The value of the 'signature_index' must be a valid index in the 'constant_pool' table. The 'constant_pool'
     *      entry at that index must be a 'CONSTANT_Utf8_info' structure representing a class signature if this
     *      'Signature' is an attribute of a 'ClassFile' structure. It is a method signature of this 'Signature' is
     *      an attribute of a 'method_info' structure. It is a field signature otherwise.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractSignature(DataInputStream dataInputStream, String location, Resource resource) throws IOException {
        int signatureIndex = dataInputStream.readUnsignedShort();
        String signature = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), signatureIndex);
        LOGGER.debug("Class signature: " + signature + " (location: " + location + ")");

        // Add signature (when applicable) to the referenced classes
        SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), signature);
    }
}
