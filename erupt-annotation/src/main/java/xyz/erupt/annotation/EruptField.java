package xyz.erupt.annotation;

import java.beans.Transient;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.annotation.sub_field.Edit;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface EruptField {

    @Comment("表格列配置")
    STColumn[] columns() default {};

    @Comment("编辑组件配置")
    Edit edit() default @Edit(title = "");

    @Transient
    @Comment("显示顺序，默认按照字段排列顺序排序")
    int sort() default 1000;

    @ToMap(key = "key")
    @Comment("自定义扩展参数")
    KV[] params() default {};
}
