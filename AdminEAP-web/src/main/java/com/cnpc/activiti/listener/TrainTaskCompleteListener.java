package com.cnpc.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * Created by billJiang on 2017/7/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 根据室主任获取指数领导,任务监听器
 */
public class TrainTaskCompleteListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        delegateTask.getExecution().setVariable("chiefId",delegateTask.getAssignee());
    }
}
