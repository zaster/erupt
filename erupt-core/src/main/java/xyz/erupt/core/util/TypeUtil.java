package xyz.erupt.core.util;

import java.util.Arrays;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author YuePeng
 * date 2018-11-01.
 */
public class TypeUtil {
    private static final String[] SIMPLE_JPA_TYPE = {
            "byte", "short", "int", "integer", "long", "float", "double", "boolean", "char", "String", "date", "character", "char"
    };
    private static final String[] NUMBER_TYPE = {
            "short", "int", "integer", "long", "float", "double", "bigdecimal", "biginteger"
    };

    /**
     * 将未知类型转换为目标类型
     */
    public static Object typeStrConvertObject(Object obj, Class<?> targetType) {
        String str = obj.toString();
        if (NumberUtils.isCreatable(str)) {
            if (str.endsWith(".0")) {  //处理gson序列化数值多了一个0
                str = str.substring(0, str.length() - 2);
            }
        }
        if (int.class == targetType || Integer.class == targetType) {
            return Integer.valueOf(str);
        } else if (short.class == targetType || Short.class == targetType) {
            return Short.valueOf(str);
        } else if (long.class == targetType || Long.class == targetType) {
            return Long.valueOf(str);
        } else if (float.class == targetType || Float.class == targetType) {
            return Float.valueOf(str);
        } else if (double.class == targetType || Double.class == targetType) {
            return Double.valueOf(str);
        } else if (boolean.class == targetType || Boolean.class == targetType) {
            return Boolean.valueOf(str);
        } else {
            return str;
        }
    }

    public static void simpleNumberTypeArrayToObject(Object obj, String type,ArrayNode array) {

        if (int.class.getSimpleName().equals(type)) {
            for (Integer i : (int[]) obj) {
                array.add(i);
            }
        } else if (short.class.getSimpleName().equals(type)) {
            for (Short i : (short[]) obj) {
                array.add(i);
            }
        } else if (long.class.getSimpleName().equals(type)) {
            for (Long i : (long[]) obj) {
                array.add(i);
            }
        } else if (float.class.getSimpleName().equals(type)) {
            for (Float i : (float[]) obj) {
                array.add(i);
            }
        } else if (double.class.getSimpleName().equals(type)) {
            for (Double i : (double[]) obj) {
                array.add(i);
            }
        }
    }
    public static void simpleNumberTypeToObject(Object obj, String type,ObjectNode onode,String fieldName) {

        if (int.class.getSimpleName().equals(type)) {
            onode.put(fieldName, (int)obj);
        } else if (short.class.getSimpleName().equals(type)) {
            onode.put(fieldName, (short)obj);
        } else if (long.class.getSimpleName().equals(type)) {
            onode.put(fieldName, (long)obj);
        } else if (float.class.getSimpleName().equals(type)) {
            onode.put(fieldName, (float)obj);
        } else if (double.class.getSimpleName().equals(type)) {
            onode.put(fieldName, (double)obj);
        }
    }
    // 判断实体类字段返回值是否为基本类型（包括String与date）
    public static boolean isFieldSimpleType(String typeName) {
        return Arrays.asList(SIMPLE_JPA_TYPE).contains(typeName.toLowerCase());
    }

    public static boolean isNumberType(String typeName) {
        return Arrays.asList(NUMBER_TYPE).contains(typeName.toLowerCase());
    }


}
