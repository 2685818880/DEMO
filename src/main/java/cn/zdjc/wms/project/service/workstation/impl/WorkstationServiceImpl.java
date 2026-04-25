package cn.zdjc.wms.project.service.workstation.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import cn.zdjc.warehouse.strategy.application.allotPolicy.ExQueryBean;
import cn.zdjc.wms.project.ProjectConfig;
import cn.zdjc.wms.project.common.socket.dto.WorkstationInfoWsDto;
import cn.zdjc.wms.project.common.utils.SqlUtil;
import cn.zdjc.wms.project.domain.dto.ws.PjOutboundOrderInfoDto;
import cn.zdjc.wms.project.domain.dto.ws.WorkstationExtDto;
import cn.zdjc.wms.project.domain.entity.ws.WorkstationExtEntity;
import cn.zdjc.wms.project.domain.enums.ws.WorkstationIsOpenEnums;
import cn.zdjc.wms.project.domain.enums.ws.WorkstationModeEnums;
import cn.zdjc.wms.project.domain.query.ws.WorkstationExtQuery;
import cn.zdjc.wms.project.mapper.outbound.OutboundExtMapper;
import cn.zdjc.wms.project.mapper.workstation.WorkstationExtMapper;
//import cn.zdjc.wms.project.repository.ws.WorkstationExtRepository;
import cn.zdjc.wms.project.service.workstation.WorkstationService;
import cn.zdjc.wms.project.vo.outbound.PjOutboundOrderTaskVo;
import cn.zdjc.wms.transport.out.domain.params.OutboundQueryParam;
import cn.zdjc.wms.transport.out.domain.repository.OutboundRepositoryService;
import cn.zdjc.wms.transport.out.infrastructure.enums.OutBoundStatus;
import cn.zdjc.wms.transport.out.infrastructure.pojo.OutboundPojo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.google.common.collect.Maps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkstationServiceImpl
        extends ServiceImpl<WorkstationExtMapper, WorkstationExtEntity>
        implements WorkstationService {
    @Resource
    private WorkstationExtMapper workstationExtMapper;

    @Resource
    private OutboundExtMapper outboundExtMapper;

    @Resource
    private OutboundRepositoryService outboundRepositoryService;

    @Override
    public PageResult<WorkstationExtDto> findInfoByPage(WorkstationExtQuery query) {
        return DatabaseExecuter.queryBeanPaged(getQuery(query).getSQL(), getQuery(query).getParams(),
                query.getPage(), query.getRow(), WorkstationExtDto.class);
    }
    private ExQueryBean getQuery(WorkstationExtQuery exQuery) {
        StringBuilder whereSql = new StringBuilder(" WHERE 1=1 ");
        Map<String, Object> param = Maps.newHashMapWithExpectedSize(8);

        if (exQuery == null) {
            exQuery = new WorkstationExtQuery();
        }

        // 精确匹配条件
        appendEqCondition(whereSql, param, "source_loc_code", exQuery.getSourceLocCode());
        appendEqCondition(whereSql, param, "tally_order_code", exQuery.getTallyOrderCode());
        appendEqCondition(whereSql, param, "target_loc_code", exQuery.getTargetLocCode());
        appendEqCondition(whereSql, param, "sku_code", exQuery.getSkuCode());
        appendEqCondition(whereSql, param, "plan_id", exQuery.getPlanId());

        // transfer_id 特殊处理
        if (exQuery.getTransferId() != null) {
            if (exQuery.getPlanId() == null) {
                whereSql.append(" AND (transfer_id = :transfer_id OR plan_id = :transfer_id)");
                param.put("transfer_id", exQuery.getTransferId());
            } else {
                whereSql.append(" AND transfer_id = :transfer_id");
                param.put("transfer_id", exQuery.getTransferId());
            }
        }

        // 安全排序
        String orderBy = buildSafeOrderBy(exQuery.getSidx(), exQuery.getSord());

        // 关键：使用带驼峰别名的列定义
        String baseSql = "SELECT " + SqlUtil.getColumnsWithAlias(WorkstationExtEntity.class)
                + " FROM " + SqlUtl.getTable(WorkstationExtEntity.class);

        return new ExQueryBean(baseSql, whereSql.toString(), orderBy, param);
    }

    /**
     * 安全追加精确匹配条件
     */
    private void appendEqCondition(StringBuilder whereSql, Map<String, Object> param,
                                   String columnName, String value) {
        if (!StringUtl.isEmpty(value)) {
            whereSql.append(" AND ").append(columnName).append(" = :").append(columnName);
            param.put(columnName, value);
        }
    }

    /**
     * 安全构建ORDER BY子句
     */
    private String buildSafeOrderBy(String sidx, String sord) {
        Set<String> allowedFields = Set.of(
                "createDatetime", "updateDatetime", "workstationCode",
                "workstationName", "sourceLocCode", "targetLocCode"
        );

        if (StringUtils.isBlank(sidx) || !allowedFields.contains(sidx.trim())) {
            return " ORDER BY create_datetime DESC";
        }

        // 将驼峰排序字段转为下划线（数据库实际列名）
        String dbColumn = SqlUtil.camelToUnderline(sidx.trim());
        String orderDirection = "DESC";
        if ("asc".equalsIgnoreCase(sord)) {
            orderDirection = "ASC";
        }

        return " ORDER BY " + dbColumn + " " + orderDirection;
    }

    /**
     * 安全追加精确匹配条件
     */
    private void appendEqCondition(StringBuilder whereSql, Map<String, Object> param,
                                   String columnName, Object value) {
        if (value != null && !StringUtl.isEmpty(String.valueOf(value))) {
            whereSql.append(" AND ").append(columnName).append(" = :").append(columnName);
            param.put(columnName, value);
        }
    }



    /**
     * 根据工作站编号精确查询有效工作站列表
     *
     * @param query 查询条件（仅使用 workstationCode 字段）
     * @return 工作站列表，若查询条件为空则返回空列表
     */
    @Override
    public List<WorkstationExtEntity> getWorkstationsByCode(WorkstationExtQuery query) {
        // 1. null 检查
        if (query == null) {
            log.warn("getWorkstationsByCode: query is null");
            return Collections.emptyList();
        }

        // 2. 打印查询条件
        log.info("查询条件 - code: {}, mode: {}, orderId: {}",
                query.getWorkstationCode(),
                query.getWorkstationMode(),
                query.getOrderId());

        // 3. 构建查询
        LambdaQueryWrapper<WorkstationExtEntity> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(query.getWorkstationCode())) {
            wrapper.eq(WorkstationExtEntity::getWorkstationCode, query.getWorkstationCode());
        }
        if (StringUtils.isNotBlank(query.getWorkstationMode())) {
            wrapper.eq(WorkstationExtEntity::getWorkstationMode, query.getWorkstationMode());
        }
        if (StringUtils.isNotBlank(query.getOrderId())) {
            wrapper.eq(WorkstationExtEntity::getOrderId, query.getOrderId());
        }

        // 4. 打印 SQL（需要开启 MyBatis-Plus 日志）
        log.info("执行的 SQL 条件: {}", wrapper.getSqlSegment());

        // 5. 执行查询
        List<WorkstationExtEntity> result = list(wrapper);

        log.info("查询结果数量: {}", result.size());

        return result;
    }

    /**
     * 根据工作站编号精确查询有效工作站列表
     *
     * @param query 查询条件（仅使用 workstationCode 字段）
     * @return 工作站列表，若查询条件为空则返回空列表
     */
    @Override
    public WorkstationExtDto getOne(WorkstationExtQuery query) {
        // 1. 基础校验
        if (query == null || StringUtils.isBlank(query.getWorkstationIp())) {
            return null;
        }

        // 2. 构建动态查询条件（全部从 query 对象取值）
        WorkstationExtEntity entity = lambdaQuery()
                .eq(StringUtils.isNotBlank(query.getWorkstationCode()),
                        WorkstationExtEntity::getWorkstationCode, query.getWorkstationCode())
                // 可选条件：只有当 query 中对应字段非空时才添加
                .eq(StringUtils.isNotBlank(query.getWorkstationMode()),
                        WorkstationExtEntity::getWorkstationMode, query.getWorkstationMode())
                .eq(StringUtils.isNotBlank(query.getWorkstationIp()),
                        WorkstationExtEntity::getWorkstationIp, query.getWorkstationIp())
                .eq(StringUtils.isNotBlank(query.getIsOpen()),
                        WorkstationExtEntity::getIsOpen, query.getIsOpen())
                .one(); // 只取一条

        // 3. 转换并返回
        return convertToDto(entity);
    }
    /**
     * 将 WorkstationEntity 转换为 WorkstationDto，并注入配置映射（模式列表、设备编码等）
     */
    private WorkstationExtDto convertToDto(WorkstationExtEntity entity) {
        if (entity == null) {
            return null;
        }

        WorkstationExtDto dto = new WorkstationExtDto();
        BeanUtil.copyProperties(entity, dto);

        // 注入工作模式列表
        Map<String, String> modeMapping = ProjectConfig.WorkstationAndModeListMapping;
        String modeStr = modeMapping != null ? modeMapping.get(entity.getWorkstationCode()) : null;
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(modeStr)) {
            dto.setWorkstationModeList(Arrays.stream(modeStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList()));
        } else {
            dto.setWorkstationModeList(new ArrayList<>());
        }

        // 注入设备编码（5个字段）
        Map<String, String> codeMapping = ProjectConfig.WorkstationCodeAndCodeMapping;
        String deviceStr = codeMapping != null ? codeMapping.get(entity.getWorkstationCode()) : null;
        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(deviceStr)) {
            List<String> codes = Arrays.stream(deviceStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            dto.setPickLeftDeviceCode(getSafeElement(codes, 0));
            dto.setPickRightDeviceCode(getSafeElement(codes, 1));
            dto.setTallyLeftDeviceCode(getSafeElement(codes, 2));
            dto.setTallyRightDeviceCode(getSafeElement(codes, 3));
            dto.setTakeStockLeftDeviceCode(getSafeElement(codes, 4));
        } else {
            // 显式置空，避免前端误解
            dto.setPickLeftDeviceCode(null);
            dto.setPickRightDeviceCode(null);
            dto.setTallyLeftDeviceCode(null);
            dto.setTallyRightDeviceCode(null);
            dto.setTakeStockLeftDeviceCode(null);
        }

        return dto;
    }


    /**
     * 安全获取列表元素，防止 IndexOutOfBoundsException
     */
    private String getSafeElement(List<String> list, int index) {
        if (list != null && index >= 0 && index < list.size()) {
            return list.get(index);
        }
        return null;
    }

    @Override
    public PageResult<WorkstationExtEntity> queryPaged(WorkstationExtQuery query) {
        if (query == null) {
            query = new WorkstationExtQuery();
        }

        LambdaQueryWrapper<WorkstationExtEntity> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.isNotBlank(query.getWorkstationCode())) {
            wrapper.like(WorkstationExtEntity::getWorkstationCode, query.getWorkstationCode());
        }
        if (StringUtils.isNotBlank(query.getWorkstationName())) {
            wrapper.like(WorkstationExtEntity::getWorkstationName, query.getWorkstationName());
        }
        if (query.getIsOpen() != null) {
            wrapper.eq(WorkstationExtEntity::getIsOpen, query.getIsOpen());
        }
        if (StringUtils.isNotBlank(query.getWorkstationIp())) {
            wrapper.like(WorkstationExtEntity::getWorkstationIp, query.getWorkstationIp());
        }
        if (StringUtils.isNotBlank(query.getWorkstationStatus())) {
            wrapper.eq(WorkstationExtEntity::getWorkstationStatus, query.getWorkstationStatus());
        }
        if (StringUtils.isNotBlank(query.getWorkstationMode())) {
            wrapper.eq(WorkstationExtEntity::getWorkstationMode, query.getWorkstationMode());
        }
        if (StringUtils.isNotBlank(query.getOperator())) {
            wrapper.like(WorkstationExtEntity::getOperator, query.getOperator());
        }

        wrapper.orderByDesc(WorkstationExtEntity::getCreateDatetime);

        long current = query.getPage() != null && query.getPage() > 0 ? query.getPage() : 1;
        long size = query.getRow() != null && query.getRow() > 0 ? query.getRow() : 10;

        IPage<WorkstationExtEntity> page = this.page(new Page<>(current, size), wrapper);

        //List<T> rows, int total, int page, int row
        return new PageResult<>(
                page.getRecords(),
                (int) query.getPage(),
                (int)query.getRow(),
                (int) page.getTotal()
        );
    }

    @Override
    public void updateStatus(WorkstationExtDto workstationExtDto) {
        workstationExtDto.checkDataUpdateStatus();
        //1. 查询工作站信息是否存在
        QueryWrapper<WorkstationExtEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WorkstationExtEntity::getWorkstationCode, workstationExtDto.getWorkstationCode());
        WorkstationExtEntity workstation = workstationExtMapper.selectOne(queryWrapper);
        String historyMode = workstation.getWorkstationMode();
        if (ObjectUtil.isEmpty(workstation)) {
            throw new IllegalStateException("当前工作站信息不存在!");
        }

        if (com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(workstationExtDto.getWorkstationIp())) {
            WorkstationExtQuery workstationExtQuery = new WorkstationExtQuery();
            workstationExtQuery.setWorkstationIp(workstationExtDto.getWorkstationIp());
            WorkstationExtEntity entity = this.getOne(workstationExtQuery.buildQueryWrapper());
            if (ObjectUtil.isNotEmpty(entity) && !entity.getWorkstationCode().equals(workstationExtDto.getWorkstationCode())) {
                throw new IllegalStateException("当前工作站IP信息存在!");
            }
        }


        //2.如果工作站关闭 需校验
        List<String> modeList = new ArrayList<>();
        modeList.add(WorkstationModeEnums.MATERIAL_RECEIVE.getCode());
        modeList.add(WorkstationModeEnums.MATERIAL_PICK.getCode());
        modeList.add(WorkstationModeEnums.TOOLING_REGISTER.getCode());
        modeList.add(WorkstationModeEnums.TOOLING_INBOUND.getCode());
        modeList.add(WorkstationModeEnums.TOOLING_PICK.getCode());
        if (!modeList.contains(workstationExtDto.getWorkstationMode())) {
            workstation.setWorkstationMode(workstation.getWorkstationMode());
        }

        if (workstationExtDto.getIsOpen().equals(WorkstationIsOpenEnums.FALSE.getCode())) {
            workstationExtDto.setWorkstationMode(historyMode);
            this.checkWorkstation(workstationExtDto, false);
        }

        WorkstationExtEntity workstationExtEntity = new WorkstationExtEntity();
        BeanUtil.copyProperties(workstationExtDto, workstationExtEntity);
        workstationExtEntity.setId(workstation.getId());
        this.updateById(workstationExtEntity);
    }

    @Override
    public void updateWorkOrder(WorkstationExtDto workstationExtDto) {

        //1. 查询工作站信息是否存在
        QueryWrapper<WorkstationExtEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WorkstationExtEntity::getWorkstationCode, workstationExtDto.getWorkstationCode());
        WorkstationExtEntity workstation = workstationExtMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(workstation)) {
            throw new IllegalStateException("当前工作站信息不存在!");
        }
        workstation.setOrderNo("");
        workstation.setOrderId("");
        this.updateById(workstation);
    }

    /**
     * 判断当前工作站是否可以退出
     *
     * @param workstationExtDto
     */
    @Override
    public void checkWorkstation(WorkstationExtDto workstationExtDto, Boolean isUpdate) {
        //1. 先判断当前工作站模式
        if (WorkstationModeEnums.MATERIAL_PICK.getCode().equals(workstationExtDto.getWorkstationMode())) {
            OutboundQueryParam queryParam = new OutboundQueryParam();
            queryParam.setTargetStation(workstationExtDto.getWorkstationCode());
            queryParam.setOutBoundStatuses(Arrays.asList(OutBoundStatus.Created,OutBoundStatus.Executing));
            List<OutboundPojo> outboundPojoList = outboundRepositoryService.findOutboundByParams(queryParam);
            if (ObjectUtil.isNotEmpty(outboundPojoList)) {
                throw new IllegalStateException("当前工作站还有未完成的拣选信息,不可退出!");
            }

        }


        //可以退出后 将工作站模式更成 没有模式
        if (isUpdate) {
            QueryWrapper<WorkstationExtEntity> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(WorkstationExtEntity::getWorkstationCode, workstationExtDto.getWorkstationCode());
            WorkstationExtEntity workstation = workstationExtMapper.selectOne(queryWrapper);
            if (ObjectUtil.isNotEmpty(workstation)) {
                workstation.setWorkstationMode(WorkstationModeEnums.NO_MODE.getCode());
                this.updateById(workstation);
            }
        }
    }

    /**
     * 通过工作站编码和模式查询信息
     *
     * @param workstationCode
     * @param workstationMode
     * @return
     */
    @Override
    public WorkstationExtEntity findInfoByCodeAndMode(String workstationCode, String workstationMode) {
        QueryWrapper<WorkstationExtEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WorkstationExtEntity::getWorkstationCode, workstationCode);
        WorkstationExtEntity workstationExtEntity = workstationExtMapper.selectOne(queryWrapper);
        if (ObjectUtil.isEmpty(workstationExtEntity)) {
            throw new IllegalStateException(String.format("当前工作站${{%s}},信息不存在!", workstationCode));
        }
//        if (!workstationExtEntity.getWorkstationMode().equals(workstationMode)) {
//            throw new IllegalStateException(String.format("当前工作站${{%s}},模式为:${{%s}},模式不符合当前业务!", workstationCode, workstationMode));
//        }
        if (workstationExtEntity.getIsOpen().equals(WorkstationIsOpenEnums.FALSE.getCode())) {
            throw new IllegalStateException(String.format("当前工作站${{%s}},已关闭不可操作!", workstationCode));
        }
        return workstationExtEntity;
    }

    @Override
    public void save(WorkstationExtDto dto) {
        WorkstationExtEntity entity = new WorkstationExtEntity();
        BeanUtil.copyProperties(dto, entity);

        entity.setId(UUID.randomUUID().toString());
        entity.setCreateDatetime(new Date());
        entity.setCreateBy("system");
        entity.setLastModifyDatetime(new Date());
        entity.setLastModifyBy("system");
        this.save(entity);
    }
    /**
     * webSocket触发工作站关闭
     *
     * @param workstationCode
     */
    @Override
    public void wsCloseStation(String workstationCode) {
        //1. 查询工作站信息是否存在
        QueryWrapper<WorkstationExtEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WorkstationExtEntity::getWorkstationCode, workstationCode);
        WorkstationExtEntity workstation = workstationExtMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotEmpty(workstation)) {
            workstation.setIsOpen(WorkstationIsOpenEnums.FALSE.getCode());
            workstation.setWorkstationStatus(WorkstationIsOpenEnums.FALSE.getCode());
            this.updateById(workstation);
        }
    }

    /**
     * webSocket触发工作站开启
     *
     * @param workstationCode
     */
    @Override
    public void wsOpenStation(String workstationCode) {
        //1. 查询工作站信息是否存在
        QueryWrapper<WorkstationExtEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(WorkstationExtEntity::getWorkstationCode, workstationCode);
        WorkstationExtEntity workstation = workstationExtMapper.selectOne(queryWrapper);
        if (ObjectUtil.isNotEmpty(workstation)) {
            workstation.setIsOpen(WorkstationIsOpenEnums.TRUE.getCode());
            workstation.setWorkstationStatus(WorkstationIsOpenEnums.TRUE.getCode());
            this.updateById(workstation);
        }
    }
//    /**
//     * 推送ws信息
//     *
//     * @param workstationInfoWsDto
//     */
//    @Override
//    @RedisLockAnnotation(lockKey = "'WS_'+#workstationInfoWsDto.workstationCode", expireTime = 2 * 60L)
//    @ContainerCodeCaptureHandle(containerCodeName = {"pickContainerCode", "tallySourceContainerCode", "orderContainerCode"})
//    public void sendWsMsg(WorkstationInfoWsDto workstationInfoWsDto) {
//        log.info("当前工作站推送消息为: " + JSONUtil.toJsonStr(workstationInfoWsDto));
//        //1. 先判断当前工作站在redis 是否存在缓存数据
//        WorkstationInfoWsDto redisWorkstationInfoWsDto = redisDataCache.getCacheObject(RedisKeys.getWorkstationWsInfoKey() + workstationInfoWsDto.getWorkstationCode());
//        if (ObjectUtil.isNotEmpty(redisWorkstationInfoWsDto)) {
//            BeanUtil.copyProperties(workstationInfoWsDto, redisWorkstationInfoWsDto, CopyOptions.create().setIgnoreNullValue(true));
//        } else {
//            redisWorkstationInfoWsDto = workstationInfoWsDto;
//        }
//        // 判断是前端刷新还是 容器到位主动发起 ：1 容器到位主动发起
//        WorkstationMsgUtil.sendMessageToOne(SocketMsgProtocol.builder().app("wms").business("workstation").channel(workstationInfoWsDto.getWorkstationCode()).data(Kv.init().set("msg", redisWorkstationInfoWsDto)).build());
//        redisDataCache.setCacheObject(RedisKeys.getWorkstationWsInfoKey() + redisWorkstationInfoWsDto.getWorkstationCode(), redisWorkstationInfoWsDto);
//    }
}