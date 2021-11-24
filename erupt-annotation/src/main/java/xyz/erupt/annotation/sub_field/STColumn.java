package xyz.erupt.annotation.sub_field;

import java.beans.Transient;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.ChoiceType;

/**
 * @author YuePeng
 * date 2018-09-28.
 */
public @interface STColumn {
    @Comment("列标题")
    String title();

    @Transient
    @Comment("列标题辅助信息")
    String desc() default "";
    
    @Comment("列显示类型")
    STColumnType type() default STColumnType.TEXT;
    
    @Comment("修饰类型为实体类对象时必须指定列名")
    String index() default "";
    String render() default "";
    String renderTitle() default "";
    @Comment("列宽度（请指定单位如：%,px）")
    String width() default "";
    @Comment("样式类名")
    String className() default "";
    int colSpan() default 0;
    boolean sort() default false;
    String numberDigits() default "";
    String dateFormat() default "yyyy-MM-dd HH:mm";
    boolean exported() default true;
    boolean resizable() default true;
    String safeType() default "safeHtml";
    @Comment("格式化表格列值，前端使用eval方法解析，支持变量：" +
            "1、item    （表格整行数据）" +
            "2、item.xxx（数据行中某一列的值）" +
            "3、value   （当前列值）")
    String template() default "";

    @Transient
    @Comment("选项配置")
    ChoiceType choices() default @ChoiceType;
    @Transient
    @Comment("选项配置")
    BoolType bools() default @BoolType;
	boolean show() default true;

    STColumnLink link() default @STColumnLink;
}
