package xyz.erupt.annotation.sub_field;

public @interface STIcon {
    String type() default "";
    String theme() default "outline";

    boolean spin() default false;

}
