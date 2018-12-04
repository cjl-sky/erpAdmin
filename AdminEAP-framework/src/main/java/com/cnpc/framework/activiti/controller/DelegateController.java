package com.cnpc.framework.activiti.controller;

import com.cnpc.framework.activiti.entity.DelegateInfo;
import com.cnpc.framework.activiti.service.DelegateService;
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
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 工作流委托
 */
@Controller
@RequestMapping(value="/activiti/delegate")
public class DelegateController {

    @Resource
    private DelegateService delegateService;

    @RequestMapping(value="/list",method = RequestMethod.GET)
    private String list(){
        return "activiti/delegate_list";
    }

    @RequestMapping(value="/mine",method = RequestMethod.GET)
    private String all(){
        return "activiti/delegate_mine";
    }

    @RefreshCSRFToken
    @RequestMapping(value="/mine/edit",method = RequestMethod.GET)
    public String edit_mine(String id,HttpServletRequest request){
        request.setAttribute("id", id);
        return "activiti/delegate_mine_edit";
    }

    @RefreshCSRFToken
    @RequestMapping(value="/list/edit",method = RequestMethod.GET)
    public String edit_list(String id,HttpServletRequest request){
        request.setAttribute("id", id);
        return "activiti/delegate_list_edit";
    }

    @RequestMapping(value="/detail",method = RequestMethod.GET)
    public String detail(String id,HttpServletRequest request){
        request.setAttribute("id", id);
        return "activiti/delegate_detail";
    }

    @RequestMapping(value="/get/{id}",method = RequestMethod.POST)
    @ResponseBody
    public DelegateInfo get(@PathVariable("id") String id){
        return delegateService.get(DelegateInfo.class, id);
    }

    @VerifyCSRFToken
    @RequestMapping(value="/save")
    @ResponseBody
    public Result save(DelegateInfo delegateinfo){
        /*if(StrUtil.isEmpty(delegateinfo.getId())){
            delegateService.save(delegateinfo);
        }
        else{
            delegateinfo.setUpdateDateTime(new Date());
            delegateService.update(delegateinfo);
        }
        return new Result(true);*/
        return delegateService.saveDelegate(delegateinfo);
    }



    @RequestMapping(value="/delete/{id}",method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@PathVariable("id") String id){
        try{
           return delegateService.deleteDelegate(id);
        }
        catch(Exception e){
            return new Result(false,"该数据已经被引用，不可删除");
        }
    }
}
