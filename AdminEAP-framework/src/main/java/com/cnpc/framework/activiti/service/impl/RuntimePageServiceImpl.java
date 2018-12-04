package com.cnpc.framework.activiti.service.impl;

import com.cnpc.framework.activiti.pojo.ActivityVo;
import com.cnpc.framework.activiti.pojo.Constants;
import com.cnpc.framework.activiti.pojo.ProcessInstanceVo;
import com.cnpc.framework.activiti.service.IdentityPageService;
import com.cnpc.framework.activiti.service.RuntimePageService;
import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.impl.BaseServiceImpl;
import com.cnpc.framework.query.entity.QueryCondition;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.*;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.FormServiceImpl;
import org.activiti.engine.impl.RuntimeServiceImpl;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.FixedValue;
import org.activiti.engine.impl.el.JuelExpression;
import org.activiti.engine.impl.el.UelExpressionCondition;
import org.activiti.engine.impl.form.*;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.juel.TreeValueExpression;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmActivity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.scripting.ScriptingEngines;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by billJiang on 2017/6/21.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程实例服务实现
 */
@Service("runtimePageService")
public class RuntimePageServiceImpl extends BaseServiceImpl implements RuntimePageService {

    private static final Logger logger = LoggerFactory.getLogger(RuntimePageServiceImpl.class);

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    @Resource
    private IdentityPageService identityPageService;

    @Autowired
    private IdentityService identityService;


    @Autowired
    private FormService formService;


    @Override
    public String getStartUserId(ProcessInstance processInstance) {
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition
                (processInstance.getProcessDefinitionId());
        String initiator = processDefinition.getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME).toString();
        String assign = runtimeService.getVariable(processInstance.getProcessInstanceId(), initiator).toString();
        return assign;
    }


    @Override
    public String getStartUserId(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        HistoricProcessInstance historicProcessInstance =
                historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .singleResult();
        return historicProcessInstance.getStartUserId();
    }


    /**
     * 启动流程
     *
     * @param processDefinitionKey 流程定义编号
     * @param name                 流程实例名称
     * @param variables            流程变量
     * @param userId
     * @param businessKey          业务ID
     * @return 是否启动成功 success=true是第二个参数为流程实例ID
     */
    @Override
    public Result startProcessInstanceByKey(String processDefinitionKey, String name, Map<String, Object> variables,
                                            String userId, String businessKey) {
        //校验流程定义是否存在
        final ProcessDefinitionEntity processDefinitionEntity = (ProcessDefinitionEntity) repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processDefinitionKey).latestVersion().active().singleResult();
        if (processDefinitionEntity == null)
            return new Result(false, "启动失败", "流程启动失败key='" + processDefinitionKey + "'的流程定义不存在");
        //设置流程启动人
        identityService.setAuthenticatedUserId(userId);
        //启动流程, 根据key获取最新版本的流程定义
        ProcessInstance processInstance = null;
        try {
            //---------------------解决在初始化时，表单变量取不到默认值的问题-------------------------------------
            Object retObj = ((FormServiceImpl) formService).getCommandExecutor().execute(new Command<Object>() {
                @Override
                public Object execute(CommandContext commandContext) {
                    ProcessDefinitionEntity processDefinition = commandContext
                            .getProcessEngineConfiguration()
                            .getDeploymentManager()
                            .findDeployedProcessDefinitionById(processDefinitionEntity.getId());
                    DefaultStartFormHandler startFormHandler = (DefaultStartFormHandler) processDefinition.getStartFormHandler();
                    for (FormPropertyHandler formPropertyHandler : startFormHandler.getFormPropertyHandlers()) {
                        //默认值
                        formPropertyHandler.setDefaultExpression(formPropertyHandler.getVariableExpression());
                    }
                    return startFormHandler.createStartFormData(processDefinition);
                }
            });
            //StartFormData startFormData1 = formService.getStartFormData(processDefinitionEntity.getId());
            StartFormData startFormData = (StartFormData) retObj;
            //--------------------------------------------------------------------------------------------

            if (startFormData != null) {
                List<FormProperty> properties = startFormData.getFormProperties();
                for (FormProperty property : properties) {
                    if (!variables.containsKey(property.getId())) {
                        variables.put(property.getId(), this.getVariable(property, property.getValue()));
                    }
                }
            }
            processInstance = runtimeService.startProcessInstanceByKey(processDefinitionKey, businessKey, variables);

        } catch (Exception ex) {
            return new Result(false, "启动异常", "流程启动异常,异常原因：" + ex.getMessage());
        }
        runtimeService.setProcessInstanceName(processInstance.getId(), name);
        return new Result(true, processInstance.getId(), "启动成功");
    }


    @Override
    public List<ProcessInstanceVo> getProcessInstanceList(QueryCondition condition, PageInfo pageInfo) {
        //查询条件
        String name = null;        //流程实例名称
        String businessKey = null;//业务key
        String category = null;    //业务类型编码
        Map<String, String> conditionMap = condition.getConditionMap();
        if (conditionMap != null) {
            if (conditionMap.get("name") != null)
                name = conditionMap.get("name").toString();
            if (conditionMap.get("businessKey") != null)
                businessKey = conditionMap.get("businessKey").toString();
            if (conditionMap.get("category") != null)
                category = conditionMap.get("category").toString();
        }
        List<ProcessInstance> instanceList;
        long count;
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
        if (!StrUtil.isEmpty(name)) {
            query = query.processInstanceNameLike(name);
        }
        if (!StrUtil.isEmpty(businessKey)) {
            query = query.processInstanceBusinessKey(businessKey);
        }
        if (!StrUtil.isEmpty(category)) {
            query = query.processDefinitionCategory(category);
        }
        count = query.count();
        instanceList = query.orderByProcessInstanceId().desc().listPage((pageInfo.getPageNum() - 1) * pageInfo
                .getPageSize(), pageInfo.getPageSize());
        pageInfo.setCount((int) count);
        //原来类型为ExecutionEntity，再向前台json格式化的时候出现异常，所以转化为ProcessInstanceVo
        List<ProcessInstanceVo> volist = new ArrayList<>();
        for (ProcessInstance processInstance : instanceList) {
            ProcessInstanceVo vo = new ProcessInstanceVo();
            BeanUtils.copyProperties(processInstance, vo);
            //业务类型
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processInstance
                    .getProcessDefinitionId());
            vo.setCategory(processDefinition.getCategory());
            vo.setStartUserId(getStartUserId(processInstance));
            vo.setStartUserName(identityPageService.getUserNamesByUserIds(vo.getStartUserId()));
            volist.add(vo);
        }
        return volist;
    }


    //流程明细 不分页
    @Override
    public List<ActivityVo> getActivityList(QueryCondition condition, PageInfo pageInfo) {
        String processInstanceId = null;
        Map<String, String> conditionMap = condition.getConditionMap();
        if (conditionMap != null) {
            if (conditionMap.get("processInstanceId") != null)
                processInstanceId = conditionMap.get("processInstanceId").toString();
        }
        //已执行的流程节点
        List<HistoricActivityInstance> historicActivityInstanceList = historyService
                .createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

        //活动的节点ID
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId
                (processInstanceId).singleResult();
        /*String activityId=processInstance.getActivityId();*/
        List<String> activeIds = new ArrayList<>();
        ProcessDefinitionEntity processDefinition;
        //已完成后processInstance为null
        String startUserId=null;
        if (processInstance != null) {
            activeIds = runtimeService.getActiveActivityIds(processInstanceId);
            processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(processInstance
                    .getProcessDefinitionId());
        } else {
            HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId
                            (processInstanceId).singleResult();
            processDefinition = (ProcessDefinitionEntity) repositoryService.getProcessDefinition
                    (historicProcessInstance.getProcessDefinitionId());
            startUserId=historicProcessInstance.getStartUserId();
        }

        List<ActivityImpl> activityList = processDefinition.getActivities();


        List<ActivityVo> voList = new ArrayList<>();
        for (HistoricActivityInstance historicActivityInstance : historicActivityInstanceList) {
            //过滤掉非用户任务
            if (!historicActivityInstance.getActivityType().equals("userTask") && !historicActivityInstance
                    .getActivityType().equals("startEvent"))
                continue;
            ActivityVo vo = new ActivityVo();
            BeanUtils.copyProperties(historicActivityInstance, vo);
            if (historicActivityInstance.getActivityType().equals("startEvent")) {
                vo.setAssigneeName(identityPageService.getUserNamesByUserIds
                        (processInstance==null?startUserId:getStartUserId
                        (processInstance)));
            } else {
                if (!StrUtil.isEmpty(vo.getAssignee())) {
                    vo.setAssigneeName(identityPageService.getUserNamesByUserIds(vo.getAssignee()));
                } else {
                    //未指定审批审批人获取
                    vo.setAssigneeName(getCandidateUserNames(getActivity(historicActivityInstance, activityList),
                            processInstanceId));
                }
            }
            //节点状态
            if (vo.getEndTime() != null)
                vo.setActivityState(Constants.STATE_DONE);
            else
                vo.setActivityState(Constants.STATE_DOING);

            //获取审批结果和审批意见
            Map<String, String> approveMap = getApproveMap(historicActivityInstance);
            if (!approveMap.isEmpty()) {
                vo.setApproved(approveMap.get(Constants.APPROVE_RESULT));
                vo.setSuggestion(approveMap.get(Constants.APPROVE_SUGGESTION));
            }
            voList.add(vo);
        }


        //活动节点对象
        List<ActivityImpl> nextActivities = new ArrayList<>();
       /* for (ActivityImpl activity : activityList) {
            for (String activeId : activeIds) {
                if (activity.getId().equals(activeId)) {
                    nextActivities.add(activity);
                    break;
                }
            }
        }*/
        PvmActivity curActivity = null;
        //另一种写法
        for (String activeId : activeIds) {
            ActivityImpl activityImpl = processDefinition.findActivity(activeId);
            if (activityImpl != null) {
                nextActivities.add(activityImpl);
                curActivity = activityImpl;
            }
        }


        //下一步节点(可能已经走过的节点：回退节点)
        /*for (ActivityImpl nextActivity : nextActivities) {
            List<PvmTransition> transitions = nextActivity.getOutgoingTransitions();
            for (PvmTransition transition : transitions) {
                List<PvmActivity> activities = findNextUserTask(transition);
                if (!activities.isEmpty()) {
                    for (PvmActivity activity : activities) {
                        if (findInDoneActivityList(historicActivityInstanceList, activity)) {
                            ActivityVo vo = new ActivityVo();
                            vo.setId(activity.getId());
                            vo.setActivityId(activity.getId());
                            vo.setActivityName(activity.getProperty("name").toString());
                            vo.setAssigneeName(getCandidateUserNames((ActivityImpl) activity, processInstanceId));
                            vo.setActivityState(Constants.STATE_TODO);
                            voList.add(vo);
                            curActivity = activity;
                        }
                    }
                }
            }
        }*/
        if (processInstance != null)//运行中
            findNextActivity(voList, processInstanceId, curActivity);
        //尚未执行的流程节点
     /*   if (processInstance != null) {
            for (ActivityImpl activity : activityList) {
                boolean done = false;
                for (ActivityVo activityVo : voList) {
                    if (activity.getId().equals(activityVo.getActivityId())) {
                        done = true;
                        break;
                    }
                }
                if (!done && activity.getProperty("type").equals("userTask")) {
                    ActivityVo vo = new ActivityVo();
                    vo.setId(activity.getId());
                    vo.setActivityName(activity.getProperty("name").toString());
                    vo.setAssigneeName(getCandidateUserNames(activity, processInstanceId));
                    vo.setActivityState(Constants.STATE_TODO);
                    voList.add(vo);
                }
            }
        }*/
        return voList;
    }

    //从当前节点出发
    public void findNextActivity(List<ActivityVo> voList, String processInstanceId, PvmActivity curActivity) {
        //一条道走到黑（结束）
        List<PvmTransition> nextTrans = curActivity.getOutgoingTransitions();
        for (PvmTransition nextTran : nextTrans) {
            Object flowName = nextTran.getProperty("name");
            PvmActivity activity = nextTran.getDestination();
            if ("userTask".equals(activity.getProperty("type").toString())) {
                if (flowName != null && isInApprovedText(Constants.APPROVED_PASSED, flowName.toString())) {
                    ActivityVo vo = new ActivityVo();
                    vo.setId(activity.getId());
                    vo.setActivityName(activity.getProperty("name").toString());
                    vo.setAssigneeName(getCandidateUserNames((ActivityImpl) activity, processInstanceId));
                    vo.setActivityState(Constants.STATE_TODO);
                    voList.add(vo);
                    findNextActivity(voList, processInstanceId, activity);
                } else if (flowName == null || !isInApprovedText(Constants.APPROVED_REJECT, flowName.toString())) {
                    //条件路径
                    Object conditionText = nextTran.getProperty("conditionText");
                    if (conditionText != null) {
                        boolean targetTask = isTargetTask(conditionText.toString(), processInstanceId, nextTran);
                        if (targetTask) {
                            ActivityVo vo = new ActivityVo();
                            vo.setId(activity.getId());
                            vo.setActivityName(activity.getProperty("name").toString());
                            vo.setAssigneeName(getCandidateUserNames((ActivityImpl) activity, processInstanceId));
                            vo.setActivityState(Constants.STATE_TODO);
                            voList.add(vo);
                            findNextActivity(voList, processInstanceId, activity);
                        }
                    }
                }
            } else {
                findNextActivity(voList, processInstanceId, activity);
            }

        }
    }

    /**
     * 节点上的文字是否在可选文字中
     *
     * @param type 同意/拒绝
     * @param text 节点文字
     * @return
     */
    public boolean isInApprovedText(String type, String text) {
        if (Constants.APPROVED_PASSED.equals(type)) {
            for (String s : Constants.APPROVED_PASSED_TEXT) {
                if (s.equals(text)) {
                    return true;
                }
            }
            return false;
        } else if (Constants.APPROVED_REJECT.equals(type)) {
            for (String s : Constants.APPROVED_REJECT_TEXT) {
                if (s.equals(text)) {
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /**
     * TODO 局部变量判断
     * 判断当前是否是合适的路径
     *
     * @param expressionText    表达式
     * @param processInstanceId 实例ID
     * @return true符合条件条件的路径
     */
    public boolean isTargetTask(final String expressionText, String processInstanceId, final PvmTransition transition) {

        final ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().executionId(processInstanceId)
                .singleResult();
        Boolean result = ((RuntimeServiceImpl) runtimeService).getCommandExecutor().execute(
                new Command<Boolean>() {
                    @Override
                    public Boolean execute(CommandContext commandContext) {
                        UelExpressionCondition flowCondition = (UelExpressionCondition) transition.getProperty("condition");
                        boolean evel_ret = flowCondition.evaluate(transition.getId(), execution);
                        return evel_ret;
                    }
                });
        return result;
        //TODO 另一种写法 自定义juel解析
        /*ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        Map<String, Object> variables = runtimeService.getVariables(processInstanceId);
        for (String key : variables.keySet()) {
            context.setVariable(key, factory.createValueExpression(variables.get(key), variables.get(key).getClass()));
        }
        try {
            ValueExpression e = factory.createValueExpression(context, expressionText, boolean.class);
            return (boolean) e.getValue(context);
        } catch (Exception ex) {
            return false;
        }*/
    }


    /**
     * 当前节点是否已经在执行节点中
     *
     * @param historicActivityInstanceList 已近执行的任务节点
     * @param activity                     判断节点
     * @return false 不在完成节点中 true 在完成节点中
     */
    public boolean findInDoneActivityList(List<HistoricActivityInstance> historicActivityInstanceList, PvmActivity
            activity) {
        for (HistoricActivityInstance activityVo : historicActivityInstanceList) {
            if (activityVo.getActivityId().equals(activity.getId()) && activityVo.getEndTime() == null) {
                return false;
            }
        }
        for (HistoricActivityInstance activityVo : historicActivityInstanceList) {
            if (activityVo.getActivityId().equals(activity.getId()) && activityVo.getEndTime() != null) {
                return true;
            }
        }
        return false;
    }


    /**
     * 历史节点对应的流程定义节点
     *
     * @param activityInstance 历史节点
     * @param activities       流程定义节点
     * @return
     */
    public ActivityImpl getActivity(HistoricActivityInstance activityInstance, List<ActivityImpl> activities) {
        for (ActivityImpl activity : activities) {
            if (activity.getId().equals(activityInstance.getActivityId())) {
                return activity;
            }
        }
        return null;
    }


    /**
     * 获取历史审批结果和审批意见
     *
     * @param activityInstance 历史任务节点
     * @return
     */
    public Map<String, String> getApproveMap(HistoricActivityInstance activityInstance) {
        //审批结果和审批意见为Local变量
        Map<String, String> map = new HashMap<>();
        if (StrUtil.isEmpty(activityInstance.getTaskId()))
            return map;
        List<HistoricVariableInstance> variableInstances = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(activityInstance.getProcessInstanceId()).taskId(activityInstance.getTaskId()).list();
        for (HistoricVariableInstance variableInstance : variableInstances) {
            if (variableInstance.getVariableName().equals(Constants.APPROVE_RESULT)) {
                map.put(Constants.APPROVE_RESULT, variableInstance.getValue().toString());
            } else {
                map.put(Constants.APPROVE_SUGGESTION, variableInstance.getValue().toString());
            }
        }
        return map;
    }

    /**
     * 递归查找下一个用户任务
     *
     * @param tranistion
     * @return
     */
    public List<PvmActivity> findNextUserTask(PvmTransition tranistion) {
        List<PvmActivity> activities = new ArrayList<>();
        PvmActivity nextActivity = tranistion.getDestination();
        if (nextActivity == null)
            return activities;
        else if ("userTask".equals(nextActivity.getProperty("type"))) {
            activities.add(nextActivity);
            return activities;
        } else {
            List<PvmTransition> transitions = nextActivity.getOutgoingTransitions();
            for (PvmTransition transition1 : transitions) {
                activities.addAll(findNextUserTask(transition1));
            }
            return activities;
        }
    }

    /**
     * 获取尚未执行的节点的可能执行人姓名，以逗号分隔
     * 需要使用命令模式动态执行表达式 不然报lazy load expression out of activiti异常
     *
     * @param activity          流程定义的用户活动节点
     * @param processInstanceId 流程实例ID
     * @return 可能执行人姓名
     */
    public String getCandidateUserNames(final ActivityImpl activity, final String processInstanceId) {
        String result = ((RuntimeServiceImpl) runtimeService).getCommandExecutor().execute(
                new Command<String>() {
                    @Override
                    public String execute(CommandContext commandContext) {
                        String retNames = "";
                        ExecutionEntity execution = (ExecutionEntity) runtimeService.createExecutionQuery().processInstanceId
                                (processInstanceId).singleResult();
                        TaskDefinition taskDefinition = (TaskDefinition) activity.getProperties().get("taskDefinition");
                        if (taskDefinition == null)
                            return retNames;

                        //代理人/审批人
                        String assignee = null;
                        if (taskDefinition.getAssigneeExpression() != null) {
                            try {
                                assignee = (String) taskDefinition.getAssigneeExpression().getValue(execution);
                            } catch (Exception ex) {
                                logger.error("获取收理人出错：" + ex.getMessage());
                                assignee = null;
                            }
                            retNames = assignee != null ? identityPageService.getUserNamesByUserIds(assignee) : "待定";
                            return retNames;
                        }
                        //委托人,同受理人同一人的情况下不显示
                        if (taskDefinition.getOwnerExpression() != null) {
                            String owner;
                            try {
                                owner = ((String) taskDefinition.getOwnerExpression().getValue(execution));
                            } catch (Exception ex) {
                                logger.error("获取委托人出错：" + ex.getMessage());
                                owner = null;
                            }
                            if (assignee != null && !assignee.equals(owner)) {
                                retNames = retNames + "(委托人:" + owner != null ? identityPageService.getUserNamesByUserIds
                                        (owner) : "待定)";
                            }
                        }

                        if (!StrUtil.isEmpty(retNames))
                            return retNames;
                        //候选组
                        if (!taskDefinition.getCandidateGroupIdExpressions().isEmpty()) {
                            List<String> groupIdList = new ArrayList();
                            for (Expression groupIdExpr : taskDefinition.getCandidateGroupIdExpressions()) {
                                Object value;
                                try {
                                    value = groupIdExpr.getValue(execution);
                                } catch (Exception ex) {
                                    logger.error("获取候选组出错：" + ex.getMessage());
                                    value = null;
                                }
                                if (value != null) {
                                    if (value instanceof String) {
                                        groupIdList.add(value.toString());
                                    } else if (value instanceof Collection) {
                                        groupIdList.addAll((Collection<String>) value);
                                    }
                                }
                            }
                            if (!groupIdList.isEmpty()) {
                                String[] groupIdArr = getStringArr(groupIdList.toArray());
                                return identityPageService.getUserNamesByGroupIds(StrUtil.join(groupIdArr));
                            } else {
                                return "待定";
                            }

                        } else if (!taskDefinition.getCandidateUserIdExpressions().isEmpty()) {
                            List<String> userIdList = new ArrayList();
                            for (Expression userIdExpr : taskDefinition.getCandidateUserIdExpressions()) {
                                Object value;
                                try {
                                    value = userIdExpr.getValue(execution);
                                } catch (Exception ex) {
                                    logger.error("获取候选人出错：" + ex.getMessage());
                                    value = null;
                                }
                                if (value != null) {
                                    if (value instanceof String) {
                                        userIdList.add((String) value);
                                    } else if (value instanceof Collection) {
                                        userIdList.addAll((Collection<String>) value);
                                    }
                                }
                            }
                            if (!userIdList.isEmpty()) {
                                String[] userIdArr = getStringArr(userIdList.toArray());
                                return identityPageService.getUserNamesByUserIds(StrUtil.join(userIdArr));
                            } else {
                                return "待定";
                            }
                        }
                        return retNames;
                    }
                });
        return result;

    }


    public String[] getStringArr(Object[] objArr) {
        String[] strArr = new String[objArr.length];
        for (int i = 0; i < objArr.length; i++) {
            strArr[i] = objArr[i].toString();
        }
        return strArr;
    }


    @Override
    public Map<String, Object> getTaskVariables(List<FormProperty> formProperties, Map<String, String> formData) {
        Map<String, Object> map = new HashMap<>();
        for (FormProperty formProperty : formProperties) {
            String id = formProperty.getId();
            if (!StrUtil.isEmpty(formData.get(id))) {
                map.put(id, this.getVariable(formProperty, formData.get(id)));
            }
        }
        return map;
    }

    @Override
    public Object getVariable(FormProperty formProperty, String value) {
        value = StrUtil.isEmpty(value) ? formProperty.getValue() : value;
        FormType formType = formProperty.getType();
        if (formType.getClass().equals(DateFormType.class)) {
            DateFormType dateType = (DateFormType) formType;
            return dateType.convertFormValueToModelValue(value);
        } else if (formType.getClass().equals(BooleanFormType.class)) {
            BooleanFormType boolType = (BooleanFormType) formType;
            return boolType.convertFormValueToModelValue(value);
        } else if (formType.getClass().equals(EnumFormType.class)) {
            EnumFormType enumType = (EnumFormType) formType;
            return enumType.convertFormValueToModelValue(value);
        } else if (formType.getClass().equals(DoubleFormType.class)) {
            DoubleFormType doubleType = (DoubleFormType) formType;
            return doubleType.convertFormValueToModelValue(value);
        } else if (formType.getClass().equals(LongFormType.class)) {
            LongFormType longType = (LongFormType) formType;
            return longType.convertFormValueToModelValue(value);
        } else {
            return value;
        }
    }


}
