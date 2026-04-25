package cn.zdjc.wms.project.repository.pick;

import cn.zdjc.wms.outbound.pick.infrastructure.enums.PickStatus;
import cn.zdjc.wms.project.domain.entity.pick.PickItemExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.exception.BusinessException;
import org.springframework.stereotype.Repository;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PickItemExtRepository extends DomainRepository<PickItemExtEntity> {

    public List<PickItemExtEntity> queryByRelationItemId(String relationItemId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PickItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(PickItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and relation_item_id = :relationItemId ");
        Map<String, Object> params = new HashMap<>();
        params.put("relationItemId", relationItemId);
        return DatabaseExecuter.queryBeanList(sql, params, PickItemExtEntity.class);
    }

    public List<PickItemExtEntity> listByIds(List<String> ids) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PickItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(PickItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and id in (:ids) ");
        Map<String, Object> params = new HashMap<>();
        params.put("ids", ids);
        return DatabaseExecuter.queryBeanList(sql, params, PickItemExtEntity.class);
    }

    public List<PickItemExtEntity> queryByRelationItemId(List<String> relationItemIdList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PickItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(PickItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and relation_item_id in (:relationItemIdList) ");
        Map<String, Object> params = new HashMap<>();
        params.put("relationItemIdList", relationItemIdList);
        return DatabaseExecuter.queryBeanList(sql, params, PickItemExtEntity.class);
    }

    public PickItemExtEntity queryByOutboundIdAndContainerCode(String outboundId, String containerCode) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PickItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(PickItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and outbound_id = :outboundId ")
                .append(" and container_code = :containerCode ");
        Map<String, Object> params = new HashMap<>();
        params.put("outboundId", outboundId);
        params.put("containerCode", containerCode);
        List<PickItemExtEntity> list = DatabaseExecuter.queryBeanList(sql, params, PickItemExtEntity.class);
        if (list.isEmpty()) {
            return null;
        }
        if (list.size() > 1) {
            throw new BusinessException("下架单ID[" + outboundId + "],容器号[" + containerCode + "],在PickItemExtEntity中非唯一!");
        }
        return list.get(0);
    }

    public List<PickItemExtEntity> queryByStorageMaterialId(String storageMaterialId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PickItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(PickItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and storage_material_id = :storageMaterialId ");
        Map<String, Object> params = new HashMap<>();
        params.put("storageMaterialId", storageMaterialId);
        return DatabaseExecuter.queryBeanList(sql, params, PickItemExtEntity.class);
    }

    public List<PickItemExtEntity> queryByContainerCode(String containerCode, List<PickStatus> pickStatusList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PickItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(PickItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and container_code = :containerCode ")
                .append(" and pick_status in (:pickStatusList) ");
        Map<String, Object> params = new HashMap<>();
        params.put("containerCode", containerCode);
        params.put("pickStatusList", pickStatusList);
        return DatabaseExecuter.queryBeanList(sql, params, PickItemExtEntity.class);
    }

    public List<PickItemExtEntity> queryByOutboundId(String outboundId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PickItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(PickItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and outbound_id = :outboundId ");
        Map<String, Object> params = new HashMap<>();
        params.put("outboundId", outboundId);
        return DatabaseExecuter.queryBeanList(sql, params, PickItemExtEntity.class);
    }
}
