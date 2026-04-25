package cn.zdjc.wms.project.repository.requisition;

import cn.hutool.core.collection.CollUtil;
import cn.zdjc.wms.definition.infrastructure.enums.FormStatus;
import cn.zdjc.wms.project.domain.entity.requisition.RequisitionItemExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.exception.BusinessException;
import com.foreris.eris.common.map.MapWrap;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class RequisitionItemExtRepository extends DomainRepository<RequisitionItemExtEntity> {
    public List<RequisitionItemExtEntity> queryByConfirmStatus(String confirmStatus) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where confirm_status = :confirmStatus ");
        Map<String, Object> params = new HashMap<>();
        params.put("confirmStatus", confirmStatus);
        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
    }

    public void updateConfirmStatus(List<String> ids, String confirmStatus) {
        String sql = " update $table set confirm_status=:confirm_status where id in (:id) ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(RequisitionItemExtEntity.class))
                .put("confirm_status", confirmStatus)
                .put("id", ids)
                .getMap());
    }

    public void updateConfirmUser(List<String> ids, String confirmUser) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        String sql = " update $table set confirm_user = :confirm_user where id in (:id) ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(RequisitionItemExtEntity.class))
                .put("confirm_user", confirmUser)
                .put("id", ids)
                .getMap());
    }

    public void updateExecutable(List<String> ids, Integer executable) {
        String sql = " update $table set executable=:executable where id in (:id) ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(RequisitionItemExtEntity.class))
                .put("executable", executable)
                .put("id", ids)
                .getMap());
    }

    public List<RequisitionItemExtEntity> queryByFormId(String orderId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and form_id = :formId ")
                .append(" order by create_datetime asc ");
        Map<String, Object> params = new HashMap<>();
        params.put("formId", orderId);
        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
    }

    public List<RequisitionItemExtEntity> queryByFormNoAndItemNo(String formNo, String itemNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and form_no = :formNo ")
                .append(" and item_no = :itemNo ")
                .append(" order by create_datetime asc ");
        Map<String, Object> params = new HashMap<>();
        params.put("formNo", formNo);
        params.put("itemNo", itemNo);
        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
    }

    public RequisitionItemExtEntity queryByWhseAndFormNoAndItemNo(String whse, String formNo, String itemNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and whse = :whse ")
                .append(" and form_no = :formNo ")
                .append(" and item_no = :itemNo ");
        Map<String, Object> params = new HashMap<>();
        params.put("whse", whse);
        params.put("formNo", formNo);
        params.put("itemNo", itemNo);
        List<RequisitionItemExtEntity> list = DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException("whse[{}],formNo[{}],itemNo[{}],在RequisitionItemExtEntity中非唯一!"
            		, whse, formNo, itemNo);
        }
        return list.get(0);
    }

//    public List<RequisitionItemExtEntity> queryWithPickCondition(List<String> waveStatusList, List<String> outStatusList,
//                                                                 List<OrderType> orderTypeList, String emergencyStatus, Integer count) {
//        StringBuilder sql = new StringBuilder();
//        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class,"t1"))
//                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class)).append(" t1 ")
//                .append(" join wms_requisition_order t2 on t1.form_id=t2.id ")
//                .append(" where t1.is_active = 1 ")
//                .append(" and t1.executable = 1 ")
//                .append(" and t1.cross_docking_status = 'N' ")
//                .append(" and t1.wave_status in (:waveStatusList) ")
//                .append(" and t1.out_status in (:outStatusList) ")
//                .append(" and t1.order_type in (:orderTypeList) ")
//                .append(" and t1.emergency_status = :emergencyStatus ")
//                .append(" and t2.form_status not in (:formStatusList) ")
//                .append(" order by t1.missing_status desc, t1.emergency_status desc, t1.wave_time asc ");
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("waveStatusList", waveStatusList);
//        params.put("outStatusList", outStatusList);
//        params.put("orderTypeList", orderTypeList);
//        params.put("emergencyStatus", emergencyStatus);
//        params.put("formStatusList", Arrays.asList(FormStatus.Finished,FormStatus.Closed,FormStatus.Invalid));
//        if (Objects.nonNull(count)) {
//            sql.append(" limit :count");
//            params.put("count", count);
//        }
//
//        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
//    }

    public List<RequisitionItemExtEntity> queryWithAllotCondition(String waveStatus, List<String> outStatusList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and executable = 1 ")
                .append(" and cross_docking_status = 'N' ")
                .append(" and wave_status = :waveStatus ")
                .append(" and out_status in (:outStatusList) ")
                .append(" order by missing_status desc, emergency_status desc, wave_time asc, out_status asc ");

        Map<String, Object> params = new HashMap<>();
        params.put("waveStatus", waveStatus);
        params.put("outStatusList", outStatusList);

        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
    }

    public List<RequisitionItemExtEntity> queryByIdList(List<String> idList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and id in (:idList) ");
        Map<String, Object> params = new HashMap<>();
        params.put("idList", idList);

        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
    }

    public List<RequisitionItemExtEntity> queryByPeriod(String startTime, String endTime) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
        		.append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and create_datetime >= :startTime and create_datetime <= :endTime ");

        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
    }


    /**
     * 成品cot超时单子
     *
     * @param time
     * @return
     */
    public List<RequisitionItemExtEntity> queryCotTimeoutItem(String time) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
        		.append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
        		.append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and order_type = 'PP' and primary_qty > confirm_qty and  wave_time <= :time ");

        Map<String, Object> params = new HashMap<>();
        params.put("time", time);

        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
    }

    public void updateRequisitionOutStatus(String outStatus, String... ids) {
        String sql = " update $table set out_status=:outStatus where id IN(:id)";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(RequisitionItemExtEntity.class))
                .put("outStatus", outStatus)
                .put("id", Arrays.asList(ids))
                .getMap());
    }

    public List<RequisitionItemExtEntity> lockItem(String id) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ")
        		.append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and id=:id limit 1 FOR UPDATE ");

        Map<String, Object> params = new HashMap<>();
        params.put("id", id);

        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
    }

    public void updateConfirmQty(Double qty, String requisitionItemId) {
        String sql = " update $table set confirm_qty=:qty+confirm_qty where id =:id ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(RequisitionItemExtEntity.class))
                .put("qty", qty)
                .put("id", requisitionItemId)
                .getMap());
    }

//    public List<RequisitionItemExtEntity> queryByFormTypeAndWaveStatus(OrderType orderType, RequisitionItemWaveStatus requisitionItemWaveStatus) {
//        String sql = "select " + SqlUtl.getColumns(RequisitionItemExtEntity.class)
//                +" from " + SqlUtl.getTable(RequisitionItemExtEntity.class)
//        		+ " where order_type =:orderType and wave_status =:waveStatus"
//        		+ " and exists(select 2 from wms_requisition_order ro where form_id=ro.id)";
//
//        Map<String, Object> params = new HashMap<>();
//        params.put("orderType", orderType);
//        params.put("waveStatus", requisitionItemWaveStatus);
//
//        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
//    }
//    public List<RequisitionItemExtEntity> queryByConfirmStatusAndOrderType(OrderType orderType,
//                                                                           RequisitionItemWaveStatus requisitionItemWaveStatus,
//                                                                           String confirmStatus) {
//        StringBuilder sql = new StringBuilder();
//        sql.append("select ").append(SqlUtl.getColumns(RequisitionItemExtEntity.class))
//                .append(" from ").append(SqlUtl.getTable(RequisitionItemExtEntity.class))
//                .append(" where confirm_status = :confirmStatus " +
//                        " and order_type =:orderType " +
//                        " and wave_status =:waveStatus "+
//                        " and exists(select 2 from wms_requisition_order ro where form_id=ro.id)");
//        Map<String, Object> params = new HashMap<>(2);
//        params.put("confirmStatus", confirmStatus);
//        params.put("orderType", orderType);
//        params.put("waveStatus", requisitionItemWaveStatus);
//        return DatabaseExecuter.queryBeanList(sql, params, RequisitionItemExtEntity.class);
//    }
}
