package net.technolords.tools.data.annotation;

import java.lang.annotation.Documented;

/**
 * Created by Technolords on 2016-Mar-15.
 */
@Documented
public @interface AnnotationUsingAnnotation {
    String description();
    @Deprecated String oldValue();
    String newValue();
}
