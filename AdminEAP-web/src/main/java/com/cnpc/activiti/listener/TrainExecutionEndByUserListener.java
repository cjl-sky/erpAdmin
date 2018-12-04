package com.cnpc.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * Created by billJiang on 2017/7/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 用户手动结束任务
 */
public class TrainExecutionEndByUserListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) throws Exception {
        System.out.println("----------TrainTaskCompleteByUserListener被调用--------------");
    }
}
