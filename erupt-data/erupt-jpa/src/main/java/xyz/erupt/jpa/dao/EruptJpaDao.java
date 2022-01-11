package xyz.erupt.jpa.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.Query;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;
import xyz.erupt.annotation.query.Condition;
import xyz.erupt.core.annotation.EruptDataSource;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;
import xyz.erupt.jpa.service.EntityManagerService;

/**
 * @author YuePeng date 2018-10-11.
 */
@Repository
@Slf4j
public class EruptJpaDao {

    @Resource
    private EntityManagerService entityManagerService;
    @Resource
    private ObjectMapper objectMapper;
    public void addEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.entityManagerTran(eruptClass, (em) -> {
            em.persist(entity);
            em.flush();
        });
    }

    public void editEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.entityManagerTran(eruptClass, (em) -> {
            em.merge(entity);
            em.flush();
        });
    }

    public void removeEntity(Class<?> eruptClass, Object entity) {
        entityManagerService.entityManagerTran(eruptClass, (em) -> {
            EruptDataSource eruptDataSource = eruptClass.getAnnotation(EruptDataSource.class);
            if (null == eruptDataSource) {
                em.remove(entity);
            } else {
                em.remove(em.merge(entity));
            }
            em.flush();
        });
    }
    public <T> Page<T> queryEruptList(EruptModel eruptModel, Page<T> page, EruptQuery eruptQuery) {
        String hql = EruptJpaUtils.generateEruptJpaHql(eruptModel, eruptModel.getEruptName(), eruptQuery, false);
        log.info(hql);
        
        String countHql = EruptJpaUtils.generateEruptJpaHql(eruptModel, "count(*)", eruptQuery, true);
        log.info(countHql);
        return entityManagerService.getEntityManager(eruptModel.getClazz(), entityManager -> {
            Query query = entityManager.createQuery(hql);
            Query countQuery = entityManager.createQuery(countHql);
            
            if (null != eruptQuery.getConditions()) {
                for (Condition condition : eruptQuery.getConditions()) {
                    Field conditionField=null;
                    try {
                        conditionField = ReflectUtil.findFieldChain(condition.getKey(), eruptModel.getClazz());
                    } catch (IllegalAccessException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    String paramKey = condition.getKey().replace(".", "_");
                    List<Object> paramList = new ArrayList<>();
                    Object paramValue = condition.getValue();
                    log.info("paramValue:"+(paramValue==null));
                    log.info("objectMapper:"+(objectMapper==null));
                    if (paramValue instanceof List) {
                        for (Object p :(List<?>)paramValue) {
                            paramList.add(objectMapper.convertValue(p, conditionField.getType()));
                        }
                    } else {
                        paramList.add(objectMapper.convertValue(paramValue, conditionField.getType()));
                    } 
                    switch (condition.getExpression()) {
                    case LIKE:
                        countQuery.setParameter(paramKey, EruptJpaUtils.PERCENT + paramList.get(0) + EruptJpaUtils.PERCENT);
                        query.setParameter(paramKey, EruptJpaUtils.PERCENT +  paramList.get(0) + EruptJpaUtils.PERCENT);
                        break;
                    case RANGE:
                        Object lParamValue = paramList.get(0);
                        Object rParamValue = paramList.get(1);
                        countQuery.setParameter(EruptJpaUtils.L_VAL_KEY + paramKey, lParamValue);
                        countQuery.setParameter(EruptJpaUtils.R_VAL_KEY + paramKey, rParamValue);
                        query.setParameter(EruptJpaUtils.L_VAL_KEY + paramKey, lParamValue);
                        query.setParameter(EruptJpaUtils.R_VAL_KEY + paramKey, rParamValue);
                        break;
                    case IN:

                        countQuery.setParameter(paramKey, paramList);
                        query.setParameter(paramKey, paramList);
                        break;
                    default:
                        countQuery.setParameter(paramKey, paramList.get(0));
                        query.setParameter(paramKey, paramList.get(0));
                        break;
                    }
                }
            }
            page.setTotal((Long) countQuery.getSingleResult());
            if (page.getTotal() > 0) {
                List<T> objects = query.setMaxResults(page.getPageSize())
                        .setFirstResult((page.getPageIndex() - 1) * page.getPageSize()).getResultList();
                page.setList(objects);
            } else {
                page.setList(new ArrayList<T>(0));
            }
            return page;
        });
    }

}
