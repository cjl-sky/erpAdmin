package com.cnpc.framework.activiti.listener;

import com.cnpc.framework.activiti.service.DelegateService;
import org.activiti.engine.delegate.event.ActivitiEvent;

import javax.annotation.Resource;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 为任务指定受理人时调用
 */
public class TaskAssignedListener implements EventHandler {

    @Resource
    private DelegateService delegateService;

    @Override
    public void handle(ActivitiEvent event) {

        /*ActivitiEntityEventImpl activitiEvent = (ActivitiEntityEventImpl) event;
        TaskEntity taskEntity = (TaskEntity) activitiEvent.getEntity();
        System.out.println("---------------指定委托人:-----------------"+taskEntity.getId());
        //------------------------获取委托人-----------------------------
        if(!StrUtil.isEmpty(taskEntity.getAssignee())) {
            String attorney=delegateService.getAttorneyByAssignee(taskEntity.getAssignee());
            if(!StrUtil.isEmpty(attorney)) {
                System.out.println("------任务委托------任务ID:"+taskEntity.getId()+"委托人:"+taskEntity.getAssignee()+";被委托人ID:"+attorney);
                taskEntity.delegate(attorney);
            }
            //taskEntity.setAssignee(attorney);
        }*/

    }
}
