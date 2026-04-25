package cn.zdjc.wms.project.form.controller;

import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;
import cn.zdjc.wms.project.form.service.FormConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/form/config")
@Api(tags = "表单配置管理")
public class FormConfigController {

    @Autowired
    private FormConfigService formConfigService;

    @PostMapping("/save")
    @ApiOperation("保存表单配置")
    public ResponseEntity<FormConfigEntity> saveFormConfig(@RequestBody FormConfigEntity entity) {
        FormConfigEntity saved = formConfigService.saveFormConfig(entity);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/list")
    @ApiOperation("查询表单配置列表")
    public ResponseEntity<List<FormConfigEntity>> listFormConfigs() {
        List<FormConfigEntity> list = formConfigService.listFormConfigs();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{code}")
    @ApiOperation("获取表单配置详情")
    public ResponseEntity<FormConfigEntity> getFormConfig(@PathVariable String code) {
        Optional<FormConfigEntity> config = formConfigService.getFormConfigByCode(code);
        return config.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{code}/status/{status}")
    @ApiOperation("启用/禁用表单配置")
    public ResponseEntity<Boolean> updateFormConfigStatus(
            @PathVariable String code,
            @PathVariable Integer status) {
        boolean success = formConfigService.updateFormConfigStatus(code, status);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{code}")
    @ApiOperation("删除表单配置")
    public ResponseEntity<Boolean> deleteFormConfig(@PathVariable String code) {
        boolean success = formConfigService.deleteFormConfig(code);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @PostMapping("/validate")
    @ApiOperation("验证表单JSON配置")
    public ResponseEntity<Boolean> validateFormJson(@RequestBody String configJson) {
        boolean valid = formConfigService.validateFormJson(configJson);
        return ResponseEntity.ok(valid);
    }
}
