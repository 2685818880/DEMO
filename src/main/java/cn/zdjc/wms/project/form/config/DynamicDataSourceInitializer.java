package cn.zdjc.wms.project.form.config;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DynamicDataSourceInitializer implements ApplicationContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicDataSourceInitializer.class);
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @EventListener(ContextRefreshedEvent.class)
    public void initializeDataSources() {
        try {
            DynamicDataSource dynamicDataSource = applicationContext.getBean(DynamicDataSource.class);
            DatabaseConfigService dbConfigService = applicationContext.getBean(DatabaseConfigService.class);

            List<DatabaseConfigEntity> configs = dbConfigService.getAllEnabledConfigs();
            if (configs.isEmpty()) {
                LOGGER.info("没有额外的数据库配置需要加载");
                return;
            }

            Map<Object, Object> targetDataSources = new HashMap<>();
            targetDataSources.put("default", dynamicDataSource.getResolvedDataSourcesMap().get("default"));

            for (DatabaseConfigEntity config : configs) {
                try {
                    DruidDataSource ds = createDataSource(config);
                    targetDataSources.put(config.getDbCode(), ds);
                    LOGGER.info("已注册数据源: {} ({})", config.getDbCode(), config.getDbType());
                } catch (Exception e) {
                    LOGGER.error("创建数据源失败: {} - {}", config.getDbCode(), e.getMessage(), e);
                }
            }

            dynamicDataSource.setDefaultTargetDataSource(targetDataSources.get("default"));
            dynamicDataSource.setTargetDataSources(targetDataSources);
            dynamicDataSource.afterPropertiesSet();

            LOGGER.info("动态数据源初始化完成，共 {} 个数据源", targetDataSources.size());
        } catch (BeansException e) {
            LOGGER.warn("数据源初始化器依赖的Bean尚未就绪: {}", e.getMessage());
        }
    }

    private DruidDataSource createDataSource(DatabaseConfigEntity config) throws SQLException {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(config.getDriverClass());
        dataSource.setUrl(config.getJdbcUrl());
        dataSource.setUsername(config.getUsername());
        dataSource.setPassword(config.getPassword());
        dataSource.setInitialSize(config.getInitialSize() != null ? config.getInitialSize() : 3);
        dataSource.setMinIdle(config.getMinIdle() != null ? config.getMinIdle() : 3);
        dataSource.setMaxActive(config.getMaxActive() != null ? config.getMaxActive() : 60);
        dataSource.setMaxWait(config.getMaxWait() != null ? config.getMaxWait() : 60000L);
        dataSource.setValidationQuery(config.getValidationQuery() != null ? config.getValidationQuery() : "SELECT 1");
        dataSource.setTestWhileIdle(true);
        dataSource.setTimeBetweenEvictionRunsMillis(60000);
        dataSource.setMinEvictableIdleTimeMillis(300000);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setPoolPreparedStatements(true);
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        dataSource.setFilters("slf4j,stat");
        dataSource.init();
        return dataSource;
    }
}
