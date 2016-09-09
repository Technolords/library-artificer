package net.technolords.tools.data.annotation;

/**
 * Created by Technolords on 2016-Mar-15.
 */
public @interface AnnotationUsingEnum {

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    String description();
    Priority value();
}
