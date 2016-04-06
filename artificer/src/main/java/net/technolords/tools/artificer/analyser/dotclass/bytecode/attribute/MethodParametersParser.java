package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Apr-06.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class MethodParametersParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodParametersParser.class);

    /**
     * Auxiliary method to extract the method parameters default associated with the resource. This is fetched from the
     * 'MethodParameters_attribute' structure, which has the following format:
     *
     * [java 8]
     * MethodParameters_attribute {
     *      u2          attribute_name_index;
     *      u4          attribute_length;
     *      u1          parameters_count;
     *      {
     *          u2      name_index;
     *          u2      access_flags;
     *      }           parameters[parameters_count];
     * }
     *
     * - parameters_count:
     *      The value of the 'parameters_count' item indicates the number of parameter descriptors in the method
     *      descriptor referenced by the 'descriptor_index' of the attribute's enclosing 'method_info' structure.
     * - parameters[]:
     *      Each entry in the parameters array contains the following pair of items:
     *
     *      - name_index:
     *          The value of the 'name_index' item must either be zero or a valid index into the 'constant_pool' table.
     *          If the value of the 'name_index' item is zero, then this parameters element indicates a formal
     *          parameter with no name.
     *          If the value of the 'name_index' item is nonzero, the 'constant_pool' entry at that index must be a
     *          'CONSTANT_Utf8_info' structure representing a valid unqualified name denoting a formal parameter.
     *      - access_flags:
     *          The value of the 'access_flags' item is as follows:
     *              0x0010 (ACC_FINAL)          Indicates that the formal parameter was declared final.
     *              0x1000 (ACC_SYNTHETIC)      Indicates that the formal parameter was not explicitly or implicitly
     *                                          declared in source code, according to the specification of the language
     *                                          in which the source code was written.
     *                                          (The formal parameter is an implementation artifact of the compiler
     *                                          which produced this class file.)
     *              0x8000 (ACC_MANDATED)       Indicates that the formal parameter was implicitly declared in source
     *                                          code, according to the specification of the language in which the source
     *                                          code was written.
     *                                          (The formal parameter is mandated by a language specification, so all
     *                                          compilers for the language must emit it.)
     *
     *      The i'th entry in the parameters array corresponds to the i'th parameter descriptor in the enclosing
     *      method's descriptor.
     *      (The 'parameters_count' item is one byte because a method descriptor is limited to 255 parameters.)
     *      Effectively, this means the parameters array stores information for all the parameters of the method.
     *      One could imagine other schemes, where entries in the parameters array specify their corresponding
     *      parameter descriptors, but it would unduly complicate the MethodParameters attribute.
     *
     *      The i'th entry in the parameters array may or may not correspond to the i'th type in the enclosing method's
     *      Signature attribute (if present), or to the i'th annotation in the enclosing method's parameter annotations.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractMethodParameters(DataInputStream dataInputStream, Resource resource) throws IOException {

    }
}
