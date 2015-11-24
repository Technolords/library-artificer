package net.technolords.tools.artificer.artifact;

import junit.framework.Assert;
import net.technolords.tools.artificer.domain.Resource;
import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.EOFException;
import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * Created by 1795 on 13-11-2015.
 */
public class ArtifactManagerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManagerTest.class);
    private Path pathToDataFolder;

    @BeforeClass
    public void configureRelativeFolders() {

        Path pathToData = FileSystems.getDefault().getPath("src\\test\\resources\\data\\class");
        if(!pathToData.toAbsolutePath().toString().contains("artificer")) {
            pathToData = FileSystems.getDefault().getPath("artificer\\src\\test\\resources\\data\\class");
        }
        this.pathToDataFolder = pathToData.toAbsolutePath();
        LOGGER.debug("Data folder set: {} and exists: {}", this.pathToDataFolder.toString(), Files.exists(this.pathToDataFolder));
    }

    @DataProvider(name = "resourceDataProvider")
    public Object[][] resourceDataProvider() {
        return new Object[][] {
                {"Analysis.class", "34"},
                {"RfuRouteBuilder.class", "33"},
//                {"abc.class", "none"}
        };
    }

    //Happy Flow
    @Test (dataProvider = "resourceDataProvider")
    public void testWithVariousResources(String resourceInput, String expectedVersion) throws ArtificerException, IOException {
        try{
            Path pathToResourceLocation = FileSystems.getDefault().getPath(this.pathToDataFolder.toAbsolutePath() + "/" + resourceInput);
            Resource resource = new Resource();
            resource.setPath(pathToResourceLocation);

            ArtifactManager artifactManager = new ArtifactManager();
            String actualVersion = artifactManager.getCompilerVersion(resource);
            LOGGER.debug("Actual Version of Resource Input = " + actualVersion);
            Assert.assertEquals(expectedVersion,actualVersion);
            }
        catch (Exception e){
            Assert.fail("Should not fail because of valid classes.");

                            }
    }

    // ToDO : Create Unhappy Test to catch Expected Exception (i.e., both IOException and Artificer Exception
    // Unhappy Flow
    @DataProvider(name = "invalidDataProvider")
    public Object[][] invalidDataProvider() {
        return new Object[][] {
                {"abc.class", ArtificerException.class},
                {"iAmEmpty.class", EOFException.class}
        };
    }

    @Test(dataProvider = "invalidDataProvider")
    public void testExceptions(String resourceInput, Class expectedClass)throws ArtificerException{
        Class actualClass = null ;
        try{
            Path pathToResourceLocation = FileSystems.getDefault().getPath(this.pathToDataFolder.toAbsolutePath() + "/invalidClass/" + resourceInput);
            LOGGER.debug("Data folder set: {} and exists: {}", pathToResourceLocation.toString(), Files.exists(pathToResourceLocation));
            Resource resource = new Resource();
            resource.setPath(pathToResourceLocation);

            ArtifactManager artifactManager = new ArtifactManager();
            String actualVersion = artifactManager.getCompilerVersion(resource);
            LOGGER.debug("Actual Version of Resource Input = " + actualVersion);
        }
        catch(Exception e){
            LOGGER.debug("Exception caused by: " + e.getCause() + " ---and--- Exception message: " + e.getMessage());
            actualClass = e.getClass();
       }
        assertEquals(actualClass, expectedClass);
    }


} //end ofTestClass