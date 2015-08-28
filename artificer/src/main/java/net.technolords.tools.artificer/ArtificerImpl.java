package net.technolords.tools.artificer;

import net.technolords.tools.artificer.domain.Analysis;
import net.technolords.tools.artificer.domain.Meta;
import net.technolords.tools.artificer.exception.ArtificerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Created by Technolords on 2015-Aug-18.
 */
public class ArtificerImpl implements Analyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArtificerImpl.class);
    private static final Charset CHARSET = Charset.forName("UTF-8");
    public static final String STATUS_OK = "200";
    public static final String STATUS_ERROR = "500";

    private Path outputLocation;
    private String outputFilename;
    private Path inputLocation;

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
        // TODO: determine if the source is valid (i.e. a zip file)
        // TODO: count the classes and determine their types (summary)
        // TODO: determine the compiled version
        // TODO: determine the imports (by reflection)
        // TODO: more stuff

        // Report analysis
        this.writeReport(analysis);
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
        int lastIndex = inputLocation.toAbsolutePath().toString().lastIndexOf("/");
        return inputLocation.toAbsolutePath().toString().substring(lastIndex + 1);
    }

    /**
     * Write the analysis as report to stream.
     *
     * @param analysis
     *  The analysis to be written out as report.
     * @throws ArtificerException
     *  When writing a report fails.
     */
    protected void writeReport(final Analysis analysis) throws ArtificerException {
        try {
            // Initialize stream to file
            Path outputFile = FileSystems.getDefault().getPath(this.outputLocation.toAbsolutePath() + "/" + analysis.getGeneratedFilename());
            BufferedWriter writer = Files.newBufferedWriter(outputFile, CHARSET, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            XMLStreamWriter xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);
            // Initialize data
            JAXBContext context = JAXBContext.newInstance(Analysis.class);
            Marshaller marshaller = context.createMarshaller();
            // Write data
            marshaller.marshal(analysis, xmlStreamWriter);
        } catch (IOException | XMLStreamException | JAXBException exception) {
            LOGGER.error("Failed to create report" + exception.getMessage(), exception);
            throw new ArtificerException("Error writing data to a report: " + exception.getMessage(), exception);
        }
    }
}
