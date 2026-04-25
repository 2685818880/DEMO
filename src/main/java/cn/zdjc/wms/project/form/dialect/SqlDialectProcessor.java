package cn.zdjc.wms.project.form.dialect;

import cn.zdjc.wms.project.form.constant.DatabaseType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.Map;

@Component
public class SqlDialectProcessor {

    public String getPaginationSql(String originalSql, DatabaseType dbType, long offset, long pageSize) {
        if (!StringUtils.hasText(originalSql)) {
            throw new IllegalArgumentException("原始SQL不能为空");
        }
        switch (dbType) {
            case MYSQL:
                return originalSql + " LIMIT " + offset + ", " + pageSize;
            case SQLSERVER:
                return "SELECT * FROM (" +
                       "    SELECT ROW_NUMBER() OVER (ORDER BY (SELECT 1)) AS row_num, * FROM (" + originalSql + ") t1" +
                       ") t2 WHERE t2.row_num BETWEEN " + (offset + 1) + " AND " + (offset + pageSize);
            case ORACLE:
                return "SELECT * FROM (" +
                       "    SELECT t.*, ROWNUM rn FROM (" + originalSql + ") t WHERE ROWNUM <= " + (offset + pageSize) +
                       ") WHERE rn > " + offset;
            default:
                throw new UnsupportedOperationException("不支持的数据库类型: " + dbType);
        }
    }

    public String getCreateTableSql(String tableName, DatabaseType dbType, Map<String, String> columnDefinitions) {
        StringBuilder sql = new StringBuilder("CREATE TABLE " + tableName + " (\n");
        switch (dbType) {
            case MYSQL:
                sql.append("    id BIGINT AUTO_INCREMENT PRIMARY KEY");
                break;
            case SQLSERVER:
                sql.append("    id BIGINT IDENTITY(1,1) PRIMARY KEY");
                break;
            case ORACLE:
                sql.append("    id NUMBER(19) PRIMARY KEY");
                break;
            default:
                sql.append("    id BIGINT PRIMARY KEY");
        }
        for (Map.Entry<String, String> entry : columnDefinitions.entrySet()) {
            sql.append(",\n    ").append(entry.getKey()).append(" ").append(entry.getValue());
        }
        sql.append("\n)");
        switch (dbType) {
            case MYSQL:
                sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
                break;
            case ORACLE:
                sql.append("\nCREATE SEQUENCE " + tableName + "_seq START WITH 1 INCREMENT BY 1");
                break;
        }
        return sql.toString();
    }

    public String mapColumnType(String javaType, DatabaseType dbType) {
        Map<String, String> mysqlMap = Map.of(
            "String", "VARCHAR(255)", "Integer", "INT", "Long", "BIGINT",
            "Double", "DOUBLE", "Boolean", "TINYINT(1)", "Date", "DATE"
        );
        Map<String, String> sqlServerMap = Map.of(
            "String", "NVARCHAR(255)", "Integer", "INT", "Long", "BIGINT",
            "Double", "FLOAT", "Boolean", "BIT", "Date", "DATE"
        );
        Map<String, String> oracleMap = Map.of(
            "String", "VARCHAR2(255 CHAR)", "Integer", "NUMBER(10)", "Long", "NUMBER(19)",
            "Double", "NUMBER(19,4)", "Boolean", "NUMBER(1)", "Date", "DATE"
        );
        switch (dbType) {
            case MYSQL: return mysqlMap.getOrDefault(javaType, "VARCHAR(255)");
            case SQLSERVER: return sqlServerMap.getOrDefault(javaType, "NVARCHAR(255)");
            case ORACLE: return oracleMap.getOrDefault(javaType, "VARCHAR2(255 CHAR)");
            default: return "VARCHAR(255)";
        }
    }

    public String getValidationQuery(DatabaseType dbType) {
        switch (dbType) {
            case MYSQL: return "SELECT 1";
            case SQLSERVER: return "SELECT 1";
            case ORACLE: return "SELECT 1 FROM DUAL";
            default: return "SELECT 1";
        }
    }
}
