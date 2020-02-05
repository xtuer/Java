## 环境搭建

1. 安装 MySQL:
   * 账号: root
   * 密码: root
   * 创建数据库 exam

2. 安装 Redis

3. 安装 Gradle

4. 在系统环境变量中设置服务器 ID 的环境变量 SERVER_ID，范围是 [0, 1023]，集群中每个服务器的 ID 唯一

   应用中使用分布式 ID 生成算法 Snowflake 生成 ID，依赖于 SERVER_ID 的值

5. 启动项目: gradle `bootRun`

## 修改项目名

项目、包名等命名为 **com.xtuer**，可修改为实际项目名字:

* IDEA 中搜索整个项目里的 **com.tuer**: `Edit -> Find -> Replace in Path...` 输入 `com.xtuer`
* 全部替换为需要的名字如 `com.foo`
* 修改 Java 源码中文件夹名 `com > xtuer` 为 `com > foo`
