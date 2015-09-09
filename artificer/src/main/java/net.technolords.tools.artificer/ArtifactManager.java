package net.technolords.tools.artificer;

import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.Meta;
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
            // TODO: determine the compiled version
            // TODO: determine the imports (by reflection)

        } catch (IOException | ZipError e) {
            // Update status
            Meta meta = analysis.getMeta();
            meta.setStatus(Analyser.STATUS_ERROR);
            meta.setErrorMessage(e.getMessage());
        }
    }
}
