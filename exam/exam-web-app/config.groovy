// 打包: gradle clean assemble -Denv=production
// 部署: gradle clean deploy   -Denv=production

// 把下面的映射添加到 hosts 文件，如果 mysql, redis 等安装在其他机器上，修改为对应机器的 IP
// 127.0.0.1 mysql.exam
// 127.0.0.1 redis.exam
// 127.0.0.1 mongodb.exam
// 127.0.0.1 activemq.exam
// 127.0.0.1 zooKeeper.exam
// 127.0.0.1 elasticsearch.exam

////////////////////////////////////////////////////////////////////////////////////
//                               定义所有环境下都有的通用配置
////////////////////////////////////////////////////////////////////////////////////
//environments {
//  usedByAllEnvironments {
        deploy {
            host     = '127.0.0.1'
            username = 'root'
            password = 'root'
        }

        database {
            host     = 'mysql.exam'
            dbname   = 'exam'
            username = 'root'
            password = 'root'
        }

        mongo {
            host = 'mongo.exam'
            port = 27017
            database = 'exam'
            username = 'exam'
            password = 'exam'
        }

        redis {
            host     = 'redis.exam'
            port     = 6379
            password = ''
            database = 0
            timeout  = 2000
            minIdle  = 2
            maxIdle  = 10
            maxTotal = 10
        }

        mq {
            brokerUrl      = 'tcp://activemq.exam:61616' // 消息队列的 Broker
            maxConnections = 10 // 最大连接数
        }

        // 应用的 ID 和 key，用于生成身份认证的 token
        appId  = 'Default_ID'
        appKey = 'Default_Key'

        thymeleafCacheable = true    // thymeleaf 使用缓存提高效率
        repoDirectory = '/exam/repo' // 文件仓库目录
        tempDirectory = '/exam/temp' // 临时文件目录，例如存储上传的临时文件，里面的文件可以超过几天不放问可以用 crontab 自动删除
        logsDirectory = '/exam/logs' // 日志目录
//  }
//}

////////////////////////////////////////////////////////////////////////////////////
//                定义不同环境下特有的配置，同路径时覆盖上面定义的通用配置
////////////////////////////////////////////////////////////////////////////////////
environments {
    /*-----------------------------------------------------------------------------|
     |                                 开发环境配置                                 |
     |----------------------------------------------------------------------------*/
    // 本机开发环境配置
    dev {
        thymeleafCacheable = false // 开发环境下 thymeleaf 不缓存页面
    }

    win {
        thymeleafCacheable = false
        repoDirectory = 'D:/exam/repo' // 文件仓库目录
        tempDirectory = 'D:/exam/temp' // 临时文件目录
        logsDirectory = 'D:/exam/logs' // 日志目录
    }

    mac {
        thymeleafCacheable = false
        repoDirectory = '/usr/local/exam/repo' // 文件仓库目录
        tempDirectory = '/usr/local/exam/temp' // 临时文件目录
        logsDirectory = '/usr/local/exam/logs' // 日志目录
    }

    /*-----------------------------------------------------------------------------|
     |                                 测试环境配置                                 |
     |----------------------------------------------------------------------------*/
    test1 {
        deploy {
            host = '192.168.10.189'
        }
    }

    test2 {
        deploy {
            host = '192.168.1.99'
        }
    }
}
