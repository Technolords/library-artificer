package net.technolords.tools.data.field;

import net.technolords.tools.data.annotation.AnnotationUsingAnnotation;
import net.technolords.tools.data.annotation.AnnotationUsingArray;
import net.technolords.tools.data.annotation.AnnotationUsingBoolean;
import net.technolords.tools.data.annotation.AnnotationUsingByte;
import net.technolords.tools.data.annotation.AnnotationUsingChar;
import net.technolords.tools.data.annotation.AnnotationUsingClass;
import net.technolords.tools.data.annotation.AnnotationUsingEnum;
import net.technolords.tools.data.annotation.AnnotationUsingInteger;

import javax.xml.bind.annotation.XmlAttribute;
import java.lang.reflect.Member;
import java.util.LinkedList;

/**
 * Created by Technolords on 2016-Mar-14.
 */
public class FieldTestWithAnnotations {

    @XmlAttribute(name = "test", required = true)
    @Deprecated
    private Member annotation1;

    @AnnotationUsingAnnotation(description = "Annotation using an annotation", newValue = "First entry")
    @AnnotationUsingAnnotation(description = "Annotation using an annotation", newValue = "Second entry")
    private String annotation2;

    @AnnotationUsingArray(description = "Annotation using an array", names = {"alpha", "beta"}, numbers = {1, 2, 4} )
    private String annotation3;

    @AnnotationUsingBoolean(description = "Annotation using a boolean", enabled = true)
    private String annotation4;

    @AnnotationUsingByte(description = "Annotation using a byte", unit = 8)
    private String annotation5;

    @AnnotationUsingChar(description = "Annotation using a char", unit = 'm')
    private String annotation6;

    @AnnotationUsingClass(description = "Annotation using a class", sample = LinkedList.class)
    private String annotation7;

    @AnnotationUsingEnum(description = "Annotation using an enum", value = AnnotationUsingEnum.Priority.HIGH)
    private String annotation8;

    // TODO: float

    @AnnotationUsingInteger(description = "Annotation using an integer", value = 100)
    private String annotation10;

    // TODO: long
    // TODO: short
    // TODO: double
    // TODO: String

}
