package net.technolords.tools.artificer.artifact;

import net.technolords.tools.artificer.domain.Resource;
import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;


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
                {"Analysis.class"},
                {"RfuRouteBuilder.class"},
                {"abc.class"}
        };
    }

    @Test (dataProvider = "resourceDataProvider")
    public void testWithVariousResources(String resourceInput) throws ArtificerException, IOException {
        try{
            Path pathToResourceLocation = FileSystems.getDefault().getPath(this.pathToDataFolder.toAbsolutePath() + "/" + resourceInput);
            Resource resource = new Resource();
            resource.setPath(pathToResourceLocation);

            ArtifactManager artifactManager = new ArtifactManager();
            String actualVersion = artifactManager.getCompilerVersion(resource);
            LOGGER.debug("Actual Version of Resource Input = " + actualVersion);
            }
        catch (Exception e){
        e.printStackTrace();
            LOGGER.debug("Exception Caught and Cause of Exception = " + e.getCause());

                            }
    }


} //end ofTestClass