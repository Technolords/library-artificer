package net.technolords.tools.artificer.analyser.dotclass.specification;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * Created by Technolords on 2015-Dec-02.
 */
public class JavaSpecification {
    private String magicNumber;
    private String version;
    private ConstantPoolConstants constantPoolConstants;

    @XmlAttribute(name = "magic-number")
    public String getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(String magicNumber) {
        this.magicNumber = magicNumber;
    }

    @XmlAttribute(name = "version")
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @XmlElement(name = "constant-pool-constants")
    public ConstantPoolConstants getConstantPoolConstants() {
        return constantPoolConstants;
    }

    public void setConstantPoolConstants(ConstantPoolConstants constantPoolConstants) {
        this.constantPoolConstants = constantPoolConstants;
    }
}
