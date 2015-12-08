package net.technolords.tools.artificer.bytecode.specification;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by Technolords on 2015-Dec-06.
 */
public class ConstantPoolInfoFragment {
    private String size;
    private String description;


    @XmlAttribute(name = "size")
    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    @XmlAttribute(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
