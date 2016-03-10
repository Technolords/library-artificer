package net.technolords.tools.artificer.analyser.dotclass;

import net.technolords.tools.artificer.domain.dotclass.Constant;
import net.technolords.tools.artificer.domain.dotclass.ConstantInfo;
import net.technolords.tools.artificer.domain.dotclass.ConstantPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Technolords on 2015-Dec-17.
 */
public class ConstantPoolAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantPoolAnalyser.class);

    public Set<String> extractReferencedClasses(ConstantPool constantPool) {
        Set<String> referencedClasses = new HashSet<>();
        if(constantPool == null) {
            return referencedClasses;
        }
        String referencedClass;
        for(Constant constant : constantPool.getConstants()) {
            if("Class".equals(constant.getType())) {
                referencedClass = extractReferencedClassFromConstantOfTypeClass(constantPool, constant);
                if(referencedClass != null) {
                    LOGGER.debug("Adding referenced class: " + referencedClass);
                    referencedClasses.add(referencedClass);
                }
            }
        }
        return referencedClasses;
    }

    /**
     * Auxiliary method to extract the referenced class from a constant of type class. The constant contains a list
     * of constant info elements, however, given the specification there is only one. This constant info has a
     * name_index which points to another constant which is of type Utf8 and actually represents the referenced
     * class. For example:
     *
     * constant-pool-index: 1
     *  -> constant-type: Class
     *      -> constant-info-list: [0]
     *          -> description: name_index
     *          -> int-value: 5
     * constant-pool-index: 5
     *  -> constant-type: Utf8
     *      -> constant-info-list: [0]
     *          -> description: string_value
     *          -> string-value: net/technolords/Sample
     *
     * @param constantPool
     *  The ConstantPool reference associated with the extraction of the referenced class.
     * @param constant
     *  The Constant reference associated with the extraction of the referenced class.
     * @return
     *  The referenced class, or null when not found.
     */
    protected String extractReferencedClassFromConstantOfTypeClass(ConstantPool constantPool, Constant constant) {
        ConstantInfo constantInfo = constant.getConstantInfoList().get(0);
        LOGGER.debug("ConstantInfo description: " + constantInfo.getDescription());
        if("name_index".equals(constantInfo.getDescription())) {
            Constant nameAndStringConstant = this.findConstantByIndex(constantPool, constantInfo.getIntValue());
            if(nameAndStringConstant != null) {
                // Found constant associated with name_index, which should be of type Utf8
                return this.extractStringValueFromConstantUtf8(nameAndStringConstant);
            }
        }
        return null;
    }

    protected Constant findConstantByIndex(ConstantPool constantPool, int index) {
        LOGGER.debug("Finding constant with index: " + index);
        for(Constant constant : constantPool.getConstants()) {
            if(constant.getConstantPoolIndex() == index) {
                return constant;
            }
        }
        return null;
    }

    protected String extractStringValueFromConstantUtf8(Constant constant) {
        if("Utf8".equals(constant.getType())) {
            ConstantInfo constantInfo = constant.getConstantInfoList().get(0);
            return constantInfo.getStringValue();
        }
        return null;
    }

}
