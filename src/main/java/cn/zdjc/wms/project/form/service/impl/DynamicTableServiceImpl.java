package cn.zdjc.wms.project.form.service.impl;

import cn.zdjc.wms.project.form.constant.DatabaseType;
import cn.zdjc.wms.project.form.dialect.SqlDialectProcessor;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import cn.zdjc.wms.project.form.service.DynamicTableService;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DynamicTableServiceImpl implements DynamicTableService {

    @Autowired
    private SqlDialectProcessor sqlDialectProcessor;

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public boolean createDynamicTable(FormConfigEntity formConfig) {
        if (formConfig == null || !StringUtils.hasText(formConfig.getCode())) {
            throw new IllegalArgumentException("表单配置无效");
        }
        String tableName = getTableName(formConfig);
        if (checkTableExists(tableName, formConfig.getDbCode())) {
            return true;
        }
        Map<String, String> columnDefinitions = parseFormFields(formConfig);
        DatabaseType dbType = getDatabaseType(formConfig.getDbCode());
        String createTableSql = sqlDialectProcessor.getCreateTableSql(tableName, dbType, columnDefinitions);
        try {
            jdbcTemplate.execute(createTableSql);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("创建动态表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean checkTableExists(String tableName, String dbCode) {
        if (!StringUtils.hasText(tableName) || !StringUtils.hasText(dbCode)) {
            return false;
        }
        try {
            Optional<DatabaseConfigEntity> config = databaseConfigService.getDatabaseConfigByCode(dbCode);
            if (config.isEmpty()) {
                return false;
            }
            DatabaseType dbType = DatabaseType.valueOf(config.get().getDbType().toUpperCase());
            String checkSql;
            switch (dbType) {
                case MYSQL:
                    checkSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = ?";
                    break;
                case SQLSERVER:
                    checkSql = "SELECT COUNT(*) FROM sys.tables WHERE name = ?";
                    break;
                case ORACLE:
                    checkSql = "SELECT COUNT(*) FROM user_tables WHERE table_name = ?";
                    break;
                default:
                    return false;
            }
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, tableName.toUpperCase());
            return count != null && count > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean dropDynamicTable(String tableName, String dbCode) {
        if (!StringUtils.hasText(tableName) || !StringUtils.hasText(dbCode)) {
            return false;
        }
        try {
            String dropSql = "DROP TABLE IF EXISTS " + tableName;
            jdbcTemplate.execute(dropSql);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("删除动态表失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean migrateFormData(FormConfigEntity oldConfig, FormConfigEntity newConfig) {
        throw new UnsupportedOperationException("数据迁移功能尚未实现");
    }

    private String getTableName(FormConfigEntity formConfig) {
        if (StringUtils.hasText(formConfig.getTableName())) {
            return formConfig.getTableName();
        }
        return "form_data_" + formConfig.getCode().toLowerCase();
    }

    private Map<String, String> parseFormFields(FormConfigEntity formConfig) {
        Map<String, String> columnDefinitions = new HashMap<>();
        try {
            JSONObject configJson = JSON.parseObject(formConfig.getConfigJson());
            JSONArray fields = configJson.getJSONArray("fields");
            if (fields != null) {
                for (int i = 0; i < fields.size(); i++) {
                    JSONObject field = fields.getJSONObject(i);
                    String fieldName = field.getString("name");
                    String fieldType = field.getString("type");
                    if (StringUtils.hasText(fieldName) && StringUtils.hasText(fieldType)) {
                        DatabaseType dbType = getDatabaseType(formConfig.getDbCode());
                        String dbTypeStr = sqlDialectProcessor.mapColumnType(fieldType, dbType);
                        columnDefinitions.put(fieldName, dbTypeStr);
                    }
                }
            }
            columnDefinitions.put("form_code", "VARCHAR(50)");
            columnDefinitions.put("business_key", "VARCHAR(100)");
            columnDefinitions.put("business_type", "VARCHAR(50)");
            columnDefinitions.put("data_status", "VARCHAR(20)");
            columnDefinitions.put("created_by", "VARCHAR(50)");
            columnDefinitions.put("created_time", "DATETIME");
        } catch (Exception e) {
            throw new IllegalArgumentException("解析表单字段配置失败: " + e.getMessage(), e);
        }
        return columnDefinitions;
    }

    private DatabaseType getDatabaseType(String dbCode) {
        if (StringUtils.hasText(dbCode)) {
            Optional<DatabaseConfigEntity> config = databaseConfigService.getDatabaseConfigByCode(dbCode);
            if (config.isPresent()) {
                try {
                    return DatabaseType.valueOf(config.get().getDbType().toUpperCase());
                } catch (IllegalArgumentException e) {
                    // fall through to default
                }
            }
        }
        return DatabaseType.MYSQL;
    }
}
