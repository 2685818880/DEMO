package cn.zdjc.wms.project.report.controller;

import cn.zdjc.wms.project.report.model.entity.ReportConfigEntity;
import cn.zdjc.wms.project.report.service.ReportConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/report/config")
@Api(tags = "报表配置管理")
public class ReportConfigController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportConfigController.class);

    @Autowired
    private ReportConfigService reportConfigService;

    @PostMapping("/save")
    @ApiOperation("保存报表配置")
    public ResponseEntity<?> saveReportConfig(@RequestBody ReportConfigEntity entity) {
        try {
            ReportConfigEntity saved = reportConfigService.saveReportConfig(entity);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            LOGGER.error("保存报表配置失败, reportCode={}, reportName={}: {}", entity.getReportCode(), entity.getReportName(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Collections.singletonMap("message", e.getMessage() != null ? e.getMessage() : "未知错误"));
        }
    }

    @GetMapping("/list")
    @ApiOperation("查询报表配置列表")
    public ResponseEntity<List<ReportConfigEntity>> listReportConfigs() {
        List<ReportConfigEntity> list = reportConfigService.listReportConfigs();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{reportCode}")
    @ApiOperation("获取报表配置详情")
    public ResponseEntity<ReportConfigEntity> getReportConfig(@PathVariable String reportCode) {
        Optional<ReportConfigEntity> config = reportConfigService.getReportConfigByCode(reportCode);
        return config.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{reportCode}/status/{status}")
    @ApiOperation("启用/禁用报表配置")
    public ResponseEntity<Boolean> updateReportConfigStatus(
            @PathVariable String reportCode,
            @PathVariable Integer status) {
        boolean success = reportConfigService.updateReportConfigStatus(reportCode, status);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{reportCode}")
    @ApiOperation("删除报表配置")
    public ResponseEntity<Boolean> deleteReportConfig(@PathVariable String reportCode) {
        boolean success = reportConfigService.deleteReportConfig(reportCode);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @PostMapping("/validate")
    @ApiOperation("验证报表JSON配置")
    public ResponseEntity<Boolean> validateReportJson(@RequestBody String configJson) {
        boolean valid = reportConfigService.validateReportConfigJson(configJson);
        return ResponseEntity.ok(valid);
    }
}
