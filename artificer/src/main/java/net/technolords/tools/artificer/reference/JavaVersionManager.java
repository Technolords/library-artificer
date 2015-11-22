package net.technolords.tools.artificer.reference;

import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.domain.meta.FoundJavaVersion;
import net.technolords.tools.artificer.domain.meta.FoundJavaVersions;
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
 * Created by Technolords on 2015-Oct-18.
 */
public class JavaVersionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaVersionManager.class);
    private static final int MAGIC_NUMBER = 0xcafebabe;
    private static final String UNKNOWN_JAVA_VERSION = "unknownJavaVersion";
    private Map<String, String> lookupMap;
    private String referenceFile;

    /**
     * Constructor using a reference file.
     *
     * @param referenceFile
     *  The location of the reference file.
     */
    public JavaVersionManager(String referenceFile) {
        this.referenceFile = referenceFile;
    }

    /**
     * Auxiliary method to perform a lookup for a particular magic number. Based on this magical number
     * a java version is determined.
     *
     * @param magicNumber
     *  The magic number associated with the look up.
     * @return
     *  The java (compiler) version.
     * @throws ArtificerException
     *  When loading the XML configuration file for lookup purposes fails.
     */
    public String lookupJavaVersion(String magicNumber) throws ArtificerException {
        if(this.lookupMap == null) {
            this.lookupMap = new HashMap<>();
            this.initializeLookupMap();
        }
        if(!this.lookupMap.containsKey(magicNumber)) {
            LOGGER.warn("Unable to map magic version: " + magicNumber + ", defaulting to unknown java version: " + UNKNOWN_JAVA_VERSION);
            return UNKNOWN_JAVA_VERSION;
        }
        return this.lookupMap.get(magicNumber);
    }

    /**
     * Auxiliary method to initialize the lookup map. It uses the default classloader to obtain
     * an inputstream as reference for the XML file and then JAXB will use this to unmarshall this
     * to an instance of the JavaVersions file.
     *
     * @throws ArtificerException
     *  When unmarshalling the XML file fails.
     */
    public void initializeLookupMap() throws ArtificerException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(JavaVersions.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(this.referenceFile);
            JavaVersions javaVersions = (JavaVersions) unmarshaller.unmarshal(inputStream);
            for(JavaVersion javaVersion : javaVersions.getJavaVersions()) {
                this.lookupMap.put(javaVersion.getMagicNumber(), javaVersion.getVersion());
            }
            LOGGER.debug("Total java versions initialized: " + this.lookupMap.size());
        } catch (JAXBException | IllegalArgumentException e) {
            throw new ArtificerException(e);

        }
    }

    /**
     * Auxiliary method to determine the compiler version. Every java class
     * has some 'leading' bytes as part of a file signature and basically
     * represents the magic number and version.
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
     * See also: http://stackoverflow.com/questions/698129/how-can-i-find-the-target-java-version-for-a-compiled-class
     *
     * @param resource
     *  The resource used to determine the compiler version.
     * @return
     *  The magic number associated with the compiler version.
     * @throws IOException
     *  When reading bytes from the class file fails.
     * @throws ArtificerException
     *  When the class file is not compliant with the standard Java identification of byte code(i.e. prefixed with CAFEBABE)
     */
    public String getMagicNumber(Resource resource) throws IOException, ArtificerException {
        DataInputStream dataInputStream = new DataInputStream(Files.newInputStream(resource.getPath()));

        // Get first 4 bytes, as that represents the magic number
        if (dataInputStream.readInt() != MAGIC_NUMBER) {
            throw new ArtificerException(resource.getName() + " is not a valid java class!");
        }
        return Integer.toHexString(dataInputStream.readInt());
    }

    /**
     * Auxiliary method to register the compiled version of this class. In any event
     * the class is corrupted or non compliant with java, it is marked as invalid so
     * further parsing for this resource is skipped.
     *
     * @param meta
     *  The Meta reference is used to register the found java compiled version on
     *  Meta level (so an overview is present).
     * @param resource
     *  The Resource reference is used to register the found java compiled version
     *  on Resource level.
     */
    public void registerCompiledVersion(Meta meta, Resource resource) {
        try {
            // Register on Resource level
            String magicNumber = this.getMagicNumber(resource);
            String javaCompilerVersion = this.lookupJavaVersion(magicNumber);
            resource.setCompiledVersion(javaCompilerVersion);

            // Register on Meta level
            FoundJavaVersions foundJavaVersions = meta.getFoundJavaVersions();
            if(foundJavaVersions == null) {
                foundJavaVersions = new FoundJavaVersions();
                meta.setFoundJavaVersions(foundJavaVersions);
            }
            FoundJavaVersion foundJavaVersion = null;
            for(FoundJavaVersion currentJavaVersion : foundJavaVersions.getFoundJavaVersionList()) {
                if(currentJavaVersion.getFoundJavaVersion().equals(javaCompilerVersion)) {
                    foundJavaVersion = currentJavaVersion;
                    break;
                }
            }
            if(foundJavaVersion == null) {
                foundJavaVersion = new FoundJavaVersion();
                foundJavaVersion.setFoundJavaVersion(javaCompilerVersion);
                foundJavaVersions.getFoundJavaVersionList().add(foundJavaVersion);
            }
            foundJavaVersion.setTotalClasses(foundJavaVersion.getTotalClasses() + 1);
        } catch (IOException | ArtificerException e) {
            // Empty .class files, or .class files not compliant with java intrinsic magic number are marked
            // as invalid (as further processing is not required)
            LOGGER.warn("Resource " + resource.getName() + ", is not a valid java class and will be skipped");
            resource.setValidClass(false);
        }
    }

}
