package com.test.activiti;

import com.cnpc.framework.activiti.service.IdentityPageService;
import com.cnpc.framework.testng.BaseTest;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by billJiang on 2017/6/18.
 * e-mail:475572229@qq.com  qq:475572229
 */
public class IdentityTest extends BaseTest {

    @Resource
    private IdentityPageService identityPageService;

    @Test(dataProvider = "dataProvider", groups = {"activiti-test"})
    public void getUserById(String id) {
        User user = identityPageService.findUserById(id);
        Assert.assertEquals(user.getId(), id);
    }

    @Test(dataProvider = "dataProvider", groups = {"activiti-test"})
    public void getGroupsByUserId(String id, Integer groupCount) {
        List<Group> groups = identityPageService.findGroupsByUser(id);
        Assert.assertEquals(groupCount.intValue(), groups.size());
    }

}
