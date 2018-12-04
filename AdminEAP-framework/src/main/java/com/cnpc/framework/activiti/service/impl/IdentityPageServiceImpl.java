package com.cnpc.framework.activiti.service.impl;

import com.cnpc.framework.activiti.service.IdentityPageService;
import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.base.service.impl.BaseServiceImpl;
import com.cnpc.framework.query.entity.QueryCondition;
import com.cnpc.framework.utils.StrUtil;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.NativeGroupQuery;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.persistence.entity.GroupEntity;
import org.activiti.engine.impl.persistence.entity.MembershipEntity;
import org.activiti.engine.impl.persistence.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by billJiang on 2017/6/16.
 * e-mail:475572229@qq.com  qq:475572229
 * 工作流用户 用户组服务实现，为区分identityService 命名为identityPageService
 */
@Service("identityPageService")
public class IdentityPageServiceImpl extends BaseServiceImpl implements IdentityPageService {

    @Autowired
    private IdentityService identityService;

    @Autowired
    private ManagementService managementService;



    @Override
    public List<User> getUserList(QueryCondition condition, PageInfo pageInfo) {
        String name = null;
        String groupId = null;
        if (condition != null) {
            name = condition.getConditionMap().get("name").toString();
            if (condition.getConditionMap().containsKey("groupId")) {
                groupId = condition.getConditionMap().get("groupId").toString();
            }
        }
        List<User> userList;
        long count;
        UserQuery query = identityService.createUserQuery();
        if (!StrUtil.isEmpty(name)) {
            query = query.userFirstNameLike(name);
        }
        if (!StrUtil.isEmpty(groupId)) {
            query = query.memberOfGroup(groupId);
        }
        count = query.count();
        userList = query.orderByUserId().asc().listPage((pageInfo.getPageNum() - 1) * pageInfo.getPageSize(),
                pageInfo.getPageSize());
        pageInfo.setCount((int) count);
        return userList;
    }


    /**
     * or 查询
     *
     * @param condition 查询条件  name (分组名称或type:code)
     * @param pageInfo  分页信息
     * @return
     */
    @Override
    public List<Group> getGroupList(QueryCondition condition, PageInfo pageInfo) {
        String name = null;
        if (condition != null) {
            name = condition.getConditionMap().get("name").toString();
            //groupId=condition.getConditionMap().get("groupId").toString();
        }
        List<Group> groupList;
        long count;
        String sql = "SELECT * FROM " + managementService.getTableName(Group.class) + " where 1=1 order by ID_";
        if (!StrUtil.isEmpty(name)) {
            sql = sql.replace("1=1", "NAME_ like '" + name + "' or TYPE_ like '" + name + "'");
        }
        NativeGroupQuery query = identityService.createNativeGroupQuery().sql(sql);
        //native查询中一下查询会有异常
        //count=query.count();
        //改成如下，查询正常
        count = identityService.createNativeGroupQuery().sql("select count(ID_) from (" + sql + ") t").count();
        groupList = query.listPage((pageInfo.getPageNum() - 1) * pageInfo.getPageSize(), pageInfo.getPageSize());
        pageInfo.setCount((int) count);
        return groupList;
    }


    @Override
    public String getUserNamesByUserIds(String userIds) {
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct FIRST_ as name from " + managementService.getTableName(User.class) + " us");
        buf.append(" where us.ID_ in (" + StrUtil.getInStr(userIds) + ")");
        List list = this.findMapBySql(buf.toString());
        return StrUtil.mapToStr(list, "name");
    }

    @Override
    public String getGroupNamesByGroupIds(String groupIds) {
        StringBuffer buf = new StringBuffer();
        buf.append("select distinct NAME_ as name from " + managementService.getTableName(Group.class) + " gp");
        buf.append(" where gp.ID_ in (" + StrUtil.getInStr(groupIds) + ")");
        List list = this.findMapBySql(buf.toString());
        return StrUtil.mapToStr(list, "name");
    }

    @Override
    public String getUserNamesByGroupIds(String groupIds){
        StringBuffer buf = new StringBuffer();
        buf.append("select DISTINCT FIRST_ as name from "+managementService.getTableName(MembershipEntity.class)+" m");
        buf.append(" left JOIN "+managementService.getTableName(User.class)+" u on m.USER_ID_=u.ID_");
        buf.append(" where m.GROUP_ID_ in ("+StrUtil.getInStr(groupIds) +")");
        List list = this.findMapBySql(buf.toString());
        return StrUtil.mapToStr(list, "name");
    }

    @Override
    public List<String> getUserIdsByGroupIds(String groupIds){
        StringBuffer buf = new StringBuffer();
        buf.append("select DISTINCT u.ID_ as id from "+managementService.getTableName(MembershipEntity.class)+" m");
        buf.append(" left JOIN "+managementService.getTableName(User.class)+" u on m.USER_ID_=u.ID_");
        buf.append(" where m.GROUP_ID_ in ("+StrUtil.getInStr(groupIds) +")");
        List list = this.findMapBySql(buf.toString());
        List<String> idlist=new ArrayList<>();
        for (Object obj : list) {
            idlist.add(((Map)obj).get("id").toString());
        }
        return idlist;
    }

    @Override

    public User findUserById(String userId) {
        String sql = "select id,name,login_name,email,password from tbl_user where id=:id";
        Map<String, Object> param = new HashMap<>();
        param.put("id", userId);
        List<Map<String, Object>> maps = super.findMapBySql(sql, param);
        if (maps == null || maps.isEmpty())
            return null;
        Map<String, Object> map = maps.get(0);
        UserEntity userEntity = new UserEntity();
        userEntity.setId(map.get("id").toString());
        userEntity.setFirstName(map.get("name").toString());
        userEntity.setLastName(map.get("login_name").toString());
        userEntity.setEmail(map.get("email").toString());
        userEntity.setPassword(map.get("password").toString());
        return userEntity;
    }

    @Override
    public List<Group> findGroupsByUser(String userId) {
        String sql = "select r.id id,name,code from tbl_role r" +
                " left join tbl_user_role ur on r.id=ur.roleid" +
                " where ur.userid=:userid";
        Map<String, Object> param = new HashMap<>();
        param.put("userid", userId);
        List<Map<String, Object>> maps = super.findMapBySql(sql, param);
        List<Group> groups = new ArrayList<>();
        for (Map<String, Object> map : maps) {
            Group group = new GroupEntity();
            group.setId(map.get("id").toString());
            group.setName(map.get("name").toString());
            group.setType(map.get("code").toString());
            groups.add(group);
        }
        return groups;
    }

    @Override
    public Group findGroupById(String groupId) {
        String sql = "select id,name,code from tbl_role where id=:id";
        Map<String, Object> param = new HashMap<>();
        param.put("id", groupId);
        List<Map<String, Object>> maps = super.findMapBySql(sql, param);
        if (maps == null || maps.isEmpty())
            return null;
        Map<String, Object> map = maps.get(0);
        GroupEntity group = new GroupEntity();
        group.setId(map.get("id").toString());
        group.setName(map.get("name").toString());
        group.setType(map.get("code").toString());
        return group;
    }

    @Override
    public Group getGroupByCode(String groupCode) {
        String sql = "select id,name,code from tbl_role where code=:code";
        Map<String, Object> param = new HashMap<>();
        param.put("code", groupCode);
        List<Map<String, Object>> maps = super.findMapBySql(sql, param);
        if (maps == null || maps.isEmpty())
            return null;
        Map<String, Object> map = maps.get(0);
        GroupEntity group = new GroupEntity();
        group.setId(map.get("id").toString());
        group.setName(map.get("name").toString());
        group.setType(map.get("code").toString());
        return group;
    }

    /**
     * 根据用户ID，获取用户信息
     *
     * @param userId
     * @return
     */
    @Override
    public User getUser(String userId) {
        User user = identityService.createUserQuery().userId(userId).singleResult();
        return user;
    }

}
