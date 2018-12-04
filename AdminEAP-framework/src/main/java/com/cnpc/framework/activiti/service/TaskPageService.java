package com.cnpc.framework.activiti.service;

import com.cnpc.framework.activiti.pojo.TaskDoneVo;
import com.cnpc.framework.activiti.pojo.TaskVo;
import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.BaseService;
import com.cnpc.framework.query.entity.QueryCondition;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by billJiang on 2017/6/21.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程任务接口 待办处理
 */
public interface TaskPageService extends BaseService {
    /**
     * 获取个人待办列表
     *
     * @param condition 查询条件
     * @param pageInfo  分页信息
     * @return
     */
    List<TaskVo> getTaskToDoList(QueryCondition condition, PageInfo pageInfo);


    /**
     * 获取个人已办列表
     */
    List<TaskDoneVo> getTaskDoneList(QueryCondition condition, PageInfo pageInfo);

    /**
     * 获取任务候选人
     *
     * @param taskId 任务ID
     * @return
     */
    Set<User> getTaskCandidate(String taskId);

    /**
     * 获取候选用户
     *
     * @param taskId
     * @return Map names  ids
     */
    Map<String, String> getTaskCandidateUser(String taskId);

    /**
     * 签收任务
     *
     * @param taskId   任务ID
     * @param assignee 签收人
     * @return
     */
    Result claimTask(String taskId, String assignee);

    /**
     * 取消签收任务
     *
     * @param taskId   任务ID
     * @param assignee 签收人
     * @return
     */
    Result unclaimTask(String taskId, String assignee);

    /**
     * 已办是否可以撤回任务
     *
     * @param processInstance 流程历史实例
     * @param userId          用户ID
     * @return data为用户任务
     */
    Result canWithdraw(HistoricProcessInstance processInstance, String userId);


    /**
     * 用户撤回任务
     *
     * @param instanceId 历史流程实例ID
     * @param userId     用户ID
     * @return
     */
    Result withdrawTask(String instanceId, String userId);


    /**
     * 流程跳转到任意节点
     *
     * @param currentTaskEntity       当前任务节点
     * @param targetTaskDefinitionKey 目标任务节点
     * @throws Exception
     */
    void jumpTask(final TaskEntity currentTaskEntity, String targetTaskDefinitionKey) throws Exception;


    /**
     * 正在运行中的任务设置被委托人，单个候选人或者指定了受理人的任务
     *
     * @param assignee 设置委托人
     * @param attorney 被委托人
     * @param moduleId 业务ID
     * @return
     */
    List<TaskVo> delegateTasks(String assignee, String attorney, String moduleId);


    /**
     * 获取当前审批节点的候选用户
     *
     * @param taskEntity
     * @return
     */
    Set<String> getCandidateUserForTask(TaskEntity taskEntity);

    /**
     * 提交流程
     * @param taskId
     * @param formData
     * @return
     */
    Result submitTask(String taskId, Map<String, String> formData);
}


