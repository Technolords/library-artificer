package net.technolords.tools.artificer.input;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.resource.Resource;

/**
 * Created by Technolords on 2015-Dec-16.
 */
public class ArtifactResourceVisitorTest extends TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactResourceVisitorTest.class);

    private static final String INPUT_CLASS     = "class" + File.separator + "ArtificerImplTest.class";
    private static final String INPUT_JAR       = "jars" + File.separator + "artificer-1.0.0-SNAPSHOT.jar";
    private static final String INPUT_UNDEFINED = "abc";

    private static Analysis analysis;
    private static Resource resource;
    private static ArtifactResourceVisitor artifactResourceVisitor;

    @BeforeGroups("setUp")
    public void createCommonObjects(){
        analysis = new Analysis();
        artifactResourceVisitor = new ArtifactResourceVisitor(analysis);
    }

    /**
     * Test case #1: Test classification of a resource.
     */

    @DataProvider(name = "dataSetClassifyResource")
    public Object[][] dataSetClassifyResource() {
        return new Object[][] {
                {"abc.txt.class", ".class"},
                {INPUT_CLASS, ".class"},
                {INPUT_UNDEFINED, "_classification_undefined_"}
        };
    }

    @Test(groups = "setUp" , dataProvider = "dataSetClassifyResource")
    public void testClassificationOfResource(final String input, final String expectedClassification) {

        Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToDataFolder() + File.separator + input);
        LOGGER.debug("The path towards the input file '" + input + "' exists: " + Files.exists(pathToResourceLocation));

        resource = new Resource();

        resource.setPath(pathToResourceLocation);
        resource.setName(input);

        artifactResourceVisitor.classifyResource(resource);

        Assert.assertTrue(analysis.getResourceGroups().containsKey(expectedClassification));
    }

    /**
     * Test case #2: Test adding a resource to a resource group.
     */

    @DataProvider(name = "dataSetResourceGroupClassification")
    public Object[][] dataSetResourceGroupClassification() {
        return new Object[][] {
                {INPUT_CLASS, ".class", 1},
                {INPUT_UNDEFINED, "_classification_undefined_", 2},
                {INPUT_JAR, ".jar", 3}
        };
    }
    @Test(groups = "setUp", dataProvider = "dataSetResourceGroupClassification")
    public void testAddResourceToClassificationGroup(final String input, final String classification, final int groupSize) {

        Path pathToResourceLocation = FileSystems.getDefault().getPath(getPathToDataFolder() + File.separator + input);
        LOGGER.debug("The path towards the input file '" + input + "' exists: " + Files.exists(pathToResourceLocation));

        resource = new Resource();
        resource.setPath(pathToResourceLocation);
        resource.setName(input);

        artifactResourceVisitor.addResourceToClassificationGroup(resource,classification);

        Assert.assertTrue(analysis.getResourceGroups().containsKey(classification));
        Assert.assertTrue(analysis.getResourceGroups().size() == groupSize);

    }
}