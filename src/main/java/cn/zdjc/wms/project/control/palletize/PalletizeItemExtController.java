package cn.zdjc.wms.project.control.palletize;

import cn.zdjc.wms.project.domain.entity.palletize.PalletizeItemExtEntity;
import cn.zdjc.wms.project.domain.query.palletize.PalletizeItemExtQuery;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.service.palletize.PalletizeItemExtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/restful/api/palletize/item")
@RequiredArgsConstructor
public class PalletizeItemExtController {

    @Resource
    private PalletizeItemExtService palletizeItemExtService;

    @PostMapping("/find-item-list")
    public ResultWrapper<List<PalletizeItemExtEntity>> queryByPalletizeItemAndForm(@RequestBody PalletizeItemExtQuery query) {
        List<PalletizeItemExtEntity> palletizeItemExtEntities = palletizeItemExtService.queryByPalletizeItemAndForm(query);
        return ResultWrapper.buildSuccess(palletizeItemExtEntities);
    }

}