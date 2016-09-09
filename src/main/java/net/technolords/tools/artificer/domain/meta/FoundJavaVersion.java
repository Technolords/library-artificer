package net.technolords.tools.artificer.domain.meta;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by Technolords on 2015-Nov-22.
 */
public class FoundJavaVersion {
    private String foundJavaVersion;
    private long totalClasses = 0;

    @XmlAttribute(name = "version")
    public String getFoundJavaVersion() {
        return foundJavaVersion;
    }

    public void setFoundJavaVersion(String foundJavaVersion) {
        this.foundJavaVersion = foundJavaVersion;
    }

    @XmlAttribute(name = "total-classes")
    public long getTotalClasses() {
        return totalClasses;
    }

    public void setTotalClasses(long totalClasses) {
        this.totalClasses = totalClasses;
    }
}
