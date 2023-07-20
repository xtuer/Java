package newdt.dsc.config;

import lombok.Data;
import newdt.dsc.bean.db.DatabaseMetadataConfig;
import newdt.dsc.bean.db.DatabaseType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 数据库元数据的配置，从 application.yml 中的 databaseMetadataConfigs 读取。
 */
@Data
@Configuration
@ConfigurationProperties
@EnableConfigurationProperties
public class DatabaseMetadataConfigs {
    private static final String JDBC = "jdbc";

    /**
     * 数据库元数据配置，每个数据库对应一个配置对象。
     */
    private List<DatabaseMetadataConfig> databaseMetadataConfigs;

    /**
     * 获取数据库的配置。
     *
     * @param type 数据库类型。
     * @return 返回数据库的配置。
     * @throws RuntimeException 查询不到则抛出 RuntimeException 异常。
     */
    public DatabaseMetadataConfig findConfig(DatabaseType type) {
        for (DatabaseMetadataConfig cfg : databaseMetadataConfigs) {
            if (cfg.getType().equals(type)) {
                return cfg;
            }
        }

        throw new RuntimeException("数据库没有 metadata 配置: " + type);
    }

    /**
     * 判断传入的查询语句是否使用 JDBC。
     *
     * @param query 查询语句。
     * @return 如果传入的查询语句使用 JDBC (等于 "jdbc") 返回 true，否则返回 false。
     */
    public static boolean useJdbc(String query) {
        return JDBC.equalsIgnoreCase(query);
    }
}
