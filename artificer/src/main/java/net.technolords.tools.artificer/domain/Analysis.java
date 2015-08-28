package net.technolords.tools.artificer.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Created by Technolords on 2015-Aug-21.
 */
// <analysed-artifact artifact-name="name">
//    ...
// </analysed-artifact>
@XmlRootElement (name = "analysed-artifact")
public class Analysis {
    private Meta meta;
    private String artifactName;
    private String generatedFilename;

    public Analysis() {
    }

    @XmlElement (name = "meta")
    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    @XmlAttribute (name = "artifact-name")
    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    @XmlTransient
    public String getGeneratedFilename() {
        return generatedFilename;
    }

    public void setGeneratedFilename(String generatedFilename) {
        this.generatedFilename = generatedFilename;
    }
}
