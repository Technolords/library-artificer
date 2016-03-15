package net.technolords.tools.data;

import net.technolords.tools.data.annotation.AnnotationUsingAnnotation;

import javax.xml.bind.annotation.XmlAttribute;
import java.lang.reflect.Member;

/**
 * Created by Technolords on 2016-Mar-14.
 */
public class FieldTestWithAnnotations {

    @XmlAttribute(name = "test", required = true)
    @Deprecated
    private Member member;

    @AnnotationUsingAnnotation(
        description = "Annotation using an annotation",
        oldValue = "Should not use",
        newValue = "Good stuff"
    )
    private String annotation1;

    // TODO: use different annotations:
    // In other words, the union describes it is one of the following types:
    // - const_value_index
    // - enum_const_value
    // - class_info_index
    // - annotation_value
    // - array_value

    /*
        Named and unnamed annotations:

        @SupressWarnings(value = "unchecked")
        vs
        @SupressWarnings("unchecked")
        This can be done when there is just one element named value, then the name can be omitted

        The annotation type can be one of the types that are defined in the 'java.lang' or 'java.lang.annotation' package

        Where annotations can be used:
        - class instance creation expression:
            new @Interned MyObject();
        - type cast:
            myString = (@NonNull String) str;
        - implements clause:
            class UnmodifiableList<T> implements @Readonly List<@ReadOnly T> {...}
        - thrown exception declaration:
            void monitorTemperature() throws @Critical TemperatureException {...}

        This form of annotation is called a 'type annotation'.
     */
}
