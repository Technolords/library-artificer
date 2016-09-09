package net.technolords.tools.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;

/**
 * Created by Technolords on 2016-Mar-15.
 */
@Documented
@Repeatable(AnnotationUsingAnnotation.Schedules.class)
public @interface AnnotationUsingAnnotation {
    String description();
    @Deprecated String newValue();

    @Documented
    @interface Schedules {
        AnnotationUsingAnnotation[] value();
    }
}
