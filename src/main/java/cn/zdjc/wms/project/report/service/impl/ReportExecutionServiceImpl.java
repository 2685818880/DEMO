package cn.zdjc.wms.project.report.service.impl;

import cn.zdjc.wms.project.form.constant.DatabaseType;
import cn.zdjc.wms.project.form.dialect.SqlDialectProcessor;
import cn.zdjc.wms.project.report.dto.ReportQueryResult;
import cn.zdjc.wms.project.report.model.entity.ReportConfigEntity;
import cn.zdjc.wms.project.report.repository.ReportConfigRepository;
import cn.zdjc.wms.project.report.service.ReportExecutionService;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportExecutionServiceImpl implements ReportExecutionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportExecutionServiceImpl.class);
    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile("(?<!'):(\\w+)(?!')");

    @Autowired
    private ReportConfigRepository reportConfigRepository;

    @Autowired
    private SqlDialectProcessor sqlDialectProcessor;

    private DataSource defaultDataSource;

    @Autowired
    public void setDefaultDataSource(DataSource defaultDataSource) {
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    public ReportQueryResult executeReport(String reportCode, Map<String, Object> params, int page, int pageSize) {
        long startTime = System.currentTimeMillis();

        ReportConfigEntity config = reportConfigRepository.findByCode(reportCode)
            .orElseThrow(() -> new IllegalArgumentException("报表未找到: " + reportCode));

        JSONObject configJson = JSONObject.parseObject(config.getConfigJson());
        String sqlTemplate = configJson.getString("sql");

        if (sqlTemplate == null || sqlTemplate.trim().isEmpty()) {
            throw new IllegalArgumentException("报表SQL配置为空");
        }

        String trimmedSql = sqlTemplate.trim().toUpperCase();
        if (!trimmedSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("只支持SELECT查询语句");
        }

        MapSqlParameterSource paramSource = buildParamSource(sqlTemplate, params);
        DataSource ds = resolveDataSource(config.getDbCode());
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(ds);

        String countSql = "SELECT COUNT(*) FROM (" + sqlTemplate + ") t";
        int total;
        try {
            total = jdbc.queryForObject(countSql, paramSource, Integer.class);
        } catch (Exception e) {
            LOGGER.warn("COUNT查询失败，使用分页不限制: {}", e.getMessage());
            total = 0;
        }

        DatabaseType dbType = resolveDbType(ds);
        String paginatedSql = sqlDialectProcessor.getPaginationSql(sqlTemplate, dbType,
            (long) (page - 1) * pageSize, (long) pageSize);

        List<Map<String, Object>> rows;
        try {
            rows = jdbc.queryForList(paginatedSql, paramSource);
        } catch (Exception e) {
            LOGGER.error("报表查询执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("报表查询执行失败: " + e.getMessage(), e);
        }

        ReportQueryResult result = new ReportQueryResult();
        result.setRows(rows);
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setExecutionTimeMs(System.currentTimeMillis() - startTime);
        return result;
    }

    @Override
    public ReportQueryResult executeReport(String reportCode, Map<String, Object> params) {
        return executeReport(reportCode, params, 1, 10000);
    }

    @Override
    public List<Map<String, Object>> executeSql(String sql, Map<String, Object> params, String dbCode) {
        MapSqlParameterSource paramSource = buildParamSource(sql, params);
        DataSource ds = resolveDataSource(dbCode);
        NamedParameterJdbcTemplate jdbc = new NamedParameterJdbcTemplate(ds);
        return jdbc.queryForList(sql, paramSource);
    }

    @Override
    public List<Map<String, String>> analyzeSql(String sql, String dbCode) {
        if (sql == null || sql.trim().isEmpty()) {
            throw new IllegalArgumentException("SQL不能为空");
        }

        DataSource ds = resolveDataSource(dbCode);
        String analyzeSql = "SELECT * FROM (" + sql + ") t WHERE 1=0";

        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(analyzeSql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            List<Map<String, String>> columns = new ArrayList<>();

            for (int i = 1; i <= columnCount; i++) {
                Map<String, String> col = new HashMap<>();
                col.put("columnName", metaData.getColumnLabel(i));
                col.put("type", metaData.getColumnTypeName(i));
                columns.add(col);
            }
            return columns;
        } catch (Exception e) {
            LOGGER.error("SQL分析失败: {}", e.getMessage(), e);
            throw new RuntimeException("SQL分析失败: " + e.getMessage(), e);
        }
    }

    private MapSqlParameterSource buildParamSource(String sqlTemplate, Map<String, Object> params) {
        MapSqlParameterSource paramSource = new MapSqlParameterSource();
        Set<String> paramNames = extractParamNames(sqlTemplate);

        if (params != null) {
            for (String paramName : paramNames) {
                Object value = params.get(paramName);
                // 空字符串转为 null，兼容前端空输入与 SQL 中 IS NULL 判断
                if (value instanceof String && ((String) value).isEmpty()) {
                    value = null;
                }
                paramSource.addValue(paramName, value);
            }
        }
        return paramSource;
    }

    private Set<String> extractParamNames(String sql) {
        Set<String> paramNames = new LinkedHashSet<>();
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }
        return paramNames;
    }

    private DataSource resolveDataSource(String dbCode) {
        if (dbCode != null && !dbCode.isEmpty()) {
            try {
                Object ds = org.springframework.context.ApplicationContext.class
                    .getMethod("getBean", String.class)
                    .invoke(null, "dataSource_" + dbCode);
                if (ds instanceof DataSource) {
                    return (DataSource) ds;
                }
            } catch (Exception e) {
                LOGGER.warn("无法获取数据源 {}，使用默认数据源", dbCode);
            }
        }
        return defaultDataSource;
    }

    private DatabaseType resolveDbType(DataSource ds) {
        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String url = meta.getURL();
            if (url.contains("oracle")) return DatabaseType.ORACLE;
            if (url.contains("sqlserver")) return DatabaseType.SQLSERVER;
            return DatabaseType.MYSQL;
        } catch (Exception e) {
            return DatabaseType.MYSQL;
        }
    }
}
