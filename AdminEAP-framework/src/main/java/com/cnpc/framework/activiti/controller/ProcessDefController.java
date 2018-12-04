package com.cnpc.framework.activiti.controller;

import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.constant.RedisConstant;
import com.cnpc.framework.utils.FileUtil;
import com.cnpc.framework.utils.PropertiesUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by billJiang on 2017/6/7.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程定义控制器
 */
@Controller
@RequestMapping("/activiti")
public class ProcessDefController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessDefController.class);
    @Autowired
    private RepositoryService repositoryService;

    private static final String DIR_PATH = PropertiesUtil.getValue("uploaderPath");

    //流程定义列表
    @RequestMapping(value = "/processdef/list", method = RequestMethod.GET)
    public String list() {
        return "activiti/processdef_list";
    }

    //删除定义，如果流程定义已被使用，则不删除  type=0 软删除 type=1 强制删除
    @RequestMapping(value = "/processdef/delete/{delType}/{pdId}", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteDeployment(@PathVariable("delType") String delType, @PathVariable("pdId") String pdId) {
        ProcessDefinition pd = repositoryService.getProcessDefinition(pdId);
        try {
            if ("0".equals(delType))
                repositoryService.deleteDeployment(pd.getDeploymentId());
            else
                repositoryService.deleteDeployment(pd.getDeploymentId(), true);
            return new Result(true, pdId, "成功删除");
        } catch (Exception e) {
            return new Result(false, pdId, "删除失败，该流程定义已经关联了正在执行的流程");
        }
    }


    //导出流程定义资源文件
    @RequestMapping(value = "/processdef/export/{type}/{id}", method = RequestMethod.GET)
    public void downloadFlow(@PathVariable("type") String type, @PathVariable("id") String id, HttpServletResponse response) {
        try {
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(id);
            String resourceName = "";
            if (type.equals("image")) {
                resourceName = processDefinition.getDiagramResourceName();
            } else if (type.equals("xml")) {
                resourceName = processDefinition.getResourceName();
            }
            InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
            IOUtils.copy(resourceAsStream, response.getOutputStream());
            response.setHeader("Content-Disposition", "attachment; filename=" + resourceName);
            response.flushBuffer();
        } catch (Exception e) {
            LOGGER.error("导出流程定义的" + type + "文件失败：processDefId={}", id, e);
        }
    }

    //显示流程资源文件
    @RequestMapping(value = "/processdef/show/{pdId}", method = RequestMethod.GET)
    public String showResoure(@PathVariable("pdId") String pdId, HttpServletRequest request) {
        Result xml = generateResource("xml", pdId, request);
        Result image = generateResource("image", pdId, request);
        request.setAttribute("xml", xml.getData());
        request.setAttribute("image", image.getData());
        request.setAttribute("pdId", pdId);
        return "activiti/processdef_show";
    }


    /**
     * 生成资源文件，并返回文件路径 给开发者看的
     */
    @RequestMapping(value = "/processdef/generate/{type}/{pdId}", method = RequestMethod.POST)
    @ResponseBody
    public Result generateResource(@PathVariable("type") String type, @PathVariable("pdId") String pdId, HttpServletRequest request) {
        try {
            String dirPath = request.getRealPath("/");
            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(pdId);
            String resourceName = "";
            if (type.equals("image")) {
                resourceName = processDefinition.getDiagramResourceName();
            } else if (type.equals("xml")) {
                resourceName = processDefinition.getResourceName();
            }
            InputStream resourceAsStream = repositoryService.getResourceAsStream(processDefinition.getDeploymentId(), resourceName);
            String realPath = dirPath + File.separator + DIR_PATH + File.separator + resourceName;
            realPath=realPath.replaceAll("\\\\", "/");
            File file = new File(realPath);
            if (file.exists()) {
                file.delete();
            }
            FileUtil.copyInputStreamToFile(resourceAsStream, file);
            String realName = (DIR_PATH + File.separator + resourceName).replaceAll("\\\\", "/");
            return new Result(true, realName, "成功生成png");
        } catch (Exception e) {
            LOGGER.error("生成资源文件异常,pdId={}", pdId, e);
            return new Result(false);
        }
    }

}
