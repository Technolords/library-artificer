package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.AccessFlagsParser;
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
public class InnerClassesParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(InnerClassesParser.class);

    /**
     * Auxiliary method to extract the inner classes associated with the resource. This is fetched from the
     * 'InnerClasses_attribute' structure, which has the following format:
     *
     * [java 8]
     * InnerClasses_attribute {
     *      u2          attribute_name_index;
     *      u4          attribute_length;
     *      u2          number_of_classes;
     *      {
     *          u2      inner_class_info_index;
     *          u2      outer_class_info_index;
     *          u2      inner_name_index;
     *          u2      inner_class_access_flags;
     *      }           classes[number_of_classes];
     * }
     *
     * - number_of_classes:
     *      The value of the 'number_of_classes' item indicates the number of entries in the classes array.
     * - classes[]:
     *      Every 'CONSTANT_Class_info' entry in the constant_pool table which represents a class or interface C that
     *      is not a package member must have exactly one corresponding entry in the classes array.
     *      If a class or interface has members that are classes or interfaces, its 'constant_pool' table (and hence
     *      its InnerClasses attribute) must refer to each such member, even if that member is not otherwise mentioned
     *      by the class. In addition, the 'constant_pool' table of every nested class and nested interface must refer
     *      to its enclosing class, so altogether, every nested class and nested interface will have InnerClasses
     *      information for each enclosing class and for each of its own nested classes and interfaces.
     *
     *      Each entry in the classes array contains the following four items:
     *
     *      - inner_class_info_index:
     *          The value of the 'inner_class_info_index' item must be a valid index into the 'constant_pool' table.
     *          The 'constant_pool' entry at that index must be a 'CONSTANT_Class_info' structure representing C. The
     *          remaining items in the classes array entry give information about C.
     *      - outer_class_info_index:
     *          If C is not a member of a class or an interface (that is, if C is a top-level class or interface
     *          or a local class or an anonymous class), the value of the 'outer_class_info_index' item must be zero.
     *          Otherwise, the value of the 'outer_class_info_index' item must be a valid index into the 'constant_pool'
     *          table, and the entry at that index must be a 'CONSTANT_Class_info' structure representing the class or
     *          interface of which C is a member.
     *      - inner_name_index:
     *          If C is anonymous, the value of the inner_name_index item must be zero. Otherwise, the value of the
     *          'inner_name_index' item must be a valid index into the 'constant_pool table', and the entry at that
     *          index must be a 'CONSTANT_Utf8_info' structure that represents the original simple name of C, as given
     *          in the source code from which this class file was compiled.
     *      - inner_class_access_flags:
     *          The value of the 'inner_class_access_flags' item is a mask of flags used to denote access permissions
     *          to and properties of class or interface C as declared in the source code from which this class file
     *          was compiled. It is used by a compiler to recover the original information when source code is not
     *          available.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractInnerClasses(DataInputStream dataInputStream, Resource resource) throws IOException {
        // Read number of inner classes
        int numberOfInnerClasses = dataInputStream.readUnsignedShort();
        LOGGER.debug("InnerClasses count: " + numberOfInnerClasses);
        for(int index = 0; index < numberOfInnerClasses; index++) {
            extractInnerClass(dataInputStream, index, resource);
        }
    }

    /**
     * Auxiliary method to extract an inner class associated with the resource.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param index
     *  The attribute index, used for precise data logging.
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractInnerClass(DataInputStream dataInputStream, int index, Resource resource) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Inner class (index: ").append(index).append(")");

        // Read inner class index
        int innerClassInfoIndex = dataInputStream.readUnsignedShort();

        // Read outer class index
        int outerClassInfoIndex = dataInputStream.readUnsignedShort();

        // Read class name index
        int classNameIndex = dataInputStream.readUnsignedShort();
        String className = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), classNameIndex);
        buffer.append(", with class name: ").append(className);
        LOGGER.debug(buffer.toString());

        // Read the access flags
        AccessFlagsParser.extractAccessFlags(dataInputStream, AccessFlagsParser.LOCATION_NESTED_CLASS_FILE);
    }

}
