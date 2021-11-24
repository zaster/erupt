package xyz.erupt.job.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
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
import xyz.erupt.annotation.sub_field.STColumnType;
import xyz.erupt.annotation.sub_field.sub_edit.BoolType;
import xyz.erupt.annotation.sub_field.sub_edit.Search;
import xyz.erupt.jpa.model.BaseModel;

/**
 * @author YuePeng
 * date 2019-12-26
 */
@EruptI18n
@Erupt(
        orderBy = "startTime desc",
        name = "任务日志",
        power = @Power(export = true, add = false, delete = false, edit = false, viewDetails = false)
)
@Entity
@Table(name = "e_job_log")
@Getter
@Setter
public class EruptJobLog extends BaseModel {

    @ManyToOne
    @JoinColumn(name = "job_id")
    @EruptField(
            columns = @STColumn(title = "任务名称", index = "name"),
            edit = @Edit(title = "任务名称", show = false, search = @Search, type = EditType.REFERENCE_TREE)
    )
    private EruptJob eruptJob;

    @EruptField(
            columns = @STColumn(title = "任务参数")
    )
    private String handlerParam;

    @EruptField(
            columns = @STColumn(title = "任务状态"),
            edit = @Edit(title = "任务状态", boolType = @BoolType(trueText = "成功", falseText = "失败"), search = @Search)
    )
    private Boolean status;

    @EruptField(
            columns = @STColumn(title = "开始时间")
    )
    private Date startTime;

    @EruptField(
            columns = @STColumn(title = "结束时间")
    )
    private Date endTime;

    @Column(length = 2000)
    @EruptField(
            columns = @STColumn(title = "执行结果"),
            edit = @Edit(title = "执行结果")
    )
    private String resultInfo;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @EruptField(
            columns = @STColumn(title = "错误信息", type = STColumnType.LINK),
            edit = @Edit(title = "错误信息")
    )
    private String errorInfo;

}
