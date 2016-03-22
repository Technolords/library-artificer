package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.SignatureAnalyser;
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
     * @param resource
     *  The resource associated woth the fields.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractFields(DataInputStream dataInputStream, Resource resource) throws IOException {
        // Read the number of fields
        int fieldsCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("Total fields: " + fieldsCount);
        if(fieldsCount != 0) {
            for(int index = 0; index < fieldsCount; index++) {
                extractField(dataInputStream, index, resource);
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
     * This method will parse the field data. In particular the descriptor is of interest as that can hold a class
     * reference. If so, this will be added to the referenced classes associated with the resource. In addition,
     * the attributes will be parsed for further inspection.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the field.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractField(DataInputStream dataInputStream, int index, Resource resource) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Field (index: ").append(index).append(")");

        // Read the access flags
        AccessFlagsParser.extractAccessFlags(dataInputStream, AccessFlagsParser.LOCATION_FIELD_INFO);

        // Read the name index
        int nameIndex = dataInputStream.readUnsignedShort();
        buffer.append(", with name (index: ").append(nameIndex).append("): ");
        buffer.append(ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), nameIndex));

        // Read the descriptor index
        int descriptorIndex = dataInputStream.readUnsignedShort();
        String descriptor = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), descriptorIndex);
        buffer.append(", with descriptor (index: ").append(descriptorIndex).append("): ").append(descriptor);

        // Read the number of attributes and delegate this information to the attribute parser
        int attributesCount = dataInputStream.readUnsignedShort();
        buffer.append(" and total attributes: ").append(attributesCount);
        LOGGER.debug(buffer.toString());

        // Add signature (when applicable) to the referenced classes
        SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), descriptor);

        // Read the attributes
        AttributesParser.extractAttributes(dataInputStream, attributesCount, resource, AttributesParser.LOCATION_FIELD_INFO);
    }

}
