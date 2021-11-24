package xyz.erupt.upms.model.log;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;

/**
 * @author YuePeng
 * date 2019-11-27.
 */
@Entity
@Table(name = "e_upms_login_log")
@Erupt(
        name = "登录日志",
        power = @Power(add = false, edit = false, viewDetails = false, delete = false,
                export = true, powerHandler = AdminPower.class),
        orderBy = "loginTime desc"
)
@EruptI18n
@Getter
@Setter
public class EruptLoginLog extends BaseModel {

    @ManyToOne
    @EruptField(
            columns = @STColumn(title = "用户", index = "name"),
            edit = @Edit(title = "用户", type = EditType.REFERENCE_TABLE
                    , search = @Search(vague = true))
    )
    private EruptUserVo eruptUser;

    @EruptField(
            columns = @STColumn(title = "登录时间", sort = true),
            edit = @Edit(title = "登录时间", search = @Search(vague = true), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date loginTime;

    @EruptField(
            columns = @STColumn(title = "IP地址"),
            edit = @Edit(title = "IP地址", search = @Search)
    )
    private String ip;

    @EruptField(
            columns = @STColumn(title = "IP来源", desc = "格式：国家 | 大区 | 省份 | 城市 | 运营商", template = "value&&value.replace(/\\|/g,' | ')"),
            edit = @Edit(title = "IP来源", search = @Search(vague = true))
    )
    private String region;

    @EruptField(
            columns = @STColumn(title = "操作系统"),
            edit = @Edit(title = "操作系统", search = @Search)
    )
    private String systemName;

    @EruptField(
            columns = @STColumn(title = "浏览器"),
            edit = @Edit(title = "浏览器", search = @Search)
    )
    private String browser;

    @EruptField(
            columns = @STColumn(title = "设备类型"),
            edit = @Edit(title = "设备类型", search = @Search)
    )
    private String deviceType;

    private String token;
}
