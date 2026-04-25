package cn.zdjc.wms.project.service.palletize.impl;

import cn.zdjc.wms.project.domain.entity.palletize.PalletizeItemExtEntity;
import cn.zdjc.wms.project.domain.query.palletize.PalletizeItemExtQuery;
import cn.zdjc.wms.project.mapper.palletize.PalletizeItemExtMapper;
import cn.zdjc.wms.project.repository.palletize.PalletizeItemExtRepository;
import cn.zdjc.wms.project.service.palletize.PalletizeItemExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 组盘明细扩展 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PalletizeItemExtServiceImpl extends ServiceImpl<PalletizeItemExtMapper, PalletizeItemExtEntity>
        implements PalletizeItemExtService {
    @Resource
    private PalletizeItemExtRepository palletizeItemExtRepository;

    @Override
    public List<PalletizeItemExtEntity> queryByPalletizeItemAndForm(PalletizeItemExtQuery query) {
        return palletizeItemExtRepository.queryByPalletizeItemAndForm(query);
    }
}