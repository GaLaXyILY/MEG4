package com.ticxo.modelengine.core.mythic.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MythicCondition {
   String name() default "";

   String[] aliases() default {};

   String author() default "";

   String description() default "";

   String version() default "4.0";

   boolean premium() default false;

   String namespace() default "meg";
}
