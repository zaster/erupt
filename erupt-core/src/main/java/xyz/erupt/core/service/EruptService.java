package xyz.erupt.core.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import xyz.erupt.annotation.config.QueryExpression;
import xyz.erupt.annotation.fun.PowerObject;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.core.exception.EruptNoLegalPowerException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.invoke.DataProxyInvoke;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.EruptUtil;
import xyz.erupt.core.util.Erupts;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.core.view.TableQueryVo;

/**
 * @author YuePeng
 * date 2020-02-29
 */
@Service
@Slf4j
public class EruptService {

    /**
     * @param eruptModel      eruptModel
     * @param tableQueryVo    前端查询对象
     * @param serverCondition 自定义条件
     * @param customCondition 条件字符串
     */
    public <TT>Page<TT> getEruptData(EruptModel<TT> eruptModel, TableQueryVo tableQueryVo, List<Condition> serverCondition, String... customCondition) {
        Erupts.powerLegal(eruptModel, PowerObject::isQuery);
        List<Condition> legalConditions = EruptUtil.geneEruptSearchCondition(eruptModel, tableQueryVo.getCondition());
        List<String> conditionStrings = new ArrayList<>();
        //DependTree logic
        LinkTree dependTree = eruptModel.getErupt().linkTree();
        if (StringUtils.isNotBlank(dependTree.field())) {
            if (null == tableQueryVo.getLinkTreeVal()) {
                if (dependTree.dependNode()) return new Page<TT>();
            } else {
                EruptModel<TT> treeErupt = EruptCoreService.getErupt(ReflectUtil.findClassField(eruptModel.getClazz(), dependTree.field()).getType().getSimpleName());
                conditionStrings.add(dependTree.field() + "." + treeErupt.getErupt().primaryKeyCol() + " = '" + tableQueryVo.getLinkTreeVal() + "'");
            }
        }
        conditionStrings.addAll(Arrays.asList(customCondition));
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> Optional.ofNullable(dataProxy.beforeFetch(legalConditions)).ifPresent(conditionStrings::add)));
        Optional.ofNullable(serverCondition).ifPresent(legalConditions::addAll);
        Page<TT> page = DataProcessorManager.getEruptDataProcessor(eruptModel.getClazz())
                .queryList(eruptModel, new Page<TT>(tableQueryVo.getPageIndex(), tableQueryVo.getPageSize(), tableQueryVo.getSort()),
                        EruptQuery.builder().orderBy(tableQueryVo.getSort()).conditionStrings(conditionStrings).conditions(legalConditions).build());
        DataProxyInvoke.invoke(eruptModel, (dataProxy -> dataProxy.afterFetch(page.getList())));
        //Optional.ofNullable(page.getList()).ifPresent(it -> DataHandlerUtil.convertDataToEruptView(eruptModel, it));
        return page;
    }

    /**
     * 校验id使用权限
     *
     * @param eruptModel eruptModel
     * @param id         标识主键
     */
    public <TT>void verifyIdPermissions(EruptModel<TT> eruptModel, String id) {
        List<Condition> conditions = new ArrayList<>();
        conditions.add(new Condition(eruptModel.getErupt().primaryKeyCol(), id, QueryExpression.EQ));
        Page<TT> page = DataProcessorManager.<TT>getEruptDataProcessor(eruptModel.getClazz())
                .queryList(eruptModel, new Page<TT>(0, 1, null),
                        EruptQuery.builder().conditions(conditions).build());
        if (page.getList().size() <= 0) {
            throw new EruptNoLegalPowerException();
        }
    }

}
