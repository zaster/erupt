package xyz.erupt.jpa.support;

import java.lang.reflect.Field;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.JoinColumn;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.core.annotation.EruptDataSource;
import xyz.erupt.core.util.ReflectUtil;
import xyz.erupt.jpa.dao.EruptDao;

@Component
public class JpaSupport {

    @Resource
    private EruptDao eruptDao;

    /**
     * 对jpa @JoinColumn提供的referencedColumnName配置实现适配
     */
    @SneakyThrows
    public void referencedColumnNameSupport(Object obj, Field field) {
        EruptField eruptField = field.getAnnotation(EruptField.class);
        if (null != eruptField) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            if (null != joinColumn && !"".equals(joinColumn.referencedColumnName())) {
                String id;
                if (eruptField.edit().type() == EditType.REFERENCE_TREE) {
                    id = eruptField.edit().referenceTreeType().id();
                } else if (eruptField.edit().type() == EditType.REFERENCE_TABLE) {
                    id = eruptField.edit().referenceTableType().id();
                } else {
                    return;
                }
                Object refObject =  PropertyUtils.getProperty(obj, field.getName());
                if (null != refObject) {
                    Field idField = ReflectUtil.findClassField(refObject.getClass(), id);
                    EntityManager em = eruptDao.getEntityManager();
                    EruptDataSource eruptDataSource = refObject.getClass().getAnnotation(EruptDataSource.class);
                    if (eruptDataSource != null) {
                        em = eruptDao.getEntityManager(eruptDataSource.value());
                    }
                    Object result = em.createQuery("from " + refObject.getClass().getSimpleName() + " I where I.id = :id")
                            .setParameter("id",  PropertyUtils.getProperty(refObject, idField.getName())).getSingleResult();
                    em.close();
                    field.set(obj, result);
                }
            }
        }

    }

}