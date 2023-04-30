package com.xtuer.config;

import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

// @Configuration
public class MyDatabaseIdProvider implements DatabaseIdProvider {
    @Override
    public String getDatabaseId(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            String dbType = conn.getMetaData().getDatabaseProductName();

            switch (dbType) {
                case "MySQL":
                    return "mysql";
                case "Oracle":
                    return "oracle";
                case "PostgreSQL":
                    return "pg";
            }

            return null;
        }
    }
}
