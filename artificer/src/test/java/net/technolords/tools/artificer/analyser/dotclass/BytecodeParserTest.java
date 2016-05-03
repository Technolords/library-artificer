package net.technolords.tools.artificer.analyser.dotclass;

import junit.framework.Assert;
import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.data.field.FieldTestWithAnnotations;
import net.technolords.tools.data.field.FieldTestWithConstants;
import net.technolords.tools.data.field.FieldTestWithInnerClasses;
import net.technolords.tools.data.field.FieldTestWithRegularFields;
import net.technolords.tools.data.field.FieldTestWithTypeAnnotations;
import net.technolords.tools.data.method.MethodTestWithAnnotations;
import net.technolords.tools.data.method.MethodTestWithGenericMethods;
import net.technolords.tools.data.method.MethodTestWithRegularMethods;
import net.technolords.tools.data.method.MethodTestWithStaticMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Technolords on 2015-Dec-04.
 */
public class BytecodeParserTest extends TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(BytecodeParserTest.class);
    private static final String KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE = "analyser/dotclass/java-specifications.xml";

    /**
     * Auxiliary method to declare a data set to support byte code parsing of fields inside Classes. The data set
     * is represented in a multi-dimensional array, where each entry represents a single set. An entry is specified
     * with three elements, each meaning:
     *
     *  [0] : The java class containing fields
     *  [1] : The expected number of fields
     *  [2] : The expected Set of referenced classes
     *
     * @return
     *  The data set.
     */
    @DataProvider (name = "dataSetWithFields", parallel = false)
    public Object[][] dataSet() {
        Set<String> expectedReferencedClassesWithRegularFields = new HashSet<>();
        expectedReferencedClassesWithRegularFields.add("java/lang/Object");

        return new Object[][] {
            { FieldTestWithConstants.class, 3, expectedReferencedClassesWithRegularFields },
            { FieldTestWithRegularFields.class, 3, expectedReferencedClassesWithRegularFields },
            { FieldTestWithInnerClasses.class,  3, expectedReferencedClassesWithRegularFields },
            { FieldTestWithAnnotations.class,   3, expectedReferencedClassesWithRegularFields },
            { FieldTestWithTypeAnnotations.class,   3, expectedReferencedClassesWithRegularFields },
        };
    }

    @Test (dataProvider = "dataSetWithFields")
    public void testWithFieldTest(Class className, int numberOfExpectedFields, Set<String> expectedReferencedClasses) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(File.separator).append("test-classes");
        buffer.append(File.separator).append("net").append(File.separator).append("technolords").append(File.separator).append("tools");
        buffer.append(File.separator).append("data").append(File.separator).append("field").append(File.separator).append(className.getSimpleName());
        buffer.append(".class");

        Path pathToDataSample = FileSystems.getDefault().getPath(super.getPathToTargetFolder().toAbsolutePath() + buffer.toString());
        LOGGER.debug("Path towards the class file exists: " + Files.exists(pathToDataSample));
        Assert.assertTrue("Expected the test class to exist...", Files.exists(pathToDataSample));

        // Create a resource reference linking to the file
        Resource resource = new Resource();
        resource.setPath(pathToDataSample);
        resource.setName(className.getSimpleName());
        resource.setCompiledVersion("1.8");

        BytecodeParser bytecodeParser = new BytecodeParser(KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE);
        bytecodeParser.analyseBytecode(resource);

        ConstantPoolAnalyser constantPoolAnalyser = new ConstantPoolAnalyser();
        Set<String> referencedClasses = constantPoolAnalyser.extractReferencedClasses(resource.getConstantPool());
        for(String referencedClass : referencedClasses) {
            LOGGER.debug("Found referenced class: " + referencedClass);
        }
        // Add resource.referencedClasses to the total (which is the entire jar)
    }


    @DataProvider (name = "dataSetWithMethods", parallel = false)
    public Object[][] dataSetWithMethods() {
        return new Object[][] {
            { MethodTestWithRegularMethods.class, null },
            { MethodTestWithStaticMethods.class, null },
            { MethodTestWithAnnotations.class, null },
            { MethodTestWithGenericMethods.class, null },
        };
    }

    @Test (dataProvider = "dataSetWithMethods")
    public void testWithMethodTest(Class className, Set<String> expectedReferencedClasses) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(File.separator).append("test-classes");
        buffer.append(File.separator).append("net").append(File.separator).append("technolords").append(File.separator).append("tools");
        buffer.append(File.separator).append("data").append(File.separator).append("method").append(File.separator).append(className.getSimpleName());
        buffer.append(".class");

        Path pathToDataSample = FileSystems.getDefault().getPath(super.getPathToTargetFolder().toAbsolutePath() + buffer.toString());
        LOGGER.debug("Path towards the class file exists: " + Files.exists(pathToDataSample));
        Assert.assertTrue("Expected the test class to exist...", Files.exists(pathToDataSample));

        // Create a resource reference linking to the file
        Resource resource = new Resource();
        resource.setPath(pathToDataSample);
        resource.setName(className.getSimpleName());
        resource.setCompiledVersion("1.8");

        BytecodeParser bytecodeParser = new BytecodeParser(KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE);
        bytecodeParser.analyseBytecode(resource);
        ConstantPoolAnalyser constantPoolAnalyser = new ConstantPoolAnalyser();
        Set<String> referencedClasses = constantPoolAnalyser.extractReferencedClasses(resource.getConstantPool());
        LOGGER.debug("Done...");
    }

    @Test
    public void testAnalyseBytecodeForSingleClass() throws Exception {
        final String CLASSNAME = "Analyser.class";
//        final String CLASSNAME = "ErrorProcessor.class";
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
    }

}