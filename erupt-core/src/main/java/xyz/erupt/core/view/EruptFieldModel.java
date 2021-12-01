package xyz.erupt.core.view;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.constant.JavaType;
import xyz.erupt.annotation.fun.VLModel;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.exception.EruptFieldAnnotationException;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.CloneSupport;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.util.TypeUtil;

/**
 * @author YuePeng
 * date 2018-10-10.
 */
@Getter
@Setter
@Slf4j
public class EruptFieldModel extends CloneSupport<EruptFieldModel> {

    private transient EruptField eruptField;

    private transient Field field;

    private transient String fieldReturnName;

    private String fieldName;

    private ObjectNode eruptFieldJson;

    private Object value;

    private List<VLModel> choiceList;

    private List<String> tagList;

    public EruptFieldModel(Field field) {
       
        this.field = field;
        this.eruptField = field.getAnnotation(EruptField.class);
        Edit edit = eruptField.edit();
        this.fieldName = field.getName();
        //数字类型转换
        if (TypeUtil.isNumberType(field.getType().getSimpleName())) {
            this.fieldReturnName = JavaType.NUMBER;
        } else {
            this.fieldReturnName = field.getType().getSimpleName();
        }
        switch (edit.type()) {
            //如果是Tab类型视图，数据必须为一对多关系管理，需要用泛型集合来存放，所以取出泛型的名称重新赋值到fieldReturnName中
            case TAB_TREE:
            case TAB_TABLE_ADD:
            case TAB_TABLE_REFER:
            case CHECKBOX:
                this.fieldReturnName = ReflectUtil.getFieldGenericName(field).get(0);
                break;
        }
        this.eruptAutoConfig();
        this.eruptFieldJson = AnnotationUtil.annotationToJsonByReflect(this.eruptField);
        this.eruptFieldJson.remove("columns");
        //校验注解的正确性
        EruptFieldAnnotationException.validateEruptFieldInfo(this);
    }

    public static final String TYPE = "type";

    /**
     * erupt自动配置
     */
    private void eruptAutoConfig() {
        
        // edit auto
        if (StringUtils.isNotBlank(this.eruptField.edit().title()) && EditType.AUTO == this.eruptField.edit().type()) {
            Map<String, Object> editValues = AnnotationUtil.getAnnotationMap(this.eruptField.edit());
            //根据返回类型推断
            if (boolean.class.getSimpleName().equalsIgnoreCase(this.fieldReturnName)) {
                editValues.put(TYPE, EditType.BOOLEAN);
            } else if (Date.class.getSimpleName().equals(this.fieldReturnName) ||
                    LocalDate.class.getSimpleName().equals(this.fieldReturnName) ||
                    LocalDateTime.class.getSimpleName().equals(this.fieldReturnName)) {
                editValues.put(TYPE, EditType.DATE);
            } else if (JavaType.NUMBER.equals(this.fieldReturnName)) {
                editValues.put(TYPE, EditType.NUMBER);
            } else {
                editValues.put(TYPE, EditType.INPUT);
            }
            //根据属性名推断
            if (ArrayUtils.contains(AnnotationUtil.getEditTypeMapping(EditType.TEXTAREA).nameInfer(), this.fieldName.toLowerCase())) {
                editValues.put(TYPE, EditType.TEXTAREA);
            }
        }
    }

}
