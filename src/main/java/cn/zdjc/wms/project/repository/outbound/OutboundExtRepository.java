package cn.zdjc.wms.project.repository.outbound;

import cn.zdjc.wms.transport.out.infrastructure.pojo.OutboundPojo;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.exception.BusinessException;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OutboundExtRepository extends DomainRepository<OutboundPojo> {
    public OutboundPojo queryByFormNo(String formNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(OutboundPojo.class))
                .append(" from ").append(SqlUtl.getTable(OutboundPojo.class))
                .append(" where is_active = 1 ")
                .append(" and form_no = :formNo");
        Map<String, Object> params = new HashMap<>();
        params.put("formNo", formNo);
        List<OutboundPojo> list = DatabaseExecuter.queryBeanList(sql, params, OutboundPojo.class);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException("单据号[" + formNo + "],在下架单中非唯一!");
        }
        return list.get(0);
    }
}
