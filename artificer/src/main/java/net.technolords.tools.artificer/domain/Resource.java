package net.technolords.tools.artificer.domain;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import java.nio.file.Path;

/**
 * Created by Technolords on 2015-Sep-04.
 */
public class Resource {
    private String name;
    private Path path;

    public Resource() {
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlTransient
    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
