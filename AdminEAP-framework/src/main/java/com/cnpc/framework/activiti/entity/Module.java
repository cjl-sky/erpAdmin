package com.cnpc.framework.activiti.entity;

import com.cnpc.framework.annotation.Header;
import com.cnpc.framework.base.entity.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Created by billJiang on 2017/6/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 业务定义
 */
@Entity
@Table(name="tbl_act_module")
public class Module extends BaseEntity {

    @Header(name="业务名称")
    @Column(name="name")
    private String name;

    @Header(name="业务编码")
    @Column(name="code")
    private String code;

    @Header(name="备注")
    @Column(name="remark")
    private String remark;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
