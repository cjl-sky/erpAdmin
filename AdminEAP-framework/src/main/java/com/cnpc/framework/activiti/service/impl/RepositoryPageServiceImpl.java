package com.cnpc.framework.activiti.service.impl;

import com.cnpc.framework.activiti.pojo.ProcessDefVo;
import com.cnpc.framework.activiti.service.RepositoryPageService;
import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.base.service.impl.BaseServiceImpl;
import com.cnpc.framework.query.entity.QueryCondition;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by billJiang on 2017/6/8.
 * e-mail:475572229@qq.com  qq:475572229
 * 工作流流程定义服务实现
 */
@Service("repositoryPageService")
public class RepositoryPageServiceImpl extends BaseServiceImpl implements RepositoryPageService {
    @Autowired
    RepositoryService repositoryService;

    @Override
    public List<ProcessDefVo> getProcessDefList(QueryCondition condition, PageInfo pageInfo) {
        String name = null;
        if (condition != null)
            name = condition.getConditionMap().get("name").toString();
        List<ProcessDefinition> processDefList;
        long count;
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
        if (!StrUtil.isEmpty(name)) {
            query = query.processDefinitionName(name);
        }
        count = query.count();
        processDefList = query.orderByProcessDefinitionId().desc()
                .listPage((pageInfo.getPageNum() - 1) * pageInfo.getPageSize(), pageInfo.getPageSize());

        pageInfo.setCount((int) count);
        List<ProcessDefVo> retList = new ArrayList<>();
        for (ProcessDefinition processDefinition : processDefList) {
            ProcessDefinitionEntity entity = (ProcessDefinitionEntity) processDefinition;
            ProcessDefVo vo = new ProcessDefVo();
            BeanUtils.copyProperties(entity, vo);
            retList.add(vo);
        }

        return retList;
    }

    @Override
    public List<Model> getModelList(QueryCondition condition, PageInfo pageInfo) {
        String name = null;
        if (condition != null)
            name = condition.getConditionMap().get("name").toString();
        List<Model> modelList;
        long count;
        ModelQuery query = repositoryService.createModelQuery();
        if (!StrUtil.isEmpty(name)) {
            query = query.modelNameLike(name);
        }
        count = query.count();
        modelList = query.orderByCreateTime().desc().listPage((pageInfo.getPageNum() - 1) * pageInfo.getPageSize(), pageInfo.getPageSize());
        pageInfo.setCount((int) count);
        return modelList;
    }
}
