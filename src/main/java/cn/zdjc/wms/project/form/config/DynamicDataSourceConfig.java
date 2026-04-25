package cn.zdjc.wms.project.form.config;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DynamicDataSourceConfig {

    @Autowired
    private Environment environment;

    @Bean
    @Primary
    public DataSource dynamicDataSource() throws SQLException {
        // 1. 创建默认数据源（主库）
        // 从 spring.datasource.* 读取连接参数，避免与 MyBatis/MyBatis-Plus 形成循环依赖
        DruidDataSource defaultDataSource = createDataSource(
                environment.getProperty("spring.datasource.driver-class-name"),
                environment.getProperty("spring.datasource.url"),
                environment.getProperty("spring.datasource.username"),
                environment.getProperty("spring.datasource.password"),
                null, null, null, null, null);

        // 2. 创建 DynamicDataSource，仅设默认数据源
        DynamicDataSource dynamicDataSource = new DynamicDataSource();
        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);
        Map<Object, Object> targets = new HashMap<>();
        targets.put("default", defaultDataSource);
        dynamicDataSource.setTargetDataSources(targets);
        dynamicDataSource.afterPropertiesSet();

        return dynamicDataSource;
    }

    private static DruidDataSource createDataSource(String driverClassName, String url,
                                                     String username, String password,
                                                     Integer initialSize, Integer minIdle,
                                                     Integer maxActive, Long maxWait,
                                                     String validationQuery) throws SQLException {
        DruidDataSource ds = new DruidDataSource();
        if (driverClassName != null) {
            ds.setDriverClassName(driverClassName);
        }
        if (url != null) {
            ds.setUrl(url);
        }
        if (username != null) {
            ds.setUsername(username);
        }
        if (password != null) {
            ds.setPassword(password);
        }
        ds.setInitialSize(initialSize != null ? initialSize : 3);
        ds.setMinIdle(minIdle != null ? minIdle : 3);
        ds.setMaxActive(maxActive != null ? maxActive : 60);
        ds.setMaxWait(maxWait != null ? maxWait : 60000L);
        ds.setValidationQuery(validationQuery != null ? validationQuery : "SELECT 1");
        ds.setTestWhileIdle(true);
        ds.setTimeBetweenEvictionRunsMillis(60000);
        ds.setMinEvictableIdleTimeMillis(300000);
        ds.setTestOnBorrow(false);
        ds.setTestOnReturn(false);
        ds.setPoolPreparedStatements(true);
        ds.setMaxPoolPreparedStatementPerConnectionSize(20);
        ds.setFilters("slf4j,stat");
        ds.setUseGlobalDataSourceStat(true);
        return ds;
    }

    /**
     * 在全部 Bean 初始化完成后加载数据库配置表中的动态数据源，
     * 避免 DatabaseConfigService → MyBatis Mapper → sqlSessionFactory → DataSource 的循环依赖。
     */
    @Component
    public static class DynamicDataSourceLoader {

        private final DynamicDataSource dynamicDataSource;
        private final DatabaseConfigService databaseConfigService;

        public DynamicDataSourceLoader(DynamicDataSource dynamicDataSource,
                                       DatabaseConfigService databaseConfigService) {
            this.dynamicDataSource = dynamicDataSource;
            this.databaseConfigService = databaseConfigService;
        }

        @EventListener(ContextRefreshedEvent.class)
        public void loadDataSources() {
            try {
                List<DatabaseConfigEntity> dbConfigs = databaseConfigService.getAllEnabledConfigs();
                // 保留已有目标数据源（含默认数据源）
                Map<Object, Object> targets = new HashMap<>(dynamicDataSource.getResolvedDataSourcesMap());

                // 加载数据库配置表中的所有启用的数据源
                for (DatabaseConfigEntity config : dbConfigs) {
                    try {
                        DruidDataSource ds = new DruidDataSource();
                        ds.setDriverClassName(config.getDriverClass());
                        ds.setUrl(config.getJdbcUrl());
                        ds.setUsername(config.getUsername());
                        ds.setPassword(config.getPassword());
                        ds.setInitialSize(config.getInitialSize() != null ? config.getInitialSize() : 3);
                        ds.setMinIdle(config.getMinIdle() != null ? config.getMinIdle() : 3);
                        ds.setMaxActive(config.getMaxActive() != null ? config.getMaxActive() : 60);
                        ds.setMaxWait(config.getMaxWait() != null ? config.getMaxWait() : 60000L);

                        if (config.getValidationQuery() != null) {
                            ds.setValidationQuery(config.getValidationQuery());
                        } else {
                            switch (config.getDbType().toUpperCase()) {
                                case "ORACLE":
                                    ds.setValidationQuery("SELECT 1 FROM DUAL");
                                    break;
                                default:
                                    ds.setValidationQuery("SELECT 1");
                            }
                        }

                        ds.setTestWhileIdle(true);
                        ds.setTimeBetweenEvictionRunsMillis(60000);
                        ds.setMinEvictableIdleTimeMillis(300000);
                        ds.setTestOnBorrow(false);
                        ds.setTestOnReturn(false);
                        ds.setPoolPreparedStatements(true);
                        ds.setMaxPoolPreparedStatementPerConnectionSize(20);
                        ds.setFilters("slf4j,stat");
                        ds.setUseGlobalDataSourceStat(true);

                        targets.put(config.getDbCode(), ds);
                    } catch (Exception e) {
                        System.err.println("创建数据源失败: " + config.getDbCode() + ", 错误: " + e.getMessage());
                    }
                }

                // 3. 更新 DynamicDataSource
                dynamicDataSource.setTargetDataSources(targets);
                dynamicDataSource.afterPropertiesSet();
            } catch (Exception e) {
                System.err.println("加载动态数据源失败: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
