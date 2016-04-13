package net.technolords.tools.artificer.analyser.dotclass;

import net.technolords.tools.artificer.analyser.dotclass.bytecode.AccessFlagsParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.AttributesParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.ClassReferenceParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.ConstantPoolParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.FieldsParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.InterfaceParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.MagicNumberParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.MethodsParser;
import net.technolords.tools.artificer.analyser.dotclass.bytecode.MinorAndMajorVersionParser;
import net.technolords.tools.artificer.analyser.dotclass.specification.JavaSpecification;
import net.technolords.tools.artificer.analyser.dotclass.specification.JavaSpecifications;
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
 *
 * Every '.class' file starts off with the following:
 * - Magic Number [4 bytes]
 * - Version Information [4 bytes]
 *
 * javac -target 1.1 ==> CA FE BA BE 00 03 00 2D
 * javac -target 1.2 ==> CA FE BA BE 00 00 00 2E
 * javac -target 1.3 ==> CA FE BA BE 00 00 00 2F
 * javac -target 1.4 ==> CA FE BA BE 00 00 00 30
 * javac -target 1.5 ==> CA FE BA BE 00 00 00 31
 * javac -target 1.6 ==> CA FE BA BE 00 00 00 32
 * javac -target 1.7 ==> CA FE BA BE 00 00 00 33
 * javac -target 1.8 ==> CA FE BA BE 00 00 00 34
 *
 * Legend:
 * - u1: unsigned one byte quantity, to be read as: readUnsignedByte
 * - u2: unsigned two byte quantity, to be read as: readUnsignedShort
 * - u4: unsigned four byte quantity, to be read as: readInt
 * - u8: unsigned eight byte quantity, to be read as: readLong
 *
 * See for reference: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html
 */
public class BytecodeParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(BytecodeParser.class);
    private String referenceFile;
    private Map<String, JavaSpecification> lookupMap;

    public BytecodeParser(String referenceFile) {
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
                this.lookupMap.put(javaSpecification.getVersion(), javaSpecification);
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
     * [java 8]
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
     * The constant pool is of particular interest, as this contains references of classes.
     *
     * @param resource
     *  The resource associated with the determination of the referenced classes.
     */
    public void analyseBytecode(Resource resource) {
        if(!resource.isValidClass()) {
            return;
        }
        try {
            if(this.lookupMap == null) {
                this.lookupMap = new HashMap<>();
                this.initializeSpecifications();
            }
            JavaSpecification javaSpecification = this.lookupMap.get(resource.getCompiledVersion());
            StringBuilder buffer = new StringBuilder();
            buffer.append("About to analyse byte code of: ").append(resource.getName());
            buffer.append(", for JVM spec: ").append((javaSpecification == null ? "None found" : javaSpecification.getVersion()));
            buffer.append(", with total mnemonics: ").append((javaSpecification == null ? "None" : javaSpecification.getMnemonics().getMnemonics().size()));
            LOGGER.info(buffer.toString());
            DataInputStream dataInputStream = new DataInputStream(Files.newInputStream(resource.getPath()));
            // Extract the magic number
            MagicNumberParser.extractMagicNumber(dataInputStream);
            // Extract the minor and major version
            MinorAndMajorVersionParser.extractMinorAndMajorVersion(dataInputStream);
            // Extract the constant pool
            ConstantPoolParser.extractConstantPool(dataInputStream, javaSpecification, resource);
            // Extract the access flags
            AccessFlagsParser.extractAccessFlags(dataInputStream, AccessFlagsParser.LOCATION_CLASS_FILE);
            // Extract the 'this' class reference
            ClassReferenceParser.extractThisClassReference(dataInputStream);
            // Extract the 'super' class reference
            ClassReferenceParser.extractSuperClassReference(dataInputStream);
            // Extract the interfaces
            InterfaceParser.extractInterfaces(dataInputStream);
            // Extract the fields
            FieldsParser.extractFields(dataInputStream, javaSpecification, resource);
            // Extract the methods
            MethodsParser.extractMethods(dataInputStream, javaSpecification, resource);
            // Extract the attributes
            AttributesParser.extractAttributesFromClassFile(dataInputStream, javaSpecification, resource);
        } catch (IOException e) {
            LOGGER.error("Unable to parse the class: " + resource.getName(), e);
        } catch (ArtificerException e) {
            LOGGER.error("Unable to initialize lookup map" + e.getMessage(), e);
        }
    }

}
