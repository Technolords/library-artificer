package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Created by Technolords on 2016-Mar-31.
 *
 * /**
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class ParameterAnnotationsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParameterAnnotationsParser.class);

    /**
     * Auxiliary method to extract the annotations associated with the resource. This is fetched from the
     * 'RuntimeInvisibleParameterAnnotations_attribute' as well as the 'RuntimeVisibleParameterAnnotations_attribute'
     * structure, which has the following format:
     *
     * [java 8]
     * RuntimeInvisibleParameterAnnotations_attribute {
     *      u2                  attribute_name_index;
     *      u4                  attribute_length;
     *      u1                  num_parameters;
     *      {
     *          u2              num_annotations;
     *          annotation      annotations[num_annotations];
     *      } parameter_annotations[num_parameters];
     * }
     *
     * RuntimeVisibleParameterAnnotations_attribute {
     *      u2                  attribute_name_index;
     *      u4                  attribute_length;
     *      u1                  num_parameters;
     *      {
     *          u2              num_annotations;
     *          annotation      annotations[num_annotations];
     *      } parameter_annotations[num_parameters];
     * }
     *
     * - num_parameters:
     *      The value of the 'num_parameters' item gives the number of formal parameters of the method represented by
     *      the method_info structure on which the annotation occurs. This duplicates information that could be
     *      extracted from the method descriptor.
     * - parameter_annotations[]:
     *      Each entry in the 'parameter_annotations' table represents all of the run-time invisible annotations on
     *      the declaration of a single formal parameter. The i'th entry in the table corresponds to the i'th formal
     *      parameter in the method descriptor. Each parameter_annotations entry contains the following two items:
     *
     *      num_annotations:
     *          The value of the 'num_annotations' item indicates the number of run-time invisible annotations on the
     *          declaration of the formal parameter corresponding to the 'parameter_annotations' entry.
     *      annotations[]:
     *          Each entry in the annotations table represents a single run-time invisible annotation on the
     *          declaration of the formal parameter corresponding to the 'parameter_annotations' entry.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractParameterAnnotations(DataInputStream dataInputStream, Resource resource) throws IOException {
        int parametersCount = dataInputStream.readUnsignedByte();
        LOGGER.debug("Total parameters annotations: " + parametersCount);

        for(int index = 0; index < parametersCount; index++) {
            extractParameterAnnotation(dataInputStream, index, resource);
        }
    }

    protected static void extractParameterAnnotation(DataInputStream dataInputStream, int index, Resource resource) throws IOException {
        LOGGER.debug("Delegated parsing for parameter annotation with index: " + index + " to AnnotationParser");
        AnnotationsParser.extractAnnotations(dataInputStream, resource);
    }

}
