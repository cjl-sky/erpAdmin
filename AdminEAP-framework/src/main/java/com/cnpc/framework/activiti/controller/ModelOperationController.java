package com.cnpc.framework.activiti.controller;

import com.cnpc.framework.activiti.pojo.ModelVo;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.utils.FileUtil;
import com.cnpc.framework.utils.PropertiesUtil;
import com.cnpc.framework.utils.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.ModelEntity;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

/**
 * Created by billJiang on 2017/6/3.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程设计器（模型管理）入口
 */
@Controller
@RequestMapping("activiti")
public class ModelOperationController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelOperationController.class);
    @Autowired
    RepositoryService repositoryService;
    @Autowired
    ObjectMapper objectMapper;

    private static final String DIR_PATH = PropertiesUtil.getValue("uploaderPath");


    @RequestMapping(value = "/modeler/{modelId}", method = RequestMethod.GET)
    public String modeler(@PathVariable("modelId") String modelId, HttpServletRequest request) {
        request.setAttribute("modelId", modelId);
        return "activiti/modeler";
    }


    //流程定义列表
    @RequestMapping(value = "/model/list", method = RequestMethod.GET)
    public String list() {
        return "activiti/model_list";
    }


    /**
     * 新建一个空模型
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    @RequestMapping(value = "/model/new", method = RequestMethod.GET)
    public String newModel() throws UnsupportedEncodingException {
        //初始化一个空模型
        Model model = repositoryService.newModel();

        //设置一些默认信息
        String name = "new-process";
        String description = "";
        int revision = 1;
        String key = "process";

        ObjectNode modelNode = objectMapper.createObjectNode();
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, name);
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, description);
        modelNode.put(ModelDataJsonConstants.MODEL_REVISION, revision);

        model.setName(name);
        model.setKey(key);
        model.setMetaInfo(modelNode.toString());

        repositoryService.saveModel(model);
        String id = model.getId();

        //完善ModelEditorSource
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace",
                "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.put("stencilset", stencilSetNode);
        repositoryService.addModelEditorSource(id, editorNode.toString().getBytes("utf-8"));
        //return ToWeb.buildResult().redirectUrl("/modeler.html?modelId="+id);
        return "redirect:/activiti/modeler/" + id;
    }


    @RequestMapping(value = "/model/edit/{id}", method = RequestMethod.GET)
    public String editorModel(@PathVariable("id") String id, HttpServletRequest request) {
        request.setAttribute("id", id);
        return "activiti/model_edit";
    }


    @RequestMapping(value = "/model/test", method = RequestMethod.POST)
    @ResponseBody
    public Result testModel(ModelVo vo) {
        System.out.println("test empty value:===========" + vo.getId());
        System.out.println("test empty value:===========" + vo.getCategory());
        return new Result(true);
    }

    /**
     * 保存模型数据
     * TODO 如果参数类型ModelVo，则传送过来的实体属性null 则变成了空字符串 jquery ajax param方法的问题，已在前端处理
     * 使用vostr参数后JSON.parseObject则不会有这个问题
     */
    @RequestMapping(value = "/model/save", method = RequestMethod.POST)
    @ResponseBody
    public Result saveModel(ModelVo vo) throws IOException {
        String modelId = vo.getId();
        ModelEntity model;
        if (StrUtil.isEmpty(modelId)) {
            //新增
            model = (ModelEntity) repositoryService.newModel();

        } else {
            //编辑
            model = (ModelEntity) repositoryService.getModel(modelId);
        }
        BeanUtils.copyProperties(vo, model, "revision");
        ObjectNode modelNode;
        if (!StrUtil.isEmpty(model.getMetaInfo()))
            modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
        else {
            modelNode = objectMapper.createObjectNode();
            modelNode.put(ModelDataJsonConstants.MODEL_REVISION, model.getRevision());
        }
        modelNode.put(ModelDataJsonConstants.MODEL_NAME, vo.getName());
        modelNode.put(ModelDataJsonConstants.MODEL_DESCRIPTION, vo.getDescription());
        model.setMetaInfo(modelNode.toString());
        repositoryService.saveModel(model);
        modelId = model.getId();
        if (StrUtil.isEmpty(model.getEditorSourceValueId())) {
            ObjectNode editorNode = objectMapper.createObjectNode();
            editorNode.put("resourceId", modelId);
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorNode.put("stencilset", stencilSetNode);
            repositoryService.addModelEditorSource(modelId, editorNode.toString().getBytes("utf-8"));
        }

        return new Result(true, modelId, "保存成功");
    }

    @RequestMapping(value = "/model/get/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ModelVo get(@PathVariable("id") String id) {

        ModelEntity model = (ModelEntity) repositoryService.getModel(id);
        ModelVo vo = new ModelVo();
        BeanUtils.copyProperties(model, vo);
        try {
            ObjectNode metaInfo = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            vo.setId(model.getId());
            String description = metaInfo.get(ModelDataJsonConstants.MODEL_DESCRIPTION).toString();
            description = description.equals("null") ? null : description;
            vo.setDescription(description);
        } catch (Exception e) {
            LOGGER.error("Error get model id=" + id, e);
            throw new ActivitiException("Error get model id=" + id, e);
        }
        return vo;
    }

    //key的唯一性校验
    @RequestMapping(value = "/model/uniquekey", method = RequestMethod.POST)
    @ResponseBody
    public Map uniqueKey(String key, String id) {
        Map<String, Boolean> map = new HashMap<>();
        if (StrUtil.isEmpty(key)) {
            map.put("valid", true);
        } else {
            List<Model> models = repositoryService.createModelQuery().modelKey(key).list();
            if (StrUtil.isEmpty(id)) {
                if (models.isEmpty())
                    map.put("valid", true);
                else
                    map.put("valid", false);
            } else {
                map.put("valid", false);
                for (Model model : models) {
                    if (model.getId().equals(id)) {
                        map.put("valid", true);
                        break;
                    }
                }
            }
        }
        return map;
    }


    /**
     * 获取所有模型
     *
     * @return
     */
    @RequestMapping(value = "/model/all", method = RequestMethod.POST)
    @ResponseBody
    public List<Model> modelList() {
        List<Model> models = repositoryService.createModelQuery().list();
        return models;
    }

    /**
     * 删除模型
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/model/delete/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteModel(@PathVariable("id") String id) {
        repositoryService.deleteModel(id);
        return new Result(true);
    }

    /**
     * 模型复制
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/model/copy/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result copyModal(@PathVariable("id") String id) throws IOException {
        ModelEntity newModel = (ModelEntity) repositoryService.newModel();
        ModelEntity model = (ModelEntity) repositoryService.getModel(id);
        BeanUtils.copyProperties(model, newModel, "id", "revision");
        ObjectNode modelNode;
        if (!StrUtil.isEmpty(model.getMetaInfo())) {
            modelNode = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            newModel.setMetaInfo(modelNode.toString());
        }
        newModel.setDeploymentId(null);
        newModel.setEditorSourceExtraValueId(null);
        newModel.setEditorSourceValueId(null);
        newModel.setName(model.getName() + "(副本)");
        repositoryService.saveModel(newModel);
        repositoryService.addModelEditorSource(newModel.getId(), repositoryService.getModelEditorSource(model.getId()));
        repositoryService.addModelEditorSourceExtra(newModel.getId(), repositoryService.getModelEditorSourceExtra
                (model.getId()));
        return new Result(true, newModel.getId(), "流程复制成功");
    }

    /**
     * 发布模型为流程定义
     *
     * @param id
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/model/deploy/{id}", method = RequestMethod.POST)
    @ResponseBody
    public Result deploy(@PathVariable("id") String id, HttpServletRequest request) throws Exception {

        //获取模型
        Model modelData = repositoryService.getModel(id);
        byte[] bytes = repositoryService.getModelEditorSource(modelData.getId());

        if (bytes == null) {
            return new Result(false, id, "模型数据为空，请先设计流程并成功保存，再进行发布。");
        }

        JsonNode modelNode = objectMapper.readTree(bytes);

        BpmnModel model = new BpmnJsonConverter().convertToBpmnModel(modelNode);
        if (model.getProcesses().size() == 0) {
            return new Result(false, id, "数据模型不符要求，请至少设计一条主线流程。");
        }
        try {
            //----------------生成zip文件-------------------------
            byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
            Result xml = generateResource("xml", id, request);
            Result image = generateResource("image", id, request);
            if (xml.getData() == null || image.getData() == null) {
                return new Result(false, "部署失败", "流程尚未定义或定义错误，不能生成有效的xml和png文件");
            }
            String basePath = request.getRealPath("/");
            String fileName = modelData.getKey() + ".bpmn20.model.zip";
            String zipFileName = DIR_PATH + File.separator + fileName;
            File file = new File(basePath + File.separator + zipFileName);
            if (file.exists()) {
                file.delete();
            }
            String zipPath = FileUtil.generateZipFile(basePath, zipFileName, xml.getData().toString(),
                    image.getData().toString());
            InputStream inputStream = new FileInputStream(zipPath);
            ZipInputStream zipInputStream = new ZipInputStream(inputStream);
            //---------------------------------------------------
            //发布流程
            String processName = modelData.getKey() + ".bpmn20.xml";
            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment()
                    .name(modelData.getName())
                    .category(modelData.getCategory())
                    .tenantId(modelData.getTenantId())
                    //使用addZipInputStream后可以预防flow连线文字丢失的问题
                    .addZipInputStream(zipInputStream);
                    //.addString(processName, new String(bpmnBytes, "UTF-8"))
                    

            List<JsonNode> forms=modelNode.findValues("formkeydefinition");
            for (JsonNode form : forms) {
                String formName=form.textValue();
                if(!StrUtil.isEmpty(formName)){
                    InputStream stream=this.getClass().getClassLoader().getResourceAsStream(formName);
                    deploymentBuilder.addInputStream(formName,stream);
                }
            }
            Deployment deployment=deploymentBuilder.deploy();
            
            //更新流程定义类别,替换掉页面的定义
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId
                    (deployment.getId()).singleResult();
            if (processDefinition != null)
                repositoryService.setProcessDefinitionCategory(processDefinition.getId(), deployment.getCategory());

            modelData.setDeploymentId(deployment.getId());
            repositoryService.saveModel(modelData);
            return new Result(true);
        } catch (Exception ex) {
            return new Result(false, "部署失败", ex.getMessage().toString());
        }
    }


    /**
     * 导出model的xml文件
     */
    @RequestMapping(value = "/model/export/xml/{modelId}", method = RequestMethod.GET)
    public void exportXml(@PathVariable("modelId") String modelId, HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = objectMapper.readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
            //没有xml
            if (bpmnModel.getProcesses().isEmpty()) {
                response.setCharacterEncoding("utf-8");
                response.getWriter().print("<script>modals.error('xml文件不存在，生成错误');</script>");
                response.flushBuffer();
                return;
            }
            byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

            ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
            IOUtils.copy(in, response.getOutputStream());
            String filename = bpmnModel.getMainProcess().getId() + ".bpmn20.model.xml";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
            LOGGER.error("导出model的xml文件失败：modelId={}", modelId, e);

        }
    }

    /**
     * 导出model的png文件
     */
    @RequestMapping(value = "/model/export/image/{modelId}", method = RequestMethod.GET)
    public void exportPng(@PathVariable("modelId") String modelId, HttpServletResponse response) {
        try {
            Model modelData = repositoryService.getModel(modelId);
            byte[] pngBytes = repositoryService.getModelEditorSourceExtra(modelData.getId());

            ByteArrayInputStream in = new ByteArrayInputStream(pngBytes);
            IOUtils.copy(in, response.getOutputStream());
            String filename = modelData.getKey() + ".process.model.png";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.flushBuffer();
        } catch (Exception e) {
            LOGGER.error("导出model的png文件失败：modelId={}", modelId, e);
        }
    }

    /**
     * 校验资源文件是否存在
     *
     * @param type    xml image 类型
     * @param modelId 模型id
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/model/exist/{type}/{modelId}", method = RequestMethod.POST)
    @ResponseBody
    public Result resourceExist(@PathVariable("type") String type, @PathVariable("modelId") String modelId) throws IOException {
        Model modelData = repositoryService.getModel(modelId);
        if (type.equals("xml")) {
            BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
            JsonNode editorNode = objectMapper.readTree(repositoryService.getModelEditorSource(modelData.getId()));
            BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
            return new Result(!bpmnModel.getProcesses().isEmpty());
        } else {
            byte[] pngBytes = repositoryService.getModelEditorSourceExtra(modelData.getId());
            return new Result(pngBytes.length > 0);
        }
    }

    /**
     * 跳转到模型资源文件查看界面
     */

    @RequestMapping(value = "/model/show/{modelId}", method = RequestMethod.GET)
    public String showResoure(@PathVariable("modelId") String modelId, HttpServletRequest request) {
        Result xml = generateResource("xml", modelId, request);
        Result image = generateResource("image", modelId, request);
        request.setAttribute("xml", xml.getData());
        request.setAttribute("image", image.getData());
        request.setAttribute("modelId", modelId);
        return "activiti/model_show";
    }

    /**
     * 生成资源文件，并返回文件路径
     */
    @RequestMapping(value = "/model/generate/{type}/{modelId}", method = RequestMethod.POST)
    @ResponseBody
    public Result generateResource(@PathVariable("type") String type, @PathVariable("modelId") String
            modelId, HttpServletRequest request) {
        try {
            String dirPath = request.getRealPath("/");
            Model model = repositoryService.getModel(modelId);
            if (type.equals("xml")) {
                Model modelData = repositoryService.getModel(modelId);
                BpmnJsonConverter jsonConverter = new BpmnJsonConverter();
                JsonNode editorNode = objectMapper.readTree(repositoryService.getModelEditorSource(modelData.getId()));
                BpmnModel bpmnModel = jsonConverter.convertToBpmnModel(editorNode);
                BpmnXMLConverter xmlConverter = new BpmnXMLConverter();
                byte[] bpmnBytes = xmlConverter.convertToXML(bpmnModel);

                ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
                String fileName = model.getKey() + ".model.bpmn";
                String realPath = dirPath + File.separator + DIR_PATH + File.separator + fileName;
                File file = new File(realPath);
                if (file.exists()) {
                    file.delete();
                }
                FileUtil.copyInputStreamToFile(in, file);
                String realName = (DIR_PATH + File.separator + fileName).replaceAll("\\\\", "/");
                return new Result(true, realName, "成功生成流程配置xml");
            } else {
                byte[] pngBytes = repositoryService.getModelEditorSourceExtra(modelId);
                String fileName = model.getKey() + ".model.png";
                String realPath = dirPath + File.separator + DIR_PATH + File.separator + fileName;
                File file = new File(realPath);
                if (file.exists()) {
                    file.delete();
                }
                ByteArrayInputStream in = new ByteArrayInputStream(pngBytes);
                FileUtil.copyInputStreamToFile(in, file);
                String realName = (DIR_PATH + File.separator + fileName).replaceAll("\\\\", "/");
                return new Result(true, realName, "成功生成png");
            }
        } catch (Exception e) {
            LOGGER.error("生成资源文件异常,modelId={}", modelId, e);
            return new Result(false);
        }
    }
}
