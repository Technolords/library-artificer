package net.technolords.tools.artificer.artifact;

import net.technolords.tools.artificer.Analyser;
import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.Meta;
import net.technolords.tools.artificer.domain.Resource;
import net.technolords.tools.artificer.domain.ResourceGroup;
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
    public static final String CLASSIFICATION_UNDEFINED = "_classification_undefined_";
    public static final String CLASSIFICATION_JAVA_CLASSES = ".class";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManager.class);

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
            // Walk the tree for initial scan, and classify the resources
            ArtifactResourceVisitor artifactResourceVisitor = new ArtifactResourceVisitor(analysis);
            FileSystem fileSystem = FileSystems.newFileSystem(pathToZipFile, null);
            Files.walkFileTree(fileSystem.getPath("/"), artifactResourceVisitor);

            // Inspect the categories
            ResourceGroup javaResourceGroup = analysis.getResourceGroups().get(CLASSIFICATION_JAVA_CLASSES);
            if(javaResourceGroup != null) {
                // TODO: determine the compiled version
                /*
                Every '.class' file starts off with the following:

                Magic Number [4 bytes]
                Version Information [4 bytes]

                A hexdump of a '.class' file compiled with each of the following options reveals:

                javac -target 1.1 ==> CA FE BA BE 00 03 00 2D
                javac -target 1.2 ==> CA FE BA BE 00 00 00 2E
                javac -target 1.3 ==> CA FE BA BE 00 00 00 2F
                javac -target 1.4 ==> CA FE BA BE 00 00 00 30

                major   minor   java version
                45      3       1.0
                45      3       1.1
                46      0       1.2
                47      0       1.3
                48      0       1.4
                49      0       1.5
                50      0       1.6
                51      0       1.7 / 7
                52      0       1.8 / 8

                http://stackoverflow.com/questions/698129/how-can-i-find-the-target-java-version-for-a-compiled-class
                 */
                // TODO: determine the imports (by reflection) minus the present and standard classes
            }

        } catch (IOException | ZipError e) {
            // Update status
            Meta meta = analysis.getMeta();
            meta.setStatus(Analyser.STATUS_ERROR);
            meta.setErrorMessage(e.getMessage());
        }
    }
}
