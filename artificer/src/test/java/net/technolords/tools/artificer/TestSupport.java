package net.technolords.tools.artificer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by Technolords on 2015-Dec-04.
 */
public class TestSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestSupport.class);
    private Path pathToClassFolder;

    /**
     * Since the artificer project is part of a multi module Maven build, the start point of executing the test
     * can differ. I.e. from 'root pom' or from 'artificer pom' which means there are different path's to the
     * test resources. This method fixes the correct location of the test resources by calculating the path.
     */
    @BeforeClass
    public void configureRelativeFolders() {
        // Create String which depends on file system (Unix vs Windows) by using the file separator
        StringBuilder buffer = new StringBuilder();
        buffer.append("src").append(File.separator).append("test").append(File.separator);
        buffer.append("resources").append(File.separator);
        buffer.append("data").append(File.separator).append("class");

        // Set path to folder containing the data, i.e. src/test/resources/data/class
        Path pathToData = FileSystems.getDefault().getPath(buffer.toString());
        if(!pathToData.toAbsolutePath().toString().contains("artificer")) {
            StringBuilder pathWithPrefix = new StringBuilder();
            pathWithPrefix.append("artificer").append(File.separator).append(buffer.toString());
            pathToData = FileSystems.getDefault().getPath(pathWithPrefix.toString());
        }
        this.pathToClassFolder = pathToData.toAbsolutePath();
        LOGGER.debug("Data folder set: {} and exists: {}", this.pathToClassFolder.toString(), Files.exists(this.pathToClassFolder));
    }

    /**
     * Auxiliary method to get a reference of the class folder, as sub folder of the data folder.
     *
     * @return
     *  The reference of the class folder.
     */
    public Path getPathToClassFolder() {
        return this.pathToClassFolder;
    }
}
