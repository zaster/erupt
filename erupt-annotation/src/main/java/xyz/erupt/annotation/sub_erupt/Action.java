package xyz.erupt.annotation.sub_erupt;

import java.beans.Transient;

import xyz.erupt.annotation.config.AutoFill;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;

public @interface Action {
    @Deprecated
    @AutoFill("T(Integer).toString(#item.text().hashCode())")
    String code() default "";
    String name() default "";
    String text() default "";
    String tooltip() default "";
    String icon() default "";
    String ifExpr() default "";

    Relation[] relations() default @Relation;

    @Transient
    ExprBool show() default @ExprBool;
    String pop() default "";
    String iifBehavior() default "hide";
    
    @Comment("操作的类型，默认为模态框")
    STColumnButtonType type() default STColumnButtonType.LINK;

    @Comment("按钮依赖本表数据模式")
    RowMode rowMode() default RowMode.SINGLE;
    @Comment("对象类型")
    Class<?> contentErupt() default void.class;
    @Comment("组件类型")
    ContentType contentType() default ContentType.TABLE;
    @Comment("表格的选择模式")
    SelectMode selectMode() default SelectMode.CHECKBOX;
    FormMode formMode() default FormMode.EDIT;
    ModalButton[] buttons() default{};
    Tpl tpl() default @Tpl(path = "");
    enum ContentType {
        FORM,
        TABLE,
        IMPORT,
        TPL,
        NONE;
        private final String value;
        private ContentType(){
            this.value = this.name();
        }
        private ContentType(String type) {
            this.value = type;
        }
        public String getValue() {
            return value.toLowerCase();
        }
        @Override
        public String toString() {
            return value.toLowerCase();
        }
    }
    enum FormMode{
        ADD,EDIT
    }

    enum SelectMode {
        RADIO,
        CHECKBOX
    }
    
    enum RowMode {
        SINGLE,
        MULTI,
        NONE
    }
    enum STColumnButtonType {
        NONE,DEL, LINK , MODEL , STATIC , DRAWER;
        private final String value;
        private STColumnButtonType(){
            this.value = this.name();
        }
        private STColumnButtonType(String type) {
            this.value = type;
        }
        public String getValue() {
            return value.toLowerCase();
        }
        @Override
        public String toString() {
            return value.toLowerCase();
        }
    }
    
}
