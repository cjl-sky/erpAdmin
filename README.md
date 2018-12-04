# 更新日志
- 2018-03-24     `左侧滚动条不出现+浏览器控制台slimeScroll报错`
- 2018-02-24     `系统异常统一处理`
- 2018-02-24     `解决shiro频繁执行doReadSession的问题`
- 2018-01-21     `用户自定义列`
- 2018-01-21     `授权管理树形菜单checkbox联动bug`
- 2018-01-20     `角色授权（列表-树）`
- 2018-01-19     `支持三级菜单`
- 2018-01-19     `支持数据权限配置，zrA/bill可查看demo`
- 2018-01-09     `弹窗垂直居中`
- 2018-01-09     `数据库连接池C3P0改为Druid`
- 2018-01-09     `修复任务管理的日期选择控件bug`
- 2017-07-19     `用户管理fnRender未定义的bug`
- 2017-07-19     `代码生成器是否生成菜单的判断逻辑`
- 2017-07-18     `BaseFile通用文件上传组件`
- 2017-07-18     `bootstrap-datatimepicker时间选择器（时分秒）`
- 2017-07-18     `集成redis`
- 2017-07-18     `shiro权限缓存/session由redis管理`
- 2017-07-18     `新增组织机构选择器`
- 2017-07-18     `新增任务调度器`
- 2017-07-18     `新增消息管理器`
- 2017-07-18     `完善代码生成器`
- 2017-07-18     `新增组织机构选择器`
- 2017-07-18     `完善权限管理组件`


# 系统简介
![AdminEAP首页](http://code.admineap.com/uploadPath/markdown/admineap.png "AdminEAP首页")

**AdminEAP** --基于AdminLTE的企业应用开发平台，封装和集成多种组件，提供前端、后端整体解决方案，使Web开发更简单。

## 系统构成
**Component 组件集成**
- 封装和集成多个前端开源组件，让前端开发更简单

**CURD 增删改查**
- 多种形式的增删改查Demo，让基础功能开发更高效、便捷

**Tool 系统工具**
- 使用在线工具，更快地辅助系统开发

**Workflow 工作流**
- 基于Activiti的可视化工作流，支持会签、委托、会商、撤回等多种场景，支撑审批业务流转

**System Security 系统安全 **
- 基于Shiro/RBAC的权限管理，同时防范SQL注入、CSRF、XSS攻击

**典型应用**
- 人脸登录、地图应用、OA办公、流程审批、体检预约等应用实例


## 相关资源
AdminEAP 介绍：[http://code.admineap.com/eap/home](http://code.admineap.com/eap/home)

AdminEAP demo：[http://www.admineap.com](http://www.admineap.com)

AdminEAP 技术文档：[http://code.admineap.com/eap/doc](http://code.admineap.com/eap/doc)

在项目部署时如出现问题，请加QQ群：`346837703`，或者发送邮件至 admin@admineap.com。

如购买专业版及相关服务，请联系QQ：`475572229`，或者发送邮件至`475572229@qq.com`
关于专业版与开源版本的区别，详见：[http://code.admineap.com/eap/home](http://code.admineap.com/eap/home)

**通过使用AdminEAP框架，能快速构建你的web应用。**

#技术选型

- 后端架构 -- ```Spring MVC```+```Spring```+```Hibernate```+```Maven```
- 前端架构 --```FreeMarker```+```AdminLTE```+ `其他bootstrap组件`
- 快速开发能力 -- 基于```XML```配置的数据列表，支持分页、查询条件、排序等配置
- 完整的权限系统 -- 基于RBAC+Shiro的权限安全框架
- 基于Quartz的任务调度器
- 代码生成器
- 消息管理器
- Redis缓存
- 基于Activiti的工作流
- 通用文件上传组件


想查看更多功能, 请访问官网Demo：http://www.admineap.com

# 系统部署
**Eclipse下部署**
- git下载代码
- AdminEAP目录下执行 ```mvn eclipse:eclipse```后，再执行```mvn clean compile```
- AdminEAP-framework目录下执行```mvn install```
- AdminEAP-web下`jdbc.porperties` 中配置数据库连接,(数据库的脚本在doc目录下)
- AdminEAP-web下`settings.porperties`中配置redis连接
- 在tomcat或者jetty中启动AdminEAP-web

**Intellij下部署**
- git下载代码
- AdminEAP目录下执行 ```mvn idea:idea```后，再执行```mvn clean compile```
- AdminEAP-framework目录下执行```mvn install```
- AdminEAP-web下`jdbc.porperties` 中配置数据库连接,(数据库的脚本在doc目录下)
- AdminEAP-web下`settings.porperties`中配置redis连接
- 在tomcat或者jetty中启动AdminEAP-web

**如购买了专业版，可远程安装部署**

# 关于版权
个人学习、个人项目可使用开源版本，不需要授权，商业用途强烈建议购买专业版本，以便我们向大家提供更专业的服务和技术支持。


