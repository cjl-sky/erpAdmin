package com.cnpc.framework.query.controller;

import com.alibaba.fastjson.JSON;
import com.cnpc.framework.base.entity.Function;
import com.cnpc.framework.base.pojo.Result;
import com.cnpc.framework.base.pojo.TreeNode;
import com.cnpc.framework.query.entity.Column;
import com.cnpc.framework.query.entity.Query;
import com.cnpc.framework.query.entity.QueryConfig;
import com.cnpc.framework.query.pojo.QueryDefinition;
import com.cnpc.framework.query.service.QueryService;
import com.cnpc.framework.utils.StrUtil;
import com.cnpc.framework.utils.TreeUtil;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * 基于xml配置的query 需要优化
 *
 * @author billjiang
 */
@Controller
@RequestMapping("/query")
public class QueryController {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryController.class);

    @Resource
    private QueryService queryService;

    /**
     * 第一次加载页面初始化
     *
     * @param reqObj 前台参数
     * @return
     */
    @RequestMapping("/loadData")
    @ResponseBody
    public Map<String, Object> loadData(String reqObj) throws Exception {

        return queryService.loadData(reqObj);
    }

    /**
     * 导出数据
     *
     * @param reqObjs   前台参数
     * @param tableName 表名
     * @param response
     * @throws Exception
     */
    @RequestMapping(value = "/exportData")
    public void exportData(String reqObjs, String tableName, HttpServletResponse response)
            throws Exception {
        String tempFile = queryService.exportData(reqObjs, tableName);
        response.getWriter().print(tempFile);
    }

    @RequestMapping("/downExport")
    public void downExport(HttpServletRequest request, HttpServletResponse response) {

        OutputStream out = null;
        try {
            String templateName = request.getParameter("tempfile");
            String fileName = request.getParameter("tableName");
            out = response.getOutputStream();
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/vnd.ms-excel;charset=UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + new String(fileName.getBytes("GBK"), "ISO-8859-1") + ".xls");
            File file = new File(request.getRealPath("/") + File.separator + "templates" + File.separator + "temp" + File.separator
                    + templateName + ".xls");
            FileInputStream inputStream = new FileInputStream(file);
            // 开始读取下载
            byte[] b = new byte[1024];
            int i = 0;
            while ((i = inputStream.read(b)) > 0) {
                out.write(b, 0, i);
            }
            inputStream.close();
            file.delete();
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("text/html; charset=utf-8");
            try {
                out.write("数据表导出异常，请重试！".getBytes("utf-8"));
            } catch (IOException e1) {
            }
        } finally {
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * 跳转到自定义表格配置界面
     */
    @RequestMapping(value = "tableConfig")
    public String tableConfig(String queryId, String pageName, Model model) {
        model.addAttribute("queryId", queryId);
        model.addAttribute("pageName", pageName);
        return "base/query/table_config";
    }


    /**
     * 获取query的所有列配置
     *
     * @param queryId
     * @param session
     * @return
     */
    @RequestMapping(value = "getColumns")
    @ResponseBody
    public List<TreeNode> getColumns(String queryId, HttpSession session) {
        Query query = QueryDefinition.getQueryById(queryId);
        Map<String, TreeNode> nodelist = new LinkedHashMap<String, TreeNode>();
        for (Column column : query.getColumnList()) {
            if (column.getHidden()) {
                continue;
            }
            TreeNode node = new TreeNode();
            node.setText(column.getHeader());
            node.setId(column.getKey());
            nodelist.put(node.getId(), node);
        }
        // 构造树形结构
        return TreeUtil.getNodeList(nodelist);
    }

    /**
     * 获取用户隐藏的列配置
     *
     * @param queryId
     * @param pageName
     * @param session
     * @return
     */
    @RequestMapping(value = "getSelectedColumns")
    @ResponseBody
    public List<String> getSelectedColumns(String queryId, String pageName, HttpSession session) {
        String userid = session.getAttribute("userId").toString();
        return queryService.getSelectedColumns(queryId, pageName, userid);
    }

    /**
     * @param configObj
     * @param session
     * @return
     */
    @RequestMapping(value = "saveQueryConfig")
    @ResponseBody
    public Result saveQueryConfig(String configObj, HttpSession session) {

        QueryConfig config = JSON.parseObject(configObj, QueryConfig.class);
        String userid = session.getAttribute("userId").toString();
        config.setUserid(userid);
        if (StrUtil.isEmpty(config.getColumnsName())) {
            queryService.delete(config);
        } else {
            queryService.deleteAndSave(config);
        }
        return new Result();
    }

    /**
     * 恢复默认设置
     *
     * @param configObj
     * @param session
     * @return
     */
    @RequestMapping(value = "setDefault")
    @ResponseBody
    public Result setDefault(String configObj, HttpSession session) {

        QueryConfig config = JSON.parseObject(configObj, QueryConfig.class);
        String userid = session.getAttribute("userId").toString();
        config.setUserid(userid);
        queryService.delete(config);
        return new Result();
    }


}
