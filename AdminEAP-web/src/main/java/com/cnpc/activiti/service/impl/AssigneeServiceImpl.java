package com.cnpc.activiti.service.impl;

import com.cnpc.activiti.service.AssigneeService;
import com.cnpc.framework.activiti.service.IdentityPageService;
import com.cnpc.framework.base.entity.Org;
import com.cnpc.framework.base.entity.User;
import com.cnpc.framework.base.service.impl.BaseServiceImpl;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by billJiang on 2017/7/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 获取审批人 以下代码根据具体业务调整 在此仅为演示用
 */
@Service("assigneeService")
public class AssigneeServiceImpl extends BaseServiceImpl implements AssigneeService {

    @Resource
    private IdentityPageService identityPageService;


    @Override
    public List<String> findChiefs(String startUserId, String groupId) {
        List<String> userIds = identityPageService.getUserIdsByGroupIds(groupId);
        User user = this.get(User.class, startUserId);
        DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
        criteria.add(Restrictions.in("id", userIds));
        criteria.add(Restrictions.eq("deptId", user.getDeptId()));
        List<String> retIds = new ArrayList<>();
        List<User> users = this.findByCriteria(criteria);
        for (User user1 : users) {
            retIds.add(user1.getId());
        }
        return retIds;
    }

    @Override
    public List<String> findDirectLeaders(String chiefId, String groupId) {

        List<String> userIds = identityPageService.getUserIdsByGroupIds(groupId);
        User user = this.get(User.class, chiefId);
        DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
        criteria.add(Restrictions.in("id", userIds));
        Org org = this.get(Org.class, user.getDeptId());
        Org parentOrg = this.get(Org.class, org.getParentId());
        criteria.add(Restrictions.eq("deptId", parentOrg.getId()));
        List<String> retIds = new ArrayList<>();
        List<User> users = this.findByCriteria(criteria);
        //TODO DEMO
        for (User user1 : users) {
            if (!user1.getName().startsWith("正职"))
                retIds.add(user1.getId());
        }
        return retIds;
    }

    @Override
    public List<String> findLeaders(String startUserId, String groupId) {

        List<String> userIds = identityPageService.getUserIdsByGroupIds(groupId);
        User user = this.get(User.class, startUserId);
        DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
        criteria.add(Restrictions.in("id", userIds));
        Org org = this.get(Org.class, user.getDeptId());
        Org parentOrg = this.get(Org.class, org.getParentId());
        criteria.add(Restrictions.eq("deptId", parentOrg.getId()));
        List<String> retIds = new ArrayList<>();
        List<User> users = this.findByCriteria(criteria);
        //TODO DEMO
        for (User user1 : users) {
            retIds.add(user1.getId());
        }
        return retIds;
    }

    @Override
    public List<String> findChiefLeaders(String startUserId, String groupId) {

        List<String> userIds = identityPageService.getUserIdsByGroupIds(groupId);
        User user = this.get(User.class, startUserId);
        DetachedCriteria criteria = DetachedCriteria.forClass(User.class);
        criteria.add(Restrictions.in("id", userIds));
        Org org = this.get(Org.class, user.getDeptId());
        Org parentOrg = this.get(Org.class, org.getParentId());
        criteria.add(Restrictions.eq("deptId", parentOrg.getId()));
        List<String> retIds = new ArrayList<>();
        List<User> users = this.findByCriteria(criteria);
        //TODO DEMO
        for (User user1 : users) {
            if (user1.getName().startsWith("正职"))
                retIds.add(user1.getId());
        }
        return retIds;
    }


}
