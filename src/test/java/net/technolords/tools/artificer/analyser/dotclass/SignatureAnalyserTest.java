package net.technolords.tools.artificer.analyser.dotclass;

import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import junit.framework.Assert;

/**
 * Created by Technolords on 2016-Mar-08.
 */
public class SignatureAnalyserTest {
    private Logger LOGGER = LoggerFactory.getLogger(getClass());

    /**
     * Auxiliary method to declare a data set to support byte code parsing of fields inside Classes. The data set
     * is represented in a multi-dimensional array, where each entry represents a single set. An entry is specified
     * with three elements, each meaning:
     *
     *  [0] : The java class containing fields
     *  [1] : The expected number of fields
     *  [2] : The expected Set of referenced classes
     *
     * @return
     *  The data set.
     */
    @DataProvider(name = "dataSetWithSignatures", parallel = true)
    public Object[][] dataSet() {
        Set<String> expectedReferencedClassesForEntry1 = new HashSet<>();
        expectedReferencedClassesForEntry1.add("java/util/List");
        expectedReferencedClassesForEntry1.add("java/util/Set");
        expectedReferencedClassesForEntry1.add("java/util/Map");

        Set<String> expectedReferencedClassesForEntry2 = new HashSet<>();
        expectedReferencedClassesForEntry2.add("java/util/List");
        expectedReferencedClassesForEntry2.add("java/lang/Integer");

        Set<String> expectedReferencedClassesForEntry3 = new HashSet<>();
        expectedReferencedClassesForEntry3.add("java/util/List");

        Set<String> expectedReferencedClassesForEntry4 = new HashSet<>();
        expectedReferencedClassesForEntry4.add("java/nio/file/Path");

        return new Object[][] {
                // <K:Ljava/lang/Object;U:Ljava/lang/Object;>()Ljava/util/stream/Collector<Ljava/util/Map$Entry<TK;TU;>;*Ljava/util/Map<TK;TU;>;>;
                // (Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;
                // (Ljava/util/List<-Lnet/technolords/tools/data/method/MethodTestWithRegularMethods;>;)V
                // <T:Ljava/lang/Object;>([TT;)Ljava/util/ArrayList<TT;>;
                // ([Ljava/lang/Object;)Ljava/util/ArrayList;
            { "Ljava/util/List<Ljava/util/Set<Ljava/util/Map<**>;>;>;", 3, expectedReferencedClassesForEntry1 },
            { "Ljava/util/List<Ljava/lang/Integer;>;", 2, expectedReferencedClassesForEntry2 },
            { "Ljava/util/List;", 1, expectedReferencedClassesForEntry3 },
            { "(Ljava/nio/file/Path;)V", 1, expectedReferencedClassesForEntry4 },
        };
    }

    @Test (dataProvider = "dataSetWithSignatures")
    public void testReferencedClasses(String signature, int totalReferencedClasses, Set<String> expectedReferencedClasses) {
        Set<String> referencedClasses = new HashSet<>();
        SignatureAnalyser.referencedClasses(referencedClasses, signature);
        // Verify the size of referenced classes
        Assert.assertEquals("Expected total referenced classes: " + totalReferencedClasses + ", but was: " + referencedClasses.size(),
            totalReferencedClasses, referencedClasses.size());
        // Verify the referenced classes
        for(String referencedClass : referencedClasses) {
            Assert.assertTrue("Expected the referenced class to be part of the expected set, but " + referencedClass +
                " was not", expectedReferencedClasses.contains(referencedClass));
        }
    }
}