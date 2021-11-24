package xyz.erupt.upms.model.log;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;
import xyz.erupt.annotation.Erupt;
import xyz.erupt.annotation.EruptField;
import xyz.erupt.annotation.EruptI18n;
import xyz.erupt.annotation.sub_erupt.Power;
import xyz.erupt.annotation.sub_field.Edit;
import xyz.erupt.annotation.sub_field.EditType;
import xyz.erupt.annotation.sub_field.STColumn;
import xyz.erupt.annotation.sub_field.STColumnLink;
import xyz.erupt.annotation.sub_field.STColumnType;
import xyz.erupt.annotation.sub_field.sub_edit.CodeEditorType;
import xyz.erupt.annotation.sub_field.sub_edit.DateType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;
import xyz.erupt.upms.model.EruptUserVo;

/**
 * @author YuePeng
 * date 2020-05-26
 */
@Entity
@Table(name = "e_upms_operate_log")
@EruptI18n
@Erupt(
        name = "操作日志",
        power = @Power(add = false, edit = false, viewDetails = false,
                delete = false, powerHandler = AdminPower.class),
        orderBy = "createTime desc"
)
@Getter
@Setter
public class EruptOperateLog extends BaseModel {

    @ManyToOne
    @EruptField(
            columns = @STColumn(title = "用户", index = "name"),
            edit = @Edit(title = "用户", type = EditType.REFERENCE_TABLE, search = @Search)
    )
    private EruptUserVo eruptUser;

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
            columns = @STColumn(title = "功能名称"),
            edit = @Edit(title = "功能名称", search = @Search(vague = true))
    )
    private String apiName;

    @Column(length = 5000)
    @EruptField(
            columns = @STColumn(title = "请求参数", type = STColumnType.LINK,link=@STColumnLink),
            edit = @Edit(title = "请求参数", type = EditType.CODE_EDITOR, codeEditType = @CodeEditorType(language = "json"))
    )
    private String reqParam;

    @EruptField(
            columns = @STColumn(title = "是否成功"),
            edit = @Edit(title = "是否成功", search = @Search)
    )
    private Boolean status;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            columns = @STColumn(title = "错误信息", type = STColumnType.LINK,link=@STColumnLink)
    )
    private String errorInfo;

    @EruptField(
            columns = @STColumn(title = "请求耗时", template = "value && value+'ms'"),
            edit = @Edit(title = "请求耗时", search = @Search(vague = true))
    )
    private Long totalTime;

    @EruptField(
            columns = @STColumn(title = "记录时间"),
            edit = @Edit(title = "记录时间", search = @Search(vague = true), dateType = @DateType(type = DateType.Type.DATE_TIME))
    )
    private Date createTime;

    @Column(length = 2083)
    @EruptField(
            columns = @STColumn(title = "请求地址", type = STColumnType.LINK,link=@STColumnLink)
    )
    private String reqAddr;

    @EruptField(
            columns = @STColumn(title = "请求方法")
    )
    private String reqMethod;

}
