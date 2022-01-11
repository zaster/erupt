package xyz.erupt.annotation.sub_erupt;

public @interface Relation {
    String key() default "id";
    String value() default "";
    String recordKey()default "id";
}
