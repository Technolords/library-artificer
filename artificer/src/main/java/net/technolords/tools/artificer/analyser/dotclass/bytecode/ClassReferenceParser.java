package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Mar-22.
 */
public class ClassReferenceParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassReferenceParser.class);

    /**
     * Auxiliary method to extract the this class associated to the resource. From the 'ClassFile'
     * structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   this_class;
     *     ...
     * }
     *
     * - this_class:
     *      The value of the 'this_class' item must be a valid index into the 'constant_pool' table. The
     *      'constant_pool' entry at that index must be a 'CONSTANT_Class_info' structure representing the class or
     *      interface defined by this class file.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractThisClassReference(DataInputStream dataInputStream) throws IOException {
        int thisClassReference = dataInputStream.readUnsignedShort();
        LOGGER.debug("ConstantPool index for thisClassReference: " + thisClassReference);
    }

    /**
     * Auxiliary method to extract the minor and major version associated to the resource. From the 'ClassFile'
     * structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   super_class;
     *     ...
     * }
     *
     * - super_class:
     *      For a class, the value of the 'super_class' item either must be zero or must be a valid index into the
     *      'constant_pool' table. If the value of the 'super_class' item is nonzero, the 'constant_pool' entry at
     *      that index must be a 'CONSTANT_Class_info' structure representing the direct superclass of the class
     *      defined by this class file. Neither the direct superclass nor any of its superclasses may have
     *      the ACC_FINAL flag set in the 'access_flags' item of its 'ClassFile' structure.
     *
     *      If the value of the 'super_class' item is zero, then this class file must represent the class Object,
     *      the only class or interface without a direct superclass.
     *
     *      For an interface, the value of the 'super_class' item must always be a valid index into the 'constant_pool'
     *      table. The 'constant_pool' entry at that index must be a 'CONSTANT_Class_info' structure representing
     *      the class Object.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractSuperClassReference(DataInputStream dataInputStream) throws IOException {
        int superClassReference = dataInputStream.readUnsignedShort();
        LOGGER.debug("ConstantPool index for superClassReference: " + superClassReference);
    }
}
