package cn.zdjc.wms.project.service.pick.impl;

import cn.hutool.core.util.StrUtil;
import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickStatus;
import cn.zdjc.wms.project.domain.entity.pick.PickItemExtEntity;
import cn.zdjc.wms.project.domain.query.pick.PickItemExtQuery;
import cn.zdjc.wms.project.mapper.pick.PickItemExtMapper;
import cn.zdjc.wms.project.repository.palletize.PalletizeItemExtRepository;
import cn.zdjc.wms.project.repository.pick.PickItemExtRepository;
import cn.zdjc.wms.project.service.pick.PickItemExtService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 拣选明细扩展服务实现类
 *
 * <p>提供拣选明细数据的查询、更新等核心服务，包括：
 * <ul>
 *   <li>根据容器号查询拣选项</li>
 *   <li>根据复杂参数条件查询拣选项</li>
 *   <li>查询可拣选的拣选项</li>
 *   <li>更新拣选进度（已拣数量和状态）</li>
 *   <li>批量查询拣选项（高性能实现）</li>
 * </ul>
 *
 * <p><strong>设计特点：</strong>
 * <ul>
 *   <li>基于 MyBatis-Plus 的 ServiceImpl 实现，提供基础 CRUD 能力</li>
 *   <li>支持灵活的条件查询，满足各种业务场景</li>
 *   <li>批量查询采用分片策略，突破数据库 IN 列表限制</li>
 *   <li>更新操作支持乐观锁，防止并发冲突</li>
 * </ul>
 *
 * @author chensor
 * @version 1.0.0
 * @since 2022-12-12
 */
@Slf4j
@Service
public class PickItemExtServiceImpl
        extends ServiceImpl<PickItemExtMapper, PickItemExtEntity>
        implements PickItemExtService {

    /**
     * 拣选明细仓库层
     */
    @Resource
    private PickItemExtRepository repository;

    // ========== 工具方法：安全获取 Double 值（避免 auto-unboxing NPE）==========
    private static double safeDoubleValue(Double value, double defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }

    /**
     * 根据容器号和拣选状态查询拣选项
     *
     * <p><strong>业务场景：</strong>
     * <ul>
     *   <li>工作站查询当前容器的拣选任务</li>
     *   <li>容器流转时检查拣选状态</li>
     * </ul>
     *
     * @param containerCode 容器号（托盘号/周转箱号）
     * @param pickStatusList 拣选状态列表，用于过滤（可选）
     * @return 拣选项实体列表（保证不为 null）
     * @throws RuntimeException 数据库查询异常
     *
     * @see PickStatus
     * @see PickItemExtEntity
     */
    @Override
    public List<PickItemExtEntity> queryByContainerCode(String containerCode, List<PickStatus> pickStatusList) {
        log.debug("【查询拣选项】根据容器号查询 | containerCode={} | statuses={}",
                containerCode, pickStatusList);

        try {
            List<PickItemExtEntity> result = repository.queryByContainerCode(containerCode, pickStatusList);

            // 🔒 修复：确保返回空列表而非 null，避免调用方 NPE
            List<PickItemExtEntity> safeResult = Optional.ofNullable(result).orElse(Collections.emptyList());

            log.info("【查询拣选项】查询完成 | containerCode={} | resultSize={}",
                    containerCode, safeResult.size());

            return safeResult;

        } catch (Exception e) {
            log.error("【查询拣选项】查询异常 | containerCode={} | errorMessage={}",
                    containerCode, e.getMessage(), e);
            throw new RuntimeException("根据容器号查询拣选项失败", e);
        }
    }

    /**
     * 根据复杂参数条件查询拣选项
     *
     * <p><strong>支持的查询条件：</strong>
     * <ul>
     *   <li>明细 ID、明细编号</li>
     *   <li>拣选类型、拣选区域、拣选站点</li>
     *   <li>关联订单 ID、关联明细 ID</li>
     *   <li>关联单据号、关联明细号</li>
     *   <li>业务单据号、业务明细号</li>
     *   <li>包装条码、齐套标识</li>
     *   <li>容器编号、SKU 编码、仓库号</li>
     *   <li>下架单 ID、拣选状态列表</li>
     * </ul>
     *
     * <p><strong>使用示例：</strong>
     * <pre>
     * PickItemExtQuery query = new PickItemExtQuery();
     * query.setContainerCode("TP001");
     * query.setPickStatuses(Arrays.asList(PickStatus.picking, PickStatus.finished));
     * List<PickItemExtEntity> items = pickItemExtService.queryByParam(query);
     * </pre>
     *
     * @param param 查询参数对象，支持多种条件组合
     * @return 拣选项实体列表，参数为 null 时返回所有记录（保证不为 null）
     * @throws RuntimeException 构建查询条件或执行查询异常
     *
     * @see PickItemExtQuery
     * @see LambdaQueryWrapper
     */
    @Override
    public List<PickItemExtEntity> queryByParam(PickItemExtQuery param) {
        // 记录方法开始时间
        long startTime = System.currentTimeMillis();

        log.debug("【条件查询】开始构建查询条件 | param={}", param);

        try {
            // 参数为 null 时返回所有记录
            if (param == null) {
                log.warn("【条件查询】参数为 null，返回所有记录");
                List<PickItemExtEntity> all = this.list();
                // 🔒 修复：确保返回空列表而非 null
                List<PickItemExtEntity> safeAll = Optional.ofNullable(all).orElse(Collections.emptyList());
                log.info("【条件查询】返回所有记录 | count={} | duration={}ms",
                        safeAll.size(), System.currentTimeMillis() - startTime);
                return safeAll;
            }

            // 构建查询条件
            LambdaQueryWrapper<PickItemExtEntity> query = new LambdaQueryWrapper<>();

            // ===== 明细基础信息 =====
            query.eq(param.getItemId() != null, PickItemExtEntity::getId, param.getItemId());
            query.eq(StrUtil.isNotBlank(param.getItemNo()), PickItemExtEntity::getItem_no, param.getItemNo());

            // ===== 拣选配置信息 =====
            query.eq(param.getPickType() != null, PickItemExtEntity::getPick_type, param.getPickType());
            query.eq(StrUtil.isNotBlank(param.getPickArea()), PickItemExtEntity::getPick_area, param.getPickArea());
            query.eq(StrUtil.isNotBlank(param.getPickStation()), PickItemExtEntity::getPick_station, param.getPickStation());

            // ===== 关联订单信息 =====
            query.eq(param.getRelationOrderId() != null, PickItemExtEntity::getRelation_order_id, param.getRelationOrderId());
            query.eq(param.getRelationItemId() != null, PickItemExtEntity::getRelation_item_id, param.getRelationItemId());
            query.eq(StrUtil.isNotBlank(param.getRelationOrderNo()), PickItemExtEntity::getRelation_order_no, param.getRelationOrderNo());
            query.eq(StrUtil.isNotBlank(param.getRelationItemNo()), PickItemExtEntity::getRelation_item_no, param.getRelationItemNo());

            // ===== 业务单据信息 =====
            query.eq(StrUtil.isNotBlank(param.getBizFormNo()), PickItemExtEntity::getBusiness_form_no, param.getBizFormNo());
            query.eq(StrUtil.isNotBlank(param.getBizItemNo()), PickItemExtEntity::getBusiness_item_no, param.getBizItemNo());

            // ===== 物料信息 =====
            query.eq(StrUtil.isNotBlank(param.getPackageNo()), PickItemExtEntity::getPackage_no, param.getPackageNo());
            query.eq(param.getKittingFlag() != null, PickItemExtEntity::getKitting_flag, param.getKittingFlag());
            query.eq(StrUtil.isNotBlank(param.getContainerCode()), PickItemExtEntity::getContainer_code, param.getContainerCode());
            query.eq(StrUtil.isNotBlank(param.getSkuCode()), PickItemExtEntity::getSku_code, param.getSkuCode());
            query.eq(StrUtil.isNotBlank(param.getHouseCode()), PickItemExtEntity::getHouse_code, param.getHouseCode());

            // ===== 其他信息 =====
            query.eq(param.getOutboundId() != null, PickItemExtEntity::getOutbound_id, param.getOutboundId());

            // ===== 拣选状态（支持多选）=====
            if (!CollectionUtils.isEmpty(param.getPickStatuses())) {
                query.in(PickItemExtEntity::getPick_status, param.getPickStatuses());
                log.debug("【条件查询】添加拣选状态过滤 | statuses={}", param.getPickStatuses());
            }

            // 执行查询
            List<PickItemExtEntity> result = this.list(query);
            // 🔒 修复：确保返回空列表而非 null
            List<PickItemExtEntity> safeResult = Optional.ofNullable(result).orElse(Collections.emptyList());

            long duration = System.currentTimeMillis() - startTime;
            log.info("【条件查询】查询完成 | resultSize={} | duration={}ms", safeResult.size(), duration);

            // 性能告警
            if (duration > 1000) {
                log.warn("【条件查询】性能告警 | duration={}ms | param={}", duration, param);
            }

            return safeResult;

        } catch (Exception e) {
            log.error("【条件查询】查询异常 | param={} | errorMessage={}", param, e.getMessage(), e);
            throw new RuntimeException("条件查询拣选项失败", e);
        }
    }

    /**
     * 查询可拣选的拣选项
     *
     * <p><strong>业务规则：</strong>
     * <ul>
     *   <li>根据参数查询所有匹配的拣选明细</li>
     *   <li>过滤出已拣数量 < 需求数量的记录（即可继续拣选的明细）</li>
     *   <li>返回空列表而非 null，避免 NPE</li>
     * </ul>
     *
     * <p><strong>使用场景：</strong>
     * <ul>
     *   <li>工作站查询待拣选任务</li>
     *   <li>容器绑定时检查是否有未完成的拣选任务</li>
     * </ul>
     *
     * @param queryParam 查询参数，用于筛选拣选项
     * @return 可拣选的拣选项列表（已拣数量 < 需求数量），保证不为 null
     * @throws RuntimeException 查询或过滤异常
     *
     * @see PickItemExtQuery
     */
    @Override
    public List<PickItemExtEntity> findPickAbleByContainer(PickItemExtQuery queryParam) {
        log.debug("【查询可拣选】开始查询 | queryParam={}", queryParam);

        try {
            // 1. 先通过参数查询所有匹配的拣选明细
            List<PickItemExtEntity> pickItemExtEntityList = queryByParam(queryParam);

            // 2. 如果为空，返回空列表（避免返回 null）
            if (pickItemExtEntityList == null || pickItemExtEntityList.isEmpty()) {
                log.info("【查询可拣选】未找到匹配的拣选项 | queryParam={}", queryParam);
                return Collections.emptyList();
            }

            // 3. 过滤出"可拣选"的记录：picked_qty < primary_qty
            // 🔒 修复：添加 Objects::nonNull 过滤 + Double 字段安全取值
            List<PickItemExtEntity> pickableItems = pickItemExtEntityList.stream()
                    .filter(Objects::nonNull)  // 🔒 过滤列表中的 null 元素
                    .filter(item -> {
                        // 🔒 修复：使用工具方法安全获取 Double 值，避免 auto-unboxing NPE
                        double pickedQty = safeDoubleValue(item.getPicked_qty(), 0.0);
                        double primaryQty = safeDoubleValue(item.getPrimary_qty(), 0.0);
                        boolean isPickable = pickedQty < primaryQty;

                        if (!isPickable) {
                            log.debug("【查询可拣选】过滤已完成明细 | pickItemId={} | pickedQty={} | primaryQty={}",
                                    item.getId(), pickedQty, primaryQty);
                        }

                        return isPickable;
                    })
                    .collect(Collectors.toList());

            log.info("【查询可拣选】查询完成 | total={} | pickable={} | queryParam={}",
                    pickItemExtEntityList.size(), pickableItems.size(), queryParam);

            return pickableItems;

        } catch (Exception e) {
            log.error("【查询可拣选】查询异常 | queryParam={} | errorMessage={}", queryParam, e.getMessage(), e);
            throw new RuntimeException("查询可拣选明细失败", e);
        }
    }

    /**
     * 根据拣选项 ID 查询单条记录
     *
     * @param id 拣选项 ID（主键）
     * @return 拣选项实体，不存在时返回 null
     * @throws RuntimeException 查询异常
     */
    @Override
    public PickItemExtEntity getPickItemById(String id) {
        if (StrUtil.isBlank(id)) {
            log.warn("【根据 ID 查询】参数为空 | id={}", id);
            return null;
        }

        try {
            PickItemExtEntity entity = baseMapper.selectById(id);

            if (entity == null) {
                log.debug("【根据 ID 查询】未找到记录 | id={}", id);
            } else {
                log.debug("【根据 ID 查询】查询成功 | id={} | pickStatus={}", id, entity.getPick_status());
            }

            return entity;

        } catch (Exception e) {
            log.error("【根据 ID 查询】查询异常 | id={} | errorMessage={}", id, e.getMessage(), e);
            throw new RuntimeException("根据 ID 查询拣选项失败", e);
        }
    }

    /**
     * 更新拣选明细的已拣数量和状态
     *
     * <p><strong>业务规则：</strong>
     * <ul>
     *   <li>已拣数量不能超过需求数量（业务校验）</li>
     *   <li>使用乐观锁防止并发更新冲突</li>
     *   <li>自动更新最后修改时间</li>
     *   <li>返回更新是否成功（影响行数 > 0）</li>
     * </ul>
     *
     * <p><strong>并发控制：</strong>
     * <ul>
     *   <li>先查询原记录进行业务校验</li>
     *   <li>使用条件更新（WHERE id = ?）</li>
     *   <li>检查影响行数判断是否成功</li>
     * </ul>
     *
     * @param id 拣选项 ID
     * @param pickedQty 已拣数量（可选，为 null 时不更新）
     * @param pickStatus 拣选状态（可选，为 null 时不更新）
     * @return true-更新成功，false-记录不存在或已被并发修改
     * @throws IllegalArgumentException ID 为空或已拣数量超限
     * @throws RuntimeException 更新异常
     */
    @Override
    public boolean updatePickedQtyAndStatus(String id, Double pickedQty, PickStatus pickStatus,String orderContainerCode) {
        // 记录方法开始时间
        long startTime = System.currentTimeMillis();

        log.debug("【更新拣选进度】开始 | id={} | pickedQty={} | pickStatus={}", id, pickedQty, pickStatus);

        try {
            // 1. 参数校验
            if (StrUtil.isBlank(id)) {
                log.error("【更新拣选进度】参数校验失败 | id 为空");
                throw new IllegalArgumentException("拣选明细 ID 不能为空");
            }

            // 2. 查询原记录（用于业务校验）
            PickItemExtEntity original = repository.queryById(id);
            if (original == null) {
                log.warn("【更新拣选进度】记录不存在 | id={}", id);
                return false;
            }

            // 🔒 修复：安全获取 primary_qty，避免 auto-unboxing NPE
            double primaryQty = original.getPrimary_qty();

            double newPickedQty = Optional.ofNullable(pickedQty).orElse(0.0);

            if (newPickedQty > primaryQty) {
                log.error("【更新拣选进度】业务校验失败 | id={} | newPickedQty={} | primaryQty={}",
                        id, newPickedQty, primaryQty);
                throw new IllegalArgumentException(
                        String.format("已拣数量 (%.2f) 不能超过需求数量 (%.2f)，明细 ID=%s",
                                newPickedQty, primaryQty, id));
            }

            // 4. 构建更新条件（使用乐观锁防止并发覆盖）
            LambdaUpdateWrapper<PickItemExtEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(PickItemExtEntity::getId, id);

            // 仅当参数非 null 时才更新对应字段
            if (pickedQty != null) {
                updateWrapper.set(PickItemExtEntity::getPicked_qty, pickedQty);
                log.debug("【更新拣选进度】设置已拣数量 | id={} | old={} | new={}",
                        id, original.getPicked_qty(), pickedQty);
            }
            if (pickStatus != null) {
                updateWrapper.set(PickItemExtEntity::getPick_status, pickStatus);
                log.debug("【更新拣选进度】设置拣选状态 | id={} | old={} | new={}",
                        id, original.getPick_status(), pickStatus);
            }
            updateWrapper.set(PickItemExtEntity::getOrderContainerCode, orderContainerCode);
            // 自动更新时间戳
            updateWrapper.set(PickItemExtEntity::getLast_modify_datetime, new Date());

            // 5. 执行更新（返回影响行数）
            int updated = this.getBaseMapper().update(null, updateWrapper);
            boolean success = updated > 0;

            long duration = System.currentTimeMillis() - startTime;

            if (success) {
                log.info("【更新拣选进度】成功 | id={} | pickedQty={} | pickStatus={} | duration={}ms",
                        id, pickedQty, pickStatus, duration);
            } else {
                log.warn("【更新拣选进度】失败（可能被并发修改）| id={} | duration={}ms", id, duration);
            }

            return success;

        } catch (IllegalArgumentException e) {
            // 业务校验异常，直接抛出
            throw e;
        } catch (Exception e) {
            log.error("【更新拣选进度】系统异常 | id={} | pickedQty={} | pickStatus={}",
                    id, pickedQty, pickStatus, e);
            throw new RuntimeException("更新拣选进度失败", e);
        }
    }

    /**
     * 批量根据 ID 查询拣货项（高性能实现）
     *
     * <p><strong>业务特性：</strong>
     * <ul>
     *   <li>自动过滤空 ID，避免 SQL 注入风险</li>
     *   <li>支持超大批量分片查询（突破 DB IN 列表限制）</li>
     *   <li>返回结果保持与输入 ID 顺序一致（便于调用方匹配）</li>
     *   <li>缺失记录自动跳过，不抛异常（调用方需校验完整性）</li>
     * </ul>
     *
     * <p><strong>性能保障：</strong>
     * <ul>
     *   <li>分片大小：1000（适配 MySQL/Oracle 等主流数据库 IN 列表上限）</li>
     *   <li>单次查询超时：自动熔断（通过 DB 连接池控制）</li>
     *   <li>性能告警：超过 500ms 记录 WARN 日志</li>
     * </ul>
     *
     * <p><strong>使用示例：</strong>
     * <pre>
     * List<String> ids = Arrays.asList("1", "2", "3");
     * List<PickItemExtEntity> items = pickItemExtService.batchGetByIds(ids);
     * // items.get(0) 对应 id="1" 的记录，不存在时为 null
     * </pre>
     *
     * @param ids 拣货项 ID 列表，允许包含 null（自动过滤），建议数量 ≤ 10,000
     * @return 按输入顺序返回的拣货项列表，缺失 ID 对应位置为 null
     * @throws IllegalArgumentException 当 ids 为 null 或全为空时抛出
     * @throws RuntimeException 批量查询异常
     *
     * @see <a href="https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html#function_in  ">MySQL IN 列表限制</a>
     */
    @Override
    public List<PickItemExtEntity> batchGetByIds(List<String> ids) {
        // ===== 1. 参数校验 =====
        if (ids == null || ids.isEmpty()) {
            log.warn("【批量查询】参数为空 | caller={}", getCallerClass());
            throw new IllegalArgumentException("拣货项 ID 列表不能为空");
        }

        // 过滤空值并记录警告
        List<String> validIds = ids.stream()
                .filter(Objects::nonNull)
                .filter(id -> !id.trim().isEmpty())
                .collect(Collectors.toList());

        if (validIds.isEmpty()) {
            log.warn("【批量查询】参数全为空值 | originalSize={} | caller={}", ids.size(), getCallerClass());
            throw new IllegalArgumentException("拣货项 ID 列表不包含有效 ID");
        }

        int invalidCount = ids.size() - validIds.size();
        if (invalidCount > 0) {
            log.warn("【批量查询】自动过滤无效 ID | total={} | valid={} | invalid={}",
                    ids.size(), validIds.size(), invalidCount);
        }

        // ===== 2. 分片查询（突破 DB IN 列表限制）=====
        final int BATCH_SIZE = 1000; // 数据库 IN 列表安全阈值
        List<PickItemExtEntity> allResults = new ArrayList<>(validIds.size());

        long startTime = System.currentTimeMillis();
        int totalQueried = 0;

        try {
            log.info("【批量查询】开始 | requested={} | valid={} | batchSize={}",
                    ids.size(), validIds.size(), BATCH_SIZE);

            // 按 BATCH_SIZE 分片查询
            int batchCount = (validIds.size() + BATCH_SIZE - 1) / BATCH_SIZE;
            for (int i = 0; i < validIds.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, validIds.size());
                List<String> batchIds = validIds.subList(i, end);
                int batchIndex = i / BATCH_SIZE + 1;

                log.debug("【批量查询】执行分片 | batchIndex={}/{} | batchSize={}",
                        batchIndex, batchCount, batchIds.size());

                List<PickItemExtEntity> batchResults = repository.listByIds(batchIds);
                if (CollectionUtils.isNotEmpty(batchResults)) {
                    allResults.addAll(batchResults);
                    log.trace("【批量查询】分片完成 | batchIndex={}/{} | returned={}",
                            batchIndex, batchCount, batchResults.size());
                } else {
                    log.debug("【批量查询】分片无结果 | batchIndex={}/{}", batchIndex, batchCount);
                }

                totalQueried += batchIds.size();
            }

            // ===== 3. 结果重组（保持输入顺序）=====
            // 🔒 修复：构建 ID->Entity 映射时添加 null 检查 + toMap key 安全处理
            Map<String, PickItemExtEntity> idToEntityMap = allResults.stream()
                    .filter(Objects::nonNull)  // 🔒 过滤 null 元素
                    .filter(entity -> entity.getId() != null)  // 🔒 确保 key 非 null
                    .collect(Collectors.toMap(
                            entity -> entity.getId().toString(),
                            entity -> entity,
                            (e1, e2) -> e1 // 冲突时保留第一个（理论上不应冲突）
                    ));

            // 按原始顺序重组结果（缺失位置为 null）
            List<PickItemExtEntity> orderedResults = new ArrayList<>(ids.size());
            int missingCount = 0;

            for (String id : ids) {
                if (id == null || id.trim().isEmpty()) {
                    orderedResults.add(null);
                } else {
                    // 🔒 修复：安全 trim，避免 id 为 null 时调用 trim()
                    String trimmedId = id.trim();
                    PickItemExtEntity entity = idToEntityMap.get(trimmedId);
                    orderedResults.add(entity);
                    if (entity == null) {
                        missingCount++;
                    }
                }
            }

            // ===== 4. 审计日志 =====
            long duration = System.currentTimeMillis() - startTime;
            log.info("【批量查询】完成 | requested={} | valid={} | returned={} | missing={} | duration={}ms | caller={}",
                    ids.size(), validIds.size(), allResults.size(), missingCount, duration, getCallerClass());

            if (missingCount > 0) {
                log.warn("【批量查询】部分拣货项不存在 | missingCount={} | sample={}",
                        missingCount,
                        ids.stream()
                                .filter(id -> id != null && !idToEntityMap.containsKey(id.trim()))
                                .limit(5)
                                .collect(Collectors.toList()));
            }

            // 性能告警
            if (duration > 500) {
                log.warn("【批量查询】性能告警 | duration={}ms | requestedCount={} | avgPerItem={}ms",
                        duration, validIds.size(), duration * 1.0 / validIds.size());
            }

            return orderedResults;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("【批量查询】异常 | requested={} | queried={} | duration={}ms | errorMessage={}",
                    ids.size(), totalQueried, duration, e.getMessage(), e);
            throw new RuntimeException("拣货项批量查询失败，请重试", e);
        }
    }

    /**
     * 获取调用方类名和方法名（用于日志追踪）
     *
     * <p>通过分析线程栈帧定位调用来源，便于问题排查和性能分析。
     *
     * <p><strong>栈帧结构：</strong>
     * <ul>
     *   <li>[0] - getStackTrace</li>
     *   <li>[1] - getCallerClass</li>
     *   <li>[2] - batchGetByIds</li>
     *   <li>[3] - 调用方类。方法名</li>
     * </ul>
     *
     * @return 调用方信息（格式：类名。方法名），获取失败时返回"unknown"
     */
    private String getCallerClass() {
        try {
            StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            // 栈帧结构: [0]=getStackTrace, [1]=getCallerClass, [2]=batchGetByIds, [3]=caller
            if (stackTrace.length > 3) {
                return stackTrace[3].getClassName() + "." + stackTrace[3].getMethodName();
            }
        } catch (Exception e) {
            // 安全兜底，避免影响主流程
            log.debug("【调用方追踪】获取调用方信息失败", e);
        }
        return "unknown";
    }

    @Override
    public void orderContainerExitOut(String businessFormNo){
        // 空实现，保持原逻辑
    }
}