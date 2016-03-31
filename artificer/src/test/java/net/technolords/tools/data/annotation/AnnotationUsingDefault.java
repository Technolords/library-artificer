package net.technolords.tools.data.annotation;

/**
 * Created by Technolords on 2016-Mar-31.
 */
public @interface AnnotationUsingDefault {
    String description();
    String[] values() default {"alpha", "beta"};
}
