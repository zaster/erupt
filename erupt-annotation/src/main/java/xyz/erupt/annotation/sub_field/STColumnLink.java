package xyz.erupt.annotation.sub_field;

public @interface STColumnLink {
    String language() default"";
    String icon() default "";
    String className() default "text-center";
    ViewType viewType();
}
