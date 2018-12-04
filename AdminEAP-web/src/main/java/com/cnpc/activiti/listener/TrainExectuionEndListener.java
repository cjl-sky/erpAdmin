package com.cnpc.activiti.listener;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * Created by billJiang on 2017/7/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程结束后的监听，比如设置业务表的状态（业务相关）
 */
public class TrainExectuionEndListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) throws Exception {
       //TODO
        System.out.println("------------------TrainExectuionEndListener:流程正常结束---------------");
    }
}
