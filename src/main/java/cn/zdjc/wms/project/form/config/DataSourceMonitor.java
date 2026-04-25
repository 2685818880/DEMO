package cn.zdjc.wms.project.form.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataSourceMonitor {

    @Autowired
    private DynamicDataSource dynamicDataSource;

    private final Map<String, DataSourceHealth> healthStatus = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 30000)
    public void monitorDataSources() {
        Map<Object, DataSource> targetDataSources = dynamicDataSource.getResolvedDataSourcesMap();
        if (targetDataSources != null) {
            for (Map.Entry<Object, DataSource> entry : targetDataSources.entrySet()) {
                String dataSourceKey = entry.getKey().toString();
                DataSource dataSource = entry.getValue();
                boolean healthy = checkDataSourceHealth(dataSource);
                DataSourceHealth health = healthStatus.computeIfAbsent(
                    dataSourceKey, k -> new DataSourceHealth()
                );
                health.setHealthy(healthy);
                health.setLastCheckTime(System.currentTimeMillis());
                if (dataSource instanceof DruidDataSource) {
                    DruidDataSource druidDataSource = (DruidDataSource) dataSource;
                    health.setActiveCount(druidDataSource.getActiveCount());
                    health.setPoolingCount(druidDataSource.getPoolingCount());
                    health.setWaitThreadCount(druidDataSource.getWaitThreadCount());
                }
            }
        }
    }

    private boolean checkDataSourceHealth(DataSource dataSource) {
        if (dataSource instanceof DruidDataSource) {
            try {
                DruidDataSource druidDataSource = (DruidDataSource) dataSource;
                return druidDataSource.isEnable();
            } catch (Exception e) {
                return false;
            }
        }
        try (var connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    public Map<String, DataSourceHealth> getHealthStatus() {
        return new ConcurrentHashMap<>(healthStatus);
    }

    public DataSourceHealth getDataSourceHealth(String dataSourceKey) {
        return healthStatus.get(dataSourceKey);
    }

    public static class DataSourceHealth {
        private boolean healthy;
        private long lastCheckTime;
        private int activeCount;
        private int poolingCount;
        private int waitThreadCount;

        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        public long getLastCheckTime() { return lastCheckTime; }
        public void setLastCheckTime(long lastCheckTime) { this.lastCheckTime = lastCheckTime; }
        public int getActiveCount() { return activeCount; }
        public void setActiveCount(int activeCount) { this.activeCount = activeCount; }
        public int getPoolingCount() { return poolingCount; }
        public void setPoolingCount(int poolingCount) { this.poolingCount = poolingCount; }
        public int getWaitThreadCount() { return waitThreadCount; }
        public void setWaitThreadCount(int waitThreadCount) { this.waitThreadCount = waitThreadCount; }
    }
}
