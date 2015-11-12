package net.technolords.tools.artificer.reference;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

/**
 * Created by Technolords on 2015-Oct-18.
 */
public class JavaVersion {
    private String magicNumber;
    private String version;

    @XmlAttribute(name = "magic-number")
    public String getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(String magicNumber) {
        this.magicNumber = magicNumber;
    }

    @XmlValue
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
