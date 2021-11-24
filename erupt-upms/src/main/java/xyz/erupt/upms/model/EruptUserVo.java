package xyz.erupt.upms.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_user")
@Erupt(
        name = "简单用户对象"
)
@EruptI18n
@Getter
@Setter
public class EruptUserVo extends BaseModel {

    @EruptField(
            columns = @STColumn(title = "姓名", sort = true),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @ManyToOne
    @EruptField(
            columns = @STColumn(title = "所属组织", index = "name"),
            edit = @Edit(title = "所属组织", type = EditType.REFERENCE_TREE, referenceTreeType = @ReferenceTreeType(pid = "parentOrg.id"))
    )
    private EruptOrg eruptOrg;

    @ManyToOne
    @EruptField(
            columns = @STColumn(title = "岗位", index = "name"),
            edit = @Edit(title = "岗位", type = EditType.REFERENCE_TREE)
    )
    private EruptPost eruptPost;

    public EruptUserVo() {
    }

    public EruptUserVo(Long id) {
        this.setId(id);
    }

}
