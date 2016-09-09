package net.technolords.tools.artificer.analyser.dotclass;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.Resource;

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
    private static final String JAVA_SPECIFICATIONS_REFERENCE = "analyser/dotclass/java-specifications.xml";
    private JavaSpecificationManager javaSpecificationManager;

    public BytecodeParser() {
        // Initialize manager of java compiler versions (for lookup)
        this.javaSpecificationManager = new JavaSpecificationManager(JAVA_SPECIFICATIONS_REFERENCE);
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
    public void analyseBytecode(Meta meta, Resource resource) {
        // Peek ahead (determine if resource is valid as well), and update meta information
        this.javaSpecificationManager.registerCompiledVersion(meta, resource);
        // Filter on valid classes
        if(!resource.isValidClass()) {
            return;
        }
        try {
            JavaSpecification javaSpecification = this.javaSpecificationManager.getSpecification(resource);
            // TODO: if spec not complete, return (else it will fail -> ConstantPoolParser Optional.get()
            StringBuilder buffer = new StringBuilder();
            buffer.append("About to analyse byte code of: ").append(resource.getName());
            buffer.append(", for JVM spec: ").append((javaSpecification == null ? "None found" : javaSpecification.getVersion()));
            buffer.append(", with total mnemonics: ").append((javaSpecification == null ? "None" :
                (javaSpecification.getMnemonics() == null ? 0 : javaSpecification.getMnemonics().getMnemonics().size())));
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
        }

        // At this point the resource has a list of referenced classes as well as a populated constant pool.
        // Adding the 'classes' from the constant pool to the list completes the analysis and all the
        // referenced classes are identified.
        resource.getReferencedClasses().addAll(ConstantPoolAnalyser.extractReferencedClasses(resource.getConstantPool()));
        LOGGER.debug("Total referenced classes: " + resource.getReferencedClasses().size());
    }

}
