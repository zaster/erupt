package xyz.erupt.upms.model;

import java.util.Date;
import java.util.Set;

import javax.annotation.Resource;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.constant.AnnotationConst;
import xyz.erupt.annotation.fun.DataProxy;
import xyz.erupt.annotation.sub_erupt.LinkTree;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.InputType;
import xyz.erupt.annotation.sub_field.sub_edit.ReferenceTreeType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.core.exception.EruptApiErrorTip;
import xyz.erupt.core.exception.EruptWebApiRuntimeException;
import xyz.erupt.core.service.I18NTranslateService;
import xyz.erupt.core.util.MD5Util;
import xyz.erupt.core.view.EruptApiModel;
import xyz.erupt.jpa.dao.EruptDao;
import xyz.erupt.upms.constant.RegexConst;
import xyz.erupt.upms.model.base.HyperModel;
import xyz.erupt.upms.service.EruptUserService;

/**
 * @author YuePeng
 * date 2018-11-22.
 */
@Entity
@Table(name = "e_upms_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "account")
})
@Erupt(
        name = "用户", desc = "用户配置",
        dataProxy = EruptUser.class,
        linkTree = @LinkTree(field = "eruptOrg")
)
@EruptI18n
@Getter
@Setter
@Component
public class EruptUser extends HyperModel implements DataProxy<EruptUser> {

    @EruptField(
            columns = @STColumn(title = "用户名", sort = true),
            edit = @Edit(title = "用户名", desc = "登录用户名", notNull = true, search = @Search(vague = true))
    )
    private String account;

    @EruptField(
            columns = @STColumn(title = "姓名", sort = true),
            edit = @Edit(title = "姓名", notNull = true, search = @Search(vague = true))
    )
    private String name;

    @EruptField(
            columns = @STColumn(title = "账户状态"),
            edit = @Edit(
                    title = "账户状态",
                    search = @Search,
                    type = EditType.BOOLEAN,
                    notNull = true,
                    boolType = @BoolType(
                            trueText = "激活",
                            falseText = "锁定"
                    )
            )
    )
    private Boolean status = true;

    @EruptField(
            columns = @STColumn(title = "手机号码"),
            edit = @Edit(title = "手机号码", search = @Search(vague = true), inputType = @InputType(regex = RegexConst.PHONE_REGEX))
    )
    private String phone;

    @EruptField(
            columns = @STColumn(title = "邮箱"),
            edit = @Edit(title = "邮箱", search = @Search(vague = true), inputType = @InputType(regex = RegexConst.EMAIL_REGEX))
    )
    private String email;

    @ManyToOne
    @EruptField(
            columns = @STColumn(title = "首页菜单", index = "name"),
            edit = @Edit(
                    title = "首页菜单",
                    type = EditType.REFERENCE_TREE,
                    referenceTreeType = @ReferenceTreeType(pid = "parentMenu.id")
            )
    )
    private EruptMenu eruptMenu;

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

    @Transient
    @EruptField(
            edit = @Edit(title = "密码", type = EditType.DIVIDE)
    )
    private String pwdDivide;

    private String password;

    @Transient
    @EruptField(
            edit = @Edit(title = "密码")
    )
    private String passwordA;

    @Transient
    @EruptField(
            edit = @Edit(title = "确认密码")
    )
    private String passwordB;

    @EruptField(
            columns = @STColumn(title = "md5加密"),
            edit = @Edit(
                    title = "md5加密",
                    type = EditType.BOOLEAN,
                    notNull = true,
                    boolType = @BoolType(
                            trueText = "加密",
                            falseText = "不加密"
                    )
            )
    )
    private Boolean isMd5 = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "e_upms_user_role",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "role_id", referencedColumnName = "id")
    )
    @EruptField(
            columns = @STColumn(title = "所属角色"),
            edit = @Edit(
                    title = "所属角色",
                    type = EditType.CHECKBOX
            )
    )
    private Set<EruptRole> roles;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(
                    title = "ip白名单",
                    desc = "ip与ip之间使用换行符间隔，不填表示不鉴权",
                    type = EditType.TEXTAREA
            )

    )
    private String whiteIp;

    @Column(length = AnnotationConst.REMARK_LENGTH)
    @EruptField(
            edit = @Edit(
                    title = "备注",
                    type = EditType.TEXTAREA
            )
    )
    private String remark;

    private Boolean isAdmin;

    @Transient
    @Resource
    private EruptDao eruptDao;

    @Transient
    @Resource
    private EruptUserService eruptUserService;

    @Transient
    @Resource
    private I18NTranslateService i18NTranslateService;

    public EruptUser() {
    }

    public EruptUser(Long id) {
        this.setId(id);
    }


    @Override
    public void beforeAdd(EruptUser eruptUser) {
        if (StringUtils.isBlank(eruptUser.getPasswordA())) {
            throw new EruptApiErrorTip(EruptApiModel.Status.WARNING, "密码必填", EruptApiModel.PromptWay.MESSAGE);
        }
        this.checkPostOrg(eruptUser);
        if (eruptUser.getPasswordA().equals(eruptUser.getPasswordB())) {
            eruptUser.setIsAdmin(false);
            eruptUser.setCreateTime(new Date());
            if (eruptUser.getIsMd5()) {
                eruptUser.setPassword(MD5Util.digest(eruptUser.getPasswordA()));
            }
        } else {
            throw new EruptWebApiRuntimeException(i18NTranslateService.translate("两次密码输入不一致"));
        }
    }

    @Override
    public void beforeUpdate(EruptUser eruptUser) {
        eruptDao.getEntityManager().clear();
        EruptUser eu = eruptDao.getEntityManager().find(EruptUser.class, eruptUser.getId());
        if (!eruptUser.getIsMd5() && eu.getIsMd5()) {
            throw new EruptWebApiRuntimeException(i18NTranslateService.translate("MD5不可逆", "MD5 irreversible"));
        }
        this.checkPostOrg(eruptUser);
        if (StringUtils.isNotBlank(eruptUser.getPasswordA())) {
            if (!eruptUser.getPasswordA().equals(eruptUser.getPasswordB())) {
                throw new EruptWebApiRuntimeException(i18NTranslateService.translate("两次密码输入不一致"));
            }
            if (eruptUser.getIsMd5()) {
                eruptUser.setPassword(MD5Util.digest(eruptUser.getPasswordA()));
            } else {
                eruptUser.setPassword(eruptUser.getPasswordA());
            }
        }
    }

    private void checkPostOrg(EruptUser eruptUser) {
        if (eruptUser.getEruptPost() != null && eruptUser.getEruptOrg() == null) {
            throw new EruptWebApiRuntimeException("选择岗位时，所属组织必填");
        }
    }
}
