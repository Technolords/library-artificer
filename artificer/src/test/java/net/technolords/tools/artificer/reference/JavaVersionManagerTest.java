package net.technolords.tools.artificer.reference;

import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by 1795 on 5-11-2015.
 */
public class JavaVersionManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaVersionManagerTest.class);
    private static final String UNKNOWN_JAVA_VERSION = "unknownJavaVersion";
    private static final String KNOWN_JAVA_VERSIONS_REFERENCE_FILE = "reference/java-versions.xml";
    private static final String MALFORMAT_REFERENCE_FILE = "reference/malformed-java-versions.xml";
    private static final String NON_EXISTING_JAVA_VERSIONS_REFERENCE_FILE = "reference/no-java-versions.xml";

    // Test Case #1 : to check Failure of Reference File
    @Test(expectedExceptions = ArtificerException.class)
    public void testFailureOfReferenceFile() throws ArtificerException {
        JavaVersionManager javaVersionManager = new JavaVersionManager(NON_EXISTING_JAVA_VERSIONS_REFERENCE_FILE);
        javaVersionManager.lookupJavaVersion("boom");
        LOGGER.info("Done");
    }

    @DataProvider(name = "compilerVersionDataProvider")
    public Object[][] compilerVersionDataProvider() {
        return new Object[][] {
                {"2D","1.1"},
                {"34","1.8"},
                {"99", UNKNOWN_JAVA_VERSION},
                {"ab3", UNKNOWN_JAVA_VERSION},
                {null, UNKNOWN_JAVA_VERSION},
        };
    }

    //Test Case #2 : happy flow with test data using Data provider
    @Test (dataProvider = "compilerVersionDataProvider")
    public void testWithVariousVersions(final String magicNumber, final String expectedVersion) throws ArtificerException {

        JavaVersionManager javaVersionManager = new JavaVersionManager(KNOWN_JAVA_VERSIONS_REFERENCE_FILE);
        String foundJavaVersion = javaVersionManager.lookupJavaVersion(magicNumber);
        assertEquals(foundJavaVersion, expectedVersion);
    }

    //Test Case #3 : unhappy flow: malformed reference file
    @Test (dataProvider = "compilerVersionDataProvider", expectedExceptions = ArtificerException.class)
    public void testMalformedReferenceFile(final String magicNumber, final String expectedVersion) throws ArtificerException{
        JavaVersionManager javaVersionManager = new JavaVersionManager(MALFORMAT_REFERENCE_FILE);
        String foundJavaVersion = javaVersionManager.lookupJavaVersion(magicNumber);
        assertEquals(foundJavaVersion, expectedVersion);
    }

}