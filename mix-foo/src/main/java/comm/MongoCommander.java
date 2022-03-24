    package comm;

    import org.apache.commons.exec.CommandLine;
    import org.apache.commons.exec.DefaultExecutor;
    import org.apache.commons.exec.ExecuteWatchdog;
    import org.apache.commons.exec.PumpStreamHandler;

    import java.io.ByteArrayOutputStream;
    import java.io.IOException;
    import java.nio.charset.StandardCharsets;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;

    /**
     * 通过 MongoDB 客户端执行 MongoDB 命令获取结果。
     *
     * 执行的命令:
     * /usr/local/bin/mongo 192.168.12.20:34005/test --authenticationDatabase admin -u admin -p admin --quiet < /temp/test.js
     * C:/temp/mongo.exe 192.168.12.20:34005/test --authenticationDatabase admin -u admin -p admin --quiet < /temp/test.js
     */
    public class MongoCommander {
        /**
         * MongoDB 客户端的路径，可配置
         */
        private static final String MONGO_CLIENT_LINUX = "/usr/local/bin/mongo";
        private static final String MONGO_CLIENT_WIN   = "C:/temp/mongo.exe";

        /**
         * Windows 系统 WIN 为 true，否则为 false
         */
        private static final boolean WIN = System.getProperty("os.name").toLowerCase().contains("win");

        public static void main(String[] args) throws IOException {
/* test.js 的内容
db.version()
db.movie.find().limit(2)
db.movie.find().skip(2).limit(3))
db.movie.aggregate([
    { $group: {
        _id: "$by_user",
        num_tutorial: { $sum: 1 }
    } }
]);
*/
            // 获取用户输入的 MongoDB 命令
            String command = new String(Files.readAllBytes(Paths.get("/Users/biao/Downloads/test.js")));
            String mongoClient = WIN ? MONGO_CLIENT_WIN : MONGO_CLIENT_LINUX;
            String result = MongoCommander.execMongoCommand(mongoClient,
                    "192.168.12.20:34005",
                    "admin", "admin", "admin",
                    "test", command);
            System.out.println(result);
        }

        /**
         * 执行 MongoDB 命令，获取返命令结果
         *
         * @param mongoClient MongoDB 客户端路径
         * @param ipPort      MongoDB IP:Port
         * @param authDb      登录认证的数据库名
         * @param username    账号
         * @param password    密码
         * @param targetDb    执行命令的数据库名
         * @param command     用户输入的命令
         * @return 返回执行结果的字符串
         */
        public static String execMongoCommand(String mongoClient,
                                              String ipPort,
                                              String authDb,
                                              String username,
                                              String password,
                                              String targetDb,
                                              String command) throws IOException {
            /* 逻辑：
               1. 把 command 保存到临时 js 文件
               2. 生成访问 MongoDB 的完整命令
               3. 把完整命令保存到临时 sh 文件
               4. 执行 sh 脚本，获取结果
               5. 删除生成的临时文件
               6. 返回结果
             */

            // [1] 把 command 保存到临时 js 文件
            Path jsPath = Files.createTempFile("mongo-script-", ".js");
            Files.write(jsPath, command.getBytes(StandardCharsets.UTF_8));

            // [2] 生成访问 MongoDB 的完整命令
            // /usr/local/bin/mongo 192.168.12.20:34005/test --authenticationDatabase admin -u admin -p admin --quiet < /temp/test.js
            String finalCommand = String.format("%s %s/%s --authenticationDatabase %s -u %s -p %s  --quiet < %s",
                    mongoClient, ipPort, targetDb, authDb, username, password, jsPath);

            // [3] 把完整命令保存到临时 sh 文件
            Path shPath = Files.createTempFile("mongo-exec-", WIN ? ".bat" : ".sh");
            Files.write(shPath, finalCommand.getBytes(StandardCharsets.UTF_8));

            // [4] 执行 sh 脚本，获取结果
            String result = MongoCommander.execSh(shPath.toString());

            // [5] 删除生成的临时文件
            Files.delete(jsPath);
            Files.delete(shPath);

            // [6] 返回结果
            return result;
        }

        /**
         * 执行 sh 脚本
         *
         * @param path 脚本路径
         */
        public static String execSh(String path) throws IOException {
            System.out.println("执行脚本: " + path);

            // Windows 直接指向 x.bat，Unix like 执行 sh x.sh
            CommandLine cmdLine = CommandLine.parse(WIN ? path : "sh " + path);
            DefaultExecutor executor = new DefaultExecutor();
            executor.setExitValues(null);

            ExecuteWatchdog watchdog = new ExecuteWatchdog(60000);
            executor.setWatchdog(watchdog);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream,errorStream);

            executor.setStreamHandler(streamHandler);
            executor.execute(cmdLine);

            // 获取程序外部程序执行结果
            String out = outputStream.toString("UTF-8");
            String error = errorStream.toString("UTF-8");

            // 处理结果
            return out + error;
        }
    }
