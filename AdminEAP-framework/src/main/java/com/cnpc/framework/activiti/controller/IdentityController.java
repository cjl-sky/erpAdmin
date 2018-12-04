package com.cnpc.framework.activiti.controller;

import com.cnpc.framework.activiti.service.IdentityPageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by billJiang on 2017/6/16.
 * e-mail:475572229@qq.com  qq:475572229
 * 用户/用户组控制器
 */
@Controller
@RequestMapping("/activiti")
public class IdentityController {

    @Resource
    private IdentityPageService identityPageService;

    //用户选择界面
    //multiple=0 单选  multiple=1 多选
    @RequestMapping(value = "/user/select/{multiple}/{ids}", method = RequestMethod.GET)
    public String selectUserPage(@PathVariable("multiple") String multiple,
                                 @PathVariable("ids") String ids, HttpServletRequest request) {
        request.setAttribute("multiple", multiple);
        request.setAttribute("ids", ids);
        return "activiti/id_user_select";
    }

    //用户组选择界面
    @RequestMapping(value = "/group/select/{ids}", method = RequestMethod.GET)
    public String selectGroupPage(@PathVariable("ids") String ids, HttpServletRequest request) {
        request.setAttribute("ids", ids);
        return "activiti/id_group_select";
    }

    @RequestMapping(value = "/{type}/names", method = RequestMethod.POST)
    @ResponseBody
    public Map getNamesByIds(@PathVariable("type") String type, String ids) {
        Map<String,String> map=new HashMap<>();
        if ("user".equals(type)) {
            String names=identityPageService.getUserNamesByUserIds(ids);
            map.put("name",names);
            return map;
        } else {
            String names=identityPageService.getGroupNamesByGroupIds(ids);
            map.put("name",names);
            return map;
        }
    }

}
