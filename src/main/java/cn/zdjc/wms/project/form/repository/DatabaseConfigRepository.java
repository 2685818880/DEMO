package cn.zdjc.wms.project.form.repository;

import cn.zdjc.wms.project.form.mapper.DatabaseConfigMapper;
import cn.zdjc.wms.project.form.model.entity.DatabaseConfigEntity;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import java.util.Optional;

@Repository
public class DatabaseConfigRepository {

    @Autowired
    private DatabaseConfigMapper databaseConfigMapper;

    public DatabaseConfigEntity save(DatabaseConfigEntity entity) {
        if (entity.getId() == null) {
            databaseConfigMapper.insert(entity);
        } else {
            databaseConfigMapper.updateById(entity);
        }
        return entity;
    }

    public Optional<DatabaseConfigEntity> findById(Long id) {
        return Optional.ofNullable(databaseConfigMapper.selectById(id));
    }

    public Optional<DatabaseConfigEntity> findByCode(String dbCode) {
        return Optional.ofNullable(databaseConfigMapper.selectByCode(dbCode));
    }

    public boolean existsByCode(String dbCode) {
        return databaseConfigMapper.checkCodeExists(dbCode) > 0;
    }

    public List<DatabaseConfigEntity> findAllEnabled() {
        return databaseConfigMapper.selectAllEnabled();
    }

    public int deleteById(Long id) {
        return databaseConfigMapper.deleteById(id);
    }
}
