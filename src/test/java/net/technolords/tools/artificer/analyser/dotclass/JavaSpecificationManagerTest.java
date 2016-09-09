package net.technolords.tools.artificer.analyser.dotclass;

import static org.testng.Assert.assertEquals;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.domain.meta.FoundJavaVersion;
import net.technolords.tools.artificer.domain.meta.FoundJavaVersions;
import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.exception.ArtificerException;

public class JavaSpecificationManagerTest extends TestSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaSpecificationManagerTest.class);
    private static final String UNKNOWN_JAVA_VERSION = "unknownJavaVersion";
    private static final String KNOWN_JAVA_VERSIONS_REFERENCE_FILE = "analyser/dotclass/java-specifications.xml";
    private static final String MALFORMED_REFERENCE_FILE = "analyser/dotclass/malformed-java-versions.xml";
    private static final String NON_EXISTING_JAVA_VERSIONS_REFERENCE_FILE = "analyser/dotclass/no-java-versions.xml";

    private static Resource resource;

    @BeforeGroups("objectCreation")
    public void setUp(){
        resource = new Resource();
    }

    /**
     * Test case #1: Test initialization of the JavaSpecificationManager with a reference of a non
     * existing file. A specific exception is expected to be thrown.
     *
     * @throws ArtificerException
     *  When initialization of the JavaSpecificationManager fails.
     */
    @Test (expectedExceptions = ArtificerException.class)
    public void testFailureOfReferenceFile() throws ArtificerException {
        JavaSpecificationManager javaSpecificationManager = new JavaSpecificationManager(NON_EXISTING_JAVA_VERSIONS_REFERENCE_FILE);
        javaSpecificationManager.lookupJavaVersion("boom");
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
     *  When initialization of the JavaSpecificationManager fails.
     */
    @Test (dataProvider = "dataSetWithMagicNumbersAndJavaVersions")
    public void testLookupJavaVersionsWithDataSet(final String magicNumber, final String expectedVersion) throws ArtificerException {
        // Instantiate javaSpecificationManager only once, so it is shared for each entry in the data set
        JavaSpecificationManager javaSpecificationManager = null;
        if(javaSpecificationManager == null) {
            javaSpecificationManager = new JavaSpecificationManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
        }
        String foundJavaVersion = javaSpecificationManager.lookupJavaVersion(magicNumber);
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
     *  When initialization of the JavaSpecificationManager fails.
     */
    @Test (dataProvider = "dataSetWithMagicNumbersAndJavaVersions", expectedExceptions = ArtificerException.class)
    public void testLookupJavaVersionsWithDataSetAndBadReferenceFile(final String magicNumber, final String expectedVersion) throws ArtificerException{
        JavaSpecificationManager javaSpecificationManager = new JavaSpecificationManager(MALFORMED_REFERENCE_FILE);
        String foundJavaVersion = javaSpecificationManager.lookupJavaVersion(magicNumber);
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
     *  When initialization of the JavaSpecificationManager fails.
     * @throws IOException
     *  When the file of which to extract the magic number does not exist.
     */
    @Test (groups = "objectCreation" , dataProvider = "dataSetWithFileNamesAndMagicNumbers")
    public void testMagicNumberExtractionWithValidClasses(final String fileName, final String expectedVersion) throws ArtificerException, IOException {
        try{
            Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToClassFolder() + File.separator + fileName);

            // Create a resource reference linking to the file
            resource.setPath(pathToResourceLocation);

            // Find the magic number associated to the resource
            JavaSpecificationManager javaSpecificationManager = new JavaSpecificationManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
            String actualVersion = javaSpecificationManager.getMagicNumber(resource);
            Assert.assertEquals(actualVersion, expectedVersion, "Actual version of the resource is: " + actualVersion);
        } catch (Exception e) {
            Assert.fail("Should not fail as the class '" + fileName + "' is a valid Java class.");
        }
    }

    /**
     * Auxiliary method to declare an invalid data set to support extraction of a magic number. The data set is
     * represented in a multi-dimensional array, where each entry represents a single set. An entry is specified
     * with two elements, each meaning:
     *
     *  [0] : The file name of an invalid class, located in the data/class folder
     *  [1] : The expected Exception
     */
    @DataProvider (name = "dataSetWithInvalidClassesAndException")
    public Object[][] dataSetWithInvalidClassesAndExceptions() {
        return new Object[][] {
            { "abc.class",           ArtificerException.class },
            { "iAmEmpty.class",      EOFException.class },
        };
    }

    /**
     * Test case 5: Test the extraction of a magic number from a class file as functionality, using the data set
     * containing invalid classes.
     */
    @Test(groups = "objectCreation", dataProvider = "dataSetWithInvalidClassesAndException")
    public void testMagicNumberExtractionWithInvalidClasses(final String fileName, final Class expectedException) throws ArtificerException, IOException {

        try{
            // Create a path to the file
            Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToClassFolder() + File.separator + "invalidClass" + File.separator + fileName);

            resource.setPath(pathToResourceLocation);

            // Find the magic number associated to the resource
            JavaSpecificationManager javaSpecificationManager = new JavaSpecificationManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
            String actualVersion = javaSpecificationManager.getMagicNumber(resource);
            Assert.fail("Should fail as the class: " + fileName + "is invalid.");

        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), expectedException);
            LOGGER.debug("Exception caused by : " + e.getCause() + " and: " + e.getMessage());
        }
    }

    /**
     * Auxiliary method to declare a data set to support testing of the model. The data set is represented
     * in a multi-dimensional array, where each entry represents a single set. An entry is specified with four
     * elements, each meaning:
     *
     *  [0] : The file name of the valid class, located in the data class folder
     *  [1] : The expected java compiler version
     *  [2] : The expected number of found java versions
     *  [3] : The expected total number of classes
     */
    @DataProvider (name = "dataSetValidClassesAndCompilerVersions")
    public Object[][] dataSetWithCompiledVersions() {
        return new Object[][] {
            { "Analysis.class",        "1.8", 1, 1},
            { "RfuRouteBuilder.class", "1.7", 1, 1},
        };
    }

    /**
     * Test case 6: Test the registration of the java versions in the model.
     */
    @Test(groups = "objectCreation", dataProvider = "dataSetValidClassesAndCompilerVersions")
    public void testRegistrationOfJavaVersionInModel(final String fileName, final String expectedCompiledVersion, final int expectedListSize, final long expectedTotalClasses){

        Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToClassFolder() + File.separator + fileName);

        resource.setPath(pathToResourceLocation);

        Meta meta = new Meta();

        JavaSpecificationManager javaSpecificationManager = new JavaSpecificationManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
        javaSpecificationManager.registerCompiledVersion(meta, resource);

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

