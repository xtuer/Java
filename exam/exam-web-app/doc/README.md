## 环境搭建

1. 安装 MySQL:
   * 账号: root
   * 密码: root
   * 数据库: exam
2. 安装 MongoDB:
   * 账号: exam
   * 密码: exam
   * 数据库: exam
3. 安装 Redis
4. 安装 Gradle 6.5 及更新版本
5. 启动项目: 
   * Mac: `gradle bootRun --args='--spring.profiles.active=mac'`
   * Win: `gradle bootRun --args='--spring.profiles.active=win'`

## 修改项目名

项目、包名等命名为 **com.xtuer**，可修改为实际项目名字:

* IDEA 中搜索整个项目里的 **com.tuer**: `Edit -> Find -> Replace in Path...` 输入 `com.xtuer`
* 全部替换为需要的名字如 `com.foo`
* 修改 Java 源码中文件夹名 `com > xtuer` 为 `com > foo`