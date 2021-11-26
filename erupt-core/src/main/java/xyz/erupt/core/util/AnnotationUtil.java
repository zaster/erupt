package xyz.erupt.core.util;

import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.SneakyThrows;
import xyz.erupt.annotation.config.AutoFill;
import xyz.erupt.annotation.config.EruptProperty;
import xyz.erupt.annotation.config.Match;
import xyz.erupt.annotation.config.ToMap;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.EditTypeMapping;
import xyz.erupt.annotation.sub_field.EditTypeSearch;

/**
 * @author YuePeng
 * date 2019-02-28.
 */
public class AnnotationUtil {

    private static final String[] ANNOTATION_NUMBER_TYPE = {"short", "int", "long", "float", "double"};

    private static final String[] ANNOTATION_STRING_TYPE = {"String", "byte", "char"};

    private static final String EMPTY_ARRAY = "[]";

    private static final ExpressionParser parser = new SpelExpressionParser();

    private static final String VALUE_VAR = "value";

    private static final String ITEM_VAR = "item";

    private static final ObjectMapper mapper = new ObjectMapper();

    @SneakyThrows
    public static ObjectNode annotationToJsonByReflect(Annotation annotation) {
        return annotationToJson(annotation);
    }

    private static ObjectNode annotationToJson(Annotation annotation)
            throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        ObjectNode jsonObject = mapper.createObjectNode();
        for (Method method : annotation.annotationType().getDeclaredMethods()) {
            Transient tran = method.getAnnotation(Transient.class);
            if (null != tran && tran.value()) {
                continue;
            }
            String methodName = method.getName();
            EruptProperty eruptProperty = method.getAnnotation(EruptProperty.class);
            if (null != eruptProperty && !AnnotationConst.EMPTY_STR.equals(eruptProperty.alias())) {
                methodName = eruptProperty.alias();
            }
            String returnType = method.getReturnType().getSimpleName();
            Object result = method.invoke(annotation);
            Match match = method.getAnnotation(Match.class);
            if (null != match) {
                EvaluationContext evaluationContext = new StandardEvaluationContext();
                evaluationContext.setVariable(VALUE_VAR, result);
                evaluationContext.setVariable(ITEM_VAR, annotation);
                Object r = parser.parseExpression(match.value()).getValue(evaluationContext);
                if (null == r || !(Boolean) r) {
                    continue;
                }
            }
            AutoFill autoFill = method.getAnnotation(AutoFill.class);
            if (null != autoFill) {
                EvaluationContext evaluationContext = new StandardEvaluationContext();
                if (AnnotationConst.EMPTY_STR.equals(result)) {
                    evaluationContext.setVariable(ITEM_VAR, annotation);
                    evaluationContext.setVariable(VALUE_VAR, result);
                    result = parser.parseExpression(autoFill.value()).getValue(evaluationContext);
                    AnnotationUtil.getAnnotationMap(annotation).put(methodName, result);
                }
            }
            if (returnType.endsWith(EMPTY_ARRAY)) {
                returnType = returnType.substring(0, returnType.length() - 2);
                ArrayNode jsonArray = mapper.createArrayNode();
                
                ToMap toMap = method.getAnnotation(ToMap.class);
                ObjectNode jsonMap = mapper.createObjectNode();
                //基本类型无法强转成Object类型数组，所以使用下面的方法进行处理
                if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    
                    TypeUtil.simpleNumberTypeArrayToObject(result, returnType, jsonArray);
                } else {
                    for (Object res : (Object[]) result) {
                        if (String.class.getSimpleName().equals(returnType)) {
                            jsonArray.add(res.toString());
                        } else if (char.class.getSimpleName().equals(returnType)) {
                            jsonArray.add((Character) res);
                        } else if (byte.class.getSimpleName().equals(returnType)) {
                            jsonArray.add((Character) res);
                        } else if (boolean.class.getSimpleName().equals(returnType)) {
                            jsonArray.add((Boolean) res);
                        } else if (Class.class.getSimpleName().equals(returnType)) {
                            jsonArray.add(((Class<?>) res).getSimpleName());
                        } else if (res.getClass().isEnum()) {
                            jsonArray.add(res.toString());
                        } else {
                            Annotation ann = (Annotation) res;
                            if (null != toMap) {
                                ObjectNode jo = annotationToJson((Annotation) res);
                                String key = ann.annotationType().getMethod(toMap.key()).invoke(res).toString();
                                jo.remove(toMap.key());
                                jsonMap.set(key, jo);
                            } else {
                                jsonArray.add(annotationToJson(ann));
                            }
                        }
                    }
                }
                if (null == toMap) {
                    jsonObject.set(methodName, jsonArray);
                } else {
                    if (jsonMap.size() > 0) {
                        jsonObject.set(methodName, jsonMap);
                    }
                }
            } else {
                if (Arrays.asList(ANNOTATION_STRING_TYPE).contains(returnType)) {
                    jsonObject.put(methodName, result.toString());
                } else if (Arrays.asList(ANNOTATION_NUMBER_TYPE).contains(returnType)) {
                    TypeUtil.simpleNumberTypeToObject(result, returnType, jsonObject, methodName);
                } else if (boolean.class.getSimpleName().equals(returnType)) {
                    jsonObject.put(methodName, (Boolean) result);
                } else if (method.getReturnType().isEnum()) {
                    jsonObject.put(methodName, result.toString());
                } else if (method.getReturnType().isAnnotation()) {
                    jsonObject.set(methodName, annotationToJson((Annotation) result));
                } else if (Class.class.getSimpleName().equals(returnType)) {
                    jsonObject.put(methodName, ((Class<?>) result).getSimpleName());
                }
            }
        }
        return jsonObject;
    }

//    @Deprecated
//    public static String annotationToJsonByReplace(String annotationStr) {
//        String convertStr = annotationStr
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.sub_attachment\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_edit\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.sub_view\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_field\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.sub_erupt\\.\\w+", "")
//                .replaceAll("@xyz\\.erupt\\.annotation\\.\\w+", "")
//                //屏蔽类信息
//                .replaceAll("class [a-zA-Z0-9.]+", "")
//                .replace("=,", "='',")
//                .replace("=)", "='')")
//                .replace("=", ":")
//                .replace("(", "{")
//                .replace(")", "}");
//        return new ObjectNode(convertStr).toString();
//    }

    @SneakyThrows
    public static Map<String, Object> getAnnotationMap(Annotation annotation) {
        InvocationHandler invocationHandler = Proxy.getInvocationHandler(annotation);
        Field field = invocationHandler.getClass().getDeclaredField("memberValues");
        field.setAccessible(true);
//        Unsafe unsafe = Unsafe.getUnsafe();
//        long offset = unsafe.objectFieldOffset(field);
//        Object o = unsafe.getObject(annotation,offset);
        return (Map) field.get(invocationHandler);
    }

    public static String switchFilterConditionToStr(Filter filter) {
        String condition = filter.value();
        if (!filter.conditionHandler().isInterface()) {
            FilterHandler ch = EruptSpringUtil.getBean(filter.conditionHandler());
            condition = ch.filter(condition, filter.params());
        }
        return condition;
    }

    public static List<String> switchFilterConditionToStr(Filter[] filters) {
        List<String> list = new ArrayList<>();
        for (Filter filter : filters) {
            String filterStr = AnnotationUtil.switchFilterConditionToStr(filter);
            if (StringUtils.isNotBlank(filterStr)) {
                list.add(filterStr);
            }
        }
        return list;
    }

    public static EditTypeMapping getEditTypeMapping(EditType editType) {
        try {
            return EditType.class.getDeclaredField(editType.name()).getAnnotation(EditTypeMapping.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static EditTypeSearch getEditTypeSearch(EditType editType) {
        try {
            return EditType.class.getDeclaredField(editType.name()).getAnnotation(EditTypeSearch.class);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return null;
        }
    }

}
