package net.technolords.tools.artificer.domain.dotclass;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Technolords on 2015-Dec-04.
 */
public class Constant {
    private int tag;
    private int constantPoolIndex;
    private String type;
    private List<ConstantInfo> constantInfoList;

    public Constant() {
        this.constantInfoList = new ArrayList<>();
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public int getConstantPoolIndex() {
        return constantPoolIndex;
    }

    public void setConstantPoolIndex(int constantPoolIndex) {
        this.constantPoolIndex = constantPoolIndex;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<ConstantInfo> getConstantInfoList() {
        return constantInfoList;
    }

    public void setConstantInfoList(List<ConstantInfo> constantInfoList) {
        this.constantInfoList = constantInfoList;
    }
}
