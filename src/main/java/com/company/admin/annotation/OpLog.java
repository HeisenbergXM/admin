package com.company.admin.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpLog {

    String value() default "";

    LogType type() default LogType.OTHER;

    boolean saveParams() default true;

    boolean saveResult() default false;

    enum LogType {
        OTHER, INSERT, UPDATE, DELETE, SELECT
    }
}
