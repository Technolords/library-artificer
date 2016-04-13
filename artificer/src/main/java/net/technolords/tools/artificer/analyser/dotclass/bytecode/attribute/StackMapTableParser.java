package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by Technolords on 2016-Apr-11.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class StackMapTableParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(StackMapTableParser.class);

    /**
     * Auxiliary method to extract the code associated with the resource. This is fetched from the
     * 'StackMapTable_attribute' structure, which has the following format:
     *
     * [java 8]
     * StackMapTable_attribute {
     *      u2                  attribute_name_index;
     *      u4                  attribute_length;
     *      u2                  number_of_entries;
     *      stack_map_frame     entries[number_of_entries];
     * }
     *
     * - number_of_entries:
     *      The value of the 'number_of_entries' item gives the number of 'stack_map_frame' entries in the entries table.
     * - entries[]:
     *      Each entry in the entries table describes one stack map frame of the method. The order of the stack map
     *      frames in the entries table is significant. A stack map frame specifies (either explicitly or implicitly)
     *      the bytecode offset at which it applies, and the verification types of local variables and operand stack
     *      entries for that offset. Each stack map frame described in the entries table relies on the previous frame
     *      for some of its semantics. The first stack map frame of a method is implicit, and computed from the method
     *      descriptor by the type checker. The 'stack_map_frame' structure at entries[0] therefore describes the
     *      second stack map frame of the method.
     *
     *      The bytecode offset at which a stack map frame applies is calculated by taking the value offset_delta
     *      specified in the frame (either explicitly or implicitly), and adding offset_delta + 1 to the bytecode
     *      offset of the previous frame, unless the previous frame is the initial frame of the method. In that case,
     *      the bytecode offset at which the stack map frame applies is the value offset_delta specified in the frame.
     *      By using an offset delta rather than storing the actual bytecode offset, we ensure, by definition, that
     *      stack map frames are in the correctly sorted order. Furthermore, by consistently using the formula
     *      offset_delta + 1 for all explicit frames (as opposed to the implicit first frame), we guarantee the
     *      absence of duplicates.
     *
     *      We say that an instruction in the bytecode has a corresponding stack map frame if the instruction starts
     *      at offset i in the code array of a Code attribute, and the Code attribute has a StackMapTable attribute
     *      whose entries array contains a stack map frame that applies at bytecode offset i.
     *
     *      A verification type specifies the type of either one or two locations, where a location is either a single
     *      local variable or a single operand stack entry. A verification type is represented by a discriminated union,
     *      verification_type_info, that consists of a one-byte tag, indicating which item of the union is in use,
     *      followed by zero or more bytes, giving more information about the tag.
     *
     *      union verification_type_info {
     *          Top_variable_info;
     *          Integer_variable_info;
     *          Float_variable_info;
     *          Long_variable_info;
     *          Double_variable_info;
     *          Null_variable_info;
     *          UninitializedThis_variable_info;
     *          Object_variable_info;
     *          Uninitialized_variable_info;
     *      }
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractStackMapTable(DataInputStream dataInputStream, Resource resource) throws IOException {

    }
}
