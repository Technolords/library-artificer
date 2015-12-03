package net.technolords.tools.artificer.artifact;

import net.technolords.tools.artificer.Analyser;
import net.technolords.tools.artificer.bytecode.BytecodeManager;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.Resource;
import net.technolords.tools.artificer.domain.resource.ResourceGroup;
import net.technolords.tools.artificer.reference.JavaVersionManager;
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
    private static final String JAVA_VERSIONS_REFERENCE = "reference/java-versions.xml";
    private static final String JAVA_SPECIFICATIONS_REFERENCE = "reference/java-specifications.xml";
    public static final String CLASSIFICATION_UNDEFINED = "_classification_undefined_";
    public static final String CLASSIFICATION_JAVA_CLASSES = ".class";
    private JavaVersionManager javaVersionManager;
    private BytecodeManager bytecodeManager;

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
            if (this.javaVersionManager == null) {
                this.javaVersionManager = new JavaVersionManager(JAVA_VERSIONS_REFERENCE);
            }

            // Walk the tree for initial scan, and classify the resources
            ArtifactResourceVisitor artifactResourceVisitor = new ArtifactResourceVisitor(analysis);
            FileSystem fileSystem = FileSystems.newFileSystem(pathToZipFile, null);
            Files.walkFileTree(fileSystem.getPath("/"), artifactResourceVisitor);

            // Initialize manager of byte code analysis (for referenced classes)
            if (this.bytecodeManager == null) {
                this.bytecodeManager = new BytecodeManager(JAVA_SPECIFICATIONS_REFERENCE);
            }

            // Inspect the category, with java classes
            ResourceGroup javaResourceGroup = analysis.getResourceGroups().get(CLASSIFICATION_JAVA_CLASSES);
            if (javaResourceGroup != null) {
                for (Resource resource : javaResourceGroup.getResources()) {
                    // Determine the compiled version of the resource, and register it
                    this.javaVersionManager.registerCompiledVersion(analysis.getMeta(), resource);

                    // Determine the references classes by the resource
                    this.bytecodeManager.analyseBytecode(resource);
                }
            }

            // TODO: chart packages and classes into visual groups using graphviz/gephi

            // TODO: analyse other type of files (i.e. OSGI, WEB-INF etc)

            // TODO: generate class diagrams

            // TODO: generate sequence diagrams

        } catch (IOException | ZipError e) {
            // Update status
            Meta meta = analysis.getMeta();
            meta.setStatus(Analyser.STATUS_ERROR);
            meta.setErrorMessage(e.getMessage());
        }
    }

}
