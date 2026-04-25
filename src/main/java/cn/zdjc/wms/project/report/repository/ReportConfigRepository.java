package cn.zdjc.wms.project.report.repository;

import cn.zdjc.wms.project.report.mapper.ReportConfigMapper;
import cn.zdjc.wms.project.report.model.entity.ReportConfigEntity;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Optional;

@Repository
public class ReportConfigRepository {

    @Autowired
    private ReportConfigMapper reportConfigMapper;

    public ReportConfigEntity save(ReportConfigEntity entity) {
        if (entity.getId() == null) {
            reportConfigMapper.insert(entity);
        } else {
            reportConfigMapper.updateById(entity);
        }
        return entity;
    }

    public Optional<ReportConfigEntity> findById(Long id) {
        return Optional.ofNullable(reportConfigMapper.selectById(id));
    }

    public Optional<ReportConfigEntity> findByCode(String reportCode) {
        return Optional.ofNullable(reportConfigMapper.selectByCode(reportCode));
    }

    public boolean existsByCode(String reportCode) {
        return reportConfigMapper.checkCodeExists(reportCode) > 0;
    }

    public int deleteById(Long id) {
        return reportConfigMapper.deleteById(id);
    }
}
