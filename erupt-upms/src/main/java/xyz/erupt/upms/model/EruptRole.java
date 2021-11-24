package xyz.erupt.upms.model;

import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.fun.FilterHandler;
import xyz.erupt.annotation.sub_erupt.Filter;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.TagsType;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.enums.MenuLimitEnum;
import xyz.erupt.upms.handler.RoleMenuFilter;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_role", uniqueConstraints = {
        @UniqueConstraint(columnNames = "code")
})
@Erupt(
        name = "用户角色",
        filter = @Filter(conditionHandler = EruptRole.class)
)
@EruptI18n
@Getter
@Setter
@Component
public class EruptRole extends BaseModel implements FilterHandler {

    @EruptField(
            columns = @STColumn(title = "编码"),
            edit = @Edit(title = "编码", notNull = true)
    )
    private String code;

    @EruptField(
            columns = @STColumn(title = "名称"),
            edit = @Edit(title = "名称", notNull = true)
    )
    private String name;

    @EruptField(
            columns = @STColumn(title = "状态"),
            edit = @Edit(
                    title = "状态",
                    type = EditType.BOOLEAN,
                    notNull = true,
                    boolType = @BoolType(trueText = "启用", falseText = "禁用")
            )
    )
    private Boolean status = true;

    @EruptField(
            columns = @STColumn(title = "操作权限", template = "value&&value.replace(/\\|/g,'<span class=\"text-red\"> | </span>')"),
            edit = @Edit(
                    title = "操作权限",
                    type = EditType.TAGS,
                    tagsType = @TagsType(fetchHandler = MenuLimitEnum.MenuLimitFetch.class, allowExtension = false)
            )
    )
    private String powerOff;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "e_upms_role_menu",
            joinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id", referencedColumnName = "id"))
    @EruptField(
            columns = @STColumn(title = "菜单权限"),
            edit = @Edit(
                    filter = @Filter(conditionHandler = RoleMenuFilter.class),
                    title = "菜单权限",
                    type = EditType.TAB_TREE
            )
    )
    private Set<EruptMenu> menus;

    @Transient
    @Resource
    private EruptUserService eruptUserService;

    @Override
    public String filter(String condition, String[] params) {
        EruptUser eruptUser = eruptUserService.getCurrentEruptUser();
        if (eruptUser.getIsAdmin()) {
            return null;
        }
        Set<String> roles = eruptUser.getRoles().stream().map(it -> it.getId().toString()).collect(Collectors.toSet());
        return String.format("id in (%s)", String.join(",", roles));
    }
}
