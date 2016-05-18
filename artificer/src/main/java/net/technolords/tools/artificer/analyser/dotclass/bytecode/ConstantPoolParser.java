package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.analyser.dotclass.specification.ConstantPoolConstant;
import net.technolords.tools.artificer.analyser.dotclass.specification.ConstantPoolConstants;
import net.technolords.tools.artificer.analyser.dotclass.specification.ConstantPoolInfoFragment;
import net.technolords.tools.artificer.analyser.dotclass.specification.JavaSpecification;
import net.technolords.tools.artificer.domain.dotclass.Constant;
import net.technolords.tools.artificer.domain.dotclass.ConstantInfo;
import net.technolords.tools.artificer.domain.dotclass.ConstantPool;
import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class ConstantPoolParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantPoolParser.class);
    private static final String READ_UNSIGNED_BYTE = "readUnsignedByte";
    private static final String READ_INT = "readInt";
    private static final String READ_FLOAT = "readFloat";
    private static final String READ_LONG = "readLong";
    private static final String READ_DOUBLE = "readDouble";
    private static final String READ_UTF = "readUTF";
    private static final String READ_UNSIGNED_SHORT = "readUnsignedShort";

    /**
     * An auxiliary method to extract the constant pool from the byte stream. Based on the detected JVM version,
     * the associated specification is fetched to construct the constant pool. The constant pool consists of
     * a number of constants.
     *
     * Auxiliary method to extract the minor and major version associated to the resource. From the 'ClassFile'
     * structure, which has the following format:
     *
     * [java 8]
     * ClassFile {
     *     ...
     *     u2                   constant_pool_count;
     *     cp_info              constant_pool[constant_pool_count-1];
     *     ...
     * }
     *
     * - constant_pool_count:
     *      The value of the 'constant_pool_count' item is equal to the number of entries in the 'constant_pool'
     *      table plus one. A 'constant_pool' index is considered valid if it is greater than zero and less than
     *      'constant_pool_count', with the exception for constants of type long and double.
     * - constant_pool[]:
     *      The 'constant_pool' is a table of structures representing various string constants, class and
     *      interface names, field names, and other constants that are referred to within the ClassFile structure
     *      and its substructures. The format of each 'constant_pool' table entry is indicated by its first "tag" byte.
     *      The constant_pool table is indexed from 1 to constant_pool_count - 1.
     *
     * All constant pool entries have the following general format:
     *
     * cp_info {
     *     u1               tag
     *     u1               info[]
     * }
     *
     * @param dataInputStream
     *  The byte stream associated with the constant pool extraction.
     * @param javaSpecification
     *  The Java specification associated with the compiled version associated with the resource (aka .class file).
     * @param resource
     *  The resource associated with the attribute.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static void extractConstantPool(DataInputStream dataInputStream, JavaSpecification javaSpecification, Resource resource) throws IOException {
        int constantPoolSize = dataInputStream.readUnsignedShort();
        LOGGER.debug("ConstantPool count: " + constantPoolSize);
        ConstantPool constantPool = new ConstantPool();

        // Extract the constants
        for(int i = 1; i < constantPoolSize; i++) {
            Constant constant = extractConstant(dataInputStream, i, javaSpecification);
            constant.setConstantPoolIndex(i);
            if(constant.getType().equals("Long") || constant.getType().equals("Double")) {
                i++;
            }
            constantPool.getConstants().add(constant);
        }
        resource.setConstantPool(constantPool);
    }

    /**
     * An auxiliary method to extract the constant from the byte stream. The constant describes the type and contains
     * more information, such as a referenced class or constant value.
     *
     * According to the JVM specification, each item in the constant_pool must begin with a 1-byte tag indicating
     * the kind of cp_info entry. The contents of the info array vary with the value of tag. The valid tags and
     * their values are (example is fetched from JVM 8 specification):
     *
     * - Class              7
     * - FieldRef           9
     * - MethodRef          10
     * - InterfaceMethodred 11
     * - String             8
     * - Integer            3
     * - Float              4
     * - Long               5
     * - Double             6
     * - NameAndType        12
     * - Utf8               1
     * - MethodHandle       15
     * - MethodType         16
     * - InvokeDynamic      18
     *
     * Each tag byte must be followed by two or more bytes giving information about the specific constant. The format
     * of the additional information varies with the tag value.
     *
     * @param dataInputStream
     *  The byte stream associated with the constant extraction.
     * @param constantPoolIndex
     *  The passed constant pool index, for logging and tracking purposes.
     * @param javaSpecification
     *  The Java specification associated with the compiled version associated with the resource (aka .class file).
     * @return
     *  The extracted constant.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static Constant extractConstant(DataInputStream dataInputStream, int constantPoolIndex, JavaSpecification javaSpecification) throws IOException {
        // Read tag
        int tag = dataInputStream.readUnsignedByte();
        // Find associated constant pool constant
        ConstantPoolConstant constantPoolConstant = findConstantPoolConstantByValue(tag, javaSpecification);
        // Instantiate constant
        Constant constant = new Constant();
        constant.setConstantPoolIndex(constantPoolIndex);
        constant.setTag(tag);
        constant.setType(constantPoolConstant.getType());
        LOGGER.debug("Constant index: " + constant.getConstantPoolIndex() + ", tag: " + constant.getTag() + ", type: " + constant.getType());
        // Extract details
        extractConstantDetails(dataInputStream, constant, constantPoolConstant);
        return constant;
    }

    /**
     * Auxiliary method to find an instance of the ConstantPoolConstant based on two parameters,
     * namely the tag and the compiled version.
     *
     * @param tag
     *  The tag associated with the constant pool constant.
     * @param javaSpecification
     *  The Java specification associated with the compiled version associated with the resource (aka .class file).
     * @return
     *  The constant pool constant.
     */
    protected static ConstantPoolConstant findConstantPoolConstantByValue(int tag, JavaSpecification javaSpecification) {
        Optional<ConstantPoolConstant> optionalConstantPoolConstant = Optional.ofNullable(null);

        if(javaSpecification != null) {
            ConstantPoolConstants constantPoolConstants = javaSpecification.getConstantPoolConstants();
            if(constantPoolConstants != null) {
                optionalConstantPoolConstant = constantPoolConstants.getConstantPoolConstants().
                    stream().
                    filter( cp -> Integer.valueOf(cp.getTag()) == tag ).
                    findFirst();
            }
        }
        return optionalConstantPoolConstant.get();
    }

    /**
     * An auxiliary method to extract the constant details from the byte stream.
     *
     * @param dataInputStream
     *  The byte stream associated with the constant extraction.
     * @param constant
     *  The constant associated with the details extracted.
     * @param constantPoolConstant
     *  The structure of the constant pool constant.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void extractConstantDetails(DataInputStream dataInputStream, Constant constant, ConstantPoolConstant constantPoolConstant) throws IOException {
        for(ConstantPoolInfoFragment infoFragment : constantPoolConstant.getFragments()) {
            ConstantInfo constantInfo = new ConstantInfo();
            constantInfo.setDescription(infoFragment.getDescription());
            readInfoSize(dataInputStream, constantInfo, infoFragment);
            constant.getConstantInfoList().add(constantInfo);
        }
    }

    /**
     * Auxiliary method to read a number of bytes, based on the constant pool info data (represented by the
     * ConstantPoolInfoFragment). This instance of reference is based on the specification, and tells how much
     * byes (as in value) must be read. The data being read is set on the ConstantInfo object for further processing.
     *
     * @param dataInputStream
     *  The byte stream associated with the extraction (reading of bytes).
     * @param constantInfo
     *  The constant info associated with the resource.
     * @param infoFragment
     *  The constant pool info fragment associated with the constant pool constant.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static void readInfoSize(DataInputStream dataInputStream, ConstantInfo constantInfo, ConstantPoolInfoFragment infoFragment) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append("Info fragment size: ").append(infoFragment.getSize());
        buffer.append(", description: ").append(infoFragment.getDescription());
        buffer.append(", value: ");
        switch(infoFragment.getSize()) {
            case READ_UNSIGNED_BYTE:
                constantInfo.setIntValue(dataInputStream.readUnsignedByte());
                buffer.append(constantInfo.getIntValue());
                LOGGER.debug(buffer.toString());
                break;
            case READ_INT:
                constantInfo.setIntValue(dataInputStream.readInt());
                buffer.append(constantInfo.getIntValue());
                LOGGER.debug(buffer.toString());
                break;
            case READ_FLOAT:
                constantInfo.setFloatValue(dataInputStream.readFloat());
                buffer.append(constantInfo.getFloatValue());
                LOGGER.debug(buffer.toString());
                break;
            case READ_LONG:
                constantInfo.setLongValue(dataInputStream.readLong());
                buffer.append(constantInfo.getLongValue());
                LOGGER.debug(buffer.toString());
                break;
            case READ_DOUBLE:
                constantInfo.setDoubleValue(dataInputStream.readDouble());
                buffer.append(constantInfo.getDoubleValue());
                LOGGER.debug(buffer.toString());
                break;
            case READ_UTF:
                constantInfo.setStringValue(dataInputStream.readUTF());
                buffer.append(constantInfo.getStringValue());
                LOGGER.debug(buffer.toString());
                break;
            case READ_UNSIGNED_SHORT:
            default:
                constantInfo.setIntValue(dataInputStream.readUnsignedShort());
                buffer.append(constantInfo.getIntValue());
                LOGGER.debug(buffer.toString());
                break;
        }
    }
}
