package cn.zdjc.wms.project.repository.sku;

import cn.zdjc.wms.project.domain.entity.sku.SkuExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.exception.BusinessException;
import com.foreris.eris.common.map.MapWrap;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Repository
public class SkuExtRepository extends DomainRepository<SkuExtEntity> {

    public SkuExtEntity queryBySkuCode(String skuCode) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(SkuExtEntity.class, " s "))
                .append(" from ").append(SqlUtl.getTable(SkuExtEntity.class)).append(" s ")
                .append(" left join wms_base_code_unit_group u ")
                .append(" on s.unit_plan_id = u.id ")
                .append(" where s.sku_code = :sku_code ");

        List<SkuExtEntity> skuList = DatabaseExecuter.queryBeanList(sql,
                new MapWrap<String, Object>().put("sku_code", skuCode).getMap(), SkuExtEntity.class);

        if (CollectionUtils.isEmpty(skuList)) {
            return null;
        }

        if (skuList.size() > 1) {
            throw new BusinessException("SKU[" + skuCode + "]表中非唯一");
        }

        return skuList.get(0);
    }

    public void updateRegion(String region, String lastModifyBy, String id) {
        String sql = " update $table set region=:region, last_modify_by=:lastModifyBy where id = :id ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(SkuExtEntity.class))
                .put("region", region)
                .put("lastModifyBy", lastModifyBy)
                .put("id", id)
                .getMap());
    }

    public List<SkuExtEntity> queryByCondition(String skuCode, String skuName, String region) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(SkuExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(SkuExtEntity.class))
                .append(" where 1 = 1 ");

        Map<String, Object> params = new HashMap<>();
        if (!StringUtl.isEmpty(skuCode)) {
            sql.append(" and sku_code like :skuCode ");
            params.put("skuCode", "%" + skuCode + "%");
        }

        if (!StringUtl.isEmpty(skuName)) {
            sql.append(" and sku_name like :skuName ");
            params.put("skuName", "%" + skuName + "%");
        }

        if (!StringUtl.isEmpty(region)) {
            sql.append(" and region = :region ");
            params.put("region", region);
        }

        return DatabaseExecuter.queryBeanList(sql, params, SkuExtEntity.class);
    }


    public List<SkuExtEntity> queryBySkuCodes(List<String> skuCodes) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(SkuExtEntity.class, " s "))
                .append(" from ").append(SqlUtl.getTable(SkuExtEntity.class)).append(" s ")
                .append(" left join wms_base_code_unit_group u ")
                .append(" on s.unit_plan_id = u.id ")
                .append(" where s.sku_code in(:sku_codes) ");


        return DatabaseExecuter.queryBeanList(sql,
                new MapWrap<String, Object>().put("sku_codes", skuCodes).getMap(), SkuExtEntity.class);
    }
}
