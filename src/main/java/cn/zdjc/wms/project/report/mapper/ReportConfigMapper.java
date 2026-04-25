package cn.zdjc.wms.project.report.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import cn.zdjc.wms.project.report.model.entity.ReportConfigEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReportConfigMapper extends BaseMapper<ReportConfigEntity> {
    ReportConfigEntity selectByCode(String reportCode);
    int checkCodeExists(String reportCode);
}
