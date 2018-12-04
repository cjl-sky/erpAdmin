package com.cnpc.framework.activiti.service;

import com.cnpc.framework.base.pojo.PageInfo;
import com.cnpc.framework.base.service.BaseService;
import com.cnpc.framework.query.entity.QueryCondition;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;

import java.util.List;

/**
 * Created by billJiang on 2017/6/16.
 * e-mail:475572229@qq.com  qq:475572229
 * activiti工作流用户/用户组服务接口
 */
public interface IdentityPageService extends BaseService {

    /**
     * 获取用户列表的接口
     *
     * @param condition 查询条件 name
     * @param pageInfo  分页信息
     * @return
     */
    List<User> getUserList(QueryCondition condition, PageInfo pageInfo);

    /**
     * 获取用户组列表的接口
     *
     * @param condition 查询条件  name (分组名称或type:code)
     * @param pageInfo  分页信息
     * @return
     */
    List<Group> getGroupList(QueryCondition condition, PageInfo pageInfo);

    /**
     * 根据用户id获取用户名称
     * @param userIds
     * @return
     */
    String getUserNamesByUserIds(String userIds);

    /**
     * 根据用户组id获取用户组名称
     * @param groupIds
     * @return
     */
    String getGroupNamesByGroupIds(String groupIds);

    /**
     * 根据用户组id获取用户名称
     * @param groupIds
     * @return
     */
    String getUserNamesByGroupIds(String groupIds);

    List<String> getUserIdsByGroupIds(String groupIds);
     /**
     * 自定义用户查询接口 覆盖原来的方法
     * @param userId 用户id
     * @return
     */
    User findUserById(String userId);

    /**
     * 自定根据用户查找角色
     * @param userId
     * @return
     */
    List<Group> findGroupsByUser(String userId);

    /**
     * 自定义查询
     * @param groupId
     * @return
     */
    Group findGroupById(String groupId);

    /**
     *根据用户组编码获取用户组
     */
    Group getGroupByCode(String groupCode);


    /**
     * 根据用户ID，获取用户信息
     * @param userId
     * @return
     */
    User getUser(String userId);


}
