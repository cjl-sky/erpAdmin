package com.cnpc.activiti.service;

import com.cnpc.activiti.entity.Train;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.BaseService;

/**
 * Created by billJiang on 2017/7/6.
 * e-mail:475572229@qq.com  qq:475572229
 */
public interface TrainDemoService extends BaseService {

    /**
     * 启动培训流程
     *
     * @param train                培训对象
     * @param processDefinitionKey 流程定义key
     * @return 启动结果
     */
    Result startFlow(Train train, String processDefinitionKey);
}
