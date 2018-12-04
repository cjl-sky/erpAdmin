package com.cnpc.framework.activiti.service.impl;

import com.cnpc.framework.activiti.entity.DelegateHistory;
import com.cnpc.framework.activiti.entity.DelegateInfo;
import com.cnpc.framework.activiti.entity.Module;
import com.cnpc.framework.activiti.pojo.TaskVo;
import com.cnpc.framework.activiti.service.DelegateService;
import com.cnpc.framework.activiti.service.TaskPageService;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.service.impl.BaseServiceImpl;
import com.cnpc.framework.utils.StrUtil;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by billJiang on 2017/7/1.
 * e-mail:475572229@qq.com  qq:475572229
 * 任务委托
 */
@Service("delegateService")
public class DelegateServiceImpl extends BaseServiceImpl implements DelegateService {

    @Resource
    private TaskPageService taskPageService;

    @Override
    public String getAttorneyByAssignee(String assignee, String moduleCode) {
        String hql = "from Module where code='" + moduleCode + "'";
        Module module = this.get(hql);
        if(module==null)
            return null;
        DetachedCriteria criteria = DetachedCriteria.forClass(DelegateInfo.class);
        criteria.add(Restrictions.eq("assignee", assignee));
        Date now = new Date();
        criteria.add(Restrictions.le("startTime", now));
        criteria.add(Restrictions.ge("endTime", now));
        criteria.add(Restrictions.like("moduleId", "%" + module.getId() + "%"));
        criteria.add(Restrictions.eq("deleted", 0));
        List<DelegateInfo> delegateInfos = this.findByCriteria(criteria);
        if (delegateInfos.isEmpty()) {
            return null;
        } else {
            return delegateInfos.get(0).getAttorney();
        }
    }

    @Override
    public Result deleteDelegate(String id) {
        DelegateInfo delegateInfo = this.get(DelegateInfo.class, id);
        String hql = "from DelegateHistory where delegateId='" + id + "'";
        List<DelegateHistory> historyList = this.find(hql);
        if (historyList.isEmpty()) {
            this.delete(delegateInfo);
            return new Result(true);
        } else {
            return new Result(false, true, "关联了委托历史，不可删除");
        }
    }

    @Override
    public Result saveDelegate(DelegateInfo delegateinfo) {
        if (StrUtil.isEmpty(delegateinfo.getId())) {
            this.save(delegateinfo);
        } else {
            delegateinfo.setUpdateDateTime(new Date());
            this.update(delegateinfo);
        }
        List<TaskVo> volist = new ArrayList<>();
        if (delegateinfo.getDeleted().equals(0)) {
            volist=taskPageService.delegateTasks(delegateinfo.getAssignee(), delegateinfo.getAttorney(), delegateinfo
                    .getModuleId());
        }
        return new Result(true, volist, "委托设置保存成功！");
    }
}
