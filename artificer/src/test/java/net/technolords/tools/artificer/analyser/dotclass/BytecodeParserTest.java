package net.technolords.tools.artificer.analyser.dotclass;

import junit.framework.Assert;
import net.technolords.tools.artificer.TestSupport;
import net.technolords.tools.artificer.domain.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Created by Technolords on 2015-Dec-04.
 */
public class BytecodeParserTest extends TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(BytecodeParserTest.class);
    private static final String KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE = "analyser/dotclass/java-specifications.xml";

    @Test
    public void testWithDataSample() {
        final String dataSample = "/test-classes/net/technolords/tools/data/FieldTest1.class";
        Path pathToDataSample = FileSystems.getDefault().getPath(super.getPathToTargetFolder().toAbsolutePath() + dataSample);
        LOGGER.debug("Path towards the class file exists: " + Files.exists(pathToDataSample));
        Assert.assertTrue("Expected the test class to exist...", Files.exists(pathToDataSample));

        // Create a resource reference linking to the file
        Resource resource = new Resource();
        resource.setPath(pathToDataSample);
        resource.setName("FieldTest1.class");
        resource.setCompiledVersion("1.8");

        BytecodeParser bytecodeParser = new BytecodeParser(KNOWN_JAVA_SPECIFICATIONS_REFERENCE_FILE);
        bytecodeParser.analyseBytecode(resource);

        ConstantPoolAnalyser constantPoolAnalyser = new ConstantPoolAnalyser();
        Set<String> referencedClasses = constantPoolAnalyser.extractReferencedClasses(resource.getConstantPool());
        for(String referencedClass : referencedClasses) {
            LOGGER.debug("Found referenced class: " + referencedClass);
        }
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
/*
constantPoolSize: 23
Constant index: 1, tag: 7, type: Class
Info fragment size: readUnsignedShort, description: name_index, value: 18
Constant index: 2, tag: 7, type: Class
Info fragment size: readUnsignedShort, description: name_index, value: 19
Constant index: 3, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: STATUS_OK
Constant index: 4, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: Ljava/lang/String;
Constant index: 5, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: ConstantValue
Constant index: 6, tag: 8, type: String
Info fragment size: readUnsignedShort, description: string_index, value: 20
Constant index: 7, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: STATUS_ERROR
Constant index: 8, tag: 8, type: String
Info fragment size: readUnsignedShort, description: string_index, value: 21
Constant index: 9, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: setOutputLocation
Constant index: 10, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: (Ljava/nio/file/Path;)V
Constant index: 11, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: setOutputFilename
Constant index: 12, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: (Ljava/lang/String;)V
Constant index: 13, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: analyseArtifact
Constant index: 14, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: Exceptions
Constant index: 15, tag: 7, type: Class
Info fragment size: readUnsignedShort, description: name_index, value: 22
Constant index: 16, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: SourceFile
Constant index: 17, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: Analyser.java
Constant index: 18, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: net/technolords/tools/artificer/Analyser
Constant index: 19, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: java/lang/Object
Constant index: 20, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: 200
Constant index: 21, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: 500
Constant index: 22, tag: 1, type: Utf8
Info fragment size: readUTF, description: string_value, value: net/technolords/tools/artificer/exception/ArtificerException
*/