package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.SignatureAnalyser;
import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Created by Technolords on 2016-Apr-11.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class LocalVariableTableParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocalVariableTableParser.class);

    private enum TableVariant {LocalVariableTable, LocalVariableTypeTable}

    /**
     * Auxiliary method to extract the code associated with the resource. This is fetched from the
     * 'LocalVariableTable_attribute' structure, which has the following format:
     *
     * [java 8]
     * LocalVariableTable_attribute {
     *      u2              attribute_name_index;
     *      u4              attribute_length;
     *      u2              local_variable_table_length;
     *      {
     *          u2          start_pc;
     *          u2          length;
     *          u2          name_index;
     *          u2          descriptor_index;
     *          u2          index;
     *      }               local_variable_table[local_variable_table_length];
     * }
     *
     * - local_variable_table_length:
     *      The value of the 'local_variable_table_length' item indicates the number of entries in the
     *      'local_variable_table' array.
     * - local_variable_table[]:
     *      Each entry in the 'local_variable_table array' indicates a range of code array offsets within which a
     *      local variable has a value. It also indicates the index into the local variable array of the current
     *      frame at which that local variable can be found. Each entry must contain the following five items:
     *
     *      - start_pc, length:
     *          The given local variable must have a value at indices into the code array in the interval ['start_pc',
     *          'start_pc' + 'length'), that is, between 'start_pc' inclusive and 'start_pc' + 'length' exclusive.
     *          The value of 'start_pc' must be a valid index into the code array of this Code attribute and must be
     *          the index of the opcode of an instruction.
     *          The value of 'start_pc' + 'length' must either be a valid index into the code array of this Code
     *          attribute and be the index of the opcode of an instruction, or it must be the first index beyond the
     *          end of that code array.
     *      - name_index:
     *          The value of the 'name_index' item must be a valid index into the 'constant_pool' table. The
     *          'constant_pool' entry at that index must contain a 'CONSTANT_Utf8_info' structure representing a valid
     *          unqualified name denoting a local variable.
     *      - descriptor_index:
     *          The value of the 'descriptor_index' item must be a valid index into the 'constant_pool' table. The
     *          'constant_pool' entry at that index must contain a 'CONSTANT_Utf8_info' structure representing a field
     *          descriptor which encodes the type of a local variable in the source program.
     *      - index:
     *          The given local variable must be at index in the local variable array of the current frame.
     *          If the local variable at index is of type double or long, it occupies both index and index + 1.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractLocalVariableTable(DataInputStream dataInputStream, Resource resource) throws IOException {
        extractTable(dataInputStream, TableVariant.LocalVariableTable, resource);
    }

    /**
     * Auxiliary method to extract the code associated with the resource. This is fetched from the
     * 'LocalVariableTypeTable_attribute' structure, which has the following format:
     *
     * [java 8]
     * LocalVariableTypeTable_attribute {
     *      u2              attribute_name_index;
     *      u4              attribute_length;
     *      u2              local_variable_table_length;
     *      {
     *          u2          start_pc;
     *          u2          length;
     *          u2          name_index;
     *          u2          descriptor_index;
     *          u2          index;
     *      }               local_variable_table[local_variable_table_length];
     * }
     *
     * - local_variable_table_length:
     *      The value of the 'local_variable_table_length' item indicates the number of entries in the
     *      'local_variable_table' array.
     * - local_variable_table[]:
     *      Each entry in the 'local_variable_table array' indicates a range of code array offsets within which a
     *      local variable has a value. It also indicates the index into the local variable array of the current
     *      frame at which that local variable can be found. Each entry must contain the following five items:
     *
     *      - start_pc, length:
     *          The given local variable must have a value at indices into the code array in the interval ['start_pc',
     *          'start_pc' + 'length'), that is, between 'start_pc' inclusive and 'start_pc' + 'length' exclusive.
     *          The value of 'start_pc' must be a valid index into the code array of this Code attribute and must be
     *          the index of the opcode of an instruction.
     *          The value of 'start_pc' + 'length' must either be a valid index into the code array of this Code
     *          attribute and be the index of the opcode of an instruction, or it must be the first index beyond the
     *          end of that code array.
     *      - name_index:
     *          The value of the 'name_index' item must be a valid index into the 'constant_pool' table. The
     *          'constant_pool' entry at that index must contain a 'CONSTANT_Utf8_info' structure representing a valid
     *          unqualified name denoting a local variable.
     *      - descriptor_index:
     *          The value of the 'descriptor_index' item must be a valid index into the 'constant_pool' table. The
     *          'constant_pool' entry at that index must contain a 'CONSTANT_Utf8_info' structure representing a field
     *          descriptor which encodes the type of a local variable in the source program.
     *      - index:
     *          The given local variable must be at index in the local variable array of the current frame.
     *          If the local variable at index is of type double or long, it occupies both index and index + 1.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractLocalVariableTypeTable(DataInputStream dataInputStream, Resource resource) throws IOException {
        extractTable(dataInputStream, TableVariant.LocalVariableTypeTable, resource);
    }

    protected static void extractTable(DataInputStream dataInputStream, TableVariant variant, Resource resource) throws IOException {
        int localVariableTableLength = dataInputStream.readUnsignedShort();
        switch (variant) {
            case LocalVariableTable:
                LOGGER.debug("Local variable table length: " + localVariableTableLength);
                break;
            default:
                LOGGER.debug("Local variable type table length: " + localVariableTableLength);
        }
        for(int variableTableIndex = 0; variableTableIndex < localVariableTableLength; variableTableIndex++) {
            int startPc = dataInputStream.readUnsignedShort();
            int length = dataInputStream.readUnsignedShort();
            int nameIndex = dataInputStream.readUnsignedShort();
            int descriptorIndex = dataInputStream.readUnsignedShort();
            int index = dataInputStream.readUnsignedShort();
            LOGGER.debug("Name index: " + nameIndex + ", descriptorIndex: " + descriptorIndex + ", index: " + index);
            String signature = ConstantPoolAnalyser.extractStringValueByConstantPoolIndex(resource.getConstantPool(), descriptorIndex);

            // Add signature (when applicable) to the referenced classes
            SignatureAnalyser.referencedClasses(resource.getReferencedClasses(), signature);
        }

        // Sample from main with hello world
        // ---------------------------------
        // Current format:
        //  Name index: 12, descriptorIndex: 13, index:
        // Alternative format (from javap -v):
        //  Start   Length  Slot    Name    Signature
        //      0        5     0    this    Lnet/technolords/tools/data/method/MethodTestWithMainMethod;
        //
        // Current format:
        //  Name index: 16, descriptorIndex: 17, index: 0
        // Alternative format (from javap -v):
        //  Start   Length  Slot    Name    Signature
        //      0        9     0    args    [Ljava/lang/String;
    }
}