package net.technolords.tools.data.annotation;

import java.util.List;

/**
 * Created by Technolords on 2016-Mar-15.
 */
public @interface AnnotationUsingClass {
    String description();
    Class<? extends List> sample();
}
