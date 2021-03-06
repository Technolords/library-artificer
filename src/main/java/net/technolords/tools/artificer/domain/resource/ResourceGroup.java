package net.technolords.tools.artificer.domain.resource;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Technolords on 2015-Sep-09.
 */
public class ResourceGroup {
    private String groupType;
    private List<Resource> resources = new ArrayList<>();

    @XmlAttribute (name = "type")
    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    @XmlElement(name ="resource")
    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }

    @XmlAttribute (name = "total")
    public int getSize() {
        return this.resources.size();
    }
}
