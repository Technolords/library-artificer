package net.technolords.tools.artificer.domain.dependencies;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;

/**
 * Created by Technolords on 2016-Mar-10.
 */
public class ReferencedClass {
    private static final int DEFAULT_HASH_CODE = 92821;
    private String packageWithClass;
    private ClassDomain classDomain;

    @XmlEnum(String.class)
    public enum ClassDomain {
        Standard, Enterprise, Self, External
    }

    /**
     * Override to support Set operations such as 'contains'.
     *
     * @param object
     * @return
     */
    @Override
    public boolean equals(Object object) {
        if(object instanceof ReferencedClass) {
            ReferencedClass other = (ReferencedClass) object;
            return this.getPackageWithClass().equals(other.getPackageWithClass());
        }
        return false;
    }

    /**
     * Override to support Set operations such as 'add'.
     * @return
     */
    @Override
    public int hashCode() {
        // Only let the property 'packageWithClass' dictate.
        return this.packageWithClass == null ? DEFAULT_HASH_CODE : this.packageWithClass.hashCode();
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
