package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Created by Technolords on 2016-Apr-06.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class AnnotationDefaultParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationDefaultParser.class);

    /**
     * Auxiliary method to extract the annotation default associated with the resource. This is fetched from the
     * 'AnnotationDefault_attribute' structure, which has the following format:
     *
     * [java 8]
     * AnnotationDefault_attribute {
     *      u2            attribute_name_index;
     *      u4            attribute_length;
     *      element_value default_value;
     * }
     *
     * - default_value:
     *      The 'default_value' item represents the default value of the annotation type element represented by the
     *      'method_info' structure enclosing this AnnotationDefault attribute.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractAnnotationDefault(DataInputStream dataInputStream, Resource resource) throws IOException {

        StringBuilder buffer = new StringBuilder();
        buffer.append("Annotation default ");

        // Read element value
        AnnotationsParser.extractElementValue(dataInputStream, buffer, resource);
    }
}
