package cn.zdjc.wms.project.report.service;

import cn.zdjc.wms.project.report.dto.ReportQueryResult;

import java.util.List;
import java.util.Map;

public interface ReportExecutionService {
    ReportQueryResult executeReport(String reportCode, Map<String, Object> params, int page, int pageSize);
    ReportQueryResult executeReport(String reportCode, Map<String, Object> params);
    List<Map<String, Object>> executeSql(String sql, Map<String, Object> params, String dbCode);
    List<Map<String, String>> analyzeSql(String sql, String dbCode);
}
