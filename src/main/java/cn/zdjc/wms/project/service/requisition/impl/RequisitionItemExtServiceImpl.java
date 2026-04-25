package cn.zdjc.wms.project.service.requisition.impl;

import cn.zdjc.wms.common.annotation.Comment;
import cn.zdjc.wms.definition.infrastructure.enums.AllotStatus;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.project.domain.dto.asn.AsnItemExtDto;
import cn.zdjc.wms.project.domain.dto.inbound.OrderExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemExtDto;
import cn.zdjc.wms.project.domain.entity.asn.AsnItemExtEntity;
import cn.zdjc.wms.project.domain.entity.requisition.RequisitionItemExtEntity;
import cn.zdjc.wms.project.domain.query.asn.AsnItemExtExQuery;
import cn.zdjc.wms.project.domain.query.requisition.RequisitionItemExtExQuery;
import cn.zdjc.wms.project.mapper.requisition.RequisitionItemExtMapper;
import cn.zdjc.wms.project.repository.pick.PickItemExtRepository;
import cn.zdjc.wms.project.repository.requisition.RequisitionItemExtRepository;
import cn.zdjc.wms.project.service.requisition.RequisitionItemExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foeris.y.common.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RequisitionItemExtServiceImpl
        extends ServiceImpl<RequisitionItemExtMapper, RequisitionItemExtEntity>
        implements RequisitionItemExtService {
    @Resource
    private RequisitionItemExtRepository repository;

    @Override
    public List<RequisitionItemExtDto> queryListWithAsnHeader(RequisitionItemExtExQuery query) {
        if (query == null) {
            log.debug(" 明细关联主表查询参数为空，返回全量数据");
            return null;
        }
        log.debug("执行  明细关联主表查询，参数: {}", query);
        List<RequisitionItemExtDto> result = baseMapper.selectWithAsnHeader(query);
        log.debug(" 明细关联查询完成，返回 {} 条记录", result.size());
        return result;
    }
    @Override
    public PageResult<RequisitionItemExtDto> queryItemAndOrderListWithAsnHeader(RequisitionItemExtExQuery query) {
        List<RequisitionItemExtDto> list = baseMapper.selectWithAsnHeader(query);

        int total = list.size();
        // 3. 构建分页结果
        PageResult<RequisitionItemExtDto> result = new PageResult<>(list, query.getPage(), query.getRow(), total);

        log.info("ASN 明细关联查询完成 - 总记录: {}, 当前页: {}, 返回: {} 条", total, query.getPage(), list.size());

        return result;

    }
    public List<OrderExtDto> orderNoListInfo(RequisitionItemExtExQuery query) {
        log.info("查询业务单号列表 - 条件: {}", query);

        // 防御性处理：防止 null
        List<RequisitionItemExtDto> itemDtoList = baseMapper.selectWithAsnHeader(query);
        if (itemDtoList == null || itemDtoList.isEmpty()) {
            return Collections.emptyList();
        }

//        return itemDtoList.stream()
//                .filter(Objects::nonNull) // 过滤 null 元素
//                .map(RequisitionItemExtDto::getBusinessFormNo)
//                .filter(StringUtils::isNotBlank) // 过滤 null/空/空白
//                .map(String::trim)
//                .distinct()
//                .map(orderNo -> {
//                    OrderExtDto dto = new OrderExtDto();
//                    dto.setBusinessOrderNo(orderNo);
//                    return dto;
//                })
//                .collect(Collectors.toList());
        List<OrderExtDto> collect = itemDtoList.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNoneBlank(
                        item.getBusinessItemNo(),
                        item.getSkuCode()))  // 核心字段非空
                .map(item -> {
                    OrderExtDto dto = new OrderExtDto();
                    dto.setBusinessOrderNo(item.getBusinessFormNo());  // 订单号
                    dto.setSkuCode(item.getSkuCode());                  // 物料号
                    dto.setSkuName(item.getSkuName());                  // 物料名称
                    dto.setBatchNo(item.getBatchNo());                  // 批次号
                    // 数量处理（根据实际类型转换）
                    if (item.getPrimaryQty() != null) {
                        dto.setPrimaryQty(item.getPrimaryQty());
                    }
                    dto.setFormStatus(item.getFormStatus());            // 单据状态
                    return dto;
                })
                .collect(Collectors.toList());
        return collect;
    }

    public PageResult<OrderExtDto> orderNoPageInfo(RequisitionItemExtExQuery query) {
        log.info("查询业务单号分页 - 条件: {}", query);

        // 防御性处理：防止 null
        List<RequisitionItemExtDto> itemDtoList = baseMapper.selectWithAsnHeader(query);
        if (itemDtoList == null || itemDtoList.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), query.getPage(), query.getRow(), 0);
        }

        // 计算去重后的结果
//        List<OrderExtDto> collect = itemDtoList.stream()
//                .filter(Objects::nonNull) // 过滤 null 元素
//                .map(RequisitionItemExtDto::getBusinessFormNo)
//                .filter(StringUtils::isNotBlank) // 过滤 null/空/空白
//                .map(String::trim)
//                .distinct()
//                .map(orderNo -> {
//                    OrderExtDto dto = new OrderExtDto();
//                    dto.setBusinessOrderNo(orderNo);
//                    return dto;
//                })
//                .collect(Collectors.toList());

        List<OrderExtDto> collect = itemDtoList.stream()
                .filter(Objects::nonNull)
                .filter(item -> StringUtils.isNoneBlank(
                        item.getBusinessItemNo(),
                        item.getSkuCode()))  // 核心字段非空
                .map(item -> {
                    OrderExtDto dto = new OrderExtDto();
                    dto.setBusinessOrderNo(item.getBusinessFormNo());  // 订单号
                    dto.setSkuCode(item.getSkuCode());                  // 物料号
                    dto.setSkuName(item.getSkuName());                  // 物料名称
                    dto.setBatchNo(item.getBatchNo());                  // 批次号
                    // 数量处理（根据实际类型转换）
                    if (item.getPrimaryQty() != null) {
                        dto.setPrimaryQty(item.getPrimaryQty());
                    }
                    dto.setFormStatus(item.getFormStatus());            // 单据状态
                    return dto;
                })
                .collect(Collectors.toList());



        // ⚠️ 关键：total 应该是原始数据总数（去重前），不是 collect.size()
        int total = (int) itemDtoList.stream()
                .filter(Objects::nonNull)
                .map(RequisitionItemExtDto::getBusinessFormNo)
                .filter(StringUtils::isNotBlank)
                .count(); // 去重前的数量

        return new PageResult<>(collect, query.getPage(), query.getRow(), total);
    }

}
