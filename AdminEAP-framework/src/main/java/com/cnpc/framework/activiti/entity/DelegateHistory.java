package com.cnpc.framework.activiti.entity;

import com.cnpc.framework.annotation.Header;
import com.cnpc.framework.base.entity.BaseEntity;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程代理历史记录
 */
@Entity
@Table(name="tbl_act_delegate_history")
public class DelegateHistory extends BaseEntity {
    @Header(name="业务ID")
    @Column(name="MODULE_ID_")
    private String moduleId;

    @Header(name="委托人")
    @Column(name="ASSIGNEE_",length = 64)
    private String assignee;

    @Header(name="被委托人")
    @Column(name="ATTORNEY_",length = 64)
    private String attorney;

    @Header(name="委托开始时间")
    @Column(name="START_TIME_")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startTime;

    @Header(name="委托结束时间")
    @Column(name="END_TIME_")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endTime;

    @Header(name="委托ID")
    @Column(name="DELEGATE_ID_")
    private String delegateId;

    @Header(name="委托时间")
    @Column(name="DELEGATE_TIME_")
    private Date delegateTime;

    @Header(name="任务ID")
    @Column(name="TASK_ID_")
    private String taskId;

    @Header(name="任务名称")
    @Column(name="TASK_NAME_")
    private String taskName;

    @Header(name="委托原因")
    @Column(name="REASON_")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDelegateId() {
        return delegateId;
    }

    public void setDelegateId(String delegateId) {
        this.delegateId = delegateId;
    }

    public Date getDelegateTime() {
        return delegateTime;
    }

    public void setDelegateTime(Date delegateTime) {
        this.delegateTime = delegateTime;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getAttorney() {
        return attorney;
    }

    public void setAttorney(String attorney) {
        this.attorney = attorney;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
