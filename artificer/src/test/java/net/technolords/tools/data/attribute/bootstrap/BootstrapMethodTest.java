package net.technolords.tools.data.attribute.bootstrap;

import junit.framework.Assert;
import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.analyser.dotclass.BytecodeParser;
import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Technolords on 2016-Apr-06.
 */
public class BootstrapMethodTest extends TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapMethodTest.class);
    private static final String KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE = "analyser/dotclass/java-specifications.xml";

    @Test
    public void testBootstrapMethodAttributeScenario() throws Exception {
        StringBuilder buffer = new StringBuilder();
        buffer.append(File.separator).append("test-classes");
        buffer.append(File.separator).append("net").append(File.separator).append("technolords").append(File.separator).append("tools");
        buffer.append(File.separator).append("data").append(File.separator).append("attribute").append(File.separator).append("bootstrap");
        buffer.append(File.separator).append(BootstrapMethodTrigger.class.getSimpleName());
        buffer.append(".class");

        Path pathToDataSample = FileSystems.getDefault().getPath(super.getPathToTargetFolder().toAbsolutePath() + buffer.toString());
        LOGGER.debug("Path towards the class file exists: " + Files.exists(pathToDataSample));
        Assert.assertTrue("Expected the test class to exist...", Files.exists(pathToDataSample));

        // Create a resource reference linking to the file
        Resource resource = new Resource();
        resource.setPath(pathToDataSample);
        resource.setName(BootstrapMethodTrigger.class.getSimpleName());
        resource.setCompiledVersion("1.8");

        BytecodeParser bytecodeParser = new BytecodeParser(KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE);
        bytecodeParser.analyseBytecode(resource);
        LOGGER.debug("Done...");
    }
}
