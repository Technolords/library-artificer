package net.technolords.tools.artificer.reference;

import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Created by 1795 on 5-11-2015.
 */
public class JavaVersionManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaVersionManagerTest.class);

    // Test Case : to check Failure of Reference File
    @Test(expectedExceptions = ArtificerException.class)
    public void testFailureOfReferenceFile() throws ArtificerException {
        JavaVersionManager javaVersionManager = new JavaVersionManager("no-java-versions.xml");
        javaVersionManager.lookupJavaVersion("boom");
        LOGGER.info("Done");
    }
    // TODO: happy flow : pass multiple set of Hexadecimal Magic Numbers using DataProvider

    // TODO: unhappy flow: pass multiple set of Hexadecimal Magic Numbers using DataProvider with incorrect data
}