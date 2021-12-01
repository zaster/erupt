package xyz.erupt.jpa.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;

/**
 * @author YuePeng date 2018-11-05.
 */
@Slf4j
public class EruptJpaUtils {

    public static final String L_VAL_KEY = "l_";

    public static final String R_VAL_KEY = "r_";

    public static final String PERCENT = "%";

    public static final String AND = " and ";

    public static final String AS = " as ";

    public static final String LEFT_JOIN = " left outer join ";

    public static <TT>Set<String> getEruptColJpaKeys(EruptModel eruptModel) {
        Set<String> cols = new HashSet<>();
        String eruptNameSymbol = eruptModel.getEruptName() + ".";
        cols.add(eruptNameSymbol + eruptModel.getErupt().primaryKeyCol() + AS + eruptModel.getErupt().primaryKeyCol());
        eruptModel.getEruptFieldModels().forEach(field -> {
            Field f = field.getField();
            if (null != f.getAnnotation(OneToMany.class)
                    || null != f.getAnnotation(ManyToMany.class)
                    || null != f.getAnnotation(Transient.class)) {
                return;
            }
            if (field.getEruptField()!=null) {
                cols.add(eruptNameSymbol + field.getFieldName()) ;
            }
        });
        return cols;
    }

    // erupt 注解信息映射成hql语句
    public static String generateEruptJpaHql(EruptModel eruptModel, String cols, EruptQuery query, boolean countSql) {
        StringBuilder hql = new StringBuilder();
        if (StringUtils.isNotBlank(cols)) {
            hql.append("select ").append(cols).append(" from ").append(eruptModel.getEruptName()).append(AS)
                    .append(eruptModel.getEruptName());
            hql.append(generateEruptJoinHql(eruptModel));

        } else {
            hql.append("from ").append(eruptModel.getEruptName());
        }
        hql.append(geneEruptHqlCondition(eruptModel, query.getConditions(), query.getConditionStrings()));
        if (!countSql) {
            hql.append(geneEruptHqlOrderBy(eruptModel, query.getOrderBy()));
        }
        return hql.toString();
    }

    public static <TT> String generateEruptJoinHql(EruptModel eruptModel) {
        StringBuilder sb = new StringBuilder();
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                sb.append(LEFT_JOIN).append(eruptModel.getEruptName()).append('.').append(field.getName()).append(AS)
                        .append(field.getName());

            }
            EruptFieldModel model = eruptModel.getEruptFieldMap().get(field.getName());
            if (model != null && model.getEruptField().columns() != null) {
                STColumn[] columns = model.getEruptField().columns();
                Set<String> pathSet = new HashSet<String>();
                for (STColumn column : columns) {
                    if (column.index().length() != 0 && column.index().contains(".")) {
                        String path = eruptModel.getEruptName() + "." + field.getName() + "."
                                + column.index().substring(0, column.index().lastIndexOf("."));
                        if (!pathSet.contains(path)) {
                            sb.append(LEFT_JOIN).append(path).append(AS)
                                    .append(path.substring(path.lastIndexOf(".") + 1));
                            pathSet.add(path);
                        }

                    }
                }
                pathSet.clear();
            }
        });
        return sb.toString();
    }

    public static <TT> String geneEruptHqlCondition(EruptModel eruptModel, List<Condition> conditions,
            List<String> customCondition) {
        StringBuilder hql = new StringBuilder();
        hql.append(" where 1 = 1 ");
        // condition
        if (null != conditions) {
            for (Condition condition : conditions) {
                String _key = EruptJpaUtils.completeHqlPath(eruptModel.getEruptName(), condition.getKey());
                String paramKey = condition.getKey().replace("\\.", "_");
                switch (condition.getExpression()) {

                    case LIKE:
                        hql.append(EruptJpaUtils.AND).append(_key).append(" like :").append(paramKey);
                        break;
                    case RANGE:
                        hql.append(EruptJpaUtils.AND).append(_key).append(" between :").append(L_VAL_KEY)
                                .append(paramKey).append(" and :").append(R_VAL_KEY)
                                .append(paramKey);
                        break;
                    case IN:
                        hql.append(EruptJpaUtils.AND).append(_key).append(" in (:").append(paramKey)
                                .append(")");
                        break;
                    default:
                        hql.append(EruptJpaUtils.AND).append(_key).append("=:").append(paramKey);
                        break;
                    }
               
            }
        }
        AnnotationUtil.switchFilterConditionToStr(eruptModel.getErupt().filter()).forEach(it -> {
            if (StringUtils.isNotBlank(it))
                hql.append(AND).append(it);
        });
        Optional.ofNullable(customCondition).ifPresent(it -> it.forEach(str -> {
            if (StringUtils.isNotBlank(str))
                hql.append(EruptJpaUtils.AND).append(str);
        }));
        return hql.toString();
    }

    public static String geneEruptHqlOrderBy(EruptModel eruptModel, String orderBy) {
        if (StringUtils.isBlank(orderBy)){
            if (StringUtils.isAllBlank(eruptModel.getErupt().orderBy()))
                return "";
            orderBy = eruptModel.getErupt().orderBy();
        }
        StringBuilder sb = new StringBuilder(" order by ");
        List<String> orderList = new ArrayList<String>();
        ///Stream.of(orderBy.split(",")).collect(ArrayList<String>::new, (List<String>list,String str)->{list.add();}, List<String>::addAll)));
        for(String str: orderBy.split(",")) {
            orderList.add(completeHqlPath(eruptModel.getEruptName(), str));
        }
        sb.append(String.join(",", orderList));

        
        return sb.toString();
    }

    // 在left join的情况下要求必须指定表信息，通过此方法生成；
    public static String completeHqlPath(String eruptName, String hqlPath) {
        return eruptName + "." + hqlPath;
    }

}
