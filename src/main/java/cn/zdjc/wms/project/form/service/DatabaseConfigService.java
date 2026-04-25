package cn.zdjc.wms.project.form.service;

import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import java.util.List;
import java.util.Optional;

public interface DatabaseConfigService {
    DatabaseConfigEntity saveDatabaseConfig(DatabaseConfigEntity entity);
    Optional<DatabaseConfigEntity> getDatabaseConfigById(Long id);
    Optional<DatabaseConfigEntity> getDatabaseConfigByCode(String dbCode);
    List<DatabaseConfigEntity> getAllEnabledConfigs();
    boolean testDatabaseConnection(DatabaseConfigEntity config);
    boolean updateDatabaseConfigStatus(String dbCode, Integer status);
    boolean deleteDatabaseConfig(String dbCode);
}
