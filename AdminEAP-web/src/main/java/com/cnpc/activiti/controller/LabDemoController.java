package com.cnpc.activiti.controller;

import com.cnpc.activiti.service.LabDemoService;
import com.cnpc.framework.base.pojo.Result;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.identity.User;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by billJiang on 2017/5/2.
 * e-mail:475572229@qq.com  qq:475572229
 * 实验室三审流程demo
 */
@Controller
@RequestMapping(value = "lab")
public class LabDemoController {
    private final static String lab_key = "experiment";
    @Resource
    private LabDemoService labDemoService;

    @Autowired
    private RuntimeService runtimeService;


    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") String id, HttpServletRequest request) {
        request.setAttribute("id", id);
        return "activiti/demo/lab_edit";
    }

    @RequestMapping(value = "/modify/{id}", method = RequestMethod.GET)
    public String modify(@PathVariable("id") String id, HttpServletRequest request) {

        request.setAttribute("id", id);
        return "activiti/demo/lab_modify";
    }


    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public String detail(@PathVariable("id") String id, HttpServletRequest request) {
        request.setAttribute("id", id);
        return "activiti/demo/lab_detail";
    }


    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ResponseBody
    public Result start(@RequestParam Map<String, String> formData) {
        return labDemoService.startFlow(formData, lab_key);
    }

    @RequestMapping(value = "/get/{code}", method = RequestMethod.POST)
    @ResponseBody
    public List<User> getCandidateUsers(@PathVariable("code") String code) {
        List<User> userList = labDemoService.getUsers(code);
        return userList;
    }

    @RequestMapping(value = "/find/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,String> getData(@PathVariable("id") String id) {
        Map<String,String> map=new HashMap<>();
        ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(id)
                .singleResult();
        map.put("name",processInstance.getName());
        map.put("verifiers",runtimeService.getVariable(processInstance.getId(),"verifiers").toString());
        map.put("approvers",runtimeService.getVariable(processInstance.getId(),"approvers").toString());
        map.put("taskState",runtimeService.getVariable(processInstance.getId(),"taskState")!=null?runtimeService
                .getVariable(processInstance.getId(),"taskState").toString():null);
        return map;
    }
}
