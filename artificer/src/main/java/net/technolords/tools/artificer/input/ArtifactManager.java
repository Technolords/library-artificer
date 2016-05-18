package net.technolords.tools.artificer.input;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.Analyser;
import net.technolords.tools.artificer.analyser.dotclass.BytecodeParser;
import net.technolords.tools.artificer.analyser.dotclass.ClassDomainAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.analyser.dotclass.JavaSpecificationManager;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.domain.resource.ResourceGroup;

/**
 * Created by Technolords on 2015-Aug-28.
 */
public class ArtifactManager {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private static final String JAVA_SPECIFICATIONS_REFERENCE = "analyser/dotclass/java-specifications.xml";
    public static final String CLASSIFICATION_UNDEFINED = "_classification_undefined_";
    public static final String CLASSIFICATION_JAVA_CLASSES = ".class";
    private JavaSpecificationManager javaSpecificationManager;
    private BytecodeParser bytecodeParser;
    private ConstantPoolAnalyser constantPoolAnalyser;
    private ClassDomainAnalyser classDomainAnalyser;

    public ArtifactManager() {
    }

    /**
     * This implementation is based on the concept that the zipfile is considered a different type
     * of filesystem. Creating or basing on different file systems is a feature of java.nio.file.
     * References:
     *  https://blogs.oracle.com/xuemingshen/entry/the_zip_filesystem_provider_in1
     *  http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
     *
     * @param analysis
     *  A reference of the model to populate.
     * @param pathToZipFile
     *  Te path to the zip file.
     */
    public void analyseArtifact(Analysis analysis, Path pathToZipFile) {
        try {

            // Initialize manager of java compiler versions (for lookup)
            if (this.javaSpecificationManager == null) {
                this.javaSpecificationManager = new JavaSpecificationManager(JAVA_SPECIFICATIONS_REFERENCE);
            }

            // Walk the tree for initial scan, and classify the resources
            ArtifactResourceVisitor artifactResourceVisitor = new ArtifactResourceVisitor(analysis);
            FileSystem fileSystem = FileSystems.newFileSystem(pathToZipFile, null);
            Files.walkFileTree(fileSystem.getPath("/"), artifactResourceVisitor);

            // Initialize manager of byte code analysis (for referenced classes)
            if (this.bytecodeParser == null) {
                this.bytecodeParser = new BytecodeParser(JAVA_SPECIFICATIONS_REFERENCE);
            }

            // Initialize constant pool analyser
            if (this.constantPoolAnalyser == null) {
                this.constantPoolAnalyser = new ConstantPoolAnalyser();
            }

            // Analyse the resource group representing java classes
            ResourceGroup javaResourceGroup = analysis.getResourceGroups().get(CLASSIFICATION_JAVA_CLASSES);
            if (javaResourceGroup != null) {
                // Analyse each resource individually
                for (Resource resource : javaResourceGroup.getResources()) {
                    // Determine the compiled version of the resource, and register it
                    // TODO: move spec manager 'behind' the bytecodeParser (avoid double parsing of xml?)
                    this.javaSpecificationManager.registerCompiledVersion(analysis.getMeta(), resource);

                    // Determine the references classes by the resource
                    this.bytecodeParser.analyseBytecode(resource);
                }
                // Now that all java classes are analysed, the 'self' classes are known. At this point we can divide
                // the resources in the appropriate groups. In other words: Self, Standard, Enterprise and External
                // See also the enums in the ReferencedClass.

                // Initialize class domain analyser, note that when there is no java resource group this is skipped.
                if (this.classDomainAnalyser == null) {
                    this.classDomainAnalyser = new ClassDomainAnalyser(javaResourceGroup);
                }
                for (Resource resource : javaResourceGroup.getResources()) {
                    this.classDomainAnalyser.analyseReferencedClassForClassDomain(analysis, resource);
                }

            }
            // TODO: chart packages and classes into visual groups using graphviz/gephi

            // TODO: analyse other type of files (i.e. OSGI, WEB-INF etc)

            // TODO: generate class diagrams

            // TODO: generate sequence diagrams

        } catch (IOException | ZipError e) {
            LOGGER.error("Updated meta with error message: " + e.getMessage(), e);
            // Update status
            Meta meta = analysis.getMeta();
            meta.setStatus(Analyser.STATUS_ERROR);
            meta.setErrorMessage(e.getMessage());
        }
    }

}
