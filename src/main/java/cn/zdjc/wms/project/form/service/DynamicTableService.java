package cn.zdjc.wms.project.form.service;

import cn.zdjc.wms.project.form.model.entity.FormConfigEntity;

public interface DynamicTableService {
    boolean createDynamicTable(FormConfigEntity formConfig);
    boolean checkTableExists(String tableName, String dbCode);
    boolean dropDynamicTable(String tableName, String dbCode);
    boolean migrateFormData(FormConfigEntity oldConfig, FormConfigEntity newConfig);
}
