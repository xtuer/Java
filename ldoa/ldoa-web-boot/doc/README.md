## 环境搭建

1. 安装 MySQL:
   * 账号: root
   * 密码: root
2. 初始化数据库
    1. 创建数据库 ldoa: `CREATE DATABASE ldoa CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;`
    2. 创建数据库表: 终端进入 sql 目录，执行 `db-init.sh root root exam`
3. ~~安装 Redis (默认已经禁用)~~
    * 如果想禁用 Redis，修改 Application.java 中的 `@EnableMethodCache` 的包名为不存在的包名，例如 `@EnableMethodCache(basePackages = "com.xtuer.service2")`
4. 安装 Gradle 6.6 即以上
5. 设置环境变量 `JASYPT_ENCRYPTOR_PASSWORD=xtuer` (数据库密码使用 Jasypt 进行了加密)
6. 启动项目: 
   * gradle `bootRun`
   * Mac: `gradle bootRun --args='--spring.profiles.active=mac'`
   * Win: `gradle bootRun --args='--spring.profiles.active=win'`

