package com.test.activiti;

import com.cnpc.framework.activiti.service.RuntimePageService;
import com.cnpc.framework.testng.BaseTest;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.*;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by billJiang on 2017/6/20.
 * e-mail:475572229@qq.com  qq:475572229
 * 请假申请流程测试
 */
public class VacationTest extends BaseTest {
    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private HistoryService historyService;

    @Resource
    private RuntimePageService runtimePageService;

    @Autowired
    private FormService formService;

    @BeforeClass
    public void before() {
        List<ProcessInstance> instances = runtimeService.createProcessInstanceQuery().list();
        for (ProcessInstance instance : instances) {
            runtimeService.deleteProcessInstance(instance.getId(), "test");
            historyService.deleteHistoricProcessInstance(instance.getId());
        }

        List<HistoricProcessInstance> historicProcessInstances=historyService.createHistoricProcessInstanceQuery()
                .list();
        for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
        }
    }


    /**
     * 流程测试-请假流程-同意
     *
     * @param key      请假流程定义key
     * @param userId   用户ID
     * @param groupId  审批人所在组ID
     * @param approver 审批人
     * @param days     请假天数
     * @param result   审批人审批结果
     */
    @Test(dataProvider = "dataProvider", groups = {"activiti-test"})
    public void testFlow(String key, String userId, String groupId, String approver, Long days, String result) {
        //----------------设置流程启动人----------------
        identityService.setAuthenticatedUserId(userId);
        System.out.println("设置流程启动人：" + userId);

        //-----------------流程启动---------------------
        // 根据ID启动
        //ProcessInstance pi=runtimeService.startProcessInstanceById(id);
        //根据key获取最新版本的流程定义
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, "业务表的id");
        //设置流程实例名称
        runtimeService.setProcessInstanceName(processInstance.getId(), "流程实例名称设置：");
        System.out.println("根据key启动最新的流程,流程定义ID：" + processInstance.getProcessDefinitionId());

        //----------------获取流程启动人-----------------
        /*
        ProcessDefinitionEntity processDefinition=(ProcessDefinitionEntity)repositoryService.getProcessDefinition(processInstance.getProcessDefinitionId());
        String initiator=processDefinition.getProperty(BpmnParse.PROPERTYNAME_INITIATOR_VARIABLE_NAME).toString();
        String assign=runtimeService.getVariable(processInstance.getProcessInstanceId(),initiator).toString();
        */
        String startUserId = runtimePageService.getStartUserId(processInstance);
        System.out.println("获取流程启动人：" + startUserId);

        //----------------发起人填报申请（可和启动配置在一起）-----------------
        //查询发起人的流程
        Task task = taskService.createTaskQuery().processInstanceId(processInstance.getProcessInstanceId()).taskAssignee(startUserId).singleResult();
        //通过taskId获取流程启动人
        System.out.println("通过taskId获取流程启动人:" + runtimePageService.getStartUserId(task.getId()));

        Map<String, Object> vactionVars = new HashMap<>();
        vactionVars.put("days", days);
        vactionVars.put("startDate", new Date());
        vactionVars.put("motivation", "不想上班了，就是这么任性");
        taskService.setVariables(task.getId(), vactionVars);
        //taskService.claim(task.getId(),startUserId);
        //taskService.complete(task.getId(),vactionVars);
        taskService.complete(task.getId(), vactionVars);

        //----------------审批人审批 -----------------
        //通过用户组获取任务
        List<Task> taskGroups = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).
                taskCandidateGroup(groupId).list();
        System.out.println("用户组审批：" + taskGroups.size());
        //通过taskCandidateOrAssigned获取审批人，taskInvoledUser可获取委托人与代理人相关流程
        List<Task> tasks = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).
                taskCandidateOrAssigned(approver).list();
        for (Task task1 : tasks) {
            String assignee = task1.getAssignee();
            //指定代理人，直接办理；否则需要签收
            if (StrUtil.isEmpty(assignee))
                taskService.claim(task1.getId(), approver);
            //获取表单变量
            List<FormProperty> list = formService.getTaskFormData(task1.getId()).getFormProperties();
            if (!list.isEmpty()) {
                for (FormProperty formProperty : list) {
                    System.out.println(formProperty.getId() + "     " + formProperty.getName() + "      " + formProperty.getValue());
                }
            }
            //审批
            Map<String, Object> vars = new HashMap<>();
            if (result.equals("1")) {
                vars.put("approved", "true");
                vars.put("suggestion", "同意请假");
            } else {
                vars.put("approved", "false");
                vars.put("suggestion", "拒绝");
            }
            taskService.complete(task1.getId(), vars);

        }
        //----------------销假 -----------------
       /* Task taskFinish = taskService.createTaskQuery().processDefinitionId(processInstance.getProcessDefinitionId()).
                taskCandidateOrAssigned(userId).singleResult();
        taskService.addComment(taskFinish.getId(),taskFinish.getProcessInstanceId(),"流程审批结束");
        taskService.complete(taskFinish.getId());*/

    }
}
