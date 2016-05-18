package net.technolords.tools.artificer.analyser.dotclass.bytecode.attribute;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.analyser.dotclass.bytecode.AttributesParser;
import net.technolords.tools.artificer.analyser.dotclass.specification.JavaSpecification;
import net.technolords.tools.artificer.analyser.dotclass.specification.Mnemonic;
import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Created by Technolords on 2016-Apr-07.
 *
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class CodeParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(CodeParser.class);
    private static final String MNEMONIC_NOT_FOUND = "Mnemonic not found!";

    /**
     * Auxiliary method to extract the code associated with the resource. This is fetched from the
     * 'Code_attribute' structure, which has the following format:
     *
     * [java 8]
     * Code_attribute {
     *      u2              attribute_name_index;
     *      u4              attribute_length;
     *      u2              max_stack;
     *      u2              max_locals;
     *      u4              code_length;
     *      u1              code[code_length];
     *      u2              exception_table_length;
     *      {
     *          u2          start_pc;
     *          u2          end_pc;
     *          u2          handler_pc;
     *          u2          catch_type;
     *      }               exception_table[exception_table_length];
     *      u2              attributes_count;
     *      attribute_info  attributes[attributes_count];
     * }
     *
     * - max_stack:
     *      The value of the 'max_stack' item gives the maximum depth of the operand stack of this method at any point
     *      during execution of the method.
     * - max_locals:
     *      The value of the 'max_locals' item gives the number of local variables in the local variable array
     *      allocated upon invocation of this method, including the local variables used to pass parameters to
     *      the method on its invocation. The greatest local variable index for a value of type long or double
     *      is max_locals - 2. The greatest local variable index for a value of any other type is max_locals - 1.
     * - code_length:
     *      The value of the 'code_length' item gives the number of bytes in the code array for this method.
     *      The value of 'code_length' must be greater than zero (as the code array must not be empty) and less
     *      than 65536.
     * - code[]:
     *      The code array gives the actual bytes of Java Virtual Machine code that implement the method.
     *      When the code array is read into memory on a byte-addressable machine, if the first byte of the array
     *      is aligned on a 4-byte boundary, the tableswitch and lookupswitch 32-bit offsets will be 4-byte aligned.
     *      (Refer to the descriptions of those instructions for more information on the consequences of
     *      code array alignment.) The detailed constraints on the contents of the code array are extensive and
     *      are given in a separate section.
     * - exception_table_length:
     *      The value of the 'exception_table_length' item gives the number of entries in the 'exception_table' table.
     * - exception_table[]:
     *      Each entry in the 'exception_table' array describes one exception handler in the code array. The order of
     *      the handlers in the 'exception_table' array is significant. Each 'exception_table' entry contains the
     *      following four items:
     *
     *      - start_pc, end_pc:
     *          The values of the two items 'start_pc' and 'end_pc' indicate the ranges in the code array at which
     *          the exception handler is active. The value of 'start_pc' must be a valid index into the code array
     *          of the opcode of an instruction. The value of 'end_pc' either must be a valid index into the
     *          code array of the opcode of an instruction or must be equal to 'code_length', the length of the
     *          code array. The value of 'start_pc' must be less than the value of 'end_pc'.
     *          The 'start_pc' is inclusive and 'end_pc' is exclusive; that is, the exception handler must be active
     *          while the program counter is within the interval [start_pc, end_pc).
     *          The fact that 'end_pc' is exclusive is a historical mistake in the design of the Java Virtual Machine:
     *          if the Java Virtual Machine code for a method is exactly 65535 bytes long and ends with an instruction
     *          that is 1 byte long, then that instruction cannot be protected by an exception handler.
     *          A compiler writer can work around this bug by limiting the maximum size of the generated Java Virtual
     *          Machine code for any method, instance initialization method, or static initializer (the size of any
     *          code array) to 65534 bytes.
     *      - handler_pc:
     *          The value of the 'handler_pc' item indicates the start of the exception handler. The value of the item
     *          must be a valid index into the code array and must be the index of the opcode of an instruction.
     *      - catch_type:
     *          If the value of the 'catch_type' item is nonzero, it must be a valid index into the 'constant_pool'
     *          table. The 'constant_pool' entry at that index must be a 'CONSTANT_Class_info' structure representing
     *          a class of exceptions that this exception handler is designated to catch. The exception handler
     *          will be called only if the thrown exception is an instance of the given class or one of its subclasses.
     *          The verifier checks that the class is Throwable or a subclass of Throwable. If the value of the
     *          'catch_type' item is zero, this exception handler is called for all exceptions. This is used to
     *          implement finally.
     *
     *  - attributes_count:
     *      The value of the 'attributes_count' item indicates the number of attributes of the Code attribute.
     *  - attributes[]:
     *      Each value of the attributes table must be an 'attribute_info' structure. A Code attribute can have
     *      any number of optional attributes associated with it.
     *
     * @param dataInputStream
     *  The byte stream associated with the resource (aka .class file).
     * @param javaSpecification
     *  The Java specification associated with the compiled version associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractCode(DataInputStream dataInputStream, JavaSpecification javaSpecification, Resource resource) throws IOException {
        int maxStack = dataInputStream.readUnsignedShort();
        int maxLocals = dataInputStream.readUnsignedShort();
        int codelength = dataInputStream.readInt();
        LOGGER.debug("MaxStack: " + maxStack + ", MaxLocals: " + maxLocals + ", CodeLength: " + codelength);

        // u1              code[code_length];
        for(int codeIndex = 0; codeIndex < codelength; codeIndex++) {
            int opcode = dataInputStream.readUnsignedByte();
            LOGGER.debug("Opcode (index: " + codeIndex + "): " + opcode + ", with mnemonic: " + extractMnemonic(opcode, javaSpecification.getMnemonics().getMnemonics()));
        }

        // Sample from main with hello world
        // ---------------------------------
        // Current format:
        //  Opcode (index: 0): 42, with mnemonic: aload_0
        //  Opcode (index: 1): 183, with mnemonic: invokespecial
        //  Opcode (index: 2): 0, with mnemonic: nop
        //  Opcode (index: 3): 1, with mnemonic: aconst_null
        //  Opcode (index: 4): 177, with mnemonic: return
        // Alternative format (from javap -v):
        //  0: aload_0
        //  1: invokespecial #1                  // Method java/lang/Object."<init>":()V
        //  4: return
        //
        // Current format:
        //  Opcode (index: 0): 178, with mnemonic: getstatic
        //  Opcode (index: 1): 0, with mnemonic: nop
        //  Opcode (index: 2): 2, with mnemonic: iconst_m1
        //  Opcode (index: 3): 18, with mnemonic: ldc
        //  Opcode (index: 4): 3, with mnemonic: iconst_0
        //  Opcode (index: 5): 182, with mnemonic: invokevirtual
        //  Opcode (index: 6): 0, with mnemonic: nop
        //  Opcode (index: 7): 4, with mnemonic: iconst_1
        //  Opcode (index: 8): 177, with mnemonic: return
        // Alternative format (from javap -v):
        //  0: getstatic     #2                  // Field java/lang/System.out:Ljava/io/PrintStream;
        //  3: ldc           #3                  // String Hello World!
        //  5: invokevirtual #4                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
        //  8: return

        int exceptionTableLength = dataInputStream.readUnsignedShort();
        LOGGER.debug("Exceptions table length: " + exceptionTableLength);
        for(int exceptionIndex = 0; exceptionIndex < exceptionTableLength; exceptionIndex++) {
            int startPc = dataInputStream.readUnsignedShort();
            int endPc = dataInputStream.readUnsignedShort();
            int handlerPc = dataInputStream.readUnsignedShort();
            int catchType = dataInputStream.readUnsignedShort();
        }
        int attributesCount = dataInputStream.readUnsignedShort();
        LOGGER.debug("Code has total attributes: " + attributesCount);
        AttributesParser.extractAttributes(dataInputStream, attributesCount, javaSpecification, resource, AttributesParser.LOCATION_CODE);
    }

    protected static String extractMnemonic(int opcode, List<Mnemonic> mnemonics) {
        for(Mnemonic mnemonic : mnemonics) {
            if(String.valueOf(opcode).equals(mnemonic.getOpcode())) {
                return mnemonic.getId();
            }
        }
        return MNEMONIC_NOT_FOUND;
    }
}
