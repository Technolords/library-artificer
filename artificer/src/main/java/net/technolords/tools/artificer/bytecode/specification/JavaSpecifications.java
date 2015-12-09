package net.technolords.tools.artificer.bytecode.specification;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Technolords on 2015-Dec-02.
 */
@XmlRootElement(name = "java-specifications")
public class JavaSpecifications {
    private List<JavaSpecification> javaSpecifications;

    public JavaSpecifications() {
        this.javaSpecifications = new ArrayList<>();
    }

    @XmlElement(name = "java-specification")
    public List<JavaSpecification> getJavaSpecifications() {
        return javaSpecifications;
    }

    public void setJavaSpecifications(List<JavaSpecification> javaSpecifications) {
        this.javaSpecifications = javaSpecifications;
    }
}
