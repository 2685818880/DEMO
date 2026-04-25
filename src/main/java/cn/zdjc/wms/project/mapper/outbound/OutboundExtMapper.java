package cn.zdjc.wms.project.mapper.outbound;

import cn.zdjc.wms.project.domain.dto.ws.PjOutboundOrderInfoDto;
import cn.zdjc.wms.project.domain.entity.outbound.OutboundExtEntity;
import cn.zdjc.wms.project.vo.outbound.PjOutboundOrderTaskVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OutboundExtMapper extends BaseMapper<OutboundExtEntity> {

    /**
     * 查询公共的出库拣选任务列表
     *
     * @return
     */
    List<PjOutboundOrderTaskVo> findCommonPickOutboundTaskList(@Param("dto") PjOutboundOrderInfoDto dto);
}
