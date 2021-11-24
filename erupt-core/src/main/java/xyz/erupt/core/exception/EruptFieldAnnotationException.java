package xyz.erupt.core.exception;

import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.core.view.EruptFieldModel;

/**
 * @author YuePeng
 * date 11/1/18.
 */
public class EruptFieldAnnotationException extends RuntimeException {

    public EruptFieldAnnotationException(String message) {
        super(message);
    }

    public static void validateEruptFieldInfo(EruptFieldModel eruptFieldModel) {
        Edit edit = eruptFieldModel.getEruptField().edit();
        switch (edit.type()) {
            case REFERENCE_TREE:
            case REFERENCE_TABLE:
                if (eruptFieldModel.getEruptField().columns().length > 0) {
                    for (STColumn column : eruptFieldModel.getEruptField().columns()) {
                        if ("".equals(column.index())) {
                            throw ExceptionAnsi.styleEruptFieldException(eruptFieldModel, "@Column注解" + column.title() + "必须指定index值");
                        }
                    }
                }
                break;
        }
    }
}
