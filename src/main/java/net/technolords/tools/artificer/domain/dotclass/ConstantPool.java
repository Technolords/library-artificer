package net.technolords.tools.artificer.domain.dotclass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Technolords on 2015-Dec-03.
 */
public class ConstantPool {
    private List<Constant> constants;

    public ConstantPool() {
        this.constants = new ArrayList<>();
    }

    public List<Constant> getConstants() {
        return constants;
    }

    public void setConstants(List<Constant> constants) {
        this.constants = constants;
    }
}
