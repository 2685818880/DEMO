package cn.zdjc.wms.project.repository.material;

import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;

import cn.zdjc.wms.project.domain.entity.material.StorageMaterialExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.map.MapWrap;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class StorageMaterialExtRepository extends DomainRepository<StorageMaterialExtEntity> {
    public List<StorageMaterialExtEntity> queryByInspectionLotNumberAndQualityStatus(String inspectionLotNumber, QualityStatus qualityStatus) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageMaterialExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(StorageMaterialExtEntity.class))
                .append(" where 1 = 1 ")
                .append(" and inspection_lot_number = :inspection_lot_number ")
                .append(" and quality_status = :quality_status ");

        Map<String, Object> params = new HashMap<>();
        params.put("inspection_lot_number", inspectionLotNumber);
        params.put("quality_status", qualityStatus);

        return DatabaseExecuter.queryBeanList(sql, params, StorageMaterialExtEntity.class);
    }

    public void updateQualityStatus(List<String> ids, QualityStatus qualityStatus) {
        String sql = " update $table set quality_status=:quality_status where id in (:id) ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(StorageMaterialExtEntity.class))
                .put("quality_status", qualityStatus)
                .put("id", ids)
                .getMap());
    }

    public List<StorageMaterialExtEntity> queryAvailableStorageMaterialBySkuCode(String skuCode, String factoryCode) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageMaterialExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(StorageMaterialExtEntity.class))
                .append(" where 1 = 1 ")
                .append(" and sku_code = :skuCode ")
                .append(" and quality_status = 'Q' ")
                .append(" and available_qty > 0 ");

        Map<String, Object> params = new HashMap<>();
        params.put("skuCode", skuCode);
        if (!StringUtl.isEmpty(factoryCode)) {
            sql.append(" and factory_code = :factoryCode ");
            params.put("factoryCode", factoryCode);
        }
        return DatabaseExecuter.queryBeanList(sql, params, StorageMaterialExtEntity.class);
    }

    public List<StorageMaterialExtEntity> queryByStorageMaterialId(List<String> storageMaterialIdList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageMaterialExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(StorageMaterialExtEntity.class))
                .append(" where 1 = 1 ")
                .append(" and id in(:storageMaterialIdList) ");

        Map<String, Object> params = new HashMap<>();
        params.put("storageMaterialIdList", storageMaterialIdList);

        return DatabaseExecuter.queryBeanList(sql, params, StorageMaterialExtEntity.class);
    }


}
