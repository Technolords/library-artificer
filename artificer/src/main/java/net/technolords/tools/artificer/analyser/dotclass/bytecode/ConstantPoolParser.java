package net.technolords.tools.artificer.analyser.dotclass.bytecode;

import net.technolords.tools.artificer.analyser.dotclass.specification.ConstantPoolConstant;
import net.technolords.tools.artificer.analyser.dotclass.specification.ConstantPoolConstants;
import net.technolords.tools.artificer.analyser.dotclass.specification.ConstantPoolInfoFragment;
import net.technolords.tools.artificer.analyser.dotclass.specification.JavaSpecification;
import net.technolords.tools.artificer.domain.dotclass.Constant;
import net.technolords.tools.artificer.domain.dotclass.ConstantInfo;
import net.technolords.tools.artificer.domain.dotclass.ConstantPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

/**
 * Legend:
 * u1: java: readUnsignedByte
 * u2: java: readUnsignedShort
 * u4: java, readInt + readFloat
 * u8: java, readLong + readDouble
 */
public class ConstantPoolParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantPoolParser.class);

    /**
     * An auxiliary method to extract the constant pool from the byte stream. Based on the detected
     * JVM version, the associated specification is fetched to construct the constant pool. The
     * constant pool returned consists of a number of constants.
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
     * @param compiledVersion
     *  The compiled version associated with the resource (aka .class file).
     * @param lookupMap
     *  The lookup map associated with the constant pool extraction.
     * @return
     *  The extracted constant pool.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    public static ConstantPool extractConstantPool(DataInputStream dataInputStream, String compiledVersion, Map<String, JavaSpecification> lookupMap) throws IOException {
        int constantPoolSize = dataInputStream.readUnsignedShort();
        LOGGER.debug("constantPoolSize: " + constantPoolSize);
        ConstantPool constantPool = new ConstantPool();

        // Extract the constants
        for(int i = 1; i < constantPoolSize; i++) {
            Constant constant = extractConstant(dataInputStream, i, compiledVersion, lookupMap);
            constant.setConstantPoolIndex(i);
            if(constant.getType().equals("Long") || constant.getType().equals("Double")) {
                i++;
            }
            constantPool.getConstants().add(constant);
        }
        return constantPool;
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
     * @param compiledVersion
     *  The compiled version associated with the resource (aka .class file).
     * @param lookupMap
     *  The lookup map associated with the constant pool extraction.
     * @return
     *  The extracted constant.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected static Constant extractConstant(DataInputStream dataInputStream, int constantPoolIndex, String compiledVersion, Map<String, JavaSpecification> lookupMap) throws IOException {
        // Read tag
        int tag = dataInputStream.readUnsignedByte();
        // Find associated constant pool constant
        ConstantPoolConstant constantPoolConstant = findConstantPoolConstantByValue(tag, compiledVersion, lookupMap);
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
     * @param compiledVersion
     *  The compiled version, used to lookup for the correct JVM specification.
     * @param lookupMap
     *  The lookup map associated with the constant pool extraction.
     * @return
     *  The constant pool constant.
     */
    protected static ConstantPoolConstant findConstantPoolConstantByValue(int tag, String compiledVersion, Map<String, JavaSpecification> lookupMap) {
        Optional<ConstantPoolConstant> optionalConstantPoolConstant = Optional.ofNullable(null);

        JavaSpecification javaSpecification = lookupMap.get(compiledVersion);
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
        switch(infoFragment.getSize()) {
            case "readUnsignedByte" :
                constantInfo.setIntValue(dataInputStream.readUnsignedByte());
                LOGGER.debug("Info fragment size: " + infoFragment.getSize() + ", description: " + infoFragment.getDescription() + ", value: " + constantInfo.getIntValue());
                break;
            case "readInt" :
                constantInfo.setIntValue(dataInputStream.readInt());
                LOGGER.debug("Info fragment size: " + infoFragment.getSize() + ", description: " + infoFragment.getDescription() + ", value: " + constantInfo.getIntValue());
                break;
            case "readFloat" :
                constantInfo.setFloatValue(dataInputStream.readFloat());
                LOGGER.debug("Info fragment size: " + infoFragment.getSize() + ", description: " + infoFragment.getDescription() + ", value: " + constantInfo.getFloatValue());
                break;
            case "readLong" :
                constantInfo.setLongValue(dataInputStream.readLong());
                LOGGER.debug("Info fragment size: " + infoFragment.getSize() + ", description: " + infoFragment.getDescription() + ", value: " + constantInfo.getLongValue());
                break;
            case "readDouble" :
                constantInfo.setDoubleValue(dataInputStream.readDouble());
                LOGGER.debug("Info fragment size: " + infoFragment.getSize() + ", description: " + infoFragment.getDescription() + ", value: " + constantInfo.getDoubleValue());
                break;
            case "readUTF":
                constantInfo.setStringValue(dataInputStream.readUTF());
                LOGGER.debug("Info fragment size: " + infoFragment.getSize() + ", description: " + infoFragment.getDescription() + ", value: " + constantInfo.getStringValue());
                break;
            case "readUnsignedShort" :
            default:
                constantInfo.setIntValue(dataInputStream.readUnsignedShort());
                LOGGER.debug("Info fragment size: " + infoFragment.getSize() + ", description: " + infoFragment.getDescription() + ", value: " + constantInfo.getIntValue());
                break;
        }
    }
}
