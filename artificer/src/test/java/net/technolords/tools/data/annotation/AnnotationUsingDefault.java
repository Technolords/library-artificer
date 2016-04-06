package net.technolords.tools.data.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Technolords on 2016-Mar-31.
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {
        ElementType.ANNOTATION_TYPE,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PACKAGE,
        ElementType.PARAMETER,
        ElementType.TYPE,
        ElementType.TYPE_PARAMETER,
})
public @interface AnnotationUsingDefault {
    enum Direction { NORTH, EAST, SOUTH, WEST}
    Direction direction() default Direction.NORTH;
    String description();
    Class source() default Object.class;
    Class<? extends Number>[] numbers() default { Byte.class, Integer.class };
    String[] values() default {"alpha", "beta"};
}
