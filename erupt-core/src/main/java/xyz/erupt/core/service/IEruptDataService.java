package xyz.erupt.core.service;

import java.util.Collection;
import java.util.List;

import xyz.erupt.annotation.config.Comment;
import xyz.erupt.core.query.Column;
import xyz.erupt.core.query.EruptQuery;
import xyz.erupt.core.view.EruptModel;
import xyz.erupt.core.view.Page;

/**
 * @author YuePeng
 * date 10/10/18.
 */
public interface IEruptDataService{

    @Comment("根据主键id获取数据")
    <TT>TT findDataById(EruptModel<TT> eruptModel, @Comment("主键值") Object id);

    @Comment("查询分页数据")
    <TT>Page<TT> queryList(EruptModel<TT> eruptModel, @Comment("分页对象") Page<TT> page, @Comment("条件") EruptQuery eruptQuery);

    @Comment("根据列查询相关数据")
    <TT>Collection<TT> queryColumn(EruptModel<TT> eruptModel, @Comment("列信息") List<Column> columns, @Comment("条件") EruptQuery eruptQuery);

    @Comment("添加数据")
    <TT>void addData(EruptModel<TT> eruptModel, @Comment("数据对象") Object object);

    @Comment("修改数据")
    <TT>void editData(EruptModel<TT> eruptModel, @Comment("数据对象") Object object);

    @Comment("删除数据")
    <TT>void deleteData(EruptModel<TT> eruptModel, @Comment("数据对象") Object object);

}
