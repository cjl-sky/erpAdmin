package com.cnpc.activiti.service;

import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.BaseService;
import org.activiti.engine.identity.User;

import java.util.List;
import java.util.Map;

/**
 * Created by billJiang on 2017/5/2.
 * e-mail:475572229@qq.com  qq:475572229
 */
public interface LabDemoService extends BaseService {


    /**
     * 启动实验室三审流程
     *
     * @param formData                培训对象
     * @param processDefinitionKey 流程定义key
     * @return 启动结果
     */
    Result startFlow(Map<String, String> formData, String processDefinitionKey);

     List<User> getUsers(String code);
}
