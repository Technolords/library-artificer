package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import java.io.DataInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Created by Technolords on 2016-Apr-11.
 *
 * Java requires all classes that are loaded to be verified, in order to maintain the security of the sandbox and
 * ensure that the code is safe to optimize. Note that this is done on the bytecode level, so the verification does
 * not verify invariants of the Java language, it merely verifies that the bytecode makes sense according to the
 * rules for bytecode.
 *
 * Among other, things bytecode verification makes sure that instructions are well formed, that all the jumps are
 * to valid instructions within the method, and that all instructions operate on values of the correct type. The
 * last one is where the stack map comes in.
 *
 * The thing is that bytecode by itself contains no explicit type information. Types are determined implicitly
 * through dataflow analysis. For example, an iconst instruction creates an integer value. If you store it in slot 1,
 * that slot now has an int. If control flow merges from code which stores a float there instead, the slot is now
 * considered to have invalid type, meaning that you can't do anything more with that value until overwriting it.
 *
 * Historically, the bytecode verifier inferred all the types using these dataflow rules. Unfortunately, it is
 * impossible to infer all the types in a single linear pass through the bytecode because a backwards jump might
 * invalidate already inferred types. The classic verifier solved this by iterating through the code until
 * everything stopped changing, potentially requiring multiple passes.
 *
 * However, verification makes class loading slow in Java. Oracle decided to solve this issue by adding a new, faster
 * verifier, that can verify bytecode in a single pass. To do this, they required all new classes starting in Java 7
 * (with Java 6 in a transitional state) to carry metadata about their types, so that the bytecode can be verified in
 * a single pass. Since the bytecode format itself can't be changed, this type information is stored separately
 * in an attribute called StackMapTable.
 *
 * Simply storing the type for every single value at every single point in the code would obviously take up a lot of
 * space and be very wasteful. In order to make the metadata smaller and more efficient, they decided to have it only
 * list the types at positions which are targets of jumps. If you think about it, this is the only time you need the
 * extra information to do a single pass verification. In between jump targets, all control flow is linear, so you
 * can infer the types at in between positions using the old inference rules.
 *
 * Each position where types are explicitly listed is known as a stack map frame. The StackMapTable attribute contains
 * a list of frames in order, though they are usually expressed as a difference from the previous frame in order to
 * reduce data size. If there are no frames in the method, which occurs when control flow never joins
 * (i.e. the CFG is a tree), then the StackMapTable attribute can be omitted entirely.
 *
 * So this is the basic idea of how StackMapTable works and why it was added. The last question is how the implicit
 * initial frame is created. The answer of course is that at the beginning of the method, the operand stack is empty
 * and the local variable slots have the types given by the types of the method parameters, which are determined
 * from the method descriptor.
 *
 * If you're used to Java, there are a few minor differences to how method parameter types work at the bytecode level.
 * First off, virtual methods have an implicit this as first parameter. Second, boolean, byte, char, and short do not
 * exist at the bytecode level. Instead, they are all implemented as ints behind the scenes.
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
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractStackMapTable(DataInputStream dataInputStream, Resource resource) throws IOException {
        // Read number of entries
        int entriesCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("Total entries: " + entriesCount);
        for(int index = 0; index < entriesCount; index++) {
            extractStackMapFrame(dataInputStream, resource);
        }
    }

    /**
     * Auxiliary method to extract the code associated with the resource. This is fetched from the
     * 'StackMapTable_attribute' structure, which has the following format:
     *
     * [java 8]
     * union stack_map_frame {
     *      same_frame;
     *      same_locals_1_stack_item_frame;
     *      same_locals_1_stack_item_frame_extended;
     *      chop_frame;
     *      same_frame_extended;
     *      append_frame;
     *      full_frame;
     * }
     *
     * - same_frame:
     *      The frame type 'same_frame' is represented by tags in the range [0-63]. This frame type indicates that
     *      the frame has exactly the same local variables as the previous frame and that the operand stack is empty.
     *      The 'offset_delta' value for the frame is the value of the tag item, frame_type.
     *
     *      same_frame {
     *          u1                          frame_type = SAME; * 0-63 *
     *      }
     *
     * - same_locals_1_stack_item_frame:
     *      The frame type 'same_locals_1_stack_item_frame' is represented by tags in the range [64, 127]. This frame
     *      type indicates that the frame has exactly the same local variables as the previous frame and that the
     *      operand stack has one entry. The 'offset_delta' value for the frame is given by the formula
     *      frame_type - 64. The verification type of the one stack entry appears after the frame type.
     *
     *      same_locals_1_stack_item_frame {
     *          u1                          frame_type = SAME_LOCALS_1_STACK_ITEM; * 64-127 *
     *          verification_type_info      stack[1];
     *      }
     *
     * - same_locals_1_stack_item_frame_extended:
     *      The frame type 'same_locals_1_stack_item_frame_extended' is represented by the tag 247. This frame type
     *      indicates that the frame has exactly the same local variables as the previous frame and that the operand
     *      stack has one entry. The 'offset_delta' value for the frame is given explicitly, unlike in the frame type
     *      'same_locals_1_stack_item_frame'. The verification type of the one stack entry appears after 'offset_delta'.
     *
     *      same_locals_1_stack_item_frame_extended {
     *          u1                          frame_type = SAME_LOCALS_1_STACK_ITEM_EXTENDED; * 247 *
     *          u2                          offset_delta;
     *          verification_type_info      stack[1];
     *      }
     *
     * - chop_frame:
     *      The frame type 'chop_frame' is represented by tags in the range [248-250]. This frame type indicates that
     *      the frame has the same local variables as the previous frame except that the last k local variables are
     *      absent, and that the operand stack is empty. The value of k is given by the formula 251 - frame_type.
     *      The 'offset_delta' value for the frame is given explicitly.
     *
     *      chop_frame {
     *          u1                          frame_type = CHOP; * 248-250 *
     *          u2                          offset_delta;
     *      }
     *
     * - same_frame_extended:
     *      The frame type 'same_frame_extended' is represented by the tag 251. This frame type indicates that the
     *      frame has exactly the same local variables as the previous frame and that the operand stack is empty.
     *      The 'offset_delta' value for the frame is given explicitly, unlike in the frame type same_frame.
     *
     *      same_frame_extended {
     *          u1                          frame_type = SAME_FRAME_EXTENDED; * 251 *
     *          u2                          offset_delta;
     *      }
     *
     * - append_frame:
     *      The frame type 'append_frame' is represented by tags in the range [252-254]. This frame type indicates
     *      that the frame has the same locals as the previous frame except that k additional locals are defined, and
     *      that the operand stack is empty. The value of k is given by the formula frame_type - 251. The
     *      'offset_delta' value for the frame is given explicitly.
     *
     *      append_frame {
     *          u1                          frame_type = APPEND; * 252-254 *
     *          u2                          offset_delta;
     *          verification_type_info      locals[frame_type - 251];
     *      }
     *
     * - full_frame:
     *      The frame type full_frame is represented by the tag 255. The offset_delta value for the frame is given
     *      explicitly.
     *
     *      full_frame {
     *          u1                          frame_type = FULL_FRAME; * 255 *
     *          u2                          offset_delta;
     *          u2                          number_of_locals;
     *          verification_type_info      locals[number_of_locals];
     *          u2                          number_of_stack_items;
     *          verification_type_info      stack[number_of_stack_items];
     *      }
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractStackMapFrame(DataInputStream dataInputStream, Resource resource) throws IOException {
        int tag = dataInputStream.readUnsignedByte();
        int offsetDelta;
        // 0 - 63       frame type: same_frame
        // 64 - 127     frame type: same_locals_1_stack_item_frame
        if(tag >= 64 && tag <= 127) {
            extractVerificationType(dataInputStream, resource);
            return;
        }
        // 128 - 246    *** reserved for future ***
        // 247          frame type: same_locals_1_stack_item_frame_extended
        if(tag == 247) {
            offsetDelta = dataInputStream.readUnsignedShort();
            extractVerificationType(dataInputStream, resource);
            return;
        }
        // 248 - 250    frame_type: chop_frame
        if(tag >= 248 && tag <= 250) {
            offsetDelta = dataInputStream.readUnsignedShort();
            return;
        }
        // 251          frame_type: same_frame_extended
        if(tag == 251) {
            offsetDelta = dataInputStream.readUnsignedShort();
            return;
        }
        // 252 - 254    frame type: append_frame
        if(tag >= 252 && tag <= 254) {
            offsetDelta = dataInputStream.readUnsignedShort();
            int appendedLocals = tag - 251;
            for(int appendedLocalIndex = 0; appendedLocalIndex < appendedLocals; appendedLocalIndex++) {
                extractVerificationType(dataInputStream, resource);
            }
            return;
        }
        // 255          frame type: full_frame
        if(tag == 255) {
            offsetDelta = dataInputStream.readUnsignedShort();
            int numberOfLocals = dataInputStream.readUnsignedShort();
            for(int numberOfLocalIndex = 0; numberOfLocalIndex < numberOfLocals; numberOfLocalIndex++) {
                extractVerificationType(dataInputStream, resource);
            }
            int numberOfStackItems = dataInputStream.readUnsignedShort();
            for(int numberOfStackIndex = 0; numberOfStackIndex < numberOfStackItems; numberOfStackIndex++) {
                extractVerificationType(dataInputStream, resource);
            }
            return;
        }
    }

    /**
     * Auxiliary method to extract the verification type associated with the stack map frame. This is fetched from the
     * 'verification_type_info' structure, which has the following format:
     *
     * [java 8]
     * union verification_type_info {
     *      Top_variable_info;
     *      Integer_variable_info;
     *      Float_variable_info;
     *      Long_variable_info;
     *      Double_variable_info;
     *      Null_variable_info;
     *      UninitializedThis_variable_info;
     *      Object_variable_info;
     *      Uninitialized_variable_info;
     * }
     *
     * A verification type specifies the type of either one or two locations, where a location is either a single
     * local variable or a single operand stack entry. A verification type is represented by a discriminated union,
     * verification_type_info, that consists of a one-byte tag, indicating which item of the union is in use,
     * followed by zero or more bytes, giving more information about the tag.
     *
     * - Top_variable_info:
     *      The Top_variable_info item indicates that the local variable has the verification type top.
     *
     *      Top_variable_info {
     *          u1 tag = ITEM_Top; * 0 *
     *      }
     *
     * - Integer_variable_info:
     *      The Integer_variable_info item indicates that the location has the verification type int.
     *
     *      Integer_variable_info {
     *          u1 tag = ITEM_Integer; * 1 *
     *      }
     *
     * - Float_variable_info:
     *      The Float_variable_info item indicates that the location has the verification type float.
     *
     *      Float_variable_info {
     *          u1 tag = ITEM_Float; * 2 *
     *      }
     *
     * - Long_variable_info:
     *      The Long_variable_info item indicates that the first of two locations has the verification type long.
     *
     *      Long_variable_info {
     *          u1 tag = ITEM_Long; * 4 *
     *      }
     *
     * - Double_variable_info:
     *      The Double_variable_info item indicates that the first of two locations has the verification type double.
     *
     *      Double_variable_info {
     *          u1 tag = ITEM_Double; * 3 *
     *      }
     *
     * - Null_variable_info:
     *      The Null_variable_info type indicates that the location has the verification type null.
     *
     *      Null_variable_info {
     *          u1 tag = ITEM_Null; * 5 *
     *      }
     *
     * - UninitializedThis_variable_info:
     *      The UninitializedThis_variable_info item indicates that the location has the verification type
     *      uninitializedThis.
     *
     *      UninitializedThis_variable_info {
     *          u1 tag = ITEM_UninitializedThis; * 6 *
     *      }
     *
     * - Object_variable_info:
     *      The Object_variable_info item indicates that the location has the verification type which is the class
     *      represented by the CONSTANT_Class_info structure found in the constant_pool table at the index given by
     *      cpool_index.
     *
     *      Object_variable_info {
     *          u1 tag = ITEM_Object; * 7 *
     *          u2 cpool_index;
     *      }
     *
     * - Uninitialized_variable_info:
     *      The Uninitialized_variable_info item indicates that the location has the verification type
     *      uninitialized(Offset). The Offset item indicates the offset, in the code array of the Code attribute that
     *      contains this StackMapTable attribute, of the new instruction (§new) that created the object being stored
     *      in the location.
     *
     *      Uninitialized_variable_info {
     *          u1 tag = ITEM_Uninitialized; * 8 *
     *          u2 offset;
     *      }
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractVerificationType(DataInputStream dataInputStream, Resource resource) throws IOException {
        int tag = dataInputStream.readUnsignedByte();
        switch (tag) {
            case 0: // ITEM_Top
                break;
            case 1: // ITEM_Integer
                break;
            case 2: // ITEM_Float
                break;
            case 3: // ITEM_Double
                break;
            case 4: // ITEM_Long
                break;
            case 5: // ITEM_Null
                break;
            case 6: // ITEM_UninitializedThis
                break;
            case 7: // ITEM_Object
                int classPoolIndex = dataInputStream.readUnsignedShort();
                break;
            case 8: // ITEM_Uninitialized
                int offset = dataInputStream.readUnsignedShort();
                break;
            default:
        }
    }
}
