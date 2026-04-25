package cn.zdjc.wms.project.form.config;

import org.springframework.util.StringUtils;

public class DataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void setDataSourceKey(String dataSourceKey) {
        CONTEXT_HOLDER.set(dataSourceKey);
    }

    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    public static String getDataSourceKeyOrDefault() {
        String key = CONTEXT_HOLDER.get();
        return StringUtils.hasText(key) ? key : "default";
    }

    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }
}
