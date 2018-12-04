package com.cnpc.activiti.service;

import com.cnpc.framework.base.service.BaseService;

import java.util.List;

/**
 * Created by billJiang on 2017/7/5.
 * e-mail:475572229@qq.com  qq:475572229
 * 任务分配用户接口，用于特殊用户分配
 */
public interface AssigneeService extends BaseService {


    /**
     * 根据启动用户查询部门室主任
     *
     * @param startUserId 启动用户Id
     * @param groupId     用户组ID
     * @return
     */
    List<String> findChiefs(String startUserId, String groupId);

    /**
     * 查询室主任的直线领导(分管所长)
     *
     * @param chiefId 室主任ID
     * @param groupId 用户组ID
     * @return
     */
    List<String> findDirectLeaders(String chiefId, String groupId);

    /**
     * 查询部门领导（处所领导）
     *
     * @param startUserId 启动用户ID
     * @param groupId     用户组ID
     * @return
     */
    List<String> findLeaders(String startUserId, String groupId);

    /**
     * 获取正职领导
     *
     * @param startUserId 启动用户ID
     * @param groupId     用户组ID
     * @return
     */
    List<String> findChiefLeaders(String startUserId, String groupId);


}
