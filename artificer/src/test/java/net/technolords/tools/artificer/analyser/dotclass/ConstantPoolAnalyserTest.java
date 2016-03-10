package net.technolords.tools.artificer.analyser.dotclass;

import net.technolords.tools.artificer.TestSupport;
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
import java.util.*;

/**
 * Created by 1795 on 3-2-2016.
 */
public class ConstantPoolAnalyserTest extends TestSupport {

    private static final Logger LOGGER = LoggerFactory.getLogger(BytecodeParserTest.class);
    private static final String KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE = "analyser/dotclass/java-specifications.xml";

    @DataProvider(name = "dataSetClasses")
    public Object[][] dataSet() {

        String[]expectedClasses = new String[]{"java/lang/Object", "net/technolords/tools/artificer/exception/ArtificerException", "net/technolords/tools/artificer/Analyser"};
//        Set<String> classSet = new HashSet<String>();
//        Collections.addAll(classSet, expectedClasses);

        return new Object[][] {
//                { "Analyser.class" , true, Arrays.asList("java/lang/Object", "net/technolords/tools/artificer/exception/ArtificerException", "net/technolords/tools/artificer/Analyser")},
                { "Analyser.class" , true, new HashSet<>(Arrays.asList(expectedClasses))},
                { "abc.class", false, new HashSet<>() }
        };
    }
    @Test(dataProvider = "dataSetClasses")
    public void testReferencedClassesExtraction(final String CLASSNAME,final boolean validClass ,final Set<String> expectedRefClasses) {

        Path pathToResourceLocation = FileSystems.getDefault().getPath(super.getPathToClassFolder().toAbsolutePath() + File.separator + CLASSNAME);
        LOGGER.debug("The path towards the class file '" + CLASSNAME + "' exists: " + Files.exists(pathToResourceLocation));

        // Create a resource reference linking to the file
        Resource resource = new Resource();
        resource.setPath(pathToResourceLocation);
        resource.setName(CLASSNAME);

        resource.setCompiledVersion("1.8");

        BytecodeParser bytecodeParser = new BytecodeParser(KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE);
        bytecodeParser.analyseBytecode(resource);

        ConstantPoolAnalyser constantPoolAnalyser = new ConstantPoolAnalyser();
        Set<String> referencedClasses = constantPoolAnalyser.extractReferencedClasses(resource.getConstantPool());

        Assert.assertEquals(referencedClasses, expectedRefClasses);

    }
}