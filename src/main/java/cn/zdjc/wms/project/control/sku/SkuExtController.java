package cn.zdjc.wms.project.control.sku;

import cn.zdjc.wms.project.domain.dto.sku.SkuExtDto;
import cn.zdjc.wms.project.domain.query.sku.SkuExtQuery;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.service.sku.SkuExtService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;


@Slf4j
@RestController
@RequestMapping("/restful/api/sku")
@RequiredArgsConstructor
public class SkuExtController {

    private final SkuExtService skuExtService;

    // ========== 工具方法：安全获取并 trim 字符串 ==========
    private static String safeTrim(String str) {
        return Optional.ofNullable(str).map(String::trim).orElse("");
    }

    /**
     * 根据 SKU 编码精确查询
     *
     * @param query SKU 查询条件
     * @return SKU 详细信息
     */
    @PostMapping("/find-sku")
    @ApiOperation(value = "根据 SKU 编码查询", notes = "通过 SKU 编码精确查询物料信息")
    public ResultWrapper<SkuExtDto> findSkuByCode(
            @ApiParam("查询条件") @Valid @RequestBody SkuExtQuery query) {
        try {
            // 🔒 修复：query 可能为 null（虽然 @Valid 有校验，但防御性编程）
            if (query == null) {
                log.warn("【查询 SKU】请求参数 query 为空");
                return ResultWrapper.buildFailure("请求参数不能为空");
            }

            String skuCode = query.getSkuCode();
            // 🔒 修复：使用 StringUtils.isBlank 替代 null + trim 组合，避免 NPE
            if (StringUtils.isBlank(skuCode)) {
                return ResultWrapper.buildFailure("SKU 编码不能为空");
            }

            // 🔒 修复：安全 trim，确保传入 service 的值非 null
            String safeSkuCode = safeTrim(skuCode);
            SkuExtDto skuExtDto = skuExtService.queryBySkuCode(safeSkuCode);

            return ResultWrapper.buildSuccess(skuExtDto);
        } catch (Exception e) {
            // 🔒 修复：日志中使用 Optional 避免 e.getMessage() 为 null 时输出异常
            log.error("查询 SKU 信息失败: {}", Optional.ofNullable(e.getMessage()).orElse("未知错误"), e);
            return ResultWrapper.buildFailure("查询 SKU 信息失败: " + Optional.ofNullable(e.getMessage()).orElse("未知错误"));
        }
    }

    /**
     * 校验 SKU 是否存在
     *
     * @param skuCode SKU 编码
     * @return 校验结果
     */
    @GetMapping("/exists/{skuCode}")
    @ApiOperation(value = "校验 SKU 是否存在", notes = "检查指定的 SKU 编码是否存在于系统中")
    public ResultWrapper<Boolean> checkSkuExists(
            @ApiParam(value = "SKU 编码", required = true, example = "SKU-BATT-LFP-100AH")
            @PathVariable String skuCode) {
        try {
            // 🔒 修复：@PathVariable 理论上不会为 null，但防御性编程 + 使用 StringUtils.isBlank
            if (StringUtils.isBlank(skuCode)) {
                return ResultWrapper.buildFailure("SKU 编码不能为空");
            }

            // 🔒 修复：安全 trim 后查询
            String safeSkuCode = safeTrim(skuCode);
            SkuExtDto skuExtDto = skuExtService.queryBySkuCode(safeSkuCode);
            Boolean exists = skuExtDto != null;

            return ResultWrapper.buildSuccess(exists);
        } catch (Exception e) {
            // 🔒 修复：日志安全输出
            log.error("校验 SKU 存在性失败: {}", Optional.ofNullable(e.getMessage()).orElse("未知错误"), e);
            return ResultWrapper.buildFailure("校验 SKU 存在性失败: " + Optional.ofNullable(e.getMessage()).orElse("未知错误"));
        }
    }

//    /**
//     * 根据 SKU 编码批量查询
//     *
//     * @param skuCodes SKU 编码列表
//     * @return SKU 详细信息列表
//     */
//    @PostMapping("/find-skus-batch")
//    @ApiOperation(value = "批量查询 SKU", notes = "通过多个 SKU 编码批量查询物料信息")
//    public ResultWrapper<List<SkuExtDto>> findSkusBatch(
//            @ApiParam(value = "SKU 编码列表", required = true, example = "[\"SKU001\", \"SKU002\"]")
//            @RequestBody List<String> skuCodes) {
//        try {
//            if (skuCodes == null || skuCodes.isEmpty()) {
//                return ResultWrapper.buildFailure("SKU 编码列表不能为空");
//            }
//            return skuExtService.queryBySkuCodes(skuCodes);
//        } catch (Exception e) {
//            log.error("批量查询 SKU 信息失败: {}", e.getMessage(), e);
//            return ResultWrapper.buildFailure("批量查询 SKU 信息失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * SKU 模糊查询（支持分页）
//     *
//     * @param query SKU 模糊查询条件
//     * @return 分页结果
//     */
//    @PostMapping("/find-skus-page")
//    @ApiOperation(value = "SKU 模糊查询（分页）", notes = "支持按 SKU 编码、名称、分类等条件模糊查询，返回分页结果")
//    public ResultWrapper<PageResult<SkuExtDto>> findSkusPage(
//            @ApiParam("查询条件") @Valid @RequestBody SkuExtQuery query) {
//        try {
//            return skuExtService.querySkuPage(query);
//        } catch (Exception e) {
//            log.error("分页查询 SKU 列表失败: {}", e.getMessage(), e);
//            return ResultWrapper.buildFailure("分页查询 SKU 列表失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * SKU 列表查询（不分页）
//     *
//     * @param query SKU 模糊查询条件
//     * @return SKU 列表
//     */
//    @PostMapping("/find-skus-list")
//     @ApiOperation(value = "SKU 列表查询", notes = "支持按 SKU 编码、名称、分类等条件模糊查询，返回完整列表")
//    public ResultWrapper<List<SkuExtDto>> findSkusList(
//            @ApiParam("查询条件") @Valid @RequestBody SkuExtQuery query) {
//        try {
//            return skuExtService.querySkuList(query);
//        } catch (Exception e) {
//            log.error("查询 SKU 列表失败: {}", e.getMessage(), e);
//            return ResultWrapper.buildFailure("查询 SKU 列表失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 根据物料分类查询 SKU 列表
//     *
//     * @param categoryCode 物料分类编码
//     * @return SKU 列表
//     */
//    @GetMapping("/find-by-category/{categoryCode}")
//    @ApiOperation(value = "根据物料分类查询 SKU", notes = "通过物料分类编码查询该分类下的所有 SKU")
//    public ResultWrapper<List<SkuExtDto>> findSkusByCategory(
//            @ApiParam(value = "物料分类编码", required = true, example = "RAW")
//            @PathVariable String categoryCode) {
//        try {
//            if (categoryCode == null || categoryCode.trim().isEmpty()) {
//                return ResultWrapper.buildFailure("物料分类编码不能为空");
//            }
//            SkuExtQuery query = new SkuExtQuery();
//            query.setCategoryCode(categoryCode);
//            return skuExtService.querySkuList(query);
//        } catch (Exception e) {
//            log.error("根据物料分类查询 SKU 失败: {}", e.getMessage(), e);
//            return ResultWrapper.buildFailure("根据物料分类查询 SKU 失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 根据工厂编码查询 SKU 列表
//     *
//     * @param plantCode 工厂编码
//     * @return SKU 列表
//     */
//    @GetMapping("/find-by-plant/{plantCode}")
//    @ApiOperation(value = "根据工厂编码查询 SKU", notes = "通过工厂编码查询该工厂下的所有 SKU")
//    public ResultWrapper<List<SkuExtDto>> findSkusByPlant(
//            @ApiParam(value = "工厂编码", required = true, example = "CN-WX-FACTORY-A")
//            @PathVariable String plantCode) {
//        try {
//            if (plantCode == null || plantCode.trim().isEmpty()) {
//                return ResultWrapper.buildFailure("工厂编码不能为空");
//            }
//            SkuExtQuery query = new SkuExtQuery();
//            query.setPlantCode(plantCode);
//            return skuExtService.querySkuList(query);
//        } catch (Exception e) {
//            log.error("根据工厂编码查询 SKU 失败: {}", e.getMessage(), e);
//            return ResultWrapper.buildFailure("根据工厂编码查询 SKU 失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * SKU 智能搜索（支持编码/名称/规格模糊匹配）
//     *
//     * @param keyword 搜索关键词
//     * @param pageSize 每页数量（可选，默认 20）
//     * @return SKU 列表
//     */
//    @GetMapping("/search")
//    @ApiOperation(value = "SKU 智能搜索", notes = "支持按 SKU 编码、名称、规格等字段模糊搜索")
//    public ResultWrapper<PageResult<SkuExtDto>> searchSku(
//            @ApiParam(value = "搜索关键词", required = true, example = "磷酸铁锂")
//            @RequestParam String keyword,
//            @ApiParam(value = "页码", defaultValue = "1")
//            @RequestParam(defaultValue = "1") Integer pageNum,
//            @ApiParam(value = "每页数量", defaultValue = "20")
//            @RequestParam(defaultValue = "20") Integer pageSize) {
//        try {
//            if (keyword == null || keyword.trim().isEmpty()) {
//                return ResultWrapper.buildFailure("搜索关键词不能为空");
//            }
//
//            SkuExtQuery query = new SkuExtQuery();
//            query.setKeyword(keyword.trim());
//            query.setPageNum(pageNum);
//            query.setPageSize(pageSize);
//
//            return skuExtService.querySkuPage(query);
//        } catch (Exception e) {
//            log.error("SKU 智能搜索失败: {}", e.getMessage(), e);
//            return ResultWrapper.buildFailure("SKU 智能搜索失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 获取常用/热门 SKU 列表
//     *
//     * @param topN 前 N 条（可选，默认 10）
//     * @return SKU 列表
//     */
//    @GetMapping("/hot-list")
//    @ApiOperation(value = "获取常用/热门 SKU", notes = "获取系统中最常用的前 N 条 SKU（按使用频率排序）")
//    public ResultWrapper<List<SkuExtDto>> getHotSkuList(
//            @ApiParam(value = "返回数量", defaultValue = "10")
//            @RequestParam(defaultValue = "10") Integer topN) {
//        try {
//            if (topN == null || topN <= 0) {
//                topN = 10;
//            }
//            if (topN > 100) {
//                topN = 100; // 限制最大值
//            }
//
//            SkuExtQuery query = new SkuExtQuery();
//            query.setTopN(topN);
//            query.setOrderByHot(true); // 按热度排序
//
//            return skuExtService.querySkuList(query);
//        } catch (Exception e) {
//            log.error("获取热门 SKU 列表失败: {}", e.getMessage(), e);
//            return ResultWrapper.buildFailure("获取热门 SKU 列表失败: " + e.getMessage());
//        }
//    }
//
//
//
//    /**
//     * 获取指定分类下的所有 SKU（树形结构）
//     *
//     * @param categoryCode 物料分类编码
//     * @return SKU 树形结构
//     */
//    @GetMapping("/tree-by-category/{categoryCode}")
//    @ApiOperation(value = "获取分类下的 SKU 树", notes = "获取指定物料分类下的所有 SKU，按层级组织成树形结构")
//    public ResultWrapper<Object> getSkuTreeByCategory(
//            @ApiParam(value = "物料分类编码", required = true, example = "RAW")
//            @PathVariable String categoryCode) {
//        try {
//            if (categoryCode == null || categoryCode.trim().isEmpty()) {
//                return ResultWrapper.buildFailure("物料分类编码不能为空");
//            }
//
//            return skuExtService.getSkuTreeByCategory(categoryCode);
//        } catch (Exception e) {
//            log.error("获取 SKU 树形结构失败: {}", e.getMessage(), e);
//            return ResultWrapper.buildFailure("获取 SKU 树形结构失败: " + e.getMessage());
//        }
//    }
}