package net.technolords.tools.artificer.analyser.dotclass.specification;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Technolords on 2015-Dec-02.
 */
public class ConstantPoolConstant {
    private String type;
    private String tag;
    private List<ConstantPoolInfoFragment> fragments;

    public void ConstantPoolConstant() {
        this.fragments = new ArrayList<>();
    }

    @XmlAttribute(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlAttribute(name = "tag")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @XmlElement(name = "info-fragment")
    public List<ConstantPoolInfoFragment> getFragments() {
        return fragments;
    }

    public void setFragments(List<ConstantPoolInfoFragment> fragments) {
        this.fragments = fragments;
    }
}
