package net.technolords.tools.artificer;

import net.technolords.tools.artificer.exception.ArtificerException;

import java.nio.file.Path;

/**
 * Created by Technolords on 2015-Aug-18.
 */
public interface Analyser {
    String STATUS_OK = "200";
    String STATUS_ERROR = "500";

    /**
     * Specify the output location of where the XML file shall be generated.
     *
     * @param outputLocation
     *  A Path reference of the output location.
     */
    void setOutputLocation(Path outputLocation);

    /**
     * Specify the output name of the XML file itself.
     *
     * @param outputFilename
     *  The filename of the XML file.
     */
    void setOutputFilename(String outputFilename);

    /**
     * Specify the input location of the artifact to be analyzed.
     *
     * @param inputLocation
     *  The location of the artifact.
     */
    void analyseArtifact(Path inputLocation) throws ArtificerException;

}
