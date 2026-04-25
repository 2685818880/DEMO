package cn.zdjc.wms.project.report.service;

import cn.zdjc.wms.project.report.model.entity.ReportConfigEntity;
import java.util.List;
import java.util.Optional;

public interface ReportConfigService {
    ReportConfigEntity saveReportConfig(ReportConfigEntity entity);
    Optional<ReportConfigEntity> getReportConfigByCode(String reportCode);
    List<ReportConfigEntity> listReportConfigs();
    boolean updateReportConfigStatus(String reportCode, Integer status);
    boolean deleteReportConfig(String reportCode);
    boolean validateReportConfigJson(String configJson);
}
