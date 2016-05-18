package net.technolords.tools.artificer;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.exception.ArtificerException;
import net.technolords.tools.artificer.input.ArtifactManager;
import net.technolords.tools.artificer.output.OutputManager;

/**
 * Created by Technolords on 2015-Aug-18.
 */
public class ArtificerImpl implements Analyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtificerImpl.class);

    private Path outputLocation;
    private String outputFilename;

    /**
     * Set the output location where the analysis report will be written to.
     *
     * @param outputLocation
     *  A Path reference of the output location.
     */
    public void setOutputLocation(Path outputLocation) {
        this.outputLocation = outputLocation;
    }

    /**
     * Set the output file name of the analysis report.
     *
     * @param outputFilename
     *  The filename.
     */
    public void setOutputFilename(String outputFilename) {
        this.outputFilename = outputFilename;
    }

    /**
     * The main method exposed to the interface. The implementation is executed with the following steps:
     * - verification of output location
     * - initialization of internal model
     * - perform analysis
     * - write report
     *
     * @param inputLocation
     *  A reference of the artifact to be analysed.
     * @throws ArtificerException
     *  When an error occurred during analysis or writing the report.
     */
    public void analyseArtifact(Path inputLocation) throws ArtificerException {
        // Verify input location exist
        if(!Files.exists(inputLocation)) {
            throw new ArtificerException("No output as the input does not exist...");
        }
        // Verify output location is set
        if(this.outputLocation == null) {
            throw new ArtificerException("No output location set...");
        }
        // Verify output filename is set
        if(this.outputFilename == null || this.outputFilename.isEmpty()) {
            throw new ArtificerException("No output filename set...");
        }
        // Verify write permission
        if(!Files.isWritable(this.outputLocation)) {
            throw new ArtificerException("No permission to write to: " + this.outputLocation.toAbsolutePath());
        }

        // Instantiate model
        Analysis analysis = new Analysis();
        analysis.setArtifactName(this.determineArtifactName(inputLocation));
        analysis.setGeneratedFilename(this.outputFilename);
        Meta meta = new Meta();
        meta.setStatus(STATUS_OK);
        analysis.setMeta(meta);

        // Start analysis
        LOGGER.debug("Starting analysis...");
        ArtifactManager artifactManager = new ArtifactManager();
        artifactManager.analyseArtifact(analysis, inputLocation);

        // Report analysis
        LOGGER.debug("Writing analysis...");
        OutputManager outputManager = new OutputManager(this.outputLocation);
        outputManager.writeReport(analysis);
    }

    /**
     * Auxiliary method to determine the artifact name.
     *
     * @param inputLocation
     *  The URI referring to the input location.
     * @return
     *  The artifact name.
     */
    protected String determineArtifactName(Path inputLocation) {
        // Expect input as ugly as: file://some/path/to/artifact.jar
        int lastIndex = inputLocation.toAbsolutePath().toString().lastIndexOf(File.separator);
        return inputLocation.toAbsolutePath().toString().substring(lastIndex + 1);
    }


}
