package net.technolords.tools.artificer.reference;

import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.domain.meta.FoundJavaVersion;
import net.technolords.tools.artificer.domain.meta.FoundJavaVersions;
import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;

import java.nio.file.Path;

import static org.testng.Assert.assertEquals;

public class JavaVersionManagerTest extends TestSupport{

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaVersionManagerTest.class);
    private static final String UNKNOWN_JAVA_VERSION = "unknownJavaVersion";
    private static final String KNOWN_JAVA_VERSIONS_REFERENCE_FILE = "reference/java-versions.xml";
    private static final String MALFORMED_REFERENCE_FILE = "reference/malformed-java-versions.xml";
    private static final String NON_EXISTING_JAVA_VERSIONS_REFERENCE_FILE = "reference/no-java-versions.xml";

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
     * Test case 4: Test the extraction of a magic number from a class file as functionality, using the data set
     * containing valid classes.
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
            Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToClassFolder() + File.separator + fileName);

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

    /** Invalid Classes Data Set to test the Magic Number Extraction.
    */
    @DataProvider (name = "dataSetWithInvalidClasses")
    public Object[][] dataSetWithInvalidClasses() {
        return new Object[][] {
                { "abc.class", ArtificerException.class},
                { "iAmEmpty.class", EOFException.class}
        };
    }
    /**
     * Test case 5: Test the extraction of a magic number from a class file as functionality, using the data set
     * containing invalid classes.
     */
    @Test(dataProvider = "dataSetWithInvalidClasses")
    public void testMagicNumberExtractionWithInvalidClasses(String fileName, Class expectedException) throws ArtificerException, IOException {

        try{
            // Create a path to the file
            Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToClassFolder() + File.separator + "invalidClass" + File.separator + fileName);

            // Create a resource reference linking to the file
            Resource resource = new Resource();
            resource.setPath(pathToResourceLocation);

            // Find the magic number associated to the resource
            JavaVersionManager javaVersionManager = new JavaVersionManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
            String actualVersion = javaVersionManager.getMagicNumber(resource);

        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), expectedException);
            LOGGER.debug("Exception caused by : " + e.getCause() + " and: " + e.getMessage());
        }
    }

    /** Data Set to test the registered Java Versions */
    @DataProvider (name = "dataSetWithCompiledVersions")
    public Object[][] dataSetWithCompiledVersions() {
        return new Object[][] {
                { "Analysis.class", "1.8", 1, 1},
                { "RfuRouteBuilder.class", "1.7", 1, 1}
        };
    }
    /**
     * Test case 6: Test the registration of the java versions in the model.
     */

    @Test(dataProvider = "dataSetWithCompiledVersions")
    public void testRegistrationOfJavaVersionInModel(String fileName, String expectedCompiledVersion, int expectedListSize, long expectedTotalClasses){

        Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToClassFolder() + File.separator + fileName);

        Resource resource = new Resource();
        resource.setPath(pathToResourceLocation);

        Meta meta = new Meta();

        JavaVersionManager javaVersionManager = new JavaVersionManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
        javaVersionManager.registerCompiledVersion(meta, resource);

        Assert.assertEquals(resource.getCompiledVersion(), expectedCompiledVersion);
        LOGGER.debug("Resource Compiled Version = " + resource.getCompiledVersion());

        FoundJavaVersions foundJavaVersions = meta.getFoundJavaVersions();

        int currentJavaVersionListSize = meta.getFoundJavaVersions().getFoundJavaVersionList().size();
        Assert.assertEquals(currentJavaVersionListSize,expectedListSize);

        for(FoundJavaVersion currentJavaVersion : foundJavaVersions.getFoundJavaVersionList()) {
            if(currentJavaVersion.getFoundJavaVersion().equals(expectedCompiledVersion)) {

                Assert.assertEquals(currentJavaVersion.getFoundJavaVersion(),expectedCompiledVersion);
                Assert.assertEquals(currentJavaVersion.getTotalClasses(),expectedTotalClasses);
                break;
            }
        }
    }
}

