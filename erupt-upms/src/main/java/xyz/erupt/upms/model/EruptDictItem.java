package xyz.erupt.upms.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.upms.model.base.HyperModel;

/**
 * @author YuePeng
 * date 2018-12-07.
 */

@Entity
@Table(name = "e_dict_item", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "erupt_dict_id"}))
@Erupt(
        name = "字典项",
        orderBy = "sort",
        power = @Power(export = true)
)
@Getter
@Setter
@EruptI18n
public class EruptDictItem extends HyperModel {

    @EruptField(
            columns = @STColumn(title = "编码"),
            edit = @Edit(title = "编码", notNull = true, search = @Search)
    )
    private String code;

    @EruptField(
            columns = @STColumn(title = "名称"),
            edit = @Edit(title = "名称", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            columns = @STColumn(title = "显示顺序", sort = true),
            edit = @Edit(title = "显示顺序")
    )
    private Integer sort;

    @EruptField(
            columns = @STColumn(title = "备注"),
            edit = @Edit(
                    title = "备注"
            )
    )
    private String remark;

    @ManyToOne
    @EruptField
    @JoinColumn(name = "erupt_dict_id")
    private EruptDict eruptDict;

}
