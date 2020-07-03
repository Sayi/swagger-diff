package com.deepoove.swagger.diff.cli;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;

@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target({FIELD})
public @interface Regex {

    String value() default "";

}
