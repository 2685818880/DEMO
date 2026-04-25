package cn.zdjc.wms.project.form.controller;

import cn.zdjc.wms.project.form.config.DataSourceMonitor;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/database/config")
@Api(tags = "数据库配置管理")
public class DatabaseConfigController {

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Autowired
    private DataSourceMonitor dataSourceMonitor;

    @PostMapping("/save")
    @ApiOperation("保存数据库配置")
    public ResponseEntity<DatabaseConfigEntity> saveDatabaseConfig(@RequestBody DatabaseConfigEntity entity) {
        DatabaseConfigEntity saved = databaseConfigService.saveDatabaseConfig(entity);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/list")
    @ApiOperation("查询数据库配置列表")
    public ResponseEntity<List<DatabaseConfigEntity>> listDatabaseConfigs() {
        List<DatabaseConfigEntity> list = databaseConfigService.getAllEnabledConfigs();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{dbCode}")
    @ApiOperation("获取数据库配置详情")
    public ResponseEntity<DatabaseConfigEntity> getDatabaseConfig(@PathVariable String dbCode) {
        Optional<DatabaseConfigEntity> config = databaseConfigService.getDatabaseConfigByCode(dbCode);
        return config.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{dbCode}/test")
    @ApiOperation("测试数据库连接")
    public ResponseEntity<Boolean> testDatabaseConnection(@PathVariable String dbCode) {
        Optional<DatabaseConfigEntity> config = databaseConfigService.getDatabaseConfigByCode(dbCode);
        if (config.isPresent()) {
            boolean success = databaseConfigService.testDatabaseConnection(config.get());
            return ResponseEntity.ok(success);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{dbCode}/status/{status}")
    @ApiOperation("启用/禁用数据库配置")
    public ResponseEntity<Boolean> updateDatabaseConfigStatus(
            @PathVariable String dbCode,
            @PathVariable Integer status) {
        boolean success = databaseConfigService.updateDatabaseConfigStatus(dbCode, status);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{dbCode}")
    @ApiOperation("删除数据库配置")
    public ResponseEntity<Boolean> deleteDatabaseConfig(@PathVariable String dbCode) {
        boolean success = databaseConfigService.deleteDatabaseConfig(dbCode);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @GetMapping("/monitor/health")
    @ApiOperation("获取数据源健康状态")
    public ResponseEntity<Map<String, DataSourceMonitor.DataSourceHealth>> getDataSourceHealth() {
        Map<String, DataSourceMonitor.DataSourceHealth> healthStatus = dataSourceMonitor.getHealthStatus();
        return ResponseEntity.ok(healthStatus);
    }

    @GetMapping("/monitor/statistics")
    @ApiOperation("获取数据源统计信息")
    public ResponseEntity<Map<String, Object>> getDataSourceStatistics() {
        Map<String, DataSourceMonitor.DataSourceHealth> healthStatus = dataSourceMonitor.getHealthStatus();
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalDataSources", healthStatus.size());
        long healthyCount = healthStatus.values().stream()
            .filter(DataSourceMonitor.DataSourceHealth::isHealthy)
            .count();
        statistics.put("healthyDataSources", healthyCount);
        int totalActiveConnections = healthStatus.values().stream()
            .mapToInt(DataSourceMonitor.DataSourceHealth::getActiveCount)
            .sum();
        statistics.put("totalActiveConnections", totalActiveConnections);
        return ResponseEntity.ok(statistics);
    }
}
