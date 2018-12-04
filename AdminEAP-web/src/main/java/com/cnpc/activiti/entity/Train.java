package com.cnpc.activiti.entity;

import com.cnpc.framework.annotation.Header;
import com.cnpc.framework.base.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by billJiang on 2017/7/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 培训选课demo
 */
@Entity
@Table(name="tbl_demo_train")
public class Train extends BaseEntity {

    @Header(name="课程名称")
    @Column(name="name")
    private String name;


    @Header(name="上课时间")
    @Column(name="courseTime")
    private String courseTime;

    @Header(name="课时")
    @Column(name="duration")
    private Integer duration;


    @Header(name="上课老师")
    @Column(name="teacher")
    private String teacher;

    @Header(name="地点")
    @Column(name="address")
    private String address;

    @Header(name="备注")
    @Column(name="remark",length = 1000)
    private String remark;

    @Header(name="流程状态")
    @Column(name="state")
    private int state;

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCourseTime() {
        return courseTime;
    }

    public void setCourseTime(String courseTime) {
        this.courseTime = courseTime;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
