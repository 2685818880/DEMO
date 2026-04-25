package cn.zdjc.wms.project.service.requisition.impl;

import cn.zdjc.warehouse.inventory.infrastructure.enums.FlagStatus;
import cn.zdjc.wms.outbound.pick.domain.entity.PickItem;
import cn.zdjc.wms.outbound.pick.domain.repository.params.PickItemQueryParam;
import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickStatus;
import cn.zdjc.wms.outbound.pick.infrastructure.persistence.jdbc.repository.PickItemRepository;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionOrderWithPalletCountDto;
import cn.zdjc.wms.project.domain.entity.pick.PickItemExtEntity;
import cn.zdjc.wms.project.domain.entity.requisition.RequisitionOrderExtEntity;
import cn.zdjc.wms.project.domain.query.pick.PickItemExtQuery;
import cn.zdjc.wms.project.domain.query.requisition.RequisitionOrderExtExQuery;
import cn.zdjc.wms.project.mapper.requisition.RequisitionOrderExtMapper;
import cn.zdjc.wms.project.repository.requisition.RequisitionItemExtRepository;
import cn.zdjc.wms.project.repository.requisition.RequisitionOrderExtRepository;
import cn.zdjc.wms.project.service.pick.PickItemExtService;
import cn.zdjc.wms.project.service.requisition.RequisitionOrderExtService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class RequisitionOrderExtServiceImpl
        extends ServiceImpl<RequisitionOrderExtMapper, RequisitionOrderExtEntity>
        implements RequisitionOrderExtService {
    @Resource
    private RequisitionOrderExtRepository repository;

    @Resource
    private PickItemRepository pickItemRepository;

    @Override
    public RequisitionOrderExtEntity findByBusinessFormNo(String businessFormNo) {
        List<RequisitionOrderExtEntity> requisitionOrderExtEntities = repository.queryByBusinessFormNoList(businessFormNo);
        if (requisitionOrderExtEntities.isEmpty()) {
            throw new IllegalArgumentException("业务单据号未获取到");
        }
        return requisitionOrderExtEntities.get(0);
    }


    /**
     * 获取订单及其托盘数量统计
     *
     * @param query 业务查询条件
     * @return 订单及托盘数量统计DTO
     */
    @Override
    public RequisitionOrderWithPalletCountDto getOrderWithPalletCount(RequisitionOrderExtExQuery query) {
        if (query == null || query.getBusinessFormNo() == null) {
            throw new IllegalArgumentException("业务单据号不能为空");
        }
//        List<RequisitionOrderExtEntity> requisitionOrderExtEntities = repository.queryByBusinessFormNoList(query.getBusinessFormNo());
//        if (requisitionOrderExtEntities.isEmpty()) {
//            throw new IllegalArgumentException("业务单据号未获取到");
//        }
//        RequisitionOrderExtEntity order = requisitionOrderExtEntities.get(0);
//        String formNo = order.getForm_no();
        // 2. 构建托盘查询条件
//        PickItemExtQuery palletQuery = new PickItemExtQuery();
//        palletQuery.setRelationOrderNo(formNo);

        // 3. 定义有效拣选状态（排除已取消/异常状态）
        List<PickStatus> validStatuses = Arrays.asList(
                PickStatus.create,
                PickStatus.executing,
                PickStatus.picking,
                PickStatus.finished
        );
//        palletQuery.setPickStatuses(validStatuses);
//        palletQuery.setPickStation(query.getWorkstationCode());
        // 4. 查询该订单下所有有效的拣选容器（托盘）
//        List<PickItemExtEntity> palletEntities = pickItemExtService.findPickAbleByContainer(palletQuery);


        PickItemQueryParam queryParam = new PickItemQueryParam();
        queryParam.setPickStatuses(validStatuses);
//        queryParam.setPickStation(query.getWorkstationCode());
        queryParam.setBizFormNo(query.getBusinessFormNo());
        queryParam.setPickAble(Boolean.TRUE);
        List<PickItem> pickItems = pickItemRepository.findPickItemByParam(queryParam);



        // 5. 统计托盘数量
        long totalPallets = pickItems.size();
        long activePallets = pickItems.stream()
                .filter(entity -> entity.getPickStatus() != null &&
                        (PickStatus.create.equals(entity.getPickStatus()) ||
                                PickStatus.executing.equals(entity.getPickStatus()) ||
                                PickStatus.picking.equals(entity.getPickStatus())||
                        PickStatus.finished.equals(entity.getPickStatus())))
                .count();
        long completedPallets = totalPallets - activePallets;
        // 6. 构建结果DTO
        return RequisitionOrderWithPalletCountDto.builder()
//                .id(order.getId().toString())
//                .formNo(order.getForm_no())
                .businessFormNo(query.getBusinessFormNo())
//                .formStatus(order.getForm_status())
//                .allotStatus(order.getAllot_status())
//                .submitStatus(order.getSubmit_status())
//                .ownerCode(order.getOwner_code())
//                .ownerName(order.getOwner_name())
//                .customCode(order.getCustom_code())
//                .customName(order.getCustom_name())
//                .startTime(order.getStart_time())
//                .finishTime(order.getFinish_time())
//                .closeTime(order.getClose_time())
//                .waveType(order.getWave_type())
//                .priority(order.getPriority())
//                .remark(order.getRemark())
                .palletCount(totalPallets)
                .activePalletCount(activePallets)
                .completedPalletCount(completedPallets)
                .build();



    }
}

