package net.technolords.tools.artificer.bytecode.specification;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by Technolords on 2015-Dec-02.
 */
public class ConstantPoolConstant {
    private String type;
    private String value;

    @XmlAttribute(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlAttribute(name = "value")
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
