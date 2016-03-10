package net.technolords.tools.artificer.analyser.dotclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Technolords on 2016-Mar-08.
 */
public class SignatureAnalyser {
    private static Logger LOGGER = LoggerFactory.getLogger(SignatureAnalyser.class);

    private static String regexForClassReference = "L(.*);";
    private static String regexForCollections = "([a-zA-Z/]*)<(.*)>";

    private static Pattern patternForClassReference = Pattern.compile(regexForClassReference);
    private static Pattern patternForCollections = Pattern.compile(regexForCollections);

    /**
     * 'Signatures' are specified using a grammar which follows the notation of:
     *
     *  Term            Type        Interpretation
     *  B               byte        signed byte
     *  C               char        Unicode character code in the Basic Multilingual Plane, encoded with UTF-16
     *  D               double      double-precision floating-point value
     *  F               float       single-precision floating-point value
     *  I               int         integer
     *  J               long        long integer
     *  L ClassName ;   reference   an instance of class ClassName
     *  S               short       signed short
     *  Z               boolean     true of false
     *  [               reference   one array dimension
     *
     *  V                           void descriptor
     *
     *  In addition to that notation:
     *
     *
     * @param signature
     *
     * @return
     */
    public static Set<String> referencedClasses(String signature) {
        Set<String> referencedClasses = new HashSet<>();
        Matcher matcher = patternForClassReference.matcher(signature);
        if(matcher.matches()) {
            LOGGER.trace("Got a match 'L;', with groupCount: " + matcher.groupCount());
//            String group = matcher.group(1);
            LOGGER.trace("Found group: " + matcher.group(1));
            Matcher nested = patternForCollections.matcher(matcher.group(1));
            if(nested.matches()) {
                LOGGER.trace("Found '<>', with groupCount: " + nested.groupCount());
                LOGGER.debug("Adding referenced class: " + nested.group(1));
                referencedClasses.add(nested.group(1));
                LOGGER.trace("Found group 2: " + nested.group(2));
                referencedClasses.addAll(referencedClasses(nested.group(2)));
            } else {
                // No nested collections, so end state
                LOGGER.trace("Not found <>");
                LOGGER.debug("Adding referenced class: " + matcher.group(1));
                referencedClasses.add(matcher.group(1));
            }
        }
        return referencedClasses;
    }
}
