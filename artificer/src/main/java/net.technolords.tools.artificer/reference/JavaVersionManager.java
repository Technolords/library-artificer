package net.technolords.tools.artificer.reference;

import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Technolords on 2015-Oct-18.
 */
public class JavaVersionManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(JavaVersionManager.class);
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
        } catch (JAXBException e) {
            throw new ArtificerException(e);

        }
    }

}
