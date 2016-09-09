package net.technolords.tools.artificer.domain.resource;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import net.technolords.tools.artificer.domain.dotclass.ConstantPool;

/**
 * Created by Technolords on 2015-Sep-04.
 */
public class Resource {
    private String name;
    private Path path;
    private Set<String> referencedClasses = new HashSet<>();
    private String compiledVersion;
    boolean validClass = true;
    private ConstantPool constantPool;

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

    @XmlTransient
    public Set<String> getReferencedClasses() {
        return referencedClasses;
    }

    public void setReferencedClasses(Set<String> referencedClasses) {
        this.referencedClasses = referencedClasses;
    }

    @XmlAttribute(name = "version", required = false)
    public String getCompiledVersion() {
        return compiledVersion;
    }

    public void setCompiledVersion(String compiledVersion) {
        this.compiledVersion = compiledVersion;
    }

    @XmlTransient
    public boolean isValidClass() {
        return validClass;
    }

    public void setValidClass(boolean validClass) {
        this.validClass = validClass;
    }

    @XmlTransient
    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public void setConstantPool(ConstantPool constantPool) {
        this.constantPool = constantPool;
    }
}
