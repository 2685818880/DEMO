package cn.zdjc.wms.project.service.sku;

import cn.zdjc.wms.project.domain.dto.sku.SkuExtDto;
import cn.zdjc.wms.project.domain.entity.sku.SkuExtEntity;
import cn.zdjc.wms.project.domain.query.sku.SkuExtQuery;
import com.baomidou.mybatisplus.extension.service.IService;
import com.foeris.y.common.result.PageResult;

import java.util.List;

/**
 * SKU 扩展信息服务接口
 */
public interface SkuExtService extends IService<SkuExtEntity> {

    /**
     * 根据查询条件获取 SKU 扩展信息列表
     */
    List<SkuExtDto> queryList(SkuExtQuery query);

    /**
     * 导出 SKU 扩展数据（分页）
     */
    PageResult<SkuExtDto> export(SkuExtQuery query);

    /**
     * 根据 SKU 编码查询单条 SKU 扩展信息
     */
    SkuExtDto queryBySkuCode(String skuCode);
}