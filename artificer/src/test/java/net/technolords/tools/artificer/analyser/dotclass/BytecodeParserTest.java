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
import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.data.field.FieldTestWithAnnotations;
import net.technolords.tools.data.field.FieldTestWithConstants;
import net.technolords.tools.data.field.FieldTestWithInnerClasses;
import net.technolords.tools.data.field.FieldTestWithRegularFields;
import net.technolords.tools.data.field.FieldTestWithTypeAnnotations;
import net.technolords.tools.data.method.MethodTestWithAnnotations;
import net.technolords.tools.data.method.MethodTestWithGenericMethods;
import net.technolords.tools.data.method.MethodTestWithLambdaMethods;
import net.technolords.tools.data.method.MethodTestWithMainMethod;
import net.technolords.tools.data.method.MethodTestWithRegularMethods;
import net.technolords.tools.data.method.MethodTestWithStaticInitializer;
import net.technolords.tools.data.method.MethodTestWithStaticMethods;

/**
 * Created by Technolords on 2015-Dec-04.
 */
public class BytecodeParserTest extends TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(BytecodeParserTest.class);
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
            { FieldTestWithInnerClasses.class, JAVA_8, this.getExpectedReferencedClassesForFieldTestWithInnerClasses() },
            { FieldTestWithAnnotations.class, JAVA_8, this.getExpectedReferencedClassesForFieldTestWithAnnotations() },
            { FieldTestWithTypeAnnotations.class, JAVA_8, this.getExpectedReferencedClassesForFieldTestWithTypeAnnotations() }
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

    private Set<String> getExpectedReferencedClassesForFieldTestWithInnerClasses() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Integer");
        expected.add("java/lang/Object");
        expected.add("net/technolords/tools/data/field/FieldTestWithInnerClasses");
        expected.add("net/technolords/tools/data/field/FieldTestWithInnerClasses$NestedClass1");
        expected.add("net/technolords/tools/data/field/FieldTestWithInnerClasses$NestedClass2");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForFieldTestWithAnnotations() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Deprecated");
        expected.add("java/lang/Object");
        expected.add("java/lang/reflect/Member");
        expected.add("java/lang/String");
        expected.add("java/util/LinkedList");
        expected.add("javax/xml/bind/annotation/XmlAttribute");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingAnnotation");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingAnnotation$Schedules");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingArray");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingBoolean");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingByte");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingChar");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingClass");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingDefault");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingEnum");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingEnum$Priority");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingInteger");
        expected.add("net/technolords/tools/data/field/FieldTestWithAnnotations");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForFieldTestWithTypeAnnotations() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Object");
        expected.add("net/technolords/tools/data/field/FieldTestWithTypeAnnotations");
        expected.add("net/technolords/tools/data/field/FieldTestWithTypeAnnotations$Invisible");
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
        BytecodeParser bytecodeParser = new BytecodeParser();
        bytecodeParser.analyseBytecode(new Meta(), resource);

        // Test result of analysis
        Assert.assertEquals("Expected total referenced classes to be equal", expectedReferencedClasses.size(), resource.getReferencedClasses().size());
        for(String referencedClass : resource.getReferencedClasses()) {
            Assert.assertTrue("Expected class (" + referencedClass + ") to be referenced, but was not", expectedReferencedClasses.contains(referencedClass));
        }
    }

    @DataProvider (name = "dataSetWithMethods", parallel = false)
    public Object[][] dataSetWithMethods() {
        return new Object[][] {
            { MethodTestWithRegularMethods.class, JAVA_8, this.getExpectedReferencedClassesForMethodTestWithRegularMethods() },
            { MethodTestWithStaticMethods.class, JAVA_8, this.getExpectedReferencedClassesForMethodTestWithStaticMethods() },
            { MethodTestWithAnnotations.class, JAVA_8, this.getExpectedReferencedClassesForMethodTestWithAnnotations() },
            { MethodTestWithGenericMethods.class, JAVA_8, this.getExpectedReferencedClassesForMethodTestWithGenericMethods() },
            { MethodTestWithMainMethod.class, JAVA_8, this.getExpectedReferencedClassesForMethodTestWithMainMethod() },
            { MethodTestWithLambdaMethods.class, JAVA_8, this.getExpectedReferencedClassesForMethodTestWithLambdaMethods() },
            { MethodTestWithStaticInitializer.class, JAVA_8, this.getExpectedReferencedClassesForMethodTestWithStaticInitializer() }
        };
    }

    private Set<String> getExpectedReferencedClassesForMethodTestWithRegularMethods() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Object");
        expected.add("java/lang/String");
        expected.add("[Ljava/lang/String;");
        expected.add("java/util/List");
        expected.add("net/technolords/tools/data/method/MethodTestWithRegularMethods");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForMethodTestWithStaticMethods() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Object");
        expected.add("[Ljava/lang/Object;");
        expected.add("java/util/ArrayList");
        expected.add("net/technolords/tools/data/method/MethodTestWithStaticMethods");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForMethodTestWithAnnotations() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/CloneNotSupportedException");
        expected.add("java/lang/Object");
        expected.add("java/lang/String");
        expected.add("java/util/concurrent/ConcurrentHashMap");
        expected.add("java/util/Map");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingBoolean");
        expected.add("net/technolords/tools/data/annotation/AnnotationUsingDefault");
        expected.add("net/technolords/tools/data/method/MethodTestWithAnnotations");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForMethodTestWithGenericMethods() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Object");
        expected.add("java/util/Comparator");
        expected.add("java/util/Iterator");
        expected.add("java/util/List");
        expected.add("java/util/List;Ljava/util/Comparator;)Ljava/lang/Object");
        expected.add("net/technolords/tools/data/method/MethodTestWithGenericMethods");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForMethodTestWithMainMethod() {
        Set<String> expected = new HashSet<>();
        expected.add("java/io/PrintStream");
        expected.add("java/lang/Object");
        expected.add("java/lang/System");
        expected.add("net/technolords/tools/data/method/MethodTestWithMainMethod");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForMethodTestWithLambdaMethods() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/invoke/MethodHandles");
        expected.add("java/lang/invoke/MethodHandles$Lookup");
        expected.add("java/lang/invoke/LambdaMetafactory");
        expected.add("java/lang/Object");
        expected.add("java/util/Map");
        expected.add("java/util/Map$Entry");
        expected.add("java/util/Map$Entry;)Ljava/lang/Object");
        expected.add("java/util/stream/Collectors");
        expected.add("net/technolords/tools/data/method/MethodTestWithLambdaMethods");
        return expected;
    }

    private Set<String> getExpectedReferencedClassesForMethodTestWithStaticInitializer() {
        Set<String> expected = new HashSet<>();
        expected.add("java/lang/Object");
        expected.add("java/lang/String");
        expected.add("java/util/Date");
        expected.add("net/technolords/tools/data/method/MethodTestWithStaticInitializer");
        return expected;
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
        resource.setCompiledVersion(compiledVersion);

        BytecodeParser bytecodeParser = new BytecodeParser();
        bytecodeParser.analyseBytecode(new Meta(), resource);

        // Test result of analysis
        Assert.assertEquals("Expected total referenced classes to be equal", expectedReferencedClasses.size(), resource.getReferencedClasses().size());
        for(String referencedClass : resource.getReferencedClasses()) {
            Assert.assertTrue("Expected class (" + referencedClass + ") to be referenced, but was not", expectedReferencedClasses.contains(referencedClass));
        }
    }

}