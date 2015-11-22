package net.technolords.tools.artificer.output;

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
import java.util.Calendar;

/**
 * Created by Technolords on 2015-Sep-09.
 */
public class OutputManager extends Marshaller.Listener {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputManager.class);
    private static final Charset CHARSET = Charset.forName("UTF-8");

    private Path outputLocation;
    private XMLStreamWriter xmlStreamWriter;

    public OutputManager(Path outputLocation) {
        this.outputLocation = outputLocation;
    }

    /**
     * Write the analysis as report to stream.
     *
     * @param analysis
     *  The analysis to be written out as report.
     * @throws ArtificerException
     *  When writing a report fails.
     */
    public void writeReport(final Analysis analysis) throws ArtificerException {
        try {
            // Initialize output stream
            Path outputFile = FileSystems.getDefault().getPath(this.outputLocation.toAbsolutePath() + "/" + analysis.getGeneratedFilename());
            BufferedWriter writer = Files.newBufferedWriter(outputFile, CHARSET, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            this.xmlStreamWriter = XMLOutputFactory.newFactory().createXMLStreamWriter(writer);

            // Initialize data and a listener
            JAXBContext context = JAXBContext.newInstance(Analysis.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setListener(this);

            // Write data
            marshaller.marshal(analysis, this.xmlStreamWriter);
        } catch (IOException | XMLStreamException | JAXBException exception) {
            LOGGER.error("Failed to create report" + exception.getMessage(), exception);
            throw new ArtificerException("Error writing data to a report: " + exception.getMessage(), exception);
        }
    }

    @Override
    public void beforeMarshal(Object source) {
        try {
            if(source instanceof Meta) {
                this.xmlStreamWriter.writeComment("Output generated at: " + Calendar.getInstance().getTime().toString());
            }
        } catch (XMLStreamException e) {
            LOGGER.error("Failed to add a comment: " + e.getMessage(), e);
        }
    }

}
