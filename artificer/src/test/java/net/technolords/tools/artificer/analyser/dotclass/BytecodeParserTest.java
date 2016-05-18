package net.technolords.tools.artificer.analyser.dotclass;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import junit.framework.Assert;
import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.data.field.FieldTestWithConstants;
import net.technolords.tools.data.field.FieldTestWithRegularFields;
import net.technolords.tools.data.method.MethodTestWithStaticInitializer;

/**
 * Created by Technolords on 2015-Dec-04.
 */
public class BytecodeParserTest extends TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(BytecodeParserTest.class);
    private static final String KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE = "analyser/dotclass/java-specifications.xml";
    private static final String JAVA_8 = "1.8";

    /**
     * Auxiliary method to declare a data set to support byte code parsing of fields inside Classes. The data set
     * is represented in a multi-dimensional array, where each entry represents a single set. An entry is specified
     * with three elements, each meaning:
     *
     *  [0] : The java class containing fields
     *  [1] : The expected Set of referenced classes
     *
     * @return
     *  The data set.
     */
    @DataProvider (name = "dataSetWithFields", parallel = false)
    public Object[][] dataSet() {
        return new Object[][] {
            { FieldTestWithConstants.class, JAVA_8, this.getExpectedReferencedClassesForFieldTestWithConstants() },
            { FieldTestWithRegularFields.class, JAVA_8, this.getExpectedReferencedClassesForFieldTestWithRegular() },
                // TODO: fix the test cases outcommented
//            { FieldTestWithInnerClasses.class, JAVA_8, this.todo() },
//            { FieldTestWithAnnotations.class, JAVA_8, this.todo() },
//            { FieldTestWithTypeAnnotations.class, JAVA_8, this.todo() },
        };
    }

    private Set<String> getExpectedReferencedClassesForFieldTestWithConstants() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Object");
        expected.add("java/lang/String");
        expected.add("net/technolords/tools/data/field/FieldTestWithConstants");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForFieldTestWithRegular() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Boolean");
        expected.add("java/lang/Integer");
        expected.add("java/lang/Object");
        expected.add("java/lang/String");
        expected.add("java/util/ArrayList");
        expected.add("java/util/HashMap");
        expected.add("java/util/List");
        expected.add("java/util/Map");
        expected.add("net/technolords/tools/data/field/FieldTestWithRegularFields");
        return expected;
    }

    @Test (dataProvider = "dataSetWithFields")
    public void testWithFieldTest(Class className, String compiledVersion, Set<String> expectedReferencedClasses) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(File.separator).append("test-classes");
        buffer.append(File.separator).append("net").append(File.separator).append("technolords").append(File.separator).append("tools");
        buffer.append(File.separator).append("data").append(File.separator).append("field").append(File.separator).append(className.getSimpleName());
        buffer.append(".class");

        // Prepare sample
        Path pathToDataSample = FileSystems.getDefault().getPath(super.getPathToTargetFolder().toAbsolutePath() + buffer.toString());
        LOGGER.debug("Path towards the class file exists: " + Files.exists(pathToDataSample));
        Assert.assertTrue("Expected the test class to exist...", Files.exists(pathToDataSample));

        // Create a resource reference linking to the file
        Resource resource = new Resource();
        resource.setPath(pathToDataSample);
        resource.setName(className.getSimpleName());
        resource.setCompiledVersion(compiledVersion);
        BytecodeParser bytecodeParser = new BytecodeParser(KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE);
        bytecodeParser.analyseBytecode(resource);

        // Test result of analysis
        Assert.assertEquals("Expected total referenced classes to be equal", expectedReferencedClasses.size(), resource.getReferencedClasses().size());
        for(String referencedClass : resource.getReferencedClasses()) {
            Assert.assertTrue("Expected class (" + referencedClass + ") to be referenced, but was not", expectedReferencedClasses.contains(referencedClass));
        }
    }


    @DataProvider (name = "dataSetWithMethods", parallel = false)
    public Object[][] dataSetWithMethods() {
        return new Object[][] {
                // TODO: fix the test cases like the other data set
//            { MethodTestWithRegularMethods.class, JAVA_8, this.todo() },
//            { MethodTestWithStaticMethods.class, JAVA_8, this.todo() },
//            { MethodTestWithAnnotations.class, JAVA_8, this.todo() },
//            { MethodTestWithGenericMethods.class, JAVA_8, this.todo() },
//            { MethodTestWithGenericMethods.class, JAVA_8, this.todo() },
//            { MethodTestWithMainMethod.class, JAVA_8, this.todo() },
//            { MethodTestWithLambdaMethods.class, JAVA_8, this.todo() },
            { MethodTestWithStaticInitializer.class, JAVA_8, this.todo() },
        };
    }

    private Object todo() {
        return null;
    }

    @Test (dataProvider = "dataSetWithMethods")
    public void testWithMethodTest(Class className, String compiledVersion, Set<String> expectedReferencedClasses) {
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
    }

}