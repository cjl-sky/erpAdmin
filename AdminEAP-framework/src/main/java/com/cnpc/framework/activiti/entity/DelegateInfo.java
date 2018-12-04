package com.cnpc.framework.activiti.entity;

import com.cnpc.framework.annotation.Header;
import com.cnpc.framework.base.entity.BaseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程代理设置
 */
@Entity
@Table(name="tbl_act_delegate_info")
public class DelegateInfo extends BaseEntity{

    @Header(name="业务ID")
    @Column(name="MODULE_ID_",length = 4000)
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

    @Header(name="委托原因")
    @Column(name="REASON_")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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
}
