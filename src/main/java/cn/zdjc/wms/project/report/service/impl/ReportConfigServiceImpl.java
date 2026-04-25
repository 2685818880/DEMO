package cn.zdjc.wms.project.report.service.impl;

import cn.zdjc.wms.project.report.mapper.ReportConfigMapper;
import cn.zdjc.wms.project.report.model.entity.ReportConfigEntity;
import cn.zdjc.wms.project.report.repository.ReportConfigRepository;
import cn.zdjc.wms.project.report.service.ReportConfigService;
import com.alibaba.fastjson2.JSONValidator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.Optional;

@Service
public class ReportConfigServiceImpl implements ReportConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportConfigServiceImpl.class);

    @Autowired
    private ReportConfigRepository reportConfigRepository;

    @Autowired
    private ReportConfigMapper reportConfigMapper;

    @Override
    public ReportConfigEntity saveReportConfig(ReportConfigEntity entity) {
        if (!StringUtils.hasText(entity.getReportCode())) {
            throw new IllegalArgumentException("报表编码不能为空");
        }
        if (!StringUtils.hasText(entity.getReportName())) {
            throw new IllegalArgumentException("报表名称不能为空");
        }
        if (!validateReportConfigJson(entity.getConfigJson())) {
            throw new IllegalArgumentException("报表JSON配置格式无效");
        }
        // 编码已存在则走更新
        Optional<ReportConfigEntity> existing = reportConfigRepository.findByCode(entity.getReportCode());
        if (existing.isPresent()) {
            entity.setId(existing.get().getId());
        }
        return reportConfigRepository.save(entity);
    }

    @Override
    public Optional<ReportConfigEntity> getReportConfigByCode(String reportCode) {
        return reportConfigRepository.findByCode(reportCode);
    }

    @Override
    public List<ReportConfigEntity> listReportConfigs() {
        QueryWrapper<ReportConfigEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("created_time");
        return reportConfigMapper.selectList(queryWrapper);
    }

    @Override
    public boolean updateReportConfigStatus(String reportCode, Integer status) {
        Optional<ReportConfigEntity> optional = reportConfigRepository.findByCode(reportCode);
        if (optional.isPresent()) {
            ReportConfigEntity entity = optional.get();
            entity.setStatus(status);
            reportConfigRepository.save(entity);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteReportConfig(String reportCode) {
        Optional<ReportConfigEntity> optional = reportConfigRepository.findByCode(reportCode);
        if (optional.isPresent()) {
            reportConfigRepository.deleteById(optional.get().getId());
            return true;
        }
        return false;
    }

    @Override
    public boolean validateReportConfigJson(String configJson) {
        if (!StringUtils.hasText(configJson)) {
            return false;
        }
        try {
            JSONValidator validator = JSONValidator.from(configJson);
            return validator.validate();
        } catch (Exception e) {
            return false;
        }
    }
}
