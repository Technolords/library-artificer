package net.technolords.tools.artificer.analyser.dotclass;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Technolords on 2016-Mar-08.
 */
public class SignatureAnalyser {
    private static Logger LOGGER = LoggerFactory.getLogger(SignatureAnalyser.class);

    private static String regexForClassReference = "\\(?L(.*);\\)?.*";
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
     * TODO: decypher BNF:
     *  See also: https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.9.1
     *
     *  A Java type signature represents either a reference type or a primitive type of the Java programming language.
     *      JavaTypeSignature:
     *          ReferenceTypeSignature
     *          BaseType
     *
     *      BaseType:
     *          (one of) B C D F I J S Z
     *
     * A reference type signature represents a reference type of the Java programming language, that is, a class or
     * interface type, a type variable, or an array type.
     *
     *      - A class type signature represents a (possibly parameterized) class or interface type. A class type
     *        signature must be formulated such that it can be reliably mapped to the binary name of the class it
     *        denotes by erasing any type arguments and converting each . character to a $ character.
     *      - A type variable signature represents a type variable.
     *      - An array type signature represents one dimension of an array type.
     *
     *      ReferenceTypeSignature:
     *           ClassTypeSignature
     *           TypeVariableSignature
     *           ArrayTypeSignature
     *
     *      ClassTypeSignature:
     *          L [PackageSpecifier] SimpleClassTypeSignature {ClassTypeSignatureSuffix} ;
     *
     *      PackageSpecifier:
     *          Identifier / {PackageSpecifier}
     *
     *      SimpleClassTypeSignature:
     *          Identifier [TypeArguments]
     *
     *      TypeArguments:
     *          < TypeArgument {TypeArgument} >
     *
     *      TypeArgument:
     *          [WildcardIndicator] ReferenceTypeSignature
     *          *
     *
     *      WildcardIndicator:
     *          +
     *          -
     *
     *      ClassTypeSignatureSuffix:
     *          . SimpleClassTypeSignature
     *
     *      TypeVariableSignature:
     *          T Identifier ;
     *
     *      ArrayTypeSignature:
     *          [ JavaTypeSignature
     *
     * @param signature
     *
     * @return
     */
    public static void referencedClasses(Set<String> referencedClasses, String signature) {
        Matcher matcher = patternForClassReference.matcher(signature);
        if(matcher.matches()) {
            LOGGER.trace("Got a match 'L;', with groupCount: " + matcher.groupCount());
            LOGGER.trace("Found group: " + matcher.group(1));
            Matcher nested = patternForCollections.matcher(matcher.group(1));
            if(nested.matches()) {
                LOGGER.trace("Found '<>', with groupCount: " + nested.groupCount());
                if(referencedClasses.add(nested.group(1))) {
                    LOGGER.debug("Adding referenced class: " + nested.group(1));
                }
                LOGGER.trace("Found group 2: " + nested.group(2));
                referencedClasses(referencedClasses, nested.group(2));
            } else {
                // No nested collections, so end state
                LOGGER.trace("Not found <>");
                if(referencedClasses.add(matcher.group(1))) {
                    LOGGER.debug("Adding referenced class: " + matcher.group(1));
                }
            }
        }
    }
}
