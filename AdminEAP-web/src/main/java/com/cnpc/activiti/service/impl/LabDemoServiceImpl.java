package com.cnpc.activiti.service.impl;

import com.cnpc.activiti.service.LabDemoService;
import com.cnpc.framework.activiti.pojo.Constants;
import com.cnpc.framework.activiti.service.IdentityPageService;
import com.cnpc.framework.activiti.service.RuntimePageService;
import com.cnpc.framework.base.entity.Role;
import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.impl.BaseServiceImpl;
import com.cnpc.framework.util.SecurityUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * Created by billJiang on 2017/5/2.
 * e-mail:475572229@qq.com  qq:475572229
 */
@Service("labDemoService")
public class LabDemoServiceImpl extends BaseServiceImpl implements LabDemoService {

    @Resource
    private RuntimePageService runtimePageService;

    @Resource
    private IdentityPageService identityPageService;


    @Override
    public Result startFlow(Map<String, String> formData, String processDefinitionKey) {
        //流程实例名称
        User user = SecurityUtil.getUser();
        String name = user.getName() + "申请试验：" + formData.get("name");
        //businessKey
        String businessKey = UUID.randomUUID().toString();
        //配置流程变量
        Map<String, Object> variables = new HashMap<>();
        variables.put(Constants.VAR_APPLYUSER_NAME, user.getName());
        variables.put(Constants.VAR_BUSINESS_KEY, businessKey);

        //---------根据当前人的角色选择不同的流程路径-----------------------
        variables.put("verifiers", formData.get("verifiers"));
        variables.put("approvers", formData.get("approvers"));
        //启动流程
        return runtimePageService.startProcessInstanceByKey(processDefinitionKey, name, variables,
                user.getId(), businessKey);
    }


    @Override
    public List<org.activiti.engine.identity.User> getUsers(String code) {
        String hql = "from Role where code=:code";
        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        Role role = this.get(hql, params);
        List<String> userIds = identityPageService.getUserIdsByGroupIds(role.getId());
        List<org.activiti.engine.identity.User> userList = new ArrayList<>();
        for (String userId : userIds) {
            userList.add(identityPageService.getUser(userId));
        }
        return userList;
    }
}
