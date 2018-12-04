package com.cnpc.framework.activiti.service;

import com.cnpc.framework.activiti.entity.DelegateInfo;
import com.cnpc.framework.activiti.pojo.TaskVo;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.BaseService;
import org.activiti.engine.impl.persistence.entity.TaskEntity;

import java.util.List;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 任务委托服务接口
 */
public interface DelegateService extends BaseService {
    /**
     * 获取被委托人ID
     *
     * @param assignee 受理人
     * @param moduleCode 业务编码
     * @return 委托人ID
     */
    String getAttorneyByAssignee(String assignee, String moduleCode);

    /**
     * 删除代理
     *
     * @param id 委托ID
     * @return
     */
    Result deleteDelegate(String id);

    /**
     * 保存委托设置
     *
     * @param delegateInfo 委托信息
     * @return 保存结果
     */
    Result saveDelegate(DelegateInfo delegateInfo);


}
