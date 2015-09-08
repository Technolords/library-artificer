package net.technolords.tools.artificer;

import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.Meta;
import net.technolords.tools.artificer.domain.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipError;

/**
 * Created by Technolords on 2015-Aug-28.
 */
public class ArtifactManager {
    public static final String UNDEFINED_CLASSIFICATION = "_undefined_classification_";
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtifactManager.class);

    public ArtifactManager() {
    }

    // This implementation is based on the concept that the zipfile is considered a different type
    // of filesystem. Creating or basing on different file systems is a feature of java.nio.file.
    //
    // References:
    //  https://blogs.oracle.com/xuemingshen/entry/the_zip_filesystem_provider_in1
    //  http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html

    public void openArtifact(Analysis analysis, Path pathToZipFile) {
        ArtifactResourceVisitor artifactResourceVisitor = new ArtifactResourceVisitor(analysis);
        try {
            // Walk the tree for initial scan
            FileSystem fileSystem = FileSystems.newFileSystem(pathToZipFile, null);
            Files.walkFileTree(fileSystem.getPath("/"), artifactResourceVisitor);

            // Count the categories
            analysis.getResources();

            // Inspect the categories
        } catch (IOException | ZipError e) {
            // Update status
            Meta meta = analysis.getMeta();
            meta.setStatus(Analyser.STATUS_ERROR);
            meta.setErrorMessage(e.getMessage());
        }
    }
}
