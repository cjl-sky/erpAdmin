package com.cnpc.framework.activiti.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSON;
import com.cnpc.framework.base.entity.Dict;
import com.cnpc.framework.base.service.DictService;
import com.cnpc.framework.utils.StrUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cnpc.framework.base.service.BaseService;
import com.cnpc.framework.annotation.RefreshCSRFToken;
import com.cnpc.framework.annotation.VerifyCSRFToken;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.activiti.entity.Module;

/**
* 业务定义管理控制器
* @author jrn
* 2017-06-05 14:57:31由代码生成器自动生成
*/
@Controller
@RequestMapping("/activiti/module")
public class ModuleController {

    @Resource
    private BaseService baseService;

    @RequestMapping(value="/list",method = RequestMethod.GET)
    public String list(){
        return "activiti/module_list";
    }

    @RefreshCSRFToken
    @RequestMapping(value="/edit",method = RequestMethod.GET)
    public String edit(String id,HttpServletRequest request){
        request.setAttribute("id", id);
        return "activiti/module_edit";
    }

    @RequestMapping(value="/detail",method = RequestMethod.GET)
    public String detail(String id,HttpServletRequest request){
        request.setAttribute("id", id);
        return "activiti/module_detail";
    }

    @RequestMapping(value="/get/{id}",method = RequestMethod.POST)
    @ResponseBody
    public Module get(@PathVariable("id") String id){
        return baseService.get(Module.class, id);
    }

    @VerifyCSRFToken
    @RequestMapping(value="/save")
    @ResponseBody
    public Result save(Module module){
        if(StrUtil.isEmpty(module.getId())){
            baseService.save(module);
        }
        else{
            module.setUpdateDateTime(new Date());
            baseService.update(module);
        }
        return new Result(true);
    }



    @RequestMapping(value="/delete/{id}",method = RequestMethod.POST)
    @ResponseBody
    public Result delete(@PathVariable("id") String id){
        Module module=this.get(id);
        try{
            baseService.delete(module);
            return new Result();
        }
        catch(Exception e){
            return new Result(false,"该数据已经被引用，不可删除");
        }
    }

    @RequestMapping(value="getByCode/{code}",method = RequestMethod.POST)
    @ResponseBody
    public Module getByCode(@PathVariable("code") String code){
        String hql="from Module where (deleted=0 or deleted is null) and code=:code";
        Map<String,Object> param=new HashMap<>();
        return baseService.get(hql,param);
    }

    @RequestMapping(value="getAll",method = RequestMethod.POST)
    @ResponseBody
    public List<Module> getAll(){
        String hql="from Module where (deleted=0 or deleted is null)";
        return baseService.find(hql);
    }


}
