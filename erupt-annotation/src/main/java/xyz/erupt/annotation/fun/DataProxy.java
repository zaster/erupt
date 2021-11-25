package xyz.erupt.annotation.fun;

import java.util.Collection;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.annotation.query.Condition;

/**
 * @author YuePeng
 * date 2018-10-09.
 */
public interface DataProxy<@Comment("Erupt类对象") T> {

    @Comment("增加前")
    default void beforeAdd(T model) {
    }

    @Comment("增加后")
    default void afterAdd(T model) {
    }

    @Comment("修改前")
    default void beforeUpdate(T model) {
    }

    @Comment("修改后")
    default void afterUpdate(T model) {
    }

    @Comment("删除前")
    default void beforeDelete(T model) {
    }

    @Comment("删除后")
    default void afterDelete(T model) {
    }

    @Comment("查询前，返回值为：自定义查询条件")
    default String beforeFetch(List<Condition> conditions) {
        return null;
    }

    @Comment("查询后结果处理")
    default void afterFetch(@Comment("查询结果") Collection<T> collection) {
    }


    @Comment("数据新增行为，可对数据做初始化等操作")
    default void addBehavior(T model) {
    }

    @Comment("数据编辑行为，对待编辑的数据做预处理")
    default void editBehavior(T model) {
    }

    @Comment("excel导出")
    default void excelExport(@Comment("POI文档对象") Workbook wb) {
    }

    @Comment("excel导入")
    @Deprecated
    default void excelImport(T model) {
    }

}
