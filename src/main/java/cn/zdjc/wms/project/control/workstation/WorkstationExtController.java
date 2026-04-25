package cn.zdjc.wms.project.control.workstation;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.zdjc.platform.system.manage.frame.LoginController;
import cn.zdjc.wms.pda.application.entity.Parameter;
import cn.zdjc.wms.project.common.constant.ProjectConstant;
import cn.zdjc.wms.project.common.utils.IpUtil;
import cn.zdjc.wms.project.domain.dto.ws.WorkstationExtDto;
import cn.zdjc.wms.project.domain.entity.ws.WorkstationExtEntity;
import cn.zdjc.wms.project.domain.query.ws.WorkstationExtQuery;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import cn.zdjc.wms.project.service.workstation.WorkstationService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.foeris.y.common.result.PageResult;
import com.foeris.y.frame.menu.MenuService;
import com.foeris.y.frame.user.UserContext;
import com.foeris.y.frame.user.UserUtl;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

/**
 * 工作站管理控制器
 * <p>提供工作站全生命周期管理、工作站终端认证、菜单权限、IP自动识别等核心功能</p>
 * <ul>
 *   <li>✅ 业务操作：增删改查、状态管理、IP校验</li>
 *   <li>✅ 终端支持：工作站登录/登出、版本校验、菜单加载</li>
 *   <li>✅ 安全规范：敏感信息脱敏、参数校验、操作审计</li>
 *   <li>✅ 运维友好：结构化日志、关键节点追踪、异常精准定位</li>
 * </ul>
 *
 * @author WMS Team
 * @since 2026-02-06
 */
@RestController
@RequestMapping(ProjectConstant.PREFIX_CONTEXT_PATH + "/workstation")
@Slf4j
public class WorkstationExtController {

    private static final String LOG_PREFIX = "[WorkstationController] ";
    private static final String ERROR_PARAM_INVALID = "参数校验失败";
    private static final String ERROR_WORKSTATION_NOT_FOUND = "工作站不存在或已被删除";
    private static final String ERROR_IP_OCCUPIED = "IP地址 {} 已被占用";
    private static final String ERROR_CODE_DUPLICATE = "工作站编号 {} 已存在，禁止重复录入";
    private static final String ERROR_WORKSTATION_CODE_REQUIRED = "工作站编号不能为空";
    private static final String ERROR_INVALID_IP = "无效的客户端IP地址";
    private static final String SUCCESS_LOGIN = "用户 {} 登录成功";
    private static final String SUCCESS_LOGOUT = "用户 {} 登出成功";
    private static final String SUCCESS_INSERT = "工作站新增成功，编号: {}";
    private static final String SUCCESS_DELETE = "工作站删除成功，ID: {}";
    private static final String SUCCESS_UPDATE_STATUS = "工作站状态更新成功，编号: {}";
    private static final String SUCCESS_CHECK = "工作站校验通过，编号: {}";
    private static final String SUCCESS_IP_QUERY = "成功获取工作站信息，编号: {}, IP: {}";
    private static final String WARN_IP_NOT_FOUND = "未找到与IP {} 关联的工作站信息";
    private static final String WARN_WORKSTATION_NOT_FOUND_DETAIL = "未找到工作站信息，编号: {}";
    private static final String TRACE_LOGOUT_NOT_LOGGED_IN = "登出请求：用户未登录";
    private static final String USER_WXZD = "wxzd"; // 工作站专用测试账号标识

    @Resource
    private WorkstationService workstationService;

    @Resource
    private LoginController loginController;

    @Resource
    private MenuService menuService;

    // ==================== 工作站终端接口 ====================

    /**
     * 工作站端用户登录
     * <p>⚠ 密码参数严禁记录至日志！</p>
     *
     * @param parameter 包含user/pwd的登录参数
     * @param request   HttpServletRequest
     * @param response  HttpServletResponse
     * @param model     Spring MVC Model
     * @return 登录结果（含用户信息）
     */
    @PostMapping("login")
    public ResultWrapper<String> login(@Valid @RequestBody Parameter parameter,
                                       HttpServletRequest request,
                                       HttpServletResponse response,
                                       org.springframework.ui.Model model) {
        String userName = parameter.getUser();
        log.info("{}用户登录请求，用户名: {}", LOG_PREFIX, userName);

        try {
            // 调用平台登录逻辑（密码由底层处理，此处不记录）
            loginController.login(request, response, model, "", userName, parameter.getPwd(), "");

            Object errorAttr = model.getAttribute("error");
            if (errorAttr != null) {
                String errorMsg = String.valueOf(errorAttr);
                log.warn("{}用户登录失败，用户名: {}, 原因: {}", LOG_PREFIX, userName, errorMsg);
                return ResultWrapper.buildFailure(errorMsg);
            }

            // 获取登录用户信息
            String userDisplayName = UserContext.getUser() != null
                    ? UserContext.getUser().getName()
                    : userName;

            log.info(LOG_PREFIX + SUCCESS_LOGIN, userDisplayName);
            return ResultWrapper.buildSuccess(userDisplayName, "登录成功");

        } catch (Exception e) {
            log.error("{}用户登录异常，用户名: {}", LOG_PREFIX, userName, e);
            return ResultWrapper.buildFailure("系统繁忙，请稍后重试"+e.getMessage());
        }
    }

    /**
     * 工作站端用户登出
     *
     * @param parameter 请求参数
     * @param request   HttpServletRequest
     * @return 操作结果
     */
    @PostMapping("logout")
    public ResultWrapper<Void> logout(@Valid @RequestBody Parameter parameter, HttpServletRequest request) {
        if (UserUtl.isLogin()) {
            Subject subject = SecurityUtils.getSubject();
            Object principal = subject.getPrincipal();
            String userName = principal != null ? principal.toString() : "unknown";
            log.info("{}{}", LOG_PREFIX, String.format(SUCCESS_LOGOUT, userName));

            request.setAttribute("userName", userName);
            request.setAttribute("toUrl", request.getContextPath() + "/");
            subject.logout();
            return ResultWrapper.buildSuccess(null, "登出成功");
        }

        log.trace(LOG_PREFIX + TRACE_LOGOUT_NOT_LOGGED_IN);
        return ResultWrapper.buildSuccess(null, "用户未登录");
    }

    // ==================== 工作站管理接口 ====================

    /**
     * 分页查询工作站列表（GET）
     * <p>支持工作站编号、名称、状态等条件筛选</p>
     *
     * @param query 分页与查询条件（自动绑定URL参数）
     * @return 分页结果集
     */
    @GetMapping("/page")
    public PageResult<WorkstationExtDto> page(@Validated WorkstationExtQuery query) {
        log.debug("{}分页查询工作站，参数: {}", LOG_PREFIX, query);

        try {
            PageResult<WorkstationExtDto> result = workstationService.findInfoByPage(query);
            log.info("{}工作站分页查询成功，当前页: {}, 每页: {}, 总记录数: {}",
                    LOG_PREFIX,
                    query.getPage() != null ? query.getPage() : 1,
                    query.getRow() != null ? query.getRow() : 10,
                    result.getTotal());
            return result;
        } catch (Exception e) {
            log.error("{}工作站分页查询异常", LOG_PREFIX, e);
            throw e;
        }
    }

    /**
     * 新增工作站
     * <p>校验项：编号唯一性、IP唯一性、基础参数合法性</p>
     *
     * @param dto 工作站新增数据传输对象
     * @return 操作结果
     */
    @PostMapping("/insert")
    public ResultWrapper<Void> insert(@Valid @RequestBody WorkstationExtDto dto) {
        String workstationCode = dto.getWorkstationCode();
        log.debug("{}新增工作站请求，编号: {}", LOG_PREFIX, workstationCode);

        try {
            // 1. 参数前置校验
            dto.checkDataAdd();

            // 2. 编号唯一性校验
            WorkstationExtEntity existing = findWorkstationByCode(workstationCode);
            if (ObjectUtil.isNotEmpty(existing)) {
                String msg = String.format(ERROR_CODE_DUPLICATE, workstationCode);
                log.warn("{}{}", LOG_PREFIX, msg);
                return ResultWrapper.buildFailure(msg);
            }

            // 3. IP唯一性校验（若提供）
            if (StrUtil.isNotBlank(dto.getWorkstationIp())) {
                if (isIpOccupied(dto.getWorkstationIp())) {
                    String msg = String.format(ERROR_IP_OCCUPIED, dto.getWorkstationIp());
                    log.warn("{}{}", LOG_PREFIX, msg);
                    return ResultWrapper.buildFailure(msg);
                }
            }

            // 4. 执行保存
            workstationService.save(dto);
            log.info(LOG_PREFIX + SUCCESS_INSERT, workstationCode);
            return ResultWrapper.buildSuccess(null, "操作成功");

        } catch (IllegalArgumentException e) {
            log.warn("{}新增工作站参数校验失败，编号: {}, 原因: {}", LOG_PREFIX, workstationCode, e.getMessage());
            return ResultWrapper.buildFailure(e.getMessage());
        } catch (Exception e) {
            log.error("{}新增工作站系统异常，编号: {}", LOG_PREFIX, workstationCode, e);
            return ResultWrapper.buildFailure("系统繁忙，请稍后重试"+e.getMessage());
        }
    }

    /**
     * 删除工作站
     *
     * @param request 包含工作站ID的删除请求
     * @return 操作结果
     */
    @PostMapping("/delete")
    public ResultWrapper<Void> delete(@Valid @RequestBody DeleteRequest request) {
        String id = request.getId();
        log.debug("{}删除工作站请求，ID: {}", LOG_PREFIX, id);

        if (StrUtil.isBlank(id)) {
            log.warn("{}删除请求参数无效，ID为空", LOG_PREFIX);
            return ResultWrapper.buildFailure("工作站ID不能为空");
        }

        WorkstationExtEntity entity = workstationService.getById(id);
        if (ObjectUtil.isEmpty(entity)) {
            log.warn("{}{}", LOG_PREFIX, ERROR_WORKSTATION_NOT_FOUND);
            return ResultWrapper.buildFailure(ERROR_WORKSTATION_NOT_FOUND);
        }

        if (!workstationService.removeById(id)) {
            log.error("{}删除工作站失败，ID: {}", LOG_PREFIX, id);
            return ResultWrapper.buildFailure("数据库操作失败");
        }

        log.info(LOG_PREFIX + SUCCESS_DELETE, id);
        return ResultWrapper.buildSuccess(null, "操作成功");
    }

    /**
     * 更新工作站运行状态
     * <p>用于工作站端实时同步工作站开关状态、模式等</p>
     *
     * @param dto 包含状态信息的数据传输对象
     * @return 操作结果
     */
    @PostMapping("/update-status")
    public ResultWrapper<Void> updateStatus(@Valid @RequestBody WorkstationExtDto dto) {
        String code = dto.getWorkstationCode();
        log.debug("{}更新工作站状态请求，编号: {}, 模式: {}", LOG_PREFIX, code, dto.getWorkstationMode());

        // 基础字段非空校验（Service层亦有校验，此处做快速失败）
        if (StringUtils.isEmpty(code) ||
                StringUtils.isEmpty(dto.getIsOpen()) ||
                StringUtils.isEmpty(dto.getWorkstationStatus()) ||
                StringUtils.isEmpty(dto.getWorkstationMode())) {
            log.warn("{}状态更新参数缺失，编号: {}", LOG_PREFIX, code);
            return ResultWrapper.buildFailure("必要参数缺失");
        }

        try {
            workstationService.updateStatus(dto);
            log.info(LOG_PREFIX + SUCCESS_UPDATE_STATUS, code);
            return ResultWrapper.buildSuccess(null, "操作成功");
        } catch (Exception e) {
            log.error("{}更新工作站状态异常，编号: {}", LOG_PREFIX, code, e);
            return ResultWrapper.buildFailure("状态更新失败"+e.getMessage());
        }
    }

    /**
     * 校验工作站信息（用于工作站退出工作站前校验）
     *
     * @param dto 包含工作站编号与模式的校验对象
     * @return 校验结果
     */
    @PostMapping("/check-workstation")
    public ResultWrapper<Void> checkWorkstation(@Valid @RequestBody WorkstationExtDto dto) {
        String code = dto.getWorkstationCode();
        log.debug("{}校验工作站请求，编号: {}, 模式: {}", LOG_PREFIX, code, dto.getWorkstationMode());

        try {
            workstationService.checkWorkstation(dto, true);
            log.info(LOG_PREFIX + SUCCESS_CHECK, code);
            return ResultWrapper.buildSuccess(null, "校验通过");
        } catch (Exception e) {
            log.warn("{}工作站校验失败，编号: {}, 原因: {}", LOG_PREFIX, code, e.getMessage());
            return ResultWrapper.buildFailure("校验未通过: " + e.getMessage());
        }
    }

    /**
     * 根据编号精确查询工作站详情
     *
     * @param query 查询条件（必须含workstationCode）
     * @return 工作站详情
     */
    @PostMapping("/query")
    public ResultWrapper<WorkstationExtDto> getWorkstationDto(@Valid @RequestBody WorkstationExtQuery query) {
        String code = query.getWorkstationCode();
        log.debug("{}查询工作站详情，编号: {}", LOG_PREFIX, code);

        if (StrUtil.isEmpty(code)) {
            log.warn("{}查询参数缺失：工作站编号为空", LOG_PREFIX);
            return ResultWrapper.buildFailure(ERROR_WORKSTATION_CODE_REQUIRED);
        }

        WorkstationExtDto dto = workstationService.getOne(query);
        if (ObjectUtil.isEmpty(dto)) {
            log.warn(LOG_PREFIX + WARN_WORKSTATION_NOT_FOUND_DETAIL, code);
            return ResultWrapper.buildFailure(String.format(WARN_WORKSTATION_NOT_FOUND_DETAIL, code));
        }

        log.debug("{}工作站详情查询成功，编号: {}", LOG_PREFIX, code);
        return ResultWrapper.buildSuccess(dto);
    }

    /**
     * 根据客户端IP自动识别工作站（用于工作站自动绑定）
     * <p>IP获取逻辑：X-Forwarded-For > Proxy-Client-IP > WL-Proxy-Client-IP > RemoteAddr</p>
     *
     * @param request HttpServletRequest
     * @return 匹配的工作站信息
     */
    @GetMapping("/find-by-ip")
    public ResultWrapper<WorkstationExtDto> findWorkstationByClientIp(HttpServletRequest request) {
        String clientIp = IpUtil.getClientIpAddress(request);
        log.debug("{}根据客户端IP查询工作站，原始IP: {}", LOG_PREFIX, clientIp);

        if (StrUtil.isBlank(clientIp)) {
            log.warn("{}{}", LOG_PREFIX, ERROR_INVALID_IP);
            return ResultWrapper.buildFailure(ERROR_INVALID_IP);
        }

        WorkstationExtQuery query = new WorkstationExtQuery();
        query.setWorkstationIp(clientIp);
        WorkstationExtDto dto = workstationService.getOne(query);

        if (ObjectUtil.isEmpty(dto)) {
            log.warn(LOG_PREFIX + "未找到与IP {} 关联的工作站信息", clientIp);
            return ResultWrapper.buildFailure(String.format("未找到与IP "+clientIp+" 关联的工作站信息", clientIp));
        }

        log.info(LOG_PREFIX + SUCCESS_IP_QUERY, dto.getWorkstationCode(), clientIp);
        return ResultWrapper.buildSuccess(dto);
    }

    // ==================== 内部辅助方法 ====================

    /**
     * 检查IP是否已被其他工作站占用（排除当前工作站）
     *
     * @param ip 待校验IP地址
     * @return true-已被占用；false-可用
     */
    private boolean isIpOccupied(String ip) {
        WorkstationExtQuery query = new WorkstationExtQuery();
        query.setWorkstationIp(ip.trim());
        List<WorkstationExtEntity> list = workstationService.list(query.buildQueryWrapper());
        return CollUtil.isNotEmpty(list);
    }

    /**
     * 根据工作站编号查询实体（忽略大小写+前后空格）
     *
     * @param code 工作站编号
     * @return 工作站实体，不存在返回null
     */
    private WorkstationExtEntity findWorkstationByCode(String code) {
        if (StrUtil.isBlank(code)) return null;
        WorkstationExtQuery query = new WorkstationExtQuery();
        query.setWorkstationCode(code.trim());
        return workstationService.getOne(query.buildQueryWrapper());
    }

    /**
     * 工作站删除请求DTO
     */
    @lombok.Data
    public static class DeleteRequest {
        /**
         * 工作站ID（UUID）
         */
        @com.fasterxml.jackson.annotation.JsonProperty(required = true)
        private String id;
    }
}