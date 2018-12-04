package com.cnpc.framework.activiti.service;

import com.cnpc.framework.activiti.pojo.ActivityVo;
import com.cnpc.framework.activiti.pojo.ProcessInstanceVo;
import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.query.entity.QueryCondition;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.runtime.ProcessInstance;

import java.util.List;
import java.util.Map;

/**
 * Created by billJiang on 2017/6/21.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程实例服务接口
 */
public interface RuntimePageService {
    /**
     * 获取流程启动人ID
     * @param processInstance 流程实例
     * @return
     */
    String getStartUserId(ProcessInstance processInstance);

    /**
     * 根据任务id获取流程启动人
     */
    String getStartUserId(String taskId);


    /**
     * 流程实例维护（给管理员使用）
     * @param condition 查询条件
     * @param pageInfo 分页信息
     * @return
     */
    List<ProcessInstanceVo> getProcessInstanceList(QueryCondition condition, PageInfo pageInfo);

    /**
     * 流程明细列表 （已经走过的节点+未走过的节点）
     * @param condition 查询条件
     * @param pageInfo 分页信息
     * @return
     */
    List<ActivityVo> getActivityList(QueryCondition condition, PageInfo pageInfo);

    /**
     * 根据流程编号启动最新流程
     * @param processDefinitionKey 流程定义key
     * @param name 流程实例名称
     * @param variables 流程变量
     * @param businessKey 业务ID
     * @return 流程实例
     */
    Result startProcessInstanceByKey(String processDefinitionKey, String name, Map<String, Object> variables, String userId, String
            businessKey);
    /**
     * 表单变量转化为流程变量
     *
     * @param formProperties
     * @param formData
     * @return
     */
    Map<String, Object> getTaskVariables(List<FormProperty> formProperties, Map<String, String> formData);

    /**
     * FormProperty转化为变量
     * @param formProperty
     * @param value
     * @return
     */
    Object getVariable(FormProperty formProperty, String value);



}
