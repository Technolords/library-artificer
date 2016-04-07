package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Apr-07.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class EnclosingMethodParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnclosingMethodParser.class);

    /**
     * Auxiliary method to extract the enclosing methods associated with the resource. This is fetched from the
     * 'EnclosingMethod_attribute' structure, which has the following format:
     *
     * [java 8]
     * EnclosingMethod_attribute {
     *      u2          attribute_name_index;
     *      u4          attribute_length;
     *      u2          class_index;
     *      u2          method_index;
     * }
     *
     * - class_index:
     *      The value of the 'class_index' item must be a valid index into the 'constant_pool' table. The
     *      'constant_pool' entry at that index must be a 'CONSTANT_Class_info' structure representing the innermost
     *      class that encloses the declaration of the current class.
     * - method_index:
     *      If the current class is not immediately enclosed by a method or constructor, then the value of the
     *      'method_index' item must be zero. In particular, 'method_index' must be zero if the current class was
     *      immediately enclosed in source code by an instance initializer, static initializer, instance variable
     *      initializer, or class variable initializer.
     *      (The first two concern both local classes and anonymous classes, while the last two concern anonymous
     *      classes declared on the right hand side of a field assignment.)
     *      Otherwise, the value of the' method_index' item must be a valid index into the 'constant_pool table'.
     *      The 'constant_pool' entry at that index must be a 'CONSTANT_NameAndType_info' structure representing
     *      the name and type of a method in the class referenced by the 'class_index' attribute above.
     *      It is the responsibility of a Java compiler to ensure that the method identified via the 'method_index'
     *      is indeed the closest lexically enclosing method of the class that contains this EnclosingMethod attribute.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractEnclosingMethod(DataInputStream dataInputStream, Resource resource) throws IOException {
        int classIndex = dataInputStream.readUnsignedShort();
        int methodIndex = dataInputStream.readUnsignedShort();
    }
}
