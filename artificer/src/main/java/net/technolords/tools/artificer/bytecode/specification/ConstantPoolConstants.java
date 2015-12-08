package net.technolords.tools.artificer.bytecode.specification;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Technolords on 2015-Dec-02.
 */
public class ConstantPoolConstants {
    private List<ConstantPoolConstant> constantPoolConstants;

    public ConstantPoolConstants() {
        this.constantPoolConstants = new ArrayList<>();
    }

    @XmlElement(name = "constant")
    public List<ConstantPoolConstant> getConstantPoolConstants() {
        return constantPoolConstants;
    }

    public void setConstantPoolConstants(List<ConstantPoolConstant> constantPoolConstants) {
        this.constantPoolConstants = constantPoolConstants;
    }
}
