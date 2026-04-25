package cn.zdjc.wms.project.repository.asn;

import cn.zdjc.wms.common.utils.ExceptionUtils;
import cn.zdjc.wms.definition.domain.entity.AsnEntity;
import cn.zdjc.wms.definition.infrastructure.enums.AsnStatus;
import cn.zdjc.wms.project.domain.entity.asn.AsnExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.exception.BusinessException;
import com.foreris.eris.common.map.MapWrap;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class AsnExtRepository extends DomainRepository<AsnExtEntity> {
    public AsnExtEntity queryByFormNo(String houseCode, String formNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(AsnExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(AsnExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and whse = :houseCode ")
                .append(" and form_no = :formNo");
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);
        params.put("formNo", formNo);
        List<AsnExtEntity> list =DatabaseExecuter.queryBeanList(sql, params, AsnExtEntity.class);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException("仓库[" + houseCode + "],单据号[" + formNo + "]AsnExtEntity,数据非唯一!");
        }
        return list.get(0);
    }

    /**
     * 查询活动通过收料单号或商业单号
     *
     * @param no 单号
     * @return {@link AsnEntity}
     */
    public AsnExtEntity queryActivityByAsnNoOrBizNo(String no) {
        String sql = "select * " +
                " from $table where (form_no = :no or business_form_no = :no ) and asn_status in (:status) ";
        List<AsnExtEntity> list = DatabaseExecuter.queryBeanList(sql, new MapWrap<String, Object>()
                .put("table", this.tableName)
                .put("no", no)
                .put("status", List.of(AsnStatus.Created, AsnStatus.Executing, AsnStatus.Finished))
                .getMap(), AsnExtEntity.class);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            ExceptionUtils.throwException("收料单[" + no + "]存在多个活动中单据,请检查");
        }
        return list.get(0);
    }

    public AsnExtEntity queryByFormNoAndWhse(String formNo, String whse) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(AsnExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(AsnExtEntity.class))
                .append(" where is_active = 1 ");
        Map<String, Object> params = new HashMap<>();

        if (!StringUtl.isEmpty(formNo)) {
            sql.append(" and form_no = :fromNo ");
            params.put("fromNo", formNo);
        }
        if (!StringUtl.isEmpty(whse)) {
            sql.append(" and whse = :whse ");
            params.put("whse", whse);
        }
        List<AsnExtEntity> list =DatabaseExecuter.queryBeanList(sql, params, AsnExtEntity.class);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException("仓库 whse[" + whse + "],单据号[" + formNo + "]AsnExtEntity,数据非唯一!");
        }
        return list.get(0);
    }
}
