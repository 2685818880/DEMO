package cn.zdjc.wms.project.repository.requisition;

import cn.zdjc.wms.project.domain.entity.requisition.RequisitionOrderExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.exception.BusinessException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RequisitionOrderExtRepository extends DomainRepository<RequisitionOrderExtEntity> {

    public RequisitionOrderExtEntity queryByHouseCodeAndFormNo(String whse, String formNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionOrderExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionOrderExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and whse = :whse ")
                .append(" and form_no = :formNo ");

        Map<String, Object> params = new HashMap<>();
        params.put("whse", whse);
        params.put("formNo", formNo);
        List<RequisitionOrderExtEntity> list = DatabaseExecuter.queryBeanList(sql, params, RequisitionOrderExtEntity.class);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException("whse[" + whse + "],formNo[" + formNo + "],在RequisitionOrderExtEntity中非唯一!");
        }
        return list.get(0);
    }

    public List<RequisitionOrderExtEntity> queryByWaveConvertStatus(String waveConvertStatus) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionOrderExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionOrderExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and wave_convert_status = :waveConvertStatus ")
                .append(" order by emergency_status desc, wave_time asc");

        Map<String, Object> params = new HashMap<>();
        params.put("waveConvertStatus", waveConvertStatus);

        return DatabaseExecuter.queryBeanList(sql, params, RequisitionOrderExtEntity.class);
    }

    public List<RequisitionOrderExtEntity> queryByWaveConvertStatus(String waveConvertStatus, Integer count) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionOrderExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionOrderExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and wave_convert_status = :waveConvertStatus ")
                .append(" order by emergency_status desc, wave_time asc")
                .append(" limit :count");

        Map<String, Object> params = new HashMap<>();
        params.put("waveConvertStatus", waveConvertStatus);
        params.put("count", count);

        return DatabaseExecuter.queryBeanList(sql, params, RequisitionOrderExtEntity.class);
    }

    public List<RequisitionOrderExtEntity> queryDivideWorkstation(String workstationCode, String time) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionOrderExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionOrderExtEntity.class))
                .append(" where is_active = 1 ");

        Map<String, Object> map = new HashMap<>();

        if (!StringUtl.isEmpty(workstationCode)) {
            sql.append(" and workstation_code =:workstationCode ");
            map.put("workstationCode", workstationCode);
        } else {
            sql.append(" and (workstation_code is null or  workstation_code = '') ");
        }

        if (!StringUtl.isEmpty(time)) {
            sql.append(" and divide_time =:time ");
            map.put("time", time);
        }

        return DatabaseExecuter.queryBeanList(sql, map, RequisitionOrderExtEntity.class);
    }

    public RequisitionOrderExtEntity queryByFormId(String formId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionOrderExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionOrderExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and id = :formId ");

        Map<String, Object> params = new HashMap<>();
        params.put("formId", formId);
        List<RequisitionOrderExtEntity> list = DatabaseExecuter.queryBeanList(sql, params, RequisitionOrderExtEntity.class);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException("formId[" + formId + "],在RequisitionOrderExtEntity中非唯一!");
        }
        return list.get(0);
    }

    public List<RequisitionOrderExtEntity> queryByFormIds(List<String> formIdList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionOrderExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionOrderExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and id in( :formIdList) ");

        Map<String, Object> params = new HashMap<>();
        params.put("formIdList", formIdList);
        List<RequisitionOrderExtEntity> list = DatabaseExecuter.queryBeanList(sql, params, RequisitionOrderExtEntity.class);
        if (list.isEmpty()) {
            return null;
        }
       return list;
    }

    public RequisitionOrderExtEntity queryByFormNo(String formNo) {

        List<RequisitionOrderExtEntity> list = queryByFormNoList( formNo) ;
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException("formNo[" + formNo + "],在RequisitionOrderExtEntity中非唯一!");
        }
        return list.get(0);
    }
    public List<RequisitionOrderExtEntity> queryByFormNoList(String formNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionOrderExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionOrderExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and form_no = :formNo ");

        Map<String, Object> params = new HashMap<>();
        params.put("formNo", formNo);
        return DatabaseExecuter.queryBeanList(sql, params, RequisitionOrderExtEntity.class);
    }


    public List<RequisitionOrderExtEntity> queryByBusinessFormNoList(String businessFormNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(RequisitionOrderExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(RequisitionOrderExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and business_form_no = :businessFormNo ");

        Map<String, Object> params = new HashMap<>();
        params.put("businessFormNo", businessFormNo);
        List<RequisitionOrderExtEntity> requisitionOrderExtEntities = DatabaseExecuter.queryBeanList(sql, params, RequisitionOrderExtEntity.class);
        return requisitionOrderExtEntities;
    }
}
