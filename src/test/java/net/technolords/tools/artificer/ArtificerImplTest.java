package net.technolords.tools.artificer;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import net.technolords.tools.artificer.exception.ArtificerException;

/**
 * Created by Technolords on 2015-Aug-18.
 */
public class ArtificerImplTest extends TestSupport{
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtificerImplTest.class);


    // Test auxiliary method: determineArtifactName
    @Test
    public void testDeterminationOfArtifactNameWithComplexURI() {
        final String EXPECTED = "file.jar";
        final String FICTIVE_LOCATION = "some" + File.separator + "random" + File.separator + "path" + File.separator + EXPECTED;
        ArtificerImpl analyser = new ArtificerImpl();
        String artifactName = analyser.determineArtifactName(FileSystems.getDefault().getPath(FICTIVE_LOCATION));
        Assert.assertEquals(artifactName, EXPECTED, "Expected: " + EXPECTED);
    }

    // Test auxiliary method: determineArtifactName
    @Test
    public void testDeterminationOfArtifactNameWithSimpleURI() {
        final String EXPECTED = "file.jar";
        ArtificerImpl analyser = new ArtificerImpl();
        String artifactName = analyser.determineArtifactName(FileSystems.getDefault().getPath("file.jar"));
        Assert.assertEquals(artifactName, EXPECTED, "Expected: " + EXPECTED);
    }

    @Test(expectedExceptions = ArtificerException.class)
    public void testExceptionThrownWhenConfigurationNotSet() throws ArtificerException {
        Analyser analyser = new ArtificerImpl();
        analyser.analyseArtifact(FileSystems.getDefault().getPath(""));
    }

    @Test
    public void testOutputGeneratedWhenProperlyConfigured() throws ArtificerException {
        final String filename = "generated-by-test.xml";
        Analyser analyser = new ArtificerImpl();
        analyser.setOutputLocation(getPathToTargetFolder());
        analyser.setOutputFilename(filename);
        Path inputFile = FileSystems.getDefault().getPath(getPathToDataFolder() + File.separator + "jars" + File.separator + "corrupted.jar");
        analyser.analyseArtifact(inputFile);
        Path pathToOutputFile = FileSystems.getDefault().getPath(getPathToTargetFolder() + File.separator + filename);
        Assert.assertTrue(Files.exists(pathToOutputFile), "Expected a file to be created with filename: " + filename);
    }

    /**
     * The test data has the following format:
     * "path to artifact", "path to expected report", "generated report name"
     * @return
     *  The configuration of the data provider
     */
    @DataProvider (name = "artifactDataProvider")
    public Object[][] artifactDataProvider() {
        return new Object[][] {
//            {"corrupted.jar", "corrupted.xml"},
//            {"navigate.zip", "navigate.xml"},
//            {"service-recommendation-1.0.0.jar", "service-recommendation.xml"},
            {"artificer-1.0.0-SNAPSHOT.jar","arificer.xml"}
        };
    }

    @Test (dataProvider = "artifactDataProvider")
    public void testWithVariousArtifacts(final String artifact,final String generatedReportFilename) throws ArtificerException {

        LOGGER.info("Testing artifact {} with expected report {}", artifact, generatedReportFilename);
        Analyser analyser = new ArtificerImpl();
        analyser.setOutputLocation(getPathToTargetFolder());
        analyser.setOutputFilename(generatedReportFilename);
        Path pathToArtifactLocation = FileSystems.getDefault().getPath(getPathToDataFolder() + File.separator + "jars" + File.separator + artifact);
        analyser.analyseArtifact(pathToArtifactLocation);

        Assert.assertTrue(Files.exists(getPathToTargetFolder()), generatedReportFilename);
    }

}