package net.technolords.tools.artificer.reference;

import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;

public class JavaVersionManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaVersionManagerTest.class);
    private static final String UNKNOWN_JAVA_VERSION = "unknownJavaVersion";
    private static final String KNOWN_JAVA_VERSIONS_REFERENCE_FILE = "reference/java-versions.xml";
    private static final String MALFORMED_REFERENCE_FILE = "reference/malformed-java-versions.xml";
    private static final String NON_EXISTING_JAVA_VERSIONS_REFERENCE_FILE = "reference/no-java-versions.xml";

    private Path pathToDataFolder;

    /**
     * Since the artificer project is part of a multi module Maven build, the start point of executing the test
     * can differ. I.e. from 'root pom' or from 'artificer pom' which means there are different path's to the
     * test resources. This method fixes the correct location of the test resources by calculating the path.
     */
    @BeforeClass
    public void configureRelativeFolders() {
        // Create String which depends on file system (Unix vs Windows) by using the file separator
        StringBuilder buffer = new StringBuilder();
        buffer.append("src").append(File.separator).append("test").append(File.separator);
        buffer.append("resources").append(File.separator);
        buffer.append("data").append(File.separator).append("class");

        // Set path to folder containing the data, i.e. src/test/resources/data/class
        Path pathToData = FileSystems.getDefault().getPath(buffer.toString());
        if(!pathToData.toAbsolutePath().toString().contains("artificer")) {
            StringBuilder pathWithPrefix = new StringBuilder();
            pathWithPrefix.append("artificer").append(File.separator).append(buffer.toString());
            pathToData = FileSystems.getDefault().getPath(pathWithPrefix.toString());
        }
        this.pathToDataFolder = pathToData.toAbsolutePath();
        LOGGER.debug("Data folder set: {} and exists: {}", this.pathToDataFolder.toString(), Files.exists(this.pathToDataFolder));
    }

    /**
     * Test case #1: Test initialization of the JavaVersionManager with a reference of a non
     * existing file. A specific exception is expected to be thrown.
     *
     * @throws ArtificerException
     *  When initialization of the JavaVersionManager fails.
     */
    @Test (expectedExceptions = ArtificerException.class)
    public void testFailureOfReferenceFile() throws ArtificerException {
        JavaVersionManager javaVersionManager = new JavaVersionManager(NON_EXISTING_JAVA_VERSIONS_REFERENCE_FILE);
        javaVersionManager.lookupJavaVersion("boom");
    }

    /**
     * Auxiliary method to declare a data set to support lookup of java versions. The data set is represented
     * in a multi-dimensional array, where each entry represents a single set. An entry is specified with two
     * elements, each meaning:
     *
     *  [0] : The magic number
     *  [1] : The expected java version matching the magic number.
     *
     * @return
     *  The data set.
     */
    @DataProvider (name = "dataSetWithMagicNumbersAndJavaVersions")
    public Object[][] dataSetWithMagicNumbersAndJavaVersions() {
        return new Object[][] {
            { "2D",   "1.1" },
            { "34",   "1.8" },
            { "99",   UNKNOWN_JAVA_VERSION },
            { "ab3",  UNKNOWN_JAVA_VERSION },
            { null,   UNKNOWN_JAVA_VERSION },
        };
    }

    /**
     * Test case #2: Test the java version lookup functionality, using the data set.
     *
     * @param magicNumber
     *  The magic number associated with the lookup.
     * @param expectedVersion
     *  The expected java version associated with the magic number.
     * @throws ArtificerException
     *  When initialization of the JavaVersionManager fails.
     */
    @Test (dataProvider = "dataSetWithMagicNumbersAndJavaVersions")
    public void testLookupJavaVersionsWithDataSet(final String magicNumber, final String expectedVersion) throws ArtificerException {
        JavaVersionManager javaVersionManager = new JavaVersionManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
        String foundJavaVersion = javaVersionManager.lookupJavaVersion(magicNumber);
        assertEquals(foundJavaVersion, expectedVersion);
    }

    /**
     * Test case #3: Test the java version lookup functionality, using the data set and a malformed reference file.
     *
     * @param magicNumber
     *  The magic number associated with the lookup.
     * @param expectedVersion
     *  The expected java version associated with the magic number.
     * @throws ArtificerException
     *  When initialization of the JavaVersionManager fails.
     */
    @Test (dataProvider = "dataSetWithMagicNumbersAndJavaVersions", expectedExceptions = ArtificerException.class)
    public void testLookupJavaVersionsWithDataSetAndBadReferenceFile(final String magicNumber, final String expectedVersion) throws ArtificerException{
        JavaVersionManager javaVersionManager = new JavaVersionManager(MALFORMED_REFERENCE_FILE);
        String foundJavaVersion = javaVersionManager.lookupJavaVersion(magicNumber);
        assertEquals(foundJavaVersion, expectedVersion);
    }

    /**
     * Auxiliary method to declare a data set to support extraction of the magic number. The data set is
     * represented in a multi-dimensional array, where each entry represents a single set. An entry is specified
     * with two elements, each meaning:
     *
     *  [0] : The file name of a java compiled class, located in the data/class folder
     *  [1] : The expected magic number associated with the class file
     *
     * @return
     *  The data set.
     */
    @DataProvider (name = "dataSetWithFileNamesAndMagicNumbers")
    public Object[][] dataSetWithFileNamesAndMagicNumbers() {
        return new Object[][] {
            { "Analysis.class",          "34" },
            { "RfuRouteBuilder.class",   "33" },
        };
    }

    /**
     * Test case 4: Test the extraction of a magic number from a class file as functionality, using the data set.
     *
     * @param fileName
     *  The file name of the class name, associated with the magic number extraction.
     * @param expectedVersion
     *  The expected java version.
     * @throws ArtificerException
     *  When initialization of the JavaVersionManager fails.
     * @throws IOException
     *  When the file of which to extract the magic number does not exist.
     */
    @Test (dataProvider = "dataSetWithFileNamesAndMagicNumbers")
    public void testMagicNumberExtractionWithValidClasses(String fileName, String expectedVersion) throws ArtificerException, IOException {
        try{
            // Create a path to the file
            Path pathToResourceLocation = FileSystems.getDefault().getPath(this.pathToDataFolder.toAbsolutePath() + File.separator + fileName);
            LOGGER.debug("The path towards the class file '" + fileName + "' exists: " + Files.exists(pathToResourceLocation));

            // Create a resource reference linking to the file
            Resource resource = new Resource();
            resource.setPath(pathToResourceLocation);

            // Find the magic number associated to the resource
            JavaVersionManager javaVersionManager = new JavaVersionManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
            String actualVersion = javaVersionManager.getMagicNumber(resource);
            Assert.assertEquals(actualVersion, expectedVersion, "Actual version of the resource is: " + actualVersion);
        } catch (Exception e) {
            Assert.fail("Should not fail as the class '" + fileName + "' is a valid Java class.");
        }
    }

    @Test
    public void testMagicNumberExtractionWithInvalidClasses() {
        // TODO: implement, using abc.class and what not...
    }

}