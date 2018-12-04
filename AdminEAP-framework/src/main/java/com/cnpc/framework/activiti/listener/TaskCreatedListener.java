package com.cnpc.framework.activiti.listener;

import com.cnpc.framework.activiti.service.DelegateService;
import com.cnpc.framework.activiti.service.TaskPageService;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Set;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 为任务指定受理人时调用 TaskCreated 在TaskAssigned之后发生
 */
public class TaskCreatedListener implements EventHandler {

    @Resource
    private DelegateService delegateService;

    @Autowired
    private RepositoryService repositoryService;

    @Resource
    private TaskPageService taskPageService;

    @Autowired
    private TaskService taskService;

    @Override
    public void handle(ActivitiEvent event) {

        ActivitiEntityEventImpl activitiEvent = (ActivitiEntityEventImpl) event;
        TaskEntity taskEntity = (TaskEntity) activitiEvent.getEntity();
        //------------------------获取委托人-----------------------------
        System.out.println("----------------任务创建----------------"+taskEntity.getId());
        /**
         * 原来使用repositoryService.getProcessDefinitionQuery() 但是取到的category不对，所以改成如下方式获取流程定义
         */
        ProcessDefinition processDefinition=repositoryService.createProcessDefinitionQuery().processDefinitionId
                (taskEntity.getProcessDefinitionId()).singleResult();
        if(!StrUtil.isEmpty(taskEntity.getAssignee())) {
            //指定了受理人后代理
            String attorney=delegateService.getAttorneyByAssignee(taskEntity.getAssignee(),processDefinition.getCategory());
            if(!StrUtil.isEmpty(attorney)) {
                System.out.println("------受理人任务委托------任务ID:"+taskEntity.getId()+"委托人:"+taskEntity.getAssignee()+";" +
                        "被委托人ID:"+attorney);
                taskEntity.delegate(attorney);
                //TODO MESSAGE
            }
            //taskEntity.setAssignee(attorney);
        }else{
            //只有一个候选人的代理
            Set<String> userIds=taskPageService.getCandidateUserForTask(taskEntity);
            if(userIds.size()==1){
                String assignee=userIds.iterator().next();
                String attorney=delegateService.getAttorneyByAssignee(assignee,processDefinition.getCategory());
                if(!StrUtil.isEmpty(attorney)) {
                    System.out.println("------候选人任务委托------任务ID:"+taskEntity.getId()+"委托人:"+taskEntity.getAssignee()
                            +";被委托人ID:"+attorney);
                    taskService.claim(taskEntity.getId(),assignee);
                    taskEntity.delegate(attorney);
                    //TODO MESSAGE
                }
            }
        }

    }
}
