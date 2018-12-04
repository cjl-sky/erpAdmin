package com.cnpc.framework.activiti.service.impl;

import com.cnpc.framework.activiti.entity.Module;
import com.cnpc.framework.activiti.pojo.Constants;
import com.cnpc.framework.activiti.pojo.TaskDoneVo;
import com.cnpc.framework.activiti.pojo.TaskVo;
import com.cnpc.framework.activiti.service.IdentityPageService;
import com.cnpc.framework.activiti.service.RuntimePageService;
import com.cnpc.framework.activiti.service.TaskPageService;
import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.impl.BaseServiceImpl;
import com.cnpc.framework.query.entity.QueryCondition;
import com.cnpc.framework.utils.DateUtil;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.IdentityLinkEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;

/**
 * Created by billJiang on 2017/6/21.
 * e-mail:475572229@qq.com  qq:475572229
 * 用户待办/已办接口
 */
@Service("taskPageService")
public class TaskPageServiceImpl extends BaseServiceImpl implements TaskPageService {

    @Autowired
    private TaskService taskService;

    @Resource
    private IdentityPageService identityPageService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private RuntimeService runtimeService;

    @Resource
    private RuntimePageService runtimePageService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ManagementService managementService;

    @Autowired
    private FormService formService;

    private static final Logger logger = LoggerFactory.getLogger(TaskPageServiceImpl.class);


    //待办
    @Override
    public List<TaskVo> getTaskToDoList(QueryCondition condition, PageInfo pageInfo) {
        String name = null;        //流程实例名称
        String businessKey = null;//业务key
        String category = null;    //业务类型编码
        String userId = null;//执行人
        Map<String, String> conditionMap = condition.getConditionMap();
        if (conditionMap != null) {
            if (conditionMap.get("userId") != null)
                userId = conditionMap.get("userId").toString();
            if (conditionMap.get("name") != null)
                name = conditionMap.get("name").toString();
            if (conditionMap.get("businessKey") != null)
                businessKey = conditionMap.get("businessKey").toString();
            if (conditionMap.get("category") != null)
                category = conditionMap.get("category").toString();
        }
        List<Task> taskList;
        long count;
        //TODO 委托的任务没有显示
        TaskQuery query = taskService.createTaskQuery().taskCandidateOrAssigned(userId);
        if (!StrUtil.isEmpty(name)) {
            query = query.taskNameLike(name);
        }
        if (!StrUtil.isEmpty(businessKey)) {
            query = query.processInstanceBusinessKey(businessKey);
        }
        if (!StrUtil.isEmpty(category)) {
            List<String> categorys = new ArrayList<>();
            categorys.add(category);
            query = query.processCategoryIn(categorys);
        }
        count = query.count();
        taskList = query.orderByTaskCreateTime().desc().listPage((pageInfo.getPageNum() - 1) * pageInfo
                .getPageSize(), pageInfo.getPageSize());
        pageInfo.setCount((int) count);
        List<TaskVo> voList = new ArrayList<>();
        for (Task task : taskList) {
            TaskVo vo = new TaskVo();
            BeanUtils.copyProperties(task, vo);
            //可在此添加额外信息
            if (!StrUtil.isEmpty(task.getAssignee())) {
                vo.setAssigneeName(identityPageService.getUser(task.getAssignee()).getFirstName());
            }
            //委托人
            if (!StrUtil.isEmpty(task.getOwner())) {
                String owner = identityPageService.getUser(task.getOwner()).getFirstName();
                if (StrUtil.isEmpty(vo.getAssigneeName())) {
                    vo.setAssigneeName(owner);
                } else {
                    vo.setAssigneeName(vo.getAssigneeName() + "(委托人:" + owner + ")");
                }
            }
            if (StrUtil.isEmpty(vo.getAssigneeName())) {
                Map<String, String> map = getTaskCandidateUser(task.getId());
                String userNames = map.get("names");
                if (!StrUtil.isEmpty(userNames))
                    vo.setAssigneeName(userNames);
            }
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task
                    .getProcessInstanceId()).singleResult();
            vo.setProcessInstanceName(processInstance.getName());
            //判断是否可以取消签收，要看是否通过候选人、候选组选择审批人的，不然指定审批人的任务取消签收后变成游离状态，不会出现在任何人的待办里
            //所以通过initialAssignee和assignee判断是否取消签收有问题（候选人签收后initialAssignee和assignee一样）
            vo.setCanUnclaim(getTaskState(task.getId()) ? "0" : "1");
            vo.setStartUserId(runtimePageService.getStartUserId(task.getId()));
            vo.setStartUserName(identityPageService.getUserNamesByUserIds(vo.getStartUserId()));
            voList.add(vo);
        }
        return voList;
    }

    /**
     * 批量委托任务，单个候选人，或者指定了受理人
     *
     * @param assignee 委托人
     * @param attorney 被委托人
     * @param moduleId 业务ids
     * @return
     */
    @Override
    public List<TaskVo> delegateTasks(String assignee, String attorney, String moduleId) {
        List<Task> tasks = taskService.createTaskQuery().taskCandidateOrAssigned(assignee).list();
        List<TaskVo> volist = new ArrayList<>();
        //判断是单个候选人还是设置了受理人
        for (Task task : tasks) {
            TaskEntity taskEntity = (TaskEntity) task;
            TaskVo vo = new TaskVo();
            BeanUtils.copyProperties(taskEntity, vo);
            vo.setProcessInstanceName(taskEntity.getProcessInstance().getName());
            if (!StrUtil.isEmpty(task.getAssignee()) && needDelegate(taskEntity, moduleId)) {
                //TODO MESSAGE
                taskService.delegateTask(task.getId(), attorney);
                volist.add(vo);
            } else {
                //单个候选人的情况下，代理任务，防止无法审批的情况下任务无人处理；多个候选人的情况下不代理（让其他人处理）
                Set<String> userIds = getCandidateUserForTask(taskEntity);
                if (userIds.size() == 1 && needDelegate(taskEntity, moduleId)) {
                    //TODO MESSAGE
                    String candidateUser = userIds.iterator().next();
                    taskService.claim(task.getId(), candidateUser);
                    taskService.delegateTask(task.getId(), attorney);
                    volist.add(vo);
                }
            }
        }
        return volist;
    }

    //是否需要代理,根据业务类型
    public boolean needDelegate(TaskEntity taskEntity, String moduleId) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(taskEntity
                .getProcessDefinitionId()).singleResult();
        String category = processDefinition.getCategory();
        String hql = "from Module where code='" + category + "'";
        Module module = this.get(hql);
        if (StrUtil.isEmpty(moduleId)) {
            return false;
        } else if (moduleId.indexOf(module.getId()) > -1) {
            return true;
        }
        return false;
    }

    @Override
    public Set<String> getCandidateUserForTask(TaskEntity taskEntity) {
        List<IdentityLinkEntity> identityLinks = taskEntity.getIdentityLinks();
        //使用Set过滤掉重复候选组的重复用户
        Set<String> userIds = new HashSet<>();
        for (IdentityLinkEntity identityLink : identityLinks) {
            if (identityLink.getType().equals(IdentityLinkType.CANDIDATE)) {
                if (!StrUtil.isEmpty(identityLink.getUserId())) {
                    userIds.add(identityLink.getUserId());
                } else if (!StrUtil.isEmpty(identityLink.getGroupId())) {
                    userIds.addAll(identityPageService.getUserIdsByGroupIds(identityLink.getGroupId()));
                }
            }
        }
        return userIds;
    }

    /**
     * 提交流程
     * @param taskId
     * @param formData
     * @return
     */
    @Override
    public Result submitTask(String taskId, Map<String, String> formData) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String formKey = formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        Object renderForm = null;
        if (!StrUtil.isEmpty(formKey) && formKey.endsWith(".form"))
            renderForm = formService.getRenderedTaskForm(taskId);
        if (renderForm != null) {
            //通过formKey获取的数据
            taskService.setVariablesLocal(task.getId(), formData);
            //委托人处理
            if (DelegationState.PENDING == task.getDelegationState()) {
                taskService.resolveTask(taskId);
            }
            Map<String,Object> variables=new HashMap<>();
            for (String s : formData.keySet()) {
                variables.put(s,formData.get(s));
            }
            taskService.complete(taskId,variables);
        } else {
            //获取的数据为formProperties
            List<FormProperty> formProperties = formService.getTaskFormData(task.getId()).getFormProperties();
            Map<String, Object> taskVariables = runtimePageService.getTaskVariables(formProperties, formData);

            taskService.setVariablesLocal(taskId, taskVariables);
            if (DelegationState.PENDING == task.getDelegationState()) {
                taskService.resolveTask(taskId);
            }
            taskService.complete(taskId,taskVariables);
        }
        //任务完成后，由TaskCreatedListener设置委托

        System.out.println(formData);
        return new Result(true);
    }

    /**
     * 获取已办的任务李彪
     *
     * @param condition 查询条件
     * @param pageInfo  分页信息
     * @return
     */
    public List<TaskDoneVo> getTaskDoneList(QueryCondition condition, PageInfo pageInfo) {
        String name = null;        //流程实例名称
        String businessKey = null;//业务key
        String category = null;    //业务类型编码
        String userId = null;//执行人
        String startTime = null;//开始启动时间
        String endTime = null;//结束启动时间
        Map<String, String> conditionMap = condition.getConditionMap();
        if (conditionMap != null) {
            if (conditionMap.get("userId") != null)
                userId = conditionMap.get("userId").toString();
            if (conditionMap.get("name") != null)
                name = conditionMap.get("name").toString();
            if (conditionMap.get("businessKey") != null)
                businessKey = conditionMap.get("businessKey").toString();
            if (conditionMap.get("category") != null)
                category = conditionMap.get("category").toString();
            if (conditionMap.get("startTime") != null)
                startTime = conditionMap.get("startTime");
            if (conditionMap.get("endTime") != null)
                endTime = conditionMap.get("endTime");
        }

        List<TaskDoneVo> volist = new ArrayList<>();
        List<HistoricProcessInstance> processInstanceList;
        long count;
        //通过此种方式过滤掉签收后出现在已办的情况
        Set<String> processInstanceIdSet = new HashSet<>();
        List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery().taskInvolvedUser
                (userId).finished().orderByTaskCreateTime().desc().list();
        for (HistoricTaskInstance taskInstance : taskInstances) {
            processInstanceIdSet.add(taskInstance.getProcessInstanceId());
        }
        //用户启动的流程
        List<HistoricProcessInstance> instances = historyService.createHistoricProcessInstanceQuery().startedBy(userId).list();
        for (HistoricProcessInstance instance : instances) {
            processInstanceIdSet.add(instance.getId());
        }

        if (processInstanceIdSet.isEmpty()) {
            pageInfo.setCount(0);
            return volist;
        }
        //---------------------------------------------
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().processInstanceIds(processInstanceIdSet);
        if (!StrUtil.isEmpty(userId))
            query.involvedUser(userId);
        if (!StrUtil.isEmpty(name))
            query.processInstanceNameLike(name);
        if (!StrUtil.isEmpty(businessKey)) {
            query = query.processInstanceBusinessKey(businessKey);
        }
        if (!StrUtil.isEmpty(category)) {
            query = query.processDefinitionCategory(category);
        }
        if (!StrUtil.isEmpty(startTime)) {
            try {
                Date startDate = DateUtil.parse(startTime + " 00:00:00", DateUtil.formatStr_yyyyMMddHHmmss);
                query.startedAfter(startDate);
            } catch (ParseException ex) {
                logger.error("开始启动时间转化错误{} ", ex.getMessage());
            }
        }
        if (!StrUtil.isEmpty(endTime)) {
            try {
                Date endDate = DateUtil.parse(endTime + " 23:59:59", DateUtil.formatStr_yyyyMMddHHmmss);
                query.startedBefore(endDate);
            } catch (ParseException ex) {
                logger.error("结束启动时间转化错误{} ", ex.getMessage());
            }
        }
        count = query.count();
        processInstanceList = query.orderByProcessInstanceStartTime().desc().listPage((pageInfo.getPageNum() - 1) *
                pageInfo.getPageSize(), pageInfo.getPageSize());

        for (HistoricProcessInstance processInstance : processInstanceList) {
            TaskDoneVo vo = new TaskDoneVo();
            BeanUtils.copyProperties(processInstance, vo);
            vo.setStartUserName(identityPageService.getUserNamesByUserIds(vo.getStartUserId()));
            //是否办结
            if (processInstance.getEndTime() != null) {
                vo.setFlowState(Constants.STATE_INSTANCE_DONE);
            } else {
                vo.setFlowState(Constants.STATE_INSTANCE_DOING);
            }
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId
                    (processInstance.getProcessDefinitionId()).singleResult();
            vo.setCategory(processDefinition.getCategory());
            vo.setCanWithdraw(canWithdraw(processInstance, userId).isSuccess() ? "1" : "0");
            volist.add(vo);
        }
        pageInfo.setCount((int) count);
        return volist;


    }

    /**
     * 判断流程是否可撤回
     * 历史流程节点中最后一个审批人是userId
     */
    @Override
    public Result canWithdraw(HistoricProcessInstance processInstance, String userId) {
        List<HistoricTaskInstance> taskInstances = historyService.createHistoricTaskInstanceQuery().processUnfinished()
                .processInstanceId(processInstance.getId()).orderByTaskCreateTime().desc().orderByTaskId().desc()
                .list();
        //Task activeTask=taskService.createTaskQuery().processInstanceId(processInstance.getId()).singleResult();
        if (taskInstances.isEmpty() || taskInstances.size() < 2)
            return new Result(false, null, "已办理，不可撤回");
        else {
            HistoricTaskInstance taskInstance = taskInstances.get(1);
            HistoricTaskInstance taskCurrent = taskInstances.get(0);
            //流程审批人未未指定（未签收+未办理）
            if (StrUtil.isEmpty(taskCurrent.getAssignee())) {
                if (taskInstance.getAssignee() != null && taskInstance.getAssignee().equals(userId)) {
                    return new Result(true, taskInstance, "可以撤回");
                }
            }
            //流程定义时指定了办理人，也可以撤回
            else if (getTaskState(taskCurrent.getId())) {
                if (taskInstance.getAssignee() != null && taskInstance.getAssignee().equals(userId)) {
                    return new Result(true, taskInstance, "可以撤回");
                }
            }

        }
        return new Result(false, null, "任务被签收或办理，不可撤回");
    }

    //获取流程状态，判断当前节点的办理人是指定的办理人还是签收的办理人
    //true=指定的审批人（可以撤回） false=签收后产生的审批人（不可撤回）
    public boolean getTaskState(String taskId) {
        List<IdentityLink> identiyLinks = taskService.getIdentityLinksForTask(taskId);
        for (IdentityLink identiyLink : identiyLinks) {
            if (IdentityLinkType.CANDIDATE.equals(identiyLink.getType())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 流程撤回  TODO MESSAGE 流程撤回需要给相关人员发送消息提醒
     *
     * @param instanceId 历史流程实例ID
     * @param userId     用户ID
     * @return
     */
    @Override
    public Result withdrawTask(String instanceId, String userId) {
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId
                (instanceId).singleResult();
        Result result = this.canWithdraw(processInstance, userId);
        if (!result.isSuccess()) {
            return new Result(false, "不可撤回", "该任务已经被签收或者办理，无法撤回，请查看流程明细");
        } else {
            HistoricTaskInstance taskInstance = (HistoricTaskInstance) result.getData();
            final TaskEntity task = (TaskEntity) taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
            try {
                this.jumpTask(task, taskInstance.getTaskDefinitionKey());
                //删除历史记录，填充签收人
                this.deleteCurrentTaskInstance(task.getId(), taskInstance);
                return new Result(true);
            } catch (Exception ex) {
                return new Result(false, "撤回异常", "任务撤回发生异常,异常原因：" + ex.getMessage());
            }

        }
    }


    /**
     * 流程跳跃到任意节点
     *
     * @param currentTaskEntity       当前任务实例
     * @param targetTaskDefinitionKey 任务定义节点key(目标节点)
     * @throws Exception
     */
    public void jumpTask(final TaskEntity currentTaskEntity, String targetTaskDefinitionKey) throws Exception {
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition
                (currentTaskEntity.getProcessDefinitionId());
        final ActivityImpl activity = processDefinition.findActivity(targetTaskDefinitionKey);

        final ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId
                (currentTaskEntity.getExecutionId()).singleResult();

        //包装一个Command对象
        ((RuntimeServiceImpl) runtimeService).getCommandExecutor().execute(
                new Command<Void>() {
                    @Override
                    public Void execute(CommandContext commandContext) {
                        //创建新任务
                        //execution.setActivity(activity);
                        execution.executeActivity(activity);


                        //删除当前的任务
                        //不能删除当前正在执行的任务，所以要先清除掉关联
                        currentTaskEntity.setExecutionId(null);
                        taskService.saveTask(currentTaskEntity);
                        taskService.deleteTask(currentTaskEntity.getId(), true);

                        return null;
                    }
                });

    }


    //删除历史记录，回填签收人以保证流程明细显示正确
    public Result deleteCurrentTaskInstance(String taskId, HistoricTaskInstance taskInstance) {
        //删除正在执行的任务
        //删除HistoricTaskInstance
        String sql_task = "delete from " + managementService.getTableName(HistoricTaskInstance.class) + " where " +
                "ID_='" + taskId + "' or ID_='" + taskInstance.getId() + "'";
        this.executeSql(sql_task);
        //删除HistoricActivityInstance
        String sql_activity = "delete from " + managementService.getTableName(HistoricActivityInstance.class) + " where " +
                "TASK_ID_='" + taskId + "' or TASK_ID_='" + taskInstance.getId() + "'";
        this.executeSql(sql_activity);
        //获取当前的任务,保存签收人
        Task task = taskService.createTaskQuery().executionId(taskInstance.getExecutionId()).singleResult();
        task.setAssignee(taskInstance.getAssignee());
        task.setOwner(taskInstance.getOwner());
        taskService.saveTask(task);
        //解决HistoricActivityInstance的Assignee为空的现象
        if (!StrUtil.isEmpty(taskInstance.getAssignee())) {
            String sql_update = "update " + managementService.getTableName(HistoricActivityInstance.class) + " set " +
                    "ASSIGNEE_='" + taskInstance.getAssignee() + "' where TASK_ID_='" + task.getId() + "'";
            this.executeSql(sql_update);
        }

        String sql_update_execution = "update " + managementService.getTableName(Execution.class) + " set " +
                "ACT_ID_='" + taskInstance.getTaskDefinitionKey() + "' where ID_='" + taskInstance.getExecutionId() + "'";
        this.executeSql(sql_update_execution);
        return new Result(true);
    }


    /**
     * 获取任务候选人
     *
     * @param taskId 任务ID
     * @return
     */
    @Override
    public Set<User> getTaskCandidate(String taskId) {
        Set<User> users = new HashSet();
        List identityLinkList = taskService.getIdentityLinksForTask(taskId);
        if (identityLinkList != null && identityLinkList.size() > 0) {
            for (Iterator iterator = identityLinkList.iterator(); iterator.hasNext(); ) {
                IdentityLink identityLink = (IdentityLink) iterator.next();
                if (identityLink.getUserId() != null) {
                    User user = identityPageService.getUser(identityLink.getUserId());
                    if (user != null)
                        users.add(user);
                }
                if (identityLink.getGroupId() != null) {
                    // 根据组获得对应人员
                    List userList = identityService.createUserQuery()
                            .memberOfGroup(identityLink.getGroupId()).list();
                    if (userList != null && userList.size() > 0)
                        users.addAll(userList);
                }
            }

        }
        return users;
    }


    /**
     * 获取候选用户
     *
     * @param taskId
     * @return Map names  ids
     */
    @Override
    public Map<String, String> getTaskCandidateUser(String taskId) {
        Set<User> users = getTaskCandidate(taskId);
        String[] names = new String[users.size()];
        String[] ids = new String[users.size()];
        Map<String, String> map = new HashMap<>();
        int i = 0;
        for (User user : users) {
            names[i] = user.getFirstName();
            ids[i] = user.getId();
            i++;
        }

        map.put("names", StrUtil.join(names));
        map.put("ids", StrUtil.join(ids));
        return map;
    }

    //TODO MESSAGE
    @Override
    public Result claimTask(String taskId, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (StrUtil.isEmpty(task.getAssignee())) {
            taskService.claim(taskId, assignee);
            return new Result(true);
        } else {
            String assgineeName = identityPageService.getUserNamesByUserIds(task.getAssignee());
            return new Result(false, "签收失败", "签收任务失败，该任务已被【" + assgineeName + "】签收");
        }
    }

    //TODO MESSAGE
    @Override
    public Result unclaimTask(String taskId, String assignee) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (StrUtil.isEmpty(task.getAssignee())) {
            return new Result(false, "取消失败", "取消签收失败，该任务未被任何人签收");
        } else if (!assignee.equals(task.getAssignee())) {
            String assgineeName = identityPageService.getUserNamesByUserIds(task.getAssignee());
            return new Result(false, "取消失败", "取消签收失败，任务已被【" + assgineeName + "】签收");
        } else {
            taskService.unclaim(taskId);
            //List<IdentityLink> identityLinks=taskService.getIdentityLinksForTask(taskId);
            //taskService.deleteUserIdentityLink(taskId,assignee, IdentityLinkType.PARTICIPANT);
            //runtimeService.deleteUserIdentityLink(task.getProcessInstanceId(),assignee,IdentityLinkType.PARTICIPANT);
            return new Result(true);
        }
    }


}
