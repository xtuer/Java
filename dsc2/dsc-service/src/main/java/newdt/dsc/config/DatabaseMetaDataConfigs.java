package newdt.dsc.config;

import lombok.Data;
import newdt.dsc.bean.db.DatabaseMetadataConfig;
import newdt.dsc.bean.db.DatabaseType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

import static newdt.dsc.bean.db.DatabaseMetadataConfig.QueryConfig;

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
     * 判断传入的查询语句是否使用 JDBC。
     *
     * @param query 查询语句。
     * @return 如果传入的查询语句使用 JDBC (等于 "jdbc") 返回 true，否则返回 false。
     */
    public static boolean useJdbc(String query) {
        return JDBC.equalsIgnoreCase(query);
    }

    /**
     * 获取数据库的配置。
     *
     * @param type 数据库类型。
     * @return 返回数据库的配置。
     * @throws RuntimeException 查询不到则抛出 RuntimeException 异常。
     */
    public DatabaseMetadataConfig findMetadataConfig(DatabaseType type) {
        for (DatabaseMetadataConfig cfg : databaseMetadataConfigs) {
            if (cfg.getType().equals(type)) {
                return cfg;
            }
        }

        throw new RuntimeException("数据库没有 metadata 配置: " + type);
    }

    /**
     * 从传入的查询配置中查找与当前数据库版本匹配的配置。
     *
     * @param conn 数据库连接。
     * @param configs 查询配置。
     * @return 返回查找到的查询配置。
     * @throws SQLException 访问数据库出错时抛出 SQL 异常。
     * @throws RuntimeException 查询不到合适的查询配置则抛出运行时异常。
     */
    public static QueryConfig findMatchedQueryConfig(Connection conn, List<DatabaseMetadataConfig.QueryConfig> configs) throws SQLException {
        /*
         逻辑:
         1. 计算数据库版本号: 版本号为 "大版本.小版本"，例如 MySQL 的 "5.7"。
         2. 查找版本匹配的配置。
         3. 如果没有找到版本匹配的配置，返回第一个版本为空的配置 (默认配置)。
         4. 如果没有找到版本匹配的配置，也没有版本为空的配置，则抛出异常。
         */

        DatabaseMetaData meta = conn.getMetaData();
        meta.getDatabaseMajorVersion();

        // [1] 计算数据库版本号: 版本号为 "大版本.小版本"，例如 MySQL 的 "5.7"。
        String version = String.format("%s.%s", meta.getDatabaseMajorVersion(), meta.getDatabaseMinorVersion());

        // [2] 查找版本匹配的配置。
        for (QueryConfig cfg : configs) {
            if (Objects.equals(version, cfg.getVersion())) {
                return cfg;
            }
        }

        // [3] 如果没有找到版本匹配的配置，返回第一个版本为空的配置 (默认配置)。
        for (QueryConfig cfg : configs) {
            if (!StringUtils.hasText(cfg.getVersion())) {
                return cfg;
            }
        }

        // [4] 如果没有找到版本匹配的配置，也没有版本为空的配置，则抛出异常。
        throw new RuntimeException("没有配置合适的数据库查询配置");
    }
}
