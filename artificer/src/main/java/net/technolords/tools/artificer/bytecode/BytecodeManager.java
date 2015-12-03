package net.technolords.tools.artificer.bytecode;

import net.technolords.tools.artificer.bytecode.specification.ConstantPoolConstant;
import net.technolords.tools.artificer.bytecode.specification.JavaSpecification;
import net.technolords.tools.artificer.bytecode.specification.JavaSpecifications;
import net.technolords.tools.artificer.domain.bytecode.ConstantPool;
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
            LOGGER.debug("About to analyse byte code of: + " + resource.getName() + " identified for JVM spec: " + resource.getCompiledVersion());
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
     *
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
     * @param dataInputStream
     *  The byte stream associated with the extraction.
     * @return
     *  The extracted constant pool.
     * @throws IOException
     *  When reading bytes from the stream fails.
     */
    protected ConstantPool extractConstantPool(DataInputStream dataInputStream, String compiledVersion) throws IOException {
        int constantPoolSize = dataInputStream.readUnsignedShort();
        LOGGER.info("constantPoolSize: " + constantPoolSize);
        ConstantPool constantPool = new ConstantPool();
        // First tag
        int tag = dataInputStream.readUnsignedByte();
        LOGGER.info("First tag: " + tag);
        return constantPool;
    }

    /**
     * Resolve three class pools:
     * - self contained
     * - packaged by SE
     *  For java 8 source, scan zip file: /usr/lib/jvm/java-8-oracle/src.zip
     * - externak
     */

    protected ConstantPoolConstant findConstantPoolConstantByValue(int value, String compiledVersion) {
        ConstantPoolConstant constantPoolConstant = null;
        JavaSpecification javaSpecification = this.lookupMap.get(compiledVersion);
        return constantPoolConstant;
    }
}
