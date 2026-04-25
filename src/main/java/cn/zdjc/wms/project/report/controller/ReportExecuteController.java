package cn.zdjc.wms.project.report.controller;

import cn.zdjc.wms.project.paas.excel.util.ExportExcelUtil;
import cn.zdjc.wms.project.report.dto.ReportQueryResult;
import cn.zdjc.wms.project.report.model.entity.ReportConfigEntity;
import cn.zdjc.wms.project.report.service.ReportConfigService;
import cn.zdjc.wms.project.report.service.ReportExecutionService;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.*;

@RestController
@RequestMapping("/api/report")
@Api(tags = "报表执行")
public class ReportExecuteController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportExecuteController.class);

    @Autowired
    private ReportExecutionService reportExecutionService;

    @Autowired
    private ReportConfigService reportConfigService;

    @PostMapping("/execute/{reportCode}")
    @ApiOperation("执行报表查询")
    public ResponseEntity<ReportQueryResult> executeReport(
            @PathVariable String reportCode,
            @RequestBody Map<String, Object> requestBody) {
        Map<String, Object> params = requestBody != null ?
            (Map<String, Object>) requestBody.getOrDefault("params", new HashMap<>()) : new HashMap<>();
        int page = requestBody != null && requestBody.get("page") != null ?
            ((Number) requestBody.get("page")).intValue() : 1;
        int pageSize = requestBody != null && requestBody.get("pageSize") != null ?
            ((Number) requestBody.get("pageSize")).intValue() : 20;

        ReportQueryResult result = reportExecutionService.executeReport(reportCode, params, page, pageSize);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/analyze-sql")
    @ApiOperation("分析SQL返回列信息")
    public ResponseEntity<List<Map<String, String>>> analyzeSql(
            @RequestBody Map<String, String> requestBody) {
        String sql = requestBody.get("sql");
        String dbCode = requestBody.get("dbCode");
        List<Map<String, String>> columns = reportExecutionService.analyzeSql(sql, dbCode);
        return ResponseEntity.ok(columns);
    }

    @PostMapping("/export/{reportCode}")
    @ApiOperation("导出报表到Excel")
    public void exportReport(
            @PathVariable String reportCode,
            @RequestBody Map<String, Object> requestBody,
            HttpServletResponse response) {
        try {
            Map<String, Object> params = requestBody != null ?
                (Map<String, Object>) requestBody.getOrDefault("params", new HashMap<>()) : new HashMap<>();

            ReportQueryResult queryResult = reportExecutionService.executeReport(reportCode, params, 1, 100000);

            ReportConfigEntity config = reportConfigService.getReportConfigByCode(reportCode)
                .orElseThrow(() -> new IllegalArgumentException("报表未找到: " + reportCode));

            JSONObject configJson = JSONObject.parseObject(config.getConfigJson());
            JSONArray columnsConfig = configJson.getJSONArray("columns");

            String fileName = config.getReportName();
            if (configJson.getJSONObject("export") != null) {
                String exportFileName = configJson.getJSONObject("export").getString("fileName");
                if (exportFileName != null && !exportFileName.isEmpty()) {
                    fileName = exportFileName;
                }
            }

            response.setContentType("application/octet-stream");
            response.setCharacterEncoding("utf-8");
            String encodedName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            response.setHeader("Content-Disposition", "attachment;filename=" + encodedName + ".xlsx");

            // 构建表头（List<List<String>> 格式，EasyExcel 要求）
            List<List<String>> headList = new ArrayList<>();
            List<String> props = new ArrayList<>();
            if (columnsConfig != null) {
                for (int i = 0; i < columnsConfig.size(); i++) {
                    JSONObject col = columnsConfig.getJSONObject(i);
                    if (col.getBooleanValue("hidden")) continue;
                    headList.add(Collections.singletonList(col.getString("label")));
                    props.add(col.getString("prop"));
                }
            } else {
                Map<String, Object> firstRow = queryResult.getRows().isEmpty() ?
                    new HashMap<>() : queryResult.getRows().get(0);
                for (String key : firstRow.keySet()) {
                    headList.add(Collections.singletonList(key));
                    props.add(key);
                }
            }

            // 构建数据行
            List<List<Object>> dataList = new ArrayList<>();
            for (Map<String, Object> row : queryResult.getRows()) {
                List<Object> dataRow = new ArrayList<>();
                for (String prop : props) {
                    dataRow.add(row.get(prop));
                }
                dataList.add(dataRow);
            }

            // 使用 head(headList) 指定表头，doWrite(dataList) 写入数据
            com.alibaba.excel.EasyExcel.write(response.getOutputStream())
                .excelType(com.alibaba.excel.support.ExcelTypeEnum.XLSX)
                .head(headList)
                .registerWriteHandler(new cn.zdjc.wms.project.paas.excel.style.ExcelCellWidthStyleStrategy())
                .registerWriteHandler(new com.alibaba.excel.write.style.row.SimpleRowHeightStyleStrategy((short) 40, (short) 30))
                .registerWriteHandler(new cn.zdjc.wms.project.paas.excel.style.FreezeAndFilter())
                .registerWriteHandler(new com.alibaba.excel.write.style.HorizontalCellStyleStrategy(
                    cn.zdjc.wms.project.paas.excel.style.StyleUtils.getHeadStyle(),
                    cn.zdjc.wms.project.paas.excel.style.StyleUtils.getContentStyle()))
                .useDefaultStyle(true)
                .sheet(fileName)
                .doWrite(dataList);

        } catch (Exception e) {
            LOGGER.error("导出报表失败: {}", e.getMessage(), e);
            try {
                response.setContentType("application/json;charset=utf-8");
                response.getWriter().write("{\"success\":false,\"message\":\"" +
                    e.getMessage().replace("\"", "'") + "\"}");
            } catch (Exception ignored) {
            }
        }
    }
}
