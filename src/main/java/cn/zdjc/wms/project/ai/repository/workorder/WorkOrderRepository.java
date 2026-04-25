package cn.zdjc.wms.project.ai.repository.workorder;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 工单数据访问层 - 负责调度、任务相关查询
 */
@Repository
@Slf4j
public class WorkOrderRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public WorkOrderRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * 查询调度信息列表
     */
    public List<Map<String, Object>> queryDispatchInfoList(String keyword) {
        String sql = "SELECT di.id, di.dispatch_no, di.dispatch_status, di.house_code, " +
                "di.create_datetime, di.plan_start_time, di.plan_end_time, " +
                "di.actual_start_time, di.actual_end_time, di.remark " +
                "FROM wms_dispatch_info di WHERE 1=1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (di.dispatch_no LIKE :keyword OR di.house_code LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY di.create_datetime DESC";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询调度信息失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询调度任务列表
     */
    public List<Map<String, Object>> queryDispatchJobList(String keyword) {
        String sql = "SELECT dj.id, dj.dispatch_no, dj.job_type, dj.job_status, " +
                "dj.sku_code, dj.primary_qty, dj.container_code, dj.loc_no, " +
                "dj.workstation_code, dj.create_datetime, dj.finish_datetime " +
                "FROM wms_dispatch_job dj WHERE 1=1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (dj.dispatch_no LIKE :keyword OR dj.sku_code LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY dj.create_datetime DESC";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询调度任务失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询排程管理列表
     */
    public List<Map<String, Object>> querySchedulerManageList(String keyword) {
        String sql = "SELECT sm.id, sm.scheduler_no, sm.scheduler_type, sm.scheduler_status, " +
                "sm.house_code, sm.zone_name, sm.start_time, sm.end_time, " +
                "sm.create_datetime, sm.remark " +
                "FROM wms_scheduler_manage sm WHERE 1=1";
        MapSqlParameterSource params = new MapSqlParameterSource();
        if (keyword != null && !keyword.isEmpty()) {
            sql += " AND (sm.scheduler_no LIKE :keyword OR sm.house_code LIKE :keyword)";
            params.addValue("keyword", "%" + keyword + "%");
        }
        sql += " ORDER BY sm.create_datetime DESC";
        try {
            return jdbcTemplate.queryForList(sql, params);
        } catch (Exception e) {
            log.error("查询排程管理失败 keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }
}
