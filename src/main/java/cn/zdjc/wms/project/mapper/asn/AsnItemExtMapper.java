package cn.zdjc.wms.project.mapper.asn;

import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.entity.asn.AsnItemExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AsnItemExtMapper extends BaseMapper<AsnItemExtEntity> {

    /**
     * 关联 asn 主表 + asn_item_ext 明细表 查询
     */
    List<AsnItemExtDto> selectWithAsnHeader(@Param("param") AsnItemExtExQuery param);
}
