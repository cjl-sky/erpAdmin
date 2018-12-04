package com.cnpc.framework.activiti.listener;

import com.cnpc.framework.utils.SpringContextUtil;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;

import java.util.Map;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 通用的事件监听器，减少流程定义的配置，用于委托代办功能
 */
public class CustomEventListener implements ActivitiEventListener {

    private Map<String, String> handlers;

    @Override
    public void onEvent(ActivitiEvent event) {
        String eventType=event.getType().name();

        //根据事件的类型ID,找到对应的事件处理器
        String eventHandlerBeanId=handlers.get(eventType);
        if(eventHandlerBeanId!=null){
            EventHandler handler=(EventHandler) SpringContextUtil.getBean(eventHandlerBeanId);
            handler.handle(event);
        }
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    public Map<String, String> getHandlers() {
        return handlers;
    }

    public void setHandlers(Map<String, String> handlers) {
        this.handlers = handlers;
    }
}
