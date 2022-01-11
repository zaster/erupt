package xyz.erupt.annotation.sub_erupt;

import java.beans.Transient;

import xyz.erupt.annotation.config.AutoFill;
import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.expr.ExprBool;
import xyz.erupt.annotation.fun.ActionHandler;

public @interface ModalButton {
    @Deprecated
    @AutoFill("T(Integer).toString(#item.label().hashCode())")
    String code() default "";
    String label() default "";
    String type() default "";   
    String shape() default "";
    @Transient
    ExprBool show() default @ExprBool;
    String ifExpr() default "";
    boolean autoLoading() default true;
    boolean danger() default false;
    @Transient
    @Comment("type为ERUPT时可用，操作按钮点击后，后台处理逻辑")
    Class<? extends ActionHandler> handler() default ActionHandler.class;
    String[] param();
}
