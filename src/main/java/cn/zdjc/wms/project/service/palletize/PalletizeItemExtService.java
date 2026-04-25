package cn.zdjc.wms.project.service.palletize;

import cn.zdjc.wms.project.domain.dto.palletize.PalletizeItemExtDto;
import cn.zdjc.wms.project.domain.entity.palletize.PalletizeItemExtEntity;
import cn.zdjc.wms.project.domain.query.palletize.PalletizeItemExtQuery;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 组盘明细扩展 服务接口
 */
public interface PalletizeItemExtService extends IService<PalletizeItemExtEntity> {

    List<PalletizeItemExtEntity> queryByPalletizeItemAndForm(PalletizeItemExtQuery query) ;
}