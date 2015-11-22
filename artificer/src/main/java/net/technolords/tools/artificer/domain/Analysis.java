package net.technolords.tools.artificer.domain;

import net.technolords.tools.artificer.domain.meta.Meta;
import net.technolords.tools.artificer.domain.resource.ResourceGroup;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Analysis element, which is the root, and contains the following attributes:
 *
 * - artifact-name     : The name of the artifact associated with the analysis
 *
 * Happy and Unhappy flow:
 *
 * <analysed-artifact artifact-name="xxx.jar">
 *     <meta>
 *         ...
 *     </meta>
 *     <resources>
 *         ...
 *     </resources>
 */
@XmlRootElement (name = "analysed-artifact")
public class Analysis {
    private String artifactName;
    private String generatedFilename;
    private Meta meta;
    private Map<String, ResourceGroup> resourceGroups = new HashMap<>();

    public Analysis() {
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

    @XmlElement (name = "meta")
    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    @XmlTransient
    public Map<String, ResourceGroup> getResourceGroups() {
        return resourceGroups;
    }

    public void setResourceGroups(Map<String, ResourceGroup> resourceGroups) {
        this.resourceGroups = resourceGroups;
    }

    @XmlElementWrapper (name = "resources")
    @XmlElement (name ="resource")
    public List<ResourceGroup> getResourcesAsGroups() {
        return new ArrayList(this.resourceGroups.values());
    }
}
