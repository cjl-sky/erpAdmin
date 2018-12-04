package com.cnpc.activiti.controller;

import com.cnpc.activiti.entity.Train;
import com.cnpc.activiti.service.TrainDemoService;
import com.cnpc.framework.annotation.RefreshCSRFToken;
import com.cnpc.framework.annotation.VerifyCSRFToken;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.utils.StrUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * Created by billJiang on 2017/7/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 培训选课流程-demo
 */
@Controller
@RequestMapping(value = "train")
public class TrainDemoController {

    private final static String train_key = "trainApprove";
    @Resource
    private TrainDemoService trainDemoService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list() {
        return "activiti/demo/train_list";
    }

    @RefreshCSRFToken
    @RequestMapping(value = "/edit/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") String id, HttpServletRequest request) {
        request.setAttribute("id", id);
        return "activiti/demo/train_edit";
    }

    @RefreshCSRFToken
    @RequestMapping(value = "/modify/{id}", method = RequestMethod.GET)
    public String modify(@PathVariable("id") String id, HttpServletRequest request) {
        request.setAttribute("id", id);
        return "activiti/demo/train_modify";
    }


    @RequestMapping(value = "/detail/{id}", method = RequestMethod.GET)
    public String detail(@PathVariable("id") String id, HttpServletRequest request) {
        request.setAttribute("id", id);
        return "activiti/demo/train_detail";
    }

    @RequestMapping(value = "/get/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Train get(@PathVariable("id") String id) {
        return trainDemoService.get(Train.class, id);
    }

    @VerifyCSRFToken
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public Result save(Train train) {
        if (StrUtil.isEmpty(train.getId())) {
            trainDemoService.save(train);
        } else {
            train.setUpdateDateTime(new Date());
            trainDemoService.update(train);
        }
        return new Result(true);
    }


    @RequestMapping(value = "/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@PathVariable("id") String id) {
        Train train = this.get(id);
        try {
            trainDemoService.delete(train);
            return new Result();
        } catch (Exception e) {
            return new Result(false, "该数据已经被引用，不可删除");
        }
    }

    @VerifyCSRFToken
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ResponseBody
    public Result start(Train train) {
        return trainDemoService.startFlow(train,train_key);
    }


}
