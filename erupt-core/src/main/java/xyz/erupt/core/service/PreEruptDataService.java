package xyz.erupt.core.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.expr.Expr;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.invoke.ExprInvoke;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.DataHandlerUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.TreeModel;

@Service
public class PreEruptDataService {

    /**
     * 根据要素生成树结构
     *
     * @param eruptModel eruptModel
     * @param id         id
     * @param label      label
     * @param pid        parent id
     * @param query      查询对象
     * @return 树对象
     */
    public <T>Collection<TreeModel> geneTree(EruptModel eruptModel, String id, String label, String pid, Expr rootId, EruptQuery query) {
        List<Column> columns = new ArrayList<>();
        columns.add(new Column(id, AnnotationConst.ID));
        columns.add(new Column(label, AnnotationConst.LABEL));
        if (!AnnotationConst.EMPTY_STR.equals(pid)) {
            columns.add(new Column(pid, AnnotationConst.PID));
        }
        Collection<T> result = this.createColumnQuery(eruptModel, columns, query);
        String root = ExprInvoke.getExpr(rootId);
        List<TreeModel> treeModels = new ArrayList<>();
        result.forEach(it -> {
            try {
                treeModels.add(new TreeModel(
                            
                    PropertyUtils.getProperty(it,AnnotationConst.ID), 
                    PropertyUtils.getProperty(it,AnnotationConst.LABEL),  
                    PropertyUtils.getProperty(it,AnnotationConst.PID), root
                ));
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        });
        if (StringUtils.isBlank(pid)) {
            return treeModels;
        } else {
            return DataHandlerUtil.quoteTree(treeModels);
        }
    }

    public <T>Collection<T> createColumnQuery(EruptModel eruptModel, List<Column> columns, EruptQuery query) {
        List<String> conditionStrings = new ArrayList<>();
        DataProxyInvoke.<T>invoke(eruptModel, (dataProxy -> {
            String condition = dataProxy.beforeFetch(query.getConditions());
            if (StringUtils.isNotBlank(condition)) {
                conditionStrings.add(condition);
            }
        }));
        conditionStrings.addAll(AnnotationUtil.switchFilterConditionToStr(eruptModel.getErupt().filter()));
        Optional.ofNullable(query.getConditionStrings()).ifPresent(conditionStrings::addAll);
        String orderBy = StringUtils.isNotBlank(query.getOrderBy()) ? query.getOrderBy() : eruptModel.getErupt().orderBy();
        Collection<T> result = DataProcessorManager.<T>getEruptDataProcessor(eruptModel.getClazz())
                .queryColumn(eruptModel, columns, EruptQuery.builder()
                        .conditions(query.getConditions()).conditionStrings(conditionStrings).orderBy(orderBy).build());
        DataProxyInvoke.invoke(eruptModel, (DataProxy<T> dataProxy) -> dataProxy.afterFetch(result));
        return result;
    }

}
