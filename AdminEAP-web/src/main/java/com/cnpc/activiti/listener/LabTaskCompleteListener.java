package com.cnpc.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * Created by billJiang on 2017/7/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 设置实验室三婶的任务监听器
 */
public class LabTaskCompleteListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        if(delegateTask.getTaskDefinitionKey().equals("verify")){
            delegateTask.getExecution().setVariable("verifiers",delegateTask.getAssignee());
        }else if(delegateTask.getTaskDefinitionKey().equals("approve")){
            delegateTask.getExecution().setVariable("approvers",delegateTask.getAssignee());
            delegateTask.getExecution().setVariable("taskState",1);
        }
    }
}
