package net.technolords.tools.artificer.artifact;

import javassist.ClassPool;
import javassist.NotFoundException;
import net.technolords.tools.artificer.Analyser;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.Meta;
import net.technolords.tools.artificer.domain.Resource;
import net.technolords.tools.artificer.domain.ResourceGroup;
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
import java.util.zip.ZipError;

/**
 * Created by Technolords on 2015-Aug-28.
 */
public class ArtifactManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManager.class);
    private static final int MAGIC_NUMBER = 0xcafebabe;
    private static final String JAVA_VERSIONS_REFERENCE = "reference/java-versions.xml";
    public static final String CLASSIFICATION_UNDEFINED = "_classification_undefined_";
    public static final String CLASSIFICATION_JAVA_CLASSES = ".class";
    private JavaVersionManager javaVersionManager;

    public ArtifactManager() {
    }

    // This implementation is based on the concept that the zipfile is considered a different type
    // of filesystem. Creating or basing on different file systems is a feature of java.nio.file.
    //
    // References:
    //  https://blogs.oracle.com/xuemingshen/entry/the_zip_filesystem_provider_in1
    //  http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html

    public void analyseArtifact(Analysis analysis, Path pathToZipFile) {
        try {
            // Initialize manager of java compiler versions (for lookup)
//            if(this.javaVersionManager == null) {
//                this.javaVersionManager = new JavaVersionManager(JAVA_VERSIONS_REFERENCE);
//            }
            // Walk the tree for initial scan, and classify the resources
            ArtifactResourceVisitor artifactResourceVisitor = new ArtifactResourceVisitor(analysis);
            FileSystem fileSystem = FileSystems.newFileSystem(pathToZipFile, null);
            Files.walkFileTree(fileSystem.getPath("/"), artifactResourceVisitor);

            // Inspect the categories
            ResourceGroup javaResourceGroup = analysis.getResourceGroups().get(CLASSIFICATION_JAVA_CLASSES);
            if (javaResourceGroup != null) {
                for (Resource resource : javaResourceGroup.getResources()) {
                    // Determine the compiled version of the resource
                    String unmappedCompilerVersion = this.getCompilerVersion(resource);
//                    resource.setCompiledVersion(this.javaVersionManager.lookupJavaVersion(unmappedCompilerVersion));

                    // TODO: keep map at high lvl to count classes per compiler version (think uber jar)
                    // note that this information is also available on all resources combined so post
                    // analysis is an option.

                    // Determine the references classes by the resource
                    this.getReferencedClasses(resource);
                }
            }

            // TODO: filter the references classes (own classes, standard classes, external classes)
            // http://stackoverflow.com/questions/1983839/determine-which-jar-file-a-class-is-from
            // CodeSource src = MyClass.class.getProtectionDomain().getCodeSource();
            // if (src != null) {
            // URL jar = src.getLocation();

            // Investigate further
            // http://docs.oracle.com/javase/specs/jls/se7/html/jls-7.html

            // TODO: chart packages and classes into visual groups using graphviz/gephi

            // TODO: analyse other type of files (i.e. OSGI, WEB-INF etc)

            // TODO: generate sequence diagrams
        } catch (ZipError e) {
            if("zip END header not found".equals(e.getMessage())) {
                LOGGER.warn("Ignoring error: Got END header not found....");
            }
        } catch (IOException | NotFoundException | ArtificerException e) {
            // Update status
            Meta meta = analysis.getMeta();
            meta.setStatus(Analyser.STATUS_ERROR);
            meta.setErrorMessage(e.getMessage());
        }
    }

    /**
     * Auxiliary method to determine the compiler version. Every java class
     * has some 'leading' bytes as part of a file signature and basically
     * represents the magic number and version.
     *
     * Every '.class' file starts off with the following:
     * - Magic Number [4 bytes]
     * - Version Information [4 bytes]
     *
     * javac -target 1.1 ==> CA FE BA BE 00 03 00 2D
     * javac -target 1.2 ==> CA FE BA BE 00 00 00 2E
     * javac -target 1.3 ==> CA FE BA BE 00 00 00 2F
     * javac -target 1.4 ==> CA FE BA BE 00 00 00 30
     * javac -target 1.5 ==> CA FE BA BE 00 00 00 31
     * javac -target 1.6 ==> CA FE BA BE 00 00 00 32
     * javac -target 1.7 ==> CA FE BA BE 00 00 00 33
     * javac -target 1.8 ==> CA FE BA BE 00 00 00 34
     *
     * See also: http://stackoverflow.com/questions/698129/how-can-i-find-the-target-java-version-for-a-compiled-class
     *
     * @param resource
     *  The resource used to determine the compiler version.
     * @return
     * @throws IOException
     * @throws ArtificerException
     */
    protected String getCompilerVersion(Resource resource) throws IOException, ArtificerException {
        DataInputStream dataInputStream = new DataInputStream(Files.newInputStream(resource.getPath()));

        // Get first 4 bytes, as that represents the magic number
        if (dataInputStream.readInt() != MAGIC_NUMBER) {
            throw new ArtificerException(resource.getName() + " is not a valid java class!");
        }
        return Integer.toHexString(dataInputStream.readInt());
    }

    protected void getReferencedClasses(Resource resource) throws NotFoundException {
        ClassPool classPool = ClassPool.getDefault();
        LOGGER.debug("About to getRefClasses of: " + resource.getName());
        // Get full package name and class
        String packageAndClassName = resource.getPath().toString();
        packageAndClassName = packageAndClassName.replaceAll("/", ".");
        int index = packageAndClassName.indexOf(".class");
        packageAndClassName = packageAndClassName.substring(1, index);
        Collection classes = classPool.get(packageAndClassName).getRefClasses();
        LOGGER.debug("Class " + resource.getName() + " has " + classes.size() + " referenced classes...");
        resource.getReferencedClasses().addAll(classes);
    }
}
