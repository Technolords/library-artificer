package net.technolords.tools.artificer.analyser.dotclass;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.technolords.tools.artificer.domain.dotclass.Constant;
import net.technolords.tools.artificer.domain.dotclass.ConstantInfo;
import net.technolords.tools.artificer.domain.dotclass.ConstantPool;

/**
 * Created by Technolords on 2015-Dec-17.
 */
public class ConstantPoolAnalyser {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConstantPoolAnalyser.class);

    public static Set<String> extractReferencedClasses(ConstantPool constantPool) {
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
    protected static String extractReferencedClassFromConstantOfTypeClass(ConstantPool constantPool, Constant constant) {
        ConstantInfo constantInfo = constant.getConstantInfoList().get(0);
        LOGGER.debug("ConstantInfo description: " + constantInfo.getDescription());
        if("name_index".equals(constantInfo.getDescription())) {
            Constant nameAndStringConstant = findConstantByIndex(constantPool, constantInfo.getIntValue());
            if(nameAndStringConstant != null) {
                // Found constant associated with name_index, which should be of type Utf8
                return extractStringValueFromConstantUtf8(nameAndStringConstant);
            }
        }
        return null;
    }

    public static Constant findConstantByIndex(ConstantPool constantPool, int index) {
        for(Constant constant : constantPool.getConstants()) {
            if(constant.getConstantPoolIndex() == index) {
                return constant;
            }
        }
        return null;
    }

    protected static String extractStringValueFromConstantUtf8(Constant constant) {
        if("Utf8".equals(constant.getType())) {
            ConstantInfo constantInfo = constant.getConstantInfoList().get(0);
            return constantInfo.getStringValue();
        }
        return null;
    }

    public static String extractStringValueByConstantPoolIndex(ConstantPool constantPool, int index) {
        Constant constant = findConstantByIndex(constantPool, index);
        if(constant != null) {
            return extractStringValueFromConstantUtf8(constant);
        }
        return "";
    }

}
