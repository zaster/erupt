package xyz.erupt.jpa.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.transaction.Transactional;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.constant.EruptConst;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.invoke.DataProcessorManager;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.service.EruptCoreService;
import xyz.erupt.core.service.I18NTranslateService;
import xyz.erupt.core.service.IEruptDataService;
import xyz.erupt.core.util.AnnotationUtil;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptFieldModel;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.dao.EruptJpaDao;
import xyz.erupt.jpa.dao.EruptJpaUtils;
import xyz.erupt.jpa.support.JpaSupport;

/**
 * @author YuePeng
 * date 2019-03-06.
 */
@Service
public class EruptDataServiceDbImpl implements IEruptDataService {

    static {
        DataProcessorManager.register(EruptConst.DEFAULT_DATA_PROCESSOR, EruptDataServiceDbImpl.class);
    }

    @Resource
    private EruptJpaDao eruptJpaDao;

    @Resource
    private EntityManagerService entityManagerService;

    @Resource
    private JpaSupport jpaSupport;

    @Resource
    private I18NTranslateService i18NTranslateService;

    @Override
    public <TT>TT findDataById(EruptModel<TT> eruptModel, Object id) {
        return entityManagerService.getEntityManager(eruptModel.getClazz(), (em) -> em.find(eruptModel.getClazz(), id));
    }

    @Override
    public <TT>Page<TT> queryList(EruptModel<TT> eruptModel, Page<TT> page, EruptQuery query) {
        return eruptJpaDao.queryEruptList(eruptModel, page, query);
    }

    @Transactional
    @Override
    public <TT>void addData(EruptModel<TT> eruptModel, Object data) {
        try {
            this.loadSupport(data);
            this.jpaManyToOneConvert(eruptModel, data);
            eruptJpaDao.addEntity(eruptModel.getClazz(), data);
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    @Transactional
    @Override
    public <TT>void editData(EruptModel<TT> eruptModel, Object data) {
        try {
            this.loadSupport(data);
            eruptJpaDao.editEntity(eruptModel.getClazz(), data);
        } catch (Exception e) {
            handlerException(e, eruptModel);
        }
    }

    private void loadSupport(Object jpaEntity) {
        for (Field field : jpaEntity.getClass().getDeclaredFields()) {
            jpaSupport.referencedColumnNameSupport(jpaEntity, field);
        }
    }

    //优化异常提示类
    private <TT>void handlerException(Exception e, EruptModel<TT> eruptModel) {
        e.printStackTrace();
        if (e instanceof DataIntegrityViolationException) {
            if (e.getMessage().contains("ConstraintViolationException")) {
                throw new EruptWebApiRuntimeException(gcRepeatHint(eruptModel));
            } else if (e.getMessage().contains("DataException")) {
                throw new EruptWebApiRuntimeException(i18NTranslateService.translate("内容超出数据库限制长度"));
            } else {
                throw new EruptWebApiRuntimeException(e.getMessage());
            }
        } else {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    @Transactional
    @Override
    public <TT>void deleteData(EruptModel<TT> eruptModel, Object object) {
        try {
            eruptJpaDao.removeEntity(eruptModel.getClazz(), object);
        } catch (DataIntegrityViolationException | ConstraintViolationException e) {
            e.printStackTrace();
            throw new EruptWebApiRuntimeException(i18NTranslateService.translate("删除失败，可能存在关联数据，无法直接删除"));
        } catch (Exception e) {
            throw new EruptWebApiRuntimeException(e.getMessage());
        }
    }

    //@ManyToOne数据处理
    private <TT>void jpaManyToOneConvert(EruptModel<TT> eruptModel, Object object) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        for (EruptFieldModel fieldModel : eruptModel.getEruptFieldModels()) {
            if (fieldModel.getEruptField().edit().type() == EditType.TAB_TABLE_ADD) {
                Field field = ReflectUtil.findClassField(object.getClass(), fieldModel.getFieldName());
                Collection<?> collection = (Collection<?>)PropertyUtils.getProperty(object, field.getName()) ;
                if (null != collection) {
                    for (Object o : collection) {
                        //强制删除主键
                        ReflectUtil.findClassField(o.getClass(),
                                EruptCoreService.getErupt(fieldModel.getFieldReturnName()).getErupt()
                                        .primaryKeyCol()).set(o, null);
                    }
                }
            }
        }
    }

    //生成数据重复的提示字符串
    private <TT>String gcRepeatHint(EruptModel<TT> eruptModel) {
        StringBuilder str = new StringBuilder();
        for (UniqueConstraint uniqueConstraint : eruptModel.getClazz().getAnnotation(Table.class).uniqueConstraints()) {
            for (String columnName : uniqueConstraint.columnNames()) {
                EruptFieldModel eruptFieldModel = eruptModel.getEruptFieldMap().get(columnName);
                if (null != eruptFieldModel) {
                    str.append(eruptFieldModel.getEruptField().columns()[0].title()).append("、");
                }
            }
        }
        String repeatTxt = i18NTranslateService.translate("数据重复");
        if (StringUtils.isNotBlank(str)) {
            return str.substring(0, str.length() - 1) + " " + repeatTxt;
        } else {
            return repeatTxt;
        }
    }

    /**
     * 根据列获取相关数据
     *
     * @param eruptModel eruptModel
     * @param columns    列
     * @param query      查询对象
     * @return 数据结果集
     */
    @Override
    public <TT>Collection<TT> queryColumn(EruptModel<TT> eruptModel, List<Column> columns, EruptQuery query) {
        StringBuilder hql = new StringBuilder();
        List<String> columnStrList = new ArrayList<>();
        columns.forEach(column -> columnStrList.add(EruptJpaUtils.completeHqlPath(eruptModel.getEruptName()
                , column.getName()) + " as " + column.getAlias()));
        hql.append("select new map(").append(String.join(", ", columnStrList))
                .append(") from ").append(eruptModel.getEruptName()).append(" as ").append(eruptModel.getEruptName());
        ReflectUtil.findClassAllFields(eruptModel.getClazz(), field -> {
            if (null != field.getAnnotation(ManyToOne.class) || null != field.getAnnotation(OneToOne.class)) {
                hql.append(" left outer join ").append(eruptModel.getEruptName()).append(".")
                        .append(field.getName()).append(" as ").append(field.getName());
            }
        });
        hql.append(" where 1 = 1 ");
        Optional.ofNullable(query.getConditions()).ifPresent(c -> c.forEach(it -> hql.append(EruptJpaUtils.AND).append(it.getKey()).append('=').append(it.getValue())));
        Optional.ofNullable(query.getConditionStrings()).ifPresent(c -> c.forEach(it -> hql.append(EruptJpaUtils.AND).append(it)));
        Arrays.stream(eruptModel.getErupt().filter()).map(AnnotationUtil::switchFilterConditionToStr)
                .filter(StringUtils::isNotBlank).forEach(it -> hql.append(EruptJpaUtils.AND).append(it));
        if (StringUtils.isNotBlank(query.getOrderBy())) {
            hql.append(" order by ").append(query.getOrderBy());
        }
        return entityManagerService.getEntityManager(eruptModel.getClazz(), (em) -> em.createQuery(hql.toString()).getResultList());
    }

}
