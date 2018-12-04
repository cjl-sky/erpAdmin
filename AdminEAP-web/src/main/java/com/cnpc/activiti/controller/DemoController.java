package com.cnpc.activiti.controller;

import com.cnpc.framework.activiti.service.RuntimePageService;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.util.SecurityUtil;
import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.UUID;

/**
 * Created by billJiang on 2017/7/4.
 * e-mail:475572229@qq.com  qq:475572229
 * 工作流Demo
 */
@Controller
@RequestMapping(value = "/activiti/demo")
public class DemoController {

    @Autowired
    private RuntimeService runtimeService;

    @Resource
    private RuntimePageService runtimePageService;

    @RequestMapping(value = "/vacation", method = RequestMethod.GET)
    private String vacation(HttpServletRequest request) {
        return "activiti/demo/vacation";
    }

    private final static String vacation_key = "vacationRequestStart";

    //流程启动接口
    @RequestMapping(value = "/vacation/startFlow", method = RequestMethod.POST)
    @ResponseBody
    private Result startFlow(@RequestParam Map<String, Object> formData) {
        //TODO 业务上保存数据
        //----------------
        //模拟业务id
        String businessKey = UUID.randomUUID().toString();
        String userId = SecurityUtil.getUserId();
        String processInstanceName = formData.get("applyUserName") + "请假" + formData.get("days") + "天，请假原因：" + formData.get("motivation");
        return  runtimePageService.startProcessInstanceByKey(vacation_key,
                processInstanceName, formData, userId, businessKey);
    }



}
