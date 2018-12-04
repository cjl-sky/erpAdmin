package com.cnpc.framework.activiti.listener;

import com.cnpc.framework.activiti.service.DelegateService;
import com.cnpc.framework.activiti.service.TaskPageService;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.impl.ActivitiEntityEventImpl;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.repository.ProcessDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Set;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 任务完成后为后的审批人，发送消息通知,或者在办理时在业务的Service发送
 */
public class TaskCompletedListener implements EventHandler {


    @Override
    public void handle(ActivitiEvent event) {
        //TODO MESSAGE
    }
}
