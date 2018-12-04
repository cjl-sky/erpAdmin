package com.cnpc.framework.activiti.listener;

import org.activiti.engine.delegate.event.ActivitiEvent;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 通用Activiti事件监听器接口
 */
public interface EventHandler {
    void handle(ActivitiEvent event);
}
