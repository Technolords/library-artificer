package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.domain.dotclass.ConstantPool;
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
public class FieldsParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldsParser.class);

    /**
     * From the 'ClassFile' structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   fields_count;
     *     field_info           fields[fields_count];
     *     ...
     * }
     *
     * - fields_count:
     *      The value of 'fields_count' item gives the number of 'field_info' structures in the 'fields' table.
     *      The 'field_info' structures represent all fields, both class variables and instance variables, declared
     *      by this class or interface type.
     * - fields[]:
     *      Each value in the 'fields' table must be a 'field_info' structure giving a complete description of a field
     *      in this class or interface. The 'fields' table include only those fields that are declared by this class
     *      or interface. It does not include items representing fields that are inherited from superclasses or
     *      superinterfaces.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param constantPool
     *  The constant pool associated with the resource.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractFields(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        int fieldsCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("total fields: " + fieldsCount);
        if(fieldsCount != 0) {
            for(int i = 0; i < fieldsCount; i++) {
                LOGGER.trace("extracting field: " + i);
                extractField(dataInputStream, constantPool);
            }
        }
    }

    /**
     * Each field is described by a 'field_info' structure. No two fields in one class file may have the same name
     * and descriptor. The structure has the following format:
     *
     * [java 8]
     * field_info {
     *      u2                  access_flags;
     *      u2                  name_index;
     *      u2                  descriptor_index;
     *      u2                  attributes_count;
     *      attributes_info     attributes[attributes_count]
     * }
     *
     * - access_flags:
     *      The value of the 'access_flags' item is a mask of flags used to denote access permissions to, and
     *      properties of this field. The interpretation of each flag, when set, is specified in the following
     *      table:
     *          flag_name       value           interpretation
     *
     *          ACC_PUBLIC      0x0001          Declared 'public' (may be accessed from outside its package).
     *          ACC_PRIVATE     0x0002          Declared 'private' (usable only within the defining class).
     *          ACC_PROTECTED   0x0004          Declared 'protected' (may be accessed within subclasses).
     *          ACC_STATIC      0x0008          Declared 'static'.
     *          ACC_FINAL       0x0010          Declared 'final' (never directly assigned to after object construction).
     *          ACC_VOLATILE    0x0040          Declared 'volatile' (cannot be cached).
     *          ACC_TRANSIENT   0x0080          Declared 'transient' (not written or read by a persistent object manager).
     *          ACC_SYNTHETIC   0x1000          Declared synthetic (not present in the source code).
     *          ACC_ENUM        0x4000          Declared as an element of an 'enum'.
     * - name_index:
     *      The value of the 'name_index' item must be a valid index into the 'constant_pool' table. The 'constant_pool'
     *      entry at that index must be a 'CONSTANT_Utf8_info' structure which represents a valid unqualified name
     *      denoting a field.
     * - descriptor_index:
     *      The value of the 'descriptor_index' item must be a valid index into the 'constant_pool' table. The
     *      'constant_pool' entry at that index must be a 'CONSTANT_Utf8_info' structure which represents a valid
     *      field descriptor.
     * - attributes_count:
     *      The value of the 'attributes_count' item indicates the number of additional attributes of this field.
     * - attributes[]:
     *      Each value of the 'attributes' table must be an 'attribute_info' structure. A field can have any number
     *      of optional attributes associated with it.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param constantPool
     *  The constant pool associated with the resource.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractField(DataInputStream dataInputStream, ConstantPool constantPool) throws IOException {
        int accessFlags = dataInputStream.readUnsignedShort();
        int nameIndex = dataInputStream.readUnsignedShort();
        int descriptorIndex = dataInputStream.readUnsignedShort();
        if(LOGGER.isDebugEnabled()) {
            StringBuilder buffer = new StringBuilder();
            buffer.append("\n\tName of field (").append(nameIndex).append(") -> ");
            buffer.append(ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(constantPool, nameIndex));
            buffer.append("\n\tDescriptor of field (").append(descriptorIndex).append(") -> ");
            buffer.append(ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(constantPool, descriptorIndex));
            LOGGER.debug(buffer.toString());
        }
        // TODO: convert descriptor and add to referencedClasses list
        // TODO: create context object holding ConstantPool and Set of referenced classes
        int attributesCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("total attributes: " + attributesCount);
        AttributesParser.extractAttributes(dataInputStream, attributesCount, constantPool, AttributesParser.LOCATION_FIELD_INFO);
    }

}
