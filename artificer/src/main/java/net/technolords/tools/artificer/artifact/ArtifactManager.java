package net.technolords.tools.artificer.artifact;

import javassist.ClassPool;
import javassist.NotFoundException;
import net.technolords.tools.artificer.Analyser;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.Meta;
import net.technolords.tools.artificer.domain.Resource;
import net.technolords.tools.artificer.domain.ResourceGroup;
import net.technolords.tools.artificer.domain.meta.FoundJavaVersion;
import net.technolords.tools.artificer.domain.meta.FoundJavaVersions;
import net.technolords.tools.artificer.exception.ArtificerException;
import net.technolords.tools.artificer.reference.JavaVersionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipError;

/**
 * Created by Technolords on 2015-Aug-28.
 */
public class ArtifactManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManager.class);
    private static final String JAVA_VERSIONS_REFERENCE = "reference/java-versions.xml";
    public static final String CLASSIFICATION_UNDEFINED = "_classification_undefined_";
    public static final String CLASSIFICATION_JAVA_CLASSES = ".class";
    private JavaVersionManager javaVersionManager;

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

            // Inspect the category, with java classes
            ResourceGroup javaResourceGroup = analysis.getResourceGroups().get(CLASSIFICATION_JAVA_CLASSES);
            if (javaResourceGroup != null) {
                for (Resource resource : javaResourceGroup.getResources()) {
                    // Determine the compiled version of the resource, and register it
                    this.javaVersionManager.registerCompiledVersion(analysis.getMeta(), resource);

                    // Determine the references classes by the resource
//                    this.getReferencedClasses(resource);
                }
            }

            // TODO: study ASM, BCEL
            // http://www.theserverside.com/news/1363881/The-Working-Developers-Guide-to-Java-Bytecode
            // http://stackoverflow.com/questions/3315938/is-it-possible-to-view-bytecode-of-class-file
            // https://commons.apache.org/proper/commons-bcel/manual.html
            // TODO: filter the references classes (own classes, standard classes, external classes)
            // http://stackoverflow.com/questions/520328/can-you-find-all-classes-in-a-package-using-reflection
            // Write util class, that reads classes from a rt jar
            // filters the anonymous inner classes (as they cannot be instantiated anyways)
            // JVM spec: http://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4

            // TODO: chart packages and classes into visual groups using graphviz/gephi

            // TODO: analyse other type of files (i.e. OSGI, WEB-INF etc)

            // TODO: generate sequence diagrams
//        } catch (ZipError e) {
//            if ("zip END header not found".equals(e.getMessage())) {
//                LOGGER.warn("Ignoring error: Got END header not found....");
//            }
//        } catch (NotFoundException e) {
//            LOGGER.error(e.getMessage(), e);
//        } catch (IOException | ArtificerException e) {
        } catch (IOException | ZipError e) {
            // Update status
            Meta meta = analysis.getMeta();
            meta.setStatus(Analyser.STATUS_ERROR);
            meta.setErrorMessage(e.getMessage());
        }
    }

    /**
     * Auxiliary method to get the referenced classes per resource.
     *
     * @param resource
     *  The resource associated with the referenced classes.
     * @throws NotFoundException
     *  When the class (resource) cannot be found (full package name and class) in the class pool.
     */
    protected void getReferencedClasses(Resource resource) throws NotFoundException {
        LOGGER.debug("About to getRefClasses of: " + resource.getName());

        // Initialize class pool
        ClassPool classPool = ClassPool.getDefault();

        // Get full package name and class
        String packageAndClassName = resource.getPath().toString();
        packageAndClassName = packageAndClassName.replaceAll("/", ".");
        int index = packageAndClassName.indexOf(".class");
        packageAndClassName = packageAndClassName.substring(1, index);

        // Obtain the referenced classes
        Collection classes = classPool.get(packageAndClassName).getRefClasses();
        LOGGER.debug("Class " + resource.getName() + " has " + classes.size() + " referenced classes...");
        resource.getReferencedClasses().addAll(classes);
    }
}
