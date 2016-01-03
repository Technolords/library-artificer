package net.technolords.tools.artificer.input;

import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.resource.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Technolords on 2015-Dec-16.
 */
public class ArtifactResourceVisitorTest extends TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResourceVisitorTest.class);

    /**
     * Test case #1: Test classification of a resource.
     */
    @DataProvider(name = "dataSetClassifyResource")
    public Object[][] dataSetClassifyResource() {
        return new Object[][] {
                {"ArtificerImplTest.class","D:/commit2Learn/techno-tools/artificer/target/test-classes/net/technolords/tools/artificer/"},
                {"abc","D:/commit2Learn/techno-tools/artificer/src/test/resources/data/"}
        };
    }

    @Test(dataProvider = "dataSetClassifyResource")
    public void testClassificationOfResource(String input, String location) {

        Path pathToResourceLocation = FileSystems.getDefault().getPath(location);
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
                {"ArtificerImplTest.class","D:/commit2Learn/techno-tools/artificer/target/test-classes/net/technolords/tools/artificer/",".class"},
                {"abc","D:/commit2Learn/techno-tools/artificer/src/test/resources/data/","_classification_undefined_"},
                {"artificer-1.0.0-SNAPSHOT.jar","D:/commit2Learn/techno-tools/artificer/src/test/resources/data/jars",".jar"}
        };
    }

    @Test(dataProvider = "dataSetResourceGroupClassification")
    public void testAddResourceToClassificationGroup(String input, String location, String classification) {

        Path pathToResourceLocation = FileSystems.getDefault().getPath(location);
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