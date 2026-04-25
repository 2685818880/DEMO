package cn.zdjc.wms.project.service.pick;

import cn.zdjc.wms.outbound.pick.domain.entity.PickItem;
import cn.zdjc.wms.outbound.pick.domain.repository.params.PickAblItemQueryParam;
import cn.zdjc.wms.project.domain.dto.pick.OrderContainerLeaveDto;
import cn.zdjc.wms.project.domain.dto.pick.WorkstationCompletePickDto;
import cn.zdjc.wms.project.domain.dto.pick.WorkstationPickInfoDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemContainerCodeExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemContainerCodeListExtDto;
import cn.zdjc.wms.project.domain.dto.requisition.RequisitionItemExtDto;
import cn.zdjc.wms.project.domain.vo.pick.PickOrderFinishVo;
import cn.zdjc.wms.project.domain.vo.pick.WorkstationPickInfoVo;
import com.foeris.y.common.exception.BusinessException;
import com.foeris.y.common.result.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 拣选扩展服务接口
 *
 * <p>提供拣选业务的核心服务，包括：
 * <ul>
 *   <li>工作站拣选信息查询</li>
 *   <li>拣选任务完成处理</li>
 *   <li>容器离开工作站业务处理</li>
 * </ul>
 *
 * @author chensor
 * @version 1.0.0
 * @since 2022-12-12
 */
public interface PickExtService {

    /**
     * 获取当前工作站的拣选信息
     *
     * <p>根据容器号和工作站信息，查询当前待处理的拣选任务列表，包括：
     * <ul>
     *   <li>订单基本信息（单号、物料信息）</li>
     *   <li>拣选任务明细（应拣数量、已拣数量）</li>
     *   <li>容器当前状态</li>
     *   <li>工作站配置信息</li>
     * </ul>
     *
     * @param workstationPickInfoDto 拣选信息查询参数，必须包含容器号和工作站编号
     * @return 拣选信息视图对象，包含订单列表和容器状态信息
     * @throws BusinessException 当容器不存在、不在当前工作站或状态异常时抛出
     * @throws IllegalArgumentException 参数校验失败时抛出
     * @see WorkstationPickInfoDto
     * @see WorkstationPickInfoVo
     */
    WorkstationPickInfoVo findPickInfo(WorkstationPickInfoDto workstationPickInfoDto);

    /**
     * 完成当前拣选任务
     *
     * <p>处理拣选完成操作，执行以下业务逻辑：
     * <ul>
     *   <li>校验拣选数量合法性（不能超过应拣数量）</li>
     *   <li>更新拣选任务状态为已完成</li>
     *   <li>更新容器状态为"已拣选"</li>
     *   <li>触发后续流程（如生成复核任务、更新库存）</li>
     *   <li>记录操作日志</li>
     * </ul>
     *
     * @param workstationCompletePickDto 拣选完成参数，包含容器号、订单明细及实际拣选数量
     * @return 拣选完成结果视图，包含完成状态、剩余任务数等信息
     * @throws BusinessException 当拣选数量超限、容器状态异常或订单不存在时抛出
     * @throws IllegalArgumentException 参数校验失败时抛出
     * @see WorkstationCompletePickDto
     * @see PickOrderFinishVo
     */
    PickOrderFinishVo completePick(WorkstationCompletePickDto workstationCompletePickDto);

    /**
     * 处理容器离开工作站业务
     *
     * <p>执行容器从工作站离开的核心业务逻辑，包括：
     * <ul>
     *   <li>校验容器当前是否在指定工作站</li>
     *   <li>校验容器状态是否允许离开（已完成拣选/复核）</li>
     *   <li>验证目标位置(toPos)合法性（warehouse/workshops）</li>
     *   <li>更新容器位置信息及状态</li>
     *   <li>记录容器流转日志</li>
     *   <li>根据目标位置触发后续流程（如入库、出库）</li>
     * </ul>
     *
     * @param dto 容器离开请求参数，包含容器号、工作站编号、目标位置等关键信息
     * @return 操作结果：true-成功，false-失败（业务校验不通过）
     * @throws BusinessException 容器状态异常、位置校验失败等业务规则违反时抛出
     * @throws IllegalArgumentException 参数缺失或格式错误时抛出
     * @throws RuntimeException 系统异常（如数据库操作失败）时抛出
     * @see OrderContainerLeaveDto
     *
     * @apiNote 该方法为事务性操作，成功时所有数据变更将持久化，失败时自动回滚
     * &#064;performance  预期响应时间 < 200ms，超时阈值 2000ms
     */
    boolean processContainerLeave(OrderContainerLeaveDto dto);

     void transportExecute(String houseCode, String containerCode, String deviceCode, String toPos, Map<String, Object> paramMap) ;

    /**
     * 依据托盘查询可拣选的明细
     * @param queryParam
     * @return
     */
    List<RequisitionItemContainerCodeExtDto> findPickAbleByContainerList(PickAblItemQueryParam queryParam);


    PageResult<RequisitionItemContainerCodeExtDto> findPickAbleByContainerPage(PickAblItemQueryParam query);



     void pickItemContainerExitOut(RequisitionItemContainerCodeListExtDto query);
}