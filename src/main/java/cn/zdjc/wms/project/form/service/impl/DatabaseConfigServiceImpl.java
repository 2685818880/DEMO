package cn.zdjc.wms.project.form.service.impl;

import cn.zdjc.wms.project.form.constant.DatabaseType;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import cn.zdjc.wms.project.form.repository.DatabaseConfigRepository;
import cn.zdjc.wms.project.form.service.DatabaseConfigService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class DatabaseConfigServiceImpl implements DatabaseConfigService {

    @Autowired
    private DatabaseConfigRepository databaseConfigRepository;

    @Override
    public DatabaseConfigEntity saveDatabaseConfig(DatabaseConfigEntity entity) {
        if (!StringUtils.hasText(entity.getDbCode())) {
            throw new IllegalArgumentException("数据库编码不能为空");
        }
        if (!StringUtils.hasText(entity.getDbName())) {
            throw new IllegalArgumentException("数据库名称不能为空");
        }
        if (!StringUtils.hasText(entity.getDbType())) {
            throw new IllegalArgumentException("数据库类型不能为空");
        }
        try {
            DatabaseType.valueOf(entity.getDbType().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的数据库类型: " + entity.getDbType());
        }
        if (!StringUtils.hasText(entity.getJdbcUrl())) {
            throw new IllegalArgumentException("JDBC连接URL不能为空");
        }
        if (databaseConfigRepository.existsByCode(entity.getDbCode())) {
            throw new IllegalArgumentException("数据库编码已存在: " + entity.getDbCode());
        }
        return databaseConfigRepository.save(entity);
    }

    @Override
    public Optional<DatabaseConfigEntity> getDatabaseConfigById(Long id) {
        return databaseConfigRepository.findById(id);
    }

    @Override
    public Optional<DatabaseConfigEntity> getDatabaseConfigByCode(String dbCode) {
        return databaseConfigRepository.findByCode(dbCode);
    }

    @Override
    public List<DatabaseConfigEntity> getAllEnabledConfigs() {
        return databaseConfigRepository.findAllEnabled();
    }

    @Override
    public boolean testDatabaseConnection(DatabaseConfigEntity config) {
        Connection connection = null;
        try {
            Class.forName(config.getDriverClass());
            connection = DriverManager.getConnection(
                config.getJdbcUrl(),
                config.getUsername(),
                config.getPassword()
            );
            if (StringUtils.hasText(config.getValidationQuery())) {
                try (var statement = connection.createStatement();
                     var resultSet = statement.executeQuery(config.getValidationQuery())) {
                    return resultSet.next();
                }
            }
            return connection.isValid(5);
        } catch (ClassNotFoundException | SQLException e) {
            return false;
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // 忽略关闭异常
                }
            }
        }
    }

    @Override
    public boolean updateDatabaseConfigStatus(String dbCode, Integer status) {
        Optional<DatabaseConfigEntity> optional = databaseConfigRepository.findByCode(dbCode);
        if (optional.isPresent()) {
            DatabaseConfigEntity entity = optional.get();
            entity.setEnabled(status);
            databaseConfigRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteDatabaseConfig(String dbCode) {
        Optional<DatabaseConfigEntity> optional = databaseConfigRepository.findByCode(dbCode);
        if (optional.isPresent()) {
            DatabaseConfigEntity entity = optional.get();
            databaseConfigRepository.deleteById(entity.getId());
            return true;
        }
        return false;
    }
}
