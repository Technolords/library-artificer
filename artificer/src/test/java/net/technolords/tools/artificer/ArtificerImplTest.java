package net.technolords.tools.artificer;

import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Technolords on 2015-Aug-18.
 */
public class ArtificerImplTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtificerImplTest.class);
    private Path pathToTargetFolder;
    private Path pathToDataFolder;

    /**
     * Depending on the execution of the test files, whether from IDE or from multi module maven project (CLI),
     * the target and data folders are relative. In order to overcome this, the folders are calculated.
     */
    @BeforeClass
    public void configureRelativeFolders() {
        Path pathToTarget = FileSystems.getDefault().getPath("target");
        if(!pathToTarget.toAbsolutePath().endsWith("artificer/target")) {
            pathToTarget = FileSystems.getDefault().getPath("artificer/target");
        }
        this.pathToTargetFolder = pathToTarget.toAbsolutePath();
        LOGGER.debug("Target folder set: {} and exists: {}", this.pathToTargetFolder.toString(), Files.exists(this.pathToTargetFolder));

        Path pathToData = FileSystems.getDefault().getPath("src/test/resources/data");
        if(!pathToData.toAbsolutePath().toString().contains("artificer")) {
            pathToData = FileSystems.getDefault().getPath("artificer/src/test/resources/data");
        }
        this.pathToDataFolder = pathToData.toAbsolutePath();
        LOGGER.debug("Data folder set: {} and exists: {}", this.pathToDataFolder.toString(), Files.exists(this.pathToDataFolder));
    }

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
        analyser.setOutputLocation(this.pathToTargetFolder);
        analyser.setOutputFilename(filename);
        Path inputFile = FileSystems.getDefault().getPath(this.pathToDataFolder.toAbsolutePath() + "/jars/corrupted.jar");
        analyser.analyseArtifact(inputFile);
        Path pathToOutputFile = FileSystems.getDefault().getPath(this.pathToTargetFolder.toAbsolutePath() + "/" + filename);
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
//            {"corrupted.jar",                    "report-of-corrupted.xml",              "corrupted.xml"},
//            {"navigate.zip",                     "report-of-navigate.xml",               "navigate.xml"},
            {"service-recommendation-1.0.0.jar", "report-of-service-recommendation.xml", "service-recommendation.xml"},
//            {"artificer-1.0.0-SNAPSHOT.jar",     "repost-of-artificer.xml",              "arificer.xml"},
        };
    }

    @Test (dataProvider = "artifactDataProvider")
    public void testWithVariousArtifacts(String artifactLocation, String expectedReportLocation, String generatedReportFilename) throws ArtificerException {
        LOGGER.info("Testing artifact {} with expected report {}", artifactLocation, expectedReportLocation);
        Analyser analyser = new ArtificerImpl();
        analyser.setOutputLocation(this.pathToTargetFolder);
        analyser.setOutputFilename(generatedReportFilename);
        Path pathToArtifactLocation = FileSystems.getDefault().getPath(this.pathToDataFolder.toAbsolutePath() + "/jars/" + artifactLocation);
        analyser.analyseArtifact(pathToArtifactLocation);
    }

}