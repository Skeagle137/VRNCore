package net.skeagle.vrncore.commands;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface VRNCommand {
    String[] cmd();

    String[] sub() default {};

    String desc();

    String perm() default "";
}
