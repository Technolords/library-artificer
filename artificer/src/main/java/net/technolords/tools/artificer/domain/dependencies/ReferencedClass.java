package net.technolords.tools.artificer.domain.dependencies;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;

/**
 * Created by Technolords on 2016-Mar-10.
 */
public class ReferencedClass {
    private String packageWithClass;
    private ClassDomain classDomain;

    @XmlEnum(String.class)
    public enum ClassDomain {
        Java_SE, Java_EE, Self, External
    }

    @XmlAttribute (name = "package")
    public String getPackageWithClass() {
        return packageWithClass;
    }

    public void setPackageWithClass(String packageWithClass) {
        this.packageWithClass = packageWithClass;
    }

    @XmlAttribute (name = "class-domain")
    public ClassDomain getClassDomain() {
        return classDomain;
    }

    public void setClassDomain(ClassDomain classDomain) {
        this.classDomain = classDomain;
    }
}
