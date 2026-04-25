package cn.zdjc.wms.project.mapper.requisition;

import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemExtDto;
import cn.zdjc.wms.project.domain.entity.requisition.RequisitionItemExtEntity;
import cn.zdjc.wms.project.domain.query.requisition.RequisitionItemExtExQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RequisitionItemExtMapper extends BaseMapper<RequisitionItemExtEntity> {


    List<RequisitionItemExtDto> selectWithAsnHeader(@Param("param") RequisitionItemExtExQuery param);
}
