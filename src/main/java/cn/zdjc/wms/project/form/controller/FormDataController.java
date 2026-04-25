package cn.zdjc.wms.project.form.controller;

import cn.zdjc.wms.project.form.annotation.FormDataSource;
import cn.zdjc.wms.project.form.model.entity.FormDataEntity;
import cn.zdjc.wms.project.form.service.FormDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/form/data")
@Api(tags = "表单数据操作")
public class FormDataController {

    @Autowired
    private FormDataService formDataService;

    @PostMapping("/submit")
    @ApiOperation("提交表单数据")
    @FormDataSource("formCode")
    public ResponseEntity<FormDataEntity> submitFormData(@RequestBody FormDataEntity entity) {
        FormDataEntity saved = formDataService.submitFormData(entity);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/list/{formCode}")
    @ApiOperation("查询表单数据列表")
    @FormDataSource("formCode")
    public ResponseEntity<List<FormDataEntity>> listFormData(@PathVariable String formCode) {
        List<FormDataEntity> list = formDataService.listFormData(formCode);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @ApiOperation("获取表单数据详情")
    public ResponseEntity<FormDataEntity> getFormData(@PathVariable Long id) {
        Optional<FormDataEntity> data = formDataService.getFormDataById(id);
        return data.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/status")
    @ApiOperation("更新表单数据状态")
    public ResponseEntity<Boolean> updateFormDataStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        boolean success = formDataService.updateFormDataStatus(id, status);
        return success ? ResponseEntity.ok(true) : ResponseEntity.notFound().build();
    }

    @PostMapping("/validate/{formCode}")
    @ApiOperation("验证表单数据")
    @FormDataSource("formCode")
    public ResponseEntity<Boolean> validateFormData(
            @PathVariable String formCode,
            @RequestBody String formDataJson) {
        boolean valid = formDataService.validateFormData(formCode, formDataJson);
        return ResponseEntity.ok(valid);
    }
}
