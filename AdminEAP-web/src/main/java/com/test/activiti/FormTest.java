package com.test.activiti;

import com.cnpc.framework.activiti.service.RuntimePageService;
import com.cnpc.framework.base.service.BaseService;
import com.cnpc.framework.testng.BaseTest;
import com.cnpc.framework.utils.StrUtil;
import com.mysql.jdbc.Blob;
import org.activiti.engine.*;
import org.activiti.engine.identity.Group;
import org.activiti.engine.impl.persistence.entity.DeploymentEntity;
import org.activiti.engine.impl.persistence.entity.ResourceEntity;
import org.activiti.engine.repository.Deployment;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by billJiang on 2017/7/7.
 * e-mail:475572229@qq.com  qq:475572229
 */
public class FormTest extends BaseTest {
    @Autowired
    private TaskService taskService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Autowired
    private HistoryService historyService;

    @Resource
    private RuntimePageService runtimePageService;

    @Autowired
    private FormService formService;

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private ManagementService managementService;

    @Resource
    private BaseService baseService;

    @Test(dataProvider = "dataProvider", groups = {"activiti-test"})
    public void updateForm(String resourceId, String formName) throws IOException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(formName);
        if (!StrUtil.isEmpty(resourceId)) {
            String sql = "update " + managementService.getTableName(ResourceEntity.class) + " set BYTES_=:bytes where " +
                    "ID_='" + resourceId + "'";
            Map<String, Object> params = new HashMap<>();
            params.put("bytes", input2byte(inputStream));
            baseService.executeSql(sql, params);

        } else {
            String sql = "update " + managementService.getTableName(ResourceEntity.class) + " set BYTES_=:bytes where" +
                    " NAME_=:name";
            Map<String, Object> params = new HashMap<>();
            params.put("bytes", input2byte(inputStream));
            params.put("name", formName);
            baseService.executeSql(sql, params);
        }
        Assert.assertEquals(1, 1);

    }

    public byte[] input2byte(InputStream inStream)
            throws IOException {
        ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
        byte[] buff = new byte[100];
        int rc = 0;
        while ((rc = inStream.read(buff, 0, 100)) > 0) {
            swapStream.write(buff, 0, rc);
        }
        byte[] in2b = swapStream.toByteArray();
        return in2b;
    }

}
