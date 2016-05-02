package net.technolords.tools.artificer.input;

import net.technolords.tools.artificer.Analyser;
import net.technolords.tools.artificer.analyser.dotclass.BytecodeParser;
import net.technolords.tools.artificer.analyser.dotclass.ConstantPoolAnalyser;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.dependencies.ReferencedClass;
import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.domain.resource.ResourceGroup;
import net.technolords.tools.artificer.analyser.dotclass.JavaSpecificationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.zip.ZipError;

/**
 * Created by Technolords on 2015-Aug-28.
 */
public class ArtifactManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManager.class);
    private static final String JAVA_SPECIFICATIONS_REFERENCE = "analyser/dotclass/java-specifications.xml";
    public static final String CLASSIFICATION_UNDEFINED = "_classification_undefined_";
    public static final String CLASSIFICATION_JAVA_CLASSES = ".class";
    private JavaSpecificationManager javaSpecificationManager;
    private BytecodeParser bytecodeParser;
    private ConstantPoolAnalyser constantPoolAnalyser;

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

            // Initialize constant pool analyzer
            if (this.constantPoolAnalyser == null) {
                this.constantPoolAnalyser = new ConstantPoolAnalyser();
            }

            // Inspect the category, with java classes
            ResourceGroup javaResourceGroup = analysis.getResourceGroups().get(CLASSIFICATION_JAVA_CLASSES);
            if (javaResourceGroup != null) {
                for (Resource resource : javaResourceGroup.getResources()) {
                    // Determine the compiled version of the resource, and register it
                    this.javaSpecificationManager.registerCompiledVersion(analysis.getMeta(), resource);

                    // Determine the references classes by the resource
                    this.bytecodeParser.analyseBytecode(resource);
                    /**
                     * Resolve three class pools:
                     * - self contained
                     * - packaged by SE
                     *  For java 8 source, scan zip file: /usr/lib/jvm/java-8-oracle/src.zip
                     * - external
                     */
                    this.constantPoolAnalyser.extractReferencedClasses(resource.getConstantPool());
                    // For each resource, fetch referenced classes from constant pool
                    // Create unique list and update model (XML)
                }
                // TODO: update aggregated set of resources (part of Analysis)
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
