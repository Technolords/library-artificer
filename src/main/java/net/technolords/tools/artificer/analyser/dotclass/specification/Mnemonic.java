package net.technolords.tools.artificer.analyser.dotclass.specification;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Created by Technolords on 2016-Apr-12.
 */
public class Mnemonic {
    private String id;
    private String opcode;

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name = "opcode")
    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode) {
        this.opcode = opcode;
    }
}
