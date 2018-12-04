package com.cnpc.framework.activiti.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by billJiang on 2017/6/4.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程设计器页面跳转入口
 */
@Controller
public class ModelPageController {

    //modeler.html内嵌的编辑界面
    @RequestMapping(value="/editor-app/{pagename}",method = RequestMethod.GET)
    public String editor(@PathVariable("pagename") String pagename){
        return "activiti/editor-app/"+pagename;
    }

    @RequestMapping(value="/editor-app/partials/{pagename}",method = RequestMethod.GET)
    public String partials(@PathVariable("pagename") String pagename){
        return "activiti/editor-app/partials/"+pagename;
    }

    @RequestMapping(value="/editor-app/popups/{pagename}",method = RequestMethod.GET)
    public String popups(@PathVariable("pagename") String pagename){
        return "activiti/editor-app/popups/"+pagename;
    }
}
