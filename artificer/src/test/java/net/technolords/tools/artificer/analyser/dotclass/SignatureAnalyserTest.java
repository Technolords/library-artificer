package net.technolords.tools.artificer.analyser.dotclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Set;

/**
 * Created by Technolords on 2016-Mar-08.
 */
public class SignatureAnalyserTest {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Test
    public void testReferencedClasses() {
        final String signature = "Ljava/util/List<Ljava/util/Set<Ljava/util/Map<**>;>;>;";
//        final String signature = "Ljava/util/List<Ljava/lang/Integer;>;";
//        final String signature = "Ljava/util/List;";
        Set<String> referencedClasses = SignatureAnalyser.referencedClasses(signature);
        LOGGER.debug("Total referenced classes: " + referencedClasses.size());
    }
}