package net.technolords.tools.artificer.domain.meta;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Technolords on 2015-Nov-22.
 */
public class FoundJavaVersions {
    List<FoundJavaVersion> foundJavaVersionList;

    public FoundJavaVersions() {
        this.foundJavaVersionList = new ArrayList<>();
    }

    @XmlElement(name = "java")
    public List<FoundJavaVersion> getFoundJavaVersionList() {
        return foundJavaVersionList;
    }

    public void setFoundJavaVersionList(List<FoundJavaVersion> foundJavaVersionList) {
        this.foundJavaVersionList = foundJavaVersionList;
    }
}
