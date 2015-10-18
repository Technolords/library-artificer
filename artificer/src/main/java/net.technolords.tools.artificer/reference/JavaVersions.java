package net.technolords.tools.artificer.reference;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Technolords on 2015-Oct-18.
 */
@XmlRootElement (name = "java-versions")
public class JavaVersions {
    private List<JavaVersion> javaVersions;

    public JavaVersions() {
        this.javaVersions = new ArrayList<>();
    }

    @XmlElement (name = "java")
    public List<JavaVersion> getJavaVersions() {
        return javaVersions;
    }

    public void setJavaVersions(List<JavaVersion> javaVersions) {
        this.javaVersions = javaVersions;
    }

}
