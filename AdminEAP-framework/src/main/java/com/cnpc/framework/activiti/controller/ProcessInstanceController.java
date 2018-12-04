package com.cnpc.framework.activiti.controller;

import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.utils.DateUtil;
import com.cnpc.framework.utils.FileUtil;
import com.cnpc.framework.utils.PropertiesUtil;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.image.ProcessDiagramGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by billJiang on 2017/6/21.
 * e-mail:475572229@qq.com  qq:475572229
 * 流程实例控制器
 */
@Controller
@RequestMapping(value = "/activiti")
public class ProcessInstanceController {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ProcessInstanceController.class);

    private static final String DIR_PATH = PropertiesUtil.getValue("uploaderPath");

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RepositoryService repositoryService;


    //流程处理引擎
    @Autowired
    private ProcessEngine processEngine;


    //流程定义列表
    @RequestMapping(value = "/processinstance/list", method = RequestMethod.GET)
    public String list() {
        return "activiti/processinstance_list";
    }


    @RequestMapping(value = "/processinstance/delete/{instanceId}", method = RequestMethod.POST)
    @ResponseBody
    public Result deleteInstance(@PathVariable("instanceId") String id) {
        try {
            runtimeService.deleteProcessInstance(id, "流程实例管理界面删除");
            historyService.deleteHistoricProcessInstance(id);
            return new Result(true);
        } catch (Exception ex) {
            return new Result(false, "删除实例失败", "失败原因:" + ex.getMessage());
        }
    }


    /**
     * 流程监控--已审批、待审批/流程图片
     *
     * @param instanceId
     * @param request
     * @return
     */
    @RequestMapping(value = "/monitor/{instanceId}", method = RequestMethod.GET)
    public String monitor(@PathVariable("instanceId") String instanceId, HttpServletRequest request) {
        request.setAttribute("instanceId", instanceId);
        return "activiti/monitor_show";
    }

    @RequestMapping(value = "/monitor/list/{instanceId}", method = RequestMethod.GET)
    public String monitor_list(@PathVariable("instanceId") String instanceId, HttpServletRequest request) {
        request.setAttribute("instanceId", instanceId);
        return "activiti/monitor_show_list";
    }

    @RequestMapping(value = "/monitor/image/{instanceId}", method = RequestMethod.GET)
    public String monitor_image(@PathVariable("instanceId") String instanceId, HttpServletRequest request) {
        Result image = generateImage(instanceId, request);
        request.setAttribute("image", image.getData());
        request.setAttribute("instanceId", instanceId);
        return "activiti/monitor_show_image";
    }

    /**
     * 生成流程实例的流程图片，并重点高亮当前节点，高亮已经执行的链路
     *
     * @param instanceId 流程实例
     * @param request
     * @return 第二个参数为生成的图片路径
     */
    @RequestMapping(value = "/processinstance/generate/{instanceId}", method = RequestMethod.POST)
    @ResponseBody
    public Result generateImage(@PathVariable("instanceId") String instanceId, HttpServletRequest request) {
        try {
            String dirPath = request.getRealPath("/");
            ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processInstanceId(instanceId)
                    .singleResult();
            BpmnModel bpmnModel;
            List<String> activeActivityIds=new ArrayList<>();
            String processDefinitionId;
            //存在活动节点，流程正在进行中
            if(processInstance!=null) {
                processDefinitionId=processInstance.getProcessDefinitionId();
                //task = taskService.createTaskQuery().processInstanceId(instanceId).singleResult();
                //流程定义
                //正在活动的节点
                activeActivityIds = runtimeService.getActiveActivityIds(instanceId);//(task.getExecutionId());
            }else {
                //流程已经结束
                HistoricProcessInstance instance=historyService.createHistoricProcessInstanceQuery()
                        .processInstanceId(instanceId).singleResult();
                processDefinitionId=instance.getProcessDefinitionId();
            }

            bpmnModel = repositoryService.getBpmnModel(processDefinitionId);

            ProcessDiagramGenerator pdg = processEngine.getProcessEngineConfiguration().getProcessDiagramGenerator();

            //-------------------------------executedActivityIdList已经执行的节点------------------------------------
            List<HistoricActivityInstance> historicActivityInstanceList = historyService
                    .createHistoricActivityInstanceQuery()
                    .processInstanceId(instanceId).orderByHistoricActivityInstanceStartTime().asc().list();

            // 已执行的节点ID集合
            List<String> executedActivityIdList = new ArrayList<>();
            for (HistoricActivityInstance activityInstance : historicActivityInstanceList) {
                executedActivityIdList.add(activityInstance.getActivityId());
            }

            ProcessDefinition processDefinition = repositoryService.getProcessDefinition(processDefinitionId);
            String resourceName = instanceId + "_" + processDefinition.getDiagramResourceName();

            List<String> highLightedFlows = getHighLightedFlows((ProcessDefinitionEntity) processDefinition,
                    historicActivityInstanceList);

            //生成流图片 所有走过的节点高亮 第三个参数 activeActivityIds=当前活动节点点高亮;executedActivityIdList=已经执行过的节点高亮
            InputStream inputStream = pdg.generateDiagram(bpmnModel, "PNG", activeActivityIds, highLightedFlows,
                    processEngine.getProcessEngineConfiguration().getActivityFontName(),
                    processEngine.getProcessEngineConfiguration().getLabelFontName(),
                    processEngine.getProcessEngineConfiguration().getActivityFontName(),
                    processEngine.getProcessEngineConfiguration().getProcessEngineConfiguration().getClassLoader(), 1);

            resourceName=DateUtil.format(new Date(),"yyyyMMddHHmmss")+"_"+resourceName;
            //生成本地图片
            String realPath = dirPath + File.separator + DIR_PATH + File.separator + resourceName;
            realPath=realPath.replaceAll("\\\\", "/");
            File file = new File(realPath);
            if (file.exists()) {
                file.delete();
            }
            FileUtil.copyInputStreamToFile(inputStream, file);
            String realName = (DIR_PATH + File.separator + resourceName).replaceAll("\\\\", "/");
            inputStream.close();
            return new Result(true, realName, "成功生成png");
        } catch (Exception e) {
            LOGGER.error("生成图像文件异常,instanceId={}", instanceId, e);
            return new Result(false);
        }
    }


    /**
     * 获取需要高亮的线,如果其他方法需要调用 重构到RuntimePageService
     *
     * @param processDefinitionEntity
     * @param historicActivityInstances
     * @return
     */
    private List<String> getHighLightedFlows(ProcessDefinitionEntity processDefinitionEntity,
                                             List<HistoricActivityInstance> historicActivityInstances) {
        List<String> highFlows = new ArrayList<>();// 用以保存高亮的线flowId
        for (int i = 0; i < historicActivityInstances.size() - 1; i++) {// 对历史流程节点进行遍历
            ActivityImpl activityImpl = processDefinitionEntity.findActivity(historicActivityInstances.get(i)
                    .getActivityId());// 得到节点定义的详细信息
            List<ActivityImpl> sameStartTimeNodes = new ArrayList<>();// 用以保存后需开始时间相同的节点
            ActivityImpl sameActivityImpl1 = processDefinitionEntity.findActivity(historicActivityInstances.get(i + 1)
                    .getActivityId());
            // 将后面第一个节点放在时间相同节点的集合里
            sameStartTimeNodes.add(sameActivityImpl1);
            for (int j = i + 1; j < historicActivityInstances.size() - 1; j++) {
                HistoricActivityInstance activityImpl1 = historicActivityInstances.get(j);// 后续第一个节点
                HistoricActivityInstance activityImpl2 = historicActivityInstances.get(j + 1);// 后续第二个节点
               //if (activityImpl1.getStartTime().equals(activityImpl2.getStartTime())) {
                    // 如果第一个节点和第二个节点开始时间相同保存
                    ActivityImpl sameActivityImpl2 = processDefinitionEntity.findActivity(activityImpl2.getActivityId
                            ());
                    sameStartTimeNodes.add(sameActivityImpl2);
                /*} else {
                    // 有不相同跳出循环
                    break;
                }*/
            }
            List<PvmTransition> pvmTransitions = activityImpl.getOutgoingTransitions();// 取出节点的所有出去的线
            for (PvmTransition pvmTransition : pvmTransitions) {
                // 对所有的线进行遍历
                ActivityImpl pvmActivityImpl = (ActivityImpl) pvmTransition.getDestination();
                // 如果取出的线的目标节点存在时间相同的节点里，保存该线的id，进行高亮显示
                if (sameStartTimeNodes.contains(pvmActivityImpl)) {
                    highFlows.add(pvmTransition.getId());
                }
            }
        }
        return highFlows;
    }

    /**
     * 改变流程实例状态  挂起/激活
     * @param instanceId
     * @return
     */
    @RequestMapping(value="/processinstance/toggleSuspensionState/{instanceId}",method = RequestMethod.POST)
    @ResponseBody
    public Result toggleSuspensionState(@PathVariable("instanceId") String instanceId){
        ProcessInstance processInstance=runtimeService.createProcessInstanceQuery().processInstanceId(instanceId)
                .singleResult();
        if(processInstance.isSuspended()){
            runtimeService.activateProcessInstanceById(instanceId);
            return new Result(true,"激活实例","流程实例成功被激活");
        }else{
            runtimeService.suspendProcessInstanceById(instanceId);
            return new Result(true,"挂起实例","流程实例成功挂起,流程挂起后用户将不能办理该流程");
        }
    }

}

