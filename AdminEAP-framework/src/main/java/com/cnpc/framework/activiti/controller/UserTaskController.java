package com.cnpc.framework.activiti.controller;

import com.cnpc.framework.activiti.pojo.Constants;
import com.cnpc.framework.activiti.service.IdentityPageService;
import com.cnpc.framework.activiti.service.RuntimePageService;
import com.cnpc.framework.activiti.service.TaskPageService;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.FormService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Created by billJiang on 2017/6/21.
 * e-mail:475572229@qq.com  qq:475572229
 * 用户待办已办控制器
 */
@Controller
@RequestMapping(value = "/activiti")
public class UserTaskController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceController.class);


    @Resource
    private TaskPageService taskPageService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private FormService formService;

    @Resource
    private IdentityPageService identityPageService;

    @Resource
    private RuntimePageService runtimePageService;


    //我的待办
    @RequestMapping(value = "/task/todo/list", method = RequestMethod.GET)
    public String list_todo() {
        return "activiti/task_list_todo";
    }

    //我的已办
    @RequestMapping(value = "/task/done/list", method = RequestMethod.GET)
    public String list_done() {
        return "activiti/task_list_done";
    }

    /**
     * 签收任务 TODO 考虑撤回
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return
     */
    @RequestMapping(value = "/task/claim/{taskId}/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public Result claimTask(@PathVariable("taskId") String taskId, @PathVariable("userId") String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (!StrUtil.isEmpty(task.getAssignee())) {
            String userName = "";
            if (!task.getAssignee().equals(userId))
                userName = "【" + identityPageService.getUserNamesByUserIds(task.getAssignee()) + "】";
            return new Result(false, "任务已被签收", "签收任务失败，该任务已被" + userName + "签收");
        }
        return taskPageService.claimTask(taskId, userId);
    }

    /**
     * 取消签收
     */
    @RequestMapping(value = "/task/unclaim/{taskId}/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public Result unclaimTask(@PathVariable("taskId") String taskId, @PathVariable("userId") String userId) {
        return taskPageService.unclaimTask(taskId, userId);
    }

    /**
     * 用户办理之前确认是否被该用户签收 TODO 考虑撤回
     */
    @RequestMapping(value = "/task/checkClaim/{taskId}/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public Result checkClaim(@PathVariable("taskId") String taskId, @PathVariable("userId") String userId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (StrUtil.isEmpty(task.getAssignee())) {
            return new Result(true, false, "该任务尚未签收，是否签收并办理该任务？");
        } else if (!task.getAssignee().equals(userId)) {
            String userName = identityPageService.getUserNamesByUserIds(task.getAssignee());
            return new Result(false, "被其他用户签收", "该任务已被【" + userName + "】签收，您无法办理");
        } else {
            return new Result(true, true);
        }
    }

    /**
     * 办理任务 TODO 考虑撤回
     */
    @RequestMapping(value = "/task/deal/{taskId}", method = RequestMethod.GET)
    public String dealTask(@PathVariable("taskId") String taskId, HttpServletRequest request) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        //流程实例ID
        request.setAttribute("processInstanceId", task.getProcessInstanceId());

        //流程内置表单变量,因为这种方式，在页面展示困难，所以这里只存储相关变量
        //页面展示的内容从formKey中.form获取，以减少流程模型的配置工作量
        List<FormProperty> formProperties = formService.getTaskFormData(task.getId()).getFormProperties();
        request.setAttribute("formProperties", formProperties);

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task
                .getProcessInstanceId()).singleResult();
        String businessKey = processInstance.getBusinessKey();

        String formUrl = null;

        //获取业务url，外嵌表单展示,注入业务url 可通过local_form_url 设置，也可设置在formKey中
        for (FormProperty formProperty : formProperties) {
            if (formProperty.getId().equals(Constants.VAR_FORM_URL)) {
                formUrl = formProperty.getValue();
                if (!StrUtil.isEmpty(formUrl)) {
                    if (!formUrl.endsWith(businessKey)) {
                        formUrl = formUrl + businessKey;
                    }
                }
            }
        }
        String formKey = formService.getTaskFormKey(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if (StrUtil.isEmpty(formUrl) && !StrUtil.isEmpty(formKey) && !formKey.endsWith(".form")) {
            formUrl = formKey;
            if (!formUrl.endsWith(businessKey)) {
                formUrl = formUrl + businessKey;
            }
        }

        if (!StrUtil.isEmpty(formUrl)) {
            request.setAttribute("formUrl", formUrl);
        }
        request.setAttribute("formName", processInstance.getName());

        //通过formKey获取通用审批字段（审批结果、审批意见） 适用于通用审批表单
        if (!StrUtil.isEmpty(formKey) && formKey.endsWith(".form")) {
            Object formData = formService.getRenderedTaskForm(task.getId());
            if (formData != null) {
                request.setAttribute("formData", formData);
            }
        }
        request.setAttribute("taskId", taskId);
        return "activiti/task_todo";
    }


    /**
     * 流程办理
     *
     * @param taskId   任务ID
     * @param formData 流程参数（审批单）
     * @return
     */
    @RequestMapping(value = "/task/complete/{taskId}", method = RequestMethod.POST)
    @ResponseBody
    public Result completeTask(@PathVariable("taskId") String taskId, @RequestParam Map<String, String> formData) {

        return taskPageService.submitTask(taskId,formData);

    }

    /**
     * 撤回任务
     *
     * @param instanceId 历史流程节点中的ID
     * @return
     */
    @RequestMapping(value = "/task/withdraw/{instanceId}/{userId}", method = RequestMethod.POST)
    @ResponseBody
    public Result withdrawTask(@PathVariable("instanceId") String instanceId, @PathVariable("userId") String userId) {
       /* HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId
                (instanceId).singleResult();
        Result result = taskPageService.canWithdraw(processInstance, userId);
        if (!result.isSuccess()) {
            return new Result(false, "不可撤回", "该任务已经被签收或者办理，无法撤回，请查看流程明细");
        } else {
            //撤回到的历史任务实例
            HistoricTaskInstance taskInstance = (HistoricTaskInstance) result.getData();
            TaskEntity task = (TaskEntity)taskService.createTaskQuery().processInstanceId(instanceId)
                    .singleResult();

            TaskEntity task1 = (TaskEntity)taskService.newTask(taskInstance.getId());
            BeanUtils.copyProperties(taskInstance, task1);
            //删除正在执行的任务，将任务回填到上一个节点
            taskPageService.deleteCurrentTaskInstance(task,taskInstance);
            taskService.saveTask(task1);
        }
        return new Result(true);*/
        return taskPageService.withdrawTask(instanceId, userId);
    }


}
