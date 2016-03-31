package net.technolords.tools.data.method;

import net.technolords.tools.data.annotation.AnnotationUsingBoolean;
import net.technolords.tools.data.annotation.AnnotationUsingDefault;

/**
 * Created by Technolords on 2016-Mar-31.
 */
public class MethodTestWithAnnotations {

    @AnnotationUsingBoolean(description = "Charming sample", enabled = true)
    public static void sample(@AnnotationUsingDefault(description = "parameter") String value) {
        if(value != null) {
            value.concat(" added content");
        }
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
