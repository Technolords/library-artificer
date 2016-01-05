package net.technolords.tools.artificer.input;

import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.resource.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Technolords on 2015-Dec-16.
 */
public class ArtifactResourceVisitorTest extends TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResourceVisitorTest.class);

    private static final String INPUT_CLASS     = "class" + File.separator + "ArtificerImplTest.class";
    private static final String INPUT_JAR       = "jars" + File.separator + "artificer-1.0.0-SNAPSHOT.jar";
    private static final String INPUT_UNDEFINED = "abc";

    /**
     * Test case #1: Test classification of a resource.
     */

    @DataProvider(name = "dataSetClassifyResource")
    public Object[][] dataSetClassifyResource() {
        return new Object[][] {
                {INPUT_CLASS},
                {INPUT_UNDEFINED}
        };
    }

    @Test(dataProvider = "dataSetClassifyResource")
    public void testClassificationOfResource(String input) {

        Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToDataFolder() + File.separator + input);
        LOGGER.debug("The path towards the input file '" + input + "' exists: " + Files.exists(pathToResourceLocation));

        Analysis analysis = new Analysis();
        analysis.setArtifactName(input);

        // Create a resource reference linking to the file
        Resource resource = new Resource();
        resource.setPath(pathToResourceLocation);
        resource.setName(input);

        ArtifactResourceVisitor artifactResourceVisitor = new ArtifactResourceVisitor(analysis);
        artifactResourceVisitor.classifyResource(resource);

        Assert.assertTrue(true);
    }

    /**
     * Test case #2: Test adding a resource to a resource group.
     */

    @DataProvider(name = "dataSetResourceGroupClassification")
    public Object[][] dataSetResourceGroupClassification() {
        return new Object[][] {
                {INPUT_CLASS, ".class"},
                {INPUT_UNDEFINED, "_classification_undefined_"},
                {INPUT_JAR, ".jar"}
        };
    }

    @Test(dataProvider = "dataSetResourceGroupClassification")
    public void testAddResourceToClassificationGroup(String input, String classification) {

        Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToDataFolder() + File.separator + input);
        LOGGER.debug("The path towards the input file '" + input + "' exists: " + Files.exists(pathToResourceLocation));

        Analysis analysis = new Analysis();
        analysis.setArtifactName("artificer-1.0.0-SNAPSHOT.jar");

        // Create a resource reference linking to the file
        Resource resource = new Resource();
        resource.setPath(pathToResourceLocation);
        resource.setName(input);

        ArtifactResourceVisitor artifactResourceVisitor = new ArtifactResourceVisitor(analysis);
        artifactResourceVisitor.addResourceToClassificationGroup(resource,classification);

        Assert.assertTrue(analysis.getResourceGroups().containsKey(classification));

        }
}