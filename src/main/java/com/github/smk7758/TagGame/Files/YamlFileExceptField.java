package com.github.smk7758.TagGame.Files;

import static java.lang.annotation.ElementType.*;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(FIELD)
@Retention(RetentionPolicy.RUNTIME)
/**
 * If you don't want to move on loadField().
 *
 * @author smk7758
 */
public @interface YamlFileExceptField {
}
