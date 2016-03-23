package net.technolords.tools.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by Technolords on 2016-Mar-22.
 */
public class FieldTestWithTypeAnnotations {

    @Invisible Object reference1;

    @Target({ElementType.FIELD, ElementType.TYPE_USE})
    @interface Invisible { }

    // http://types.cs.washington.edu/jsr308/specification/java-annotation-design.html
    // view-source:http://mail.openjdk.java.net/pipermail/jls-jvms-spec-comments/attachments/20150314/ed7d53a6/JSE8TypeAnnotationExample.java
}
