package com.cnpc.framework.activiti.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 */
public class TestTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("test TaskListener begin----------------");
        System.out.println(delegateTask.getName());
        System.out.println(delegateTask.getAssignee());
    }
}
