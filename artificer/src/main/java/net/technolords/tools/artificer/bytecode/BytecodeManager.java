package net.technolords.tools.artificer.bytecode;

import net.technolords.tools.artificer.bytecode.specification.ConstantPoolConstant;
import net.technolords.tools.artificer.bytecode.specification.ConstantPoolConstants;
import net.technolords.tools.artificer.bytecode.specification.JavaSpecification;
import net.technolords.tools.artificer.bytecode.specification.JavaSpecifications;
import net.technolords.tools.artificer.domain.bytecode.Constant;
import net.technolords.tools.artificer.domain.bytecode.ConstantInfo;
import net.technolords.tools.artificer.domain.bytecode.ConstantPool;
import net.technolords.tools.artificer.bytecode.specification.ConstantPoolInfoFragment;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Technolords on 2015-Nov-25.
 */
public class BytecodeManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(BytecodeManager.class);
    private String referenceFile;
    private Map<String, JavaSpecification> lookupMap;

    public BytecodeManager(String referenceFile) {
        this.referenceFile = referenceFile;
    }

    /**
     * Auxiliary method to initialize the lookup map. It uses the default classloader to obtain
     * an inputstream as reference for the XML file and then JAXB will use this to unmarshall this
     * to an instance of the JavaSpecifications class.
     *
     * @throws ArtificerException
     *  When unmarshalling the XML file fails.
     */
    public void initializeSpecifications() throws ArtificerException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JavaSpecifications.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(this.referenceFile);
            JavaSpecifications javaSpecifications = (JavaSpecifications) unmarshaller.unmarshal(inputStream);
            for(JavaSpecification javaSpecification : javaSpecifications.getJavaSpecifications()) {
                this.lookupMap.put(javaSpecification.getAlias(), javaSpecification);
            }
            LOGGER.debug("Total java specifications initialized: " + this.lookupMap.size());
        } catch (JAXBException e) {
            throw new ArtificerException(e);
        }
    }

    /**
     * An auxiliary method to analyse the byte code of the class to determine the makeup. This is done by
     * reading bytes from the DataInputStream representing the resource. According to the JVM specification the
     * class file has the following structure (example is fetched from JVM 8 specification):
     *
     * ClassFile {
     *     u4               magic_number
     *     u2               minor_version
     *     u2               major_version
     *     u2               constant_pool_count
     *     cp_info          constant_pool[constant_pool_count -1]
     *     u2               access_flags
     *     u2               this_class
     *     u2               super_class
     *     u2               interfaces_count
     *     u2               interfaces[interfaces_count]
     *     u2               fields_count
     *     field_info       fields[fields_count]
     *     u2               methods_count
     *     method_info      methods[methods_count]
     *     u2               attributes_count
     *     attributes_info  attributes[attributes_count]
     * }
     *
     * Legend:
     * - u1: unsigned one byte quantity, to be read as: readUnsignedByte
     * - u2: unsigned two byte quantity, to be read as: readUnsignedShort
     * - u4: unsigned four byte quantity, to be read as: readInt
     * - u8: unsigned eight byte quantity, to be read as: readLong
     *
     * The constant pool is of particular interest, as this contains references of classes.
     *
     * @param resource
     *  The resource associated with the determination of the referenced classes.
     */
    public void analyseBytecode(Resource resource) {
        try {
            if(this.lookupMap == null) {
                this.lookupMap = new HashMap<>();
                this.initializeSpecifications();
            }
            LOGGER.debug("About to analyse byte code of: + " + resource.getName() + ", for JVM spec: " + resource.getCompiledVersion());
            DataInputStream dataInputStream = new DataInputStream(Files.newInputStream(resource.getPath()));
            // Absorb magic number, minor and major version
            this.absorbOverhead(dataInputStream);
            // Extract the constant pool
            ConstantPool constantPool = this.extractConstantPool(dataInputStream, resource.getCompiledVersion());
        } catch (IOException e) {
            LOGGER.error("Unable to parse the class: " + resource.getName(), e);
        } catch (ArtificerException e) {
            LOGGER.error("Unable to initialize lookup map" + e.getMessage(), e);
        }
    }

    protected void absorbOverhead(DataInputStream dataInputStream) throws IOException {
        // Absorb magic number (as it is already known)
        dataInputStream.readInt();
        // Absorb minor and major number
        dataInputStream.readInt();
    }

    /**
     * An auxiliary method to extract the constant pool from the byte stream. Note that the value of
     * the constant_pool_count is equal to the number of entries in the constant_pool table plus one.
     *
     * A constant_pool index is considered valid if it is greater than zero and less than the
     * constant_pool_count, with the exceptions for constants of type long and double.
     *
     * The constant_pool table is indexed from 1 to constant_pool_count - 1
     *
     * All constant pool entries have the following general format:
     *
     * cp_info {
     *     u1               tag
     *     u1               info[]
     * }
     *
     * Legend:
     * - u1: unsigned one byte quantity, to be read as: readUnsignedByte
     * - u2: unsigned two byte quantity, to be read as: readUnsignedShort
     * - u4: unsigned four byte quantity, to be read as: readInt
     * - u8: unsigned eight byte quantity, to be read as: readLong
     *
     * @param dataInputStream
     *  The byte stream associated with the extraction.
     * @param compiledVersion
     *  The compiled version associated with the byte stream (constant pool).
     * @return
     *  The extracted constant pool.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected ConstantPool extractConstantPool(DataInputStream dataInputStream, String compiledVersion) throws IOException {
        int constantPoolSize = dataInputStream.readUnsignedShort();
        LOGGER.info("constantPoolSize: " + constantPoolSize);
        ConstantPool constantPool = new ConstantPool();

        // Extract the constants
        for(int i = 1; i < constantPoolSize; i++) {
            Constant constant = this.extractConstant(dataInputStream, i, compiledVersion);
            constant.setConstantPoolIndex(i);
            if(constant.getType().equals("Long") || constant.getType().equals("Double")) {
                i++;
            }
            constantPool.getConstants().add(constant);
        }
        return constantPool;
    }

    /**
     * Each item in the constant_pool must begin with a 1-byte tag indicating the kind of cp_info entry. The contents
     * of the info array vary with the value of tag. The valid tags and their values are (example is fetched from JVM 8
     * specification):
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
     * @return
     */
    protected Constant extractConstant(DataInputStream dataInputStream, int constantPoolIndex, String compiledVersion) throws IOException {
        // Read tag
        int tag = dataInputStream.readUnsignedByte();
        // Find associated constant pool constant
        ConstantPoolConstant constantPoolConstant = this.findConstantPoolConstantByValue(tag, compiledVersion);
        // Instantiate constant
        Constant constant = new Constant();
        constant.setConstantPoolIndex(constantPoolIndex);
        constant.setTag(tag);
        constant.setType(constantPoolConstant.getType());
        LOGGER.info("Constant index: " + constant.getConstantPoolIndex() + ", tag: " + constant.getTag() + ", type: " + constant.getType());
        // Extract details
        this.extractConstantDetails(dataInputStream, constant, constantPoolConstant);
        return constant;
    }

    protected void extractConstantDetails(DataInputStream dataInputStream, Constant constant, ConstantPoolConstant constantPoolConstant) throws IOException {
        for(ConstantPoolInfoFragment infoFragment : constantPoolConstant.getFragments()) {
            ConstantInfo constantInfo = new ConstantInfo();
            constantInfo.setDescription(infoFragment.getDescription());
            this.readInfoSize(dataInputStream, constantInfo, infoFragment);
            constant.getConstantInfoList().add(constantInfo);
        }
    }

    protected void readInfoSize(DataInputStream dataInputStream, ConstantInfo constantInfo, ConstantPoolInfoFragment infoFragment) throws IOException {
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
                constantInfo.setDoubeValue(dataInputStream.readDouble());
                LOGGER.debug("Info fragment size: " + infoFragment.getSize() + ", description: " + infoFragment.getDescription() + ", value: " + constantInfo.getDoubeValue());
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

    // 2015-12-04 00:40:59,025 [INFO] [main] [net.technolords.tools.artificer.bytecode.BytecodeManager] constantPoolSize: 23

    protected ConstantPoolConstant findConstantPoolConstantByValue(int tag, String compiledVersion) {
        // TODO: rewrite this in lambda format?
        JavaSpecification javaSpecification = this.lookupMap.get(compiledVersion);
        if(javaSpecification != null) {
            ConstantPoolConstants constantPoolConstants = javaSpecification.getConstantPoolConstants();
            if(constantPoolConstants != null) {
                for(ConstantPoolConstant constantPoolConstant : constantPoolConstants.getConstantPoolConstants()) {
                    if(Integer.valueOf(constantPoolConstant.getTag()) == tag) {
                        return constantPoolConstant;
                    }
                }
            }
        }
        return null;
    }
}
