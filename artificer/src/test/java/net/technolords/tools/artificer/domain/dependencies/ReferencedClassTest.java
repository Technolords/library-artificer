package net.technolords.tools.artificer.domain.dependencies;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

/**
 * Created by Technolords on 2016-May-16.
 */
public class ReferencedClassTest {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Test
    public void testEqualsMethod() {
        Set<ReferencedClass> referencedClasses = new HashSet<>();
        ReferencedClass ref1 = createReferencedClass("java/lang/Object", ReferencedClass.ClassDomain.Standard);
        ReferencedClass ref2 = createReferencedClass("java/lang/Object", ReferencedClass.ClassDomain.Standard);
        referencedClasses.add(ref1);
        referencedClasses.add(ref2);
        LOGGER.debug("Total referenced classes: " + referencedClasses.size());
    }

    private ReferencedClass createReferencedClass(String packageWithClass, ReferencedClass.ClassDomain classDomain) {
        ReferencedClass referencedClass = new ReferencedClass();
        referencedClass.setPackageWithClass(packageWithClass);
        referencedClass.setClassDomain(classDomain);
        return referencedClass;
    }
}