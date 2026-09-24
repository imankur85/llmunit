package io.llmunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Metrics.class)
public @interface Metric {

    MetricEnum metric();

    double threshold() default 0.5;

    String key() default "";

    String type() default "string";
}