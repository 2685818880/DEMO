package cn.zdjc.wms.project.form.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import javax.sql.DataSource;
import java.util.Map;

public class DynamicDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceKeyOrDefault();
    }

    public static void switchDataSource(String dbCode) {
        if (org.springframework.util.StringUtils.hasText(dbCode)) {
            DataSourceContextHolder.setDataSourceKey(dbCode);
        } else {
            DataSourceContextHolder.clearDataSourceKey();
        }
    }

    public static void resetToDefault() {
        DataSourceContextHolder.clearDataSourceKey();
    }

    public Map<Object, DataSource> getResolvedDataSourcesMap() {
        return getResolvedDataSources();
    }
}
