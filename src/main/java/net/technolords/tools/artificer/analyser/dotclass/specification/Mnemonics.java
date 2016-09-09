package net.technolords.tools.artificer.analyser.dotclass.specification;

import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Technolords on 2016-Apr-12.
 */
public class Mnemonics {
    private List<Mnemonic> mnemonics;

    public Mnemonics() {
        this.mnemonics = new ArrayList<>();
    }

    @XmlElement(name = "mnemonic")
    public List<Mnemonic> getMnemonics() {
        return mnemonics;
    }

    public void setMnemonics(List<Mnemonic> mnemonics) {
        this.mnemonics = mnemonics;
    }
}
