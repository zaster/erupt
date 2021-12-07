package xyz.erupt.annotation.sub_erupt;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.sub_field.STIcon;

public @interface Action {
    String name() default "";
    String text() default "";
    String tooltip() default "";
    STIcon icon() default @STIcon;
    String ifExpr() default "";
    String pop() default "";
    String iifBehavior() default "hide";
    @Comment("操作的类型，默认为模态框")
    STColumnButtonType type() default STColumnButtonType.MODEL;
    @Comment("模态框组件类型")
    ContentMode contentMode() default ContentMode.TABLE;

    FormMode formMode() default FormMode.ADD;
    @Comment("按钮依赖本表数据模式")
    RowMode rowMode() default RowMode.SINGLE;

    @Comment("模态框中表格的选择模式")
    SelectMode contentSelectMode() default SelectMode.CHECKBOX;
    @Comment("模态框中的数据对象类型")
    Class<?> contentClass() default Void.class;

    String method() default "";

    Tpl tpl() default @Tpl(path = "");

    enum ContentMode {
        FORM,
        TABLE,
        IMPORT,
        TPL
    }
    enum FormMode{
        ADD,EDIT
    }
    enum RowMode {
        SINGLE,
        MULTI,
        NONE
    }
    enum SelectMode {
        RADIO,
        CHECKBOX
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
