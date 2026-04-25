package cn.zdjc.wms.project.repository.asn;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.zdjc.warehouse.WmsConfig;
import cn.zdjc.warehouse.inventory.infrastructure.enums.QualityStatus;

import cn.zdjc.wms.project.domain.entity.asn.AsnItemExtEntity;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.map.MapWrap;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class AsnItemExtRepository extends DomainRepository<AsnItemExtEntity> {
    public List<AsnItemExtEntity> queryByAsnId(String asnId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(AsnItemExtEntity.class)).append(" from ").append(SqlUtl.getTable(AsnItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and asn_id = :asnId");
        Map<String, Object> params = new HashMap<>();
        params.put("asnId", asnId);

        return DatabaseExecuter.queryBeanList(sql, params, AsnItemExtEntity.class);
    }

    public List<AsnItemExtEntity> queryByInspectionLotNumber(String inspectionLotNumber) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(AsnItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(AsnItemExtEntity.class))
                .append(" where is_active = 1 ").append(" and inspection_lot_number = :inspectionLotNumber ");
        Map<String, Object> params = new HashMap<>();
        params.put("inspectionLotNumber", inspectionLotNumber);
        return DatabaseExecuter.queryBeanList(sql, params, AsnItemExtEntity.class);
    }

    public List<AsnItemExtEntity> queryByConfirmStatus(String confirmStatus) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(AsnItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(AsnItemExtEntity.class))
                .append(" where 1 = 1 ").append(" and confirm_status = :confirmStatus ");
        Map<String, Object> params = new HashMap<>();
        params.put("confirmStatus", confirmStatus);
        return DatabaseExecuter.queryBeanList(sql, params, AsnItemExtEntity.class);
    }

    public void updateConfirmStatus(List<String> ids, String confirmStatus) {
        String sql = " update $table set confirm_status=:confirm_status where id in (:id) ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(AsnItemExtEntity.class))
                .put("confirm_status", confirmStatus)
                .put("id", ids)
                .getMap());
    }

    public void updateConfirmUser(List<String> ids, String confirmUser) {
        if (CollUtil.isEmpty(ids)) {
            return;
        }
        String sql = " update $table set confirm_user=:confirm_user where id in (:id) ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(AsnItemExtEntity.class))
                .put("confirm_user", confirmUser)
                .put("id", ids)
                .getMap());
    }

    public List<AsnItemExtEntity> queryByFormNoAndItemNo(String fromNo, String itemNo, String whse) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(AsnItemExtEntity.class))
        		.append(" from ").append(SqlUtl.getTable(AsnItemExtEntity.class))
                .append(" where 1=1");
        Map<String, Object> params = new HashMap<>();
        if (!StringUtl.isEmpty(fromNo)) {
            sql.append(" and asn_no = :fromNo");
            params.put("fromNo", fromNo);
        }
        if (!StringUtl.isEmpty(itemNo)) {
            sql.append(" and item_no = :itemNo");
            params.put("itemNo", itemNo);
        }
        if (params.isEmpty()) {//2025-3-6 jzx fix 出现没有传值情况，应该是垃圾数据，导致表的全部记录都返回了
        	throw new IllegalArgumentException("fromNo和itemNo参数都是空，必须传一个");
        }
        if (!StringUtl.isEmpty(whse)) {
            sql.append(" and whse = :whse");
            params.put("whse", whse);
        }
        return DatabaseExecuter.queryBeanList(sql, params, AsnItemExtEntity.class);
    }
    public AsnItemExtEntity queryByFormNoAndItemNoOne(String formNo, String itemNo, String wareHouse) {
        Assert.notNull(formNo, "asnNo不能为空");
        Assert.notNull(itemNo, "itemNo不能为空");
        // 初始化StringBuilder用于构建查询语句
        StringBuilder sql = new StringBuilder("SELECT ")
                .append(SqlUtl.getColumns(AsnItemExtEntity.class))
                .append(" FROM ")
                .append(SqlUtl.getTable(AsnItemExtEntity.class))
                .append(" WHERE 1=1");
        // 使用Map存储查询参数
        Map<String, Object> params = new HashMap<>();
        sql.append(" AND asn_no = :formNo");
        params.put("formNo", formNo);
        sql.append(" AND item_no = :itemNo");
        params.put("itemNo", itemNo);
        if (isNotEmpty(wareHouse)) {
            sql.append(" AND whse = :whse");
            params.put("whse", wareHouse);
        }
        // 添加LIMIT 1以限制结果数量
        sql.append(" LIMIT 1");
        return DatabaseExecuter.queryBeanListFirst(sql, params, AsnItemExtEntity.class);
    }

    // 辅助方法，简化字符串非空检查
    private boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }



    public List<AsnItemExtEntity> queryCrossDockingItemList(String whse, String skuCode, String factory, QualityStatus qualityStatus,
                                                            String specialStockIndicator, String specialStockNbr,
                                                            String inventoryArea, String crossDockingQty) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(AsnItemExtEntity.class))
                .append(" from ").append(SqlUtl.getTable(AsnItemExtEntity.class))
                .append(" where is_active = 1 ")
                .append(" and hedge_status = 'N' ")
                .append(" and house_code = :houseCode ")
                .append(" and sku_code = :skuCode ")
                .append(" and quality_status = :qualityStatus ")
                .append(" and cross_docking_qty = :crossDockingQty ");

        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", WmsConfig.DefaultHouseCode);
        params.put("skuCode", skuCode);
        params.put("qualityStatus", qualityStatus);
        params.put("crossDockingQty", crossDockingQty);

        if (Objects.nonNull(whse)) {
            sql.append(" and whse = :whse ");
            params.put("whse", whse);
        } else {
            sql.append(" and whse is null ");
        }

        if (Objects.nonNull(factory)) {
            sql.append(" and factory = :factory ");
            params.put("factory", factory);
        } else {
            sql.append(" and factory is null ");
        }

        if (Objects.nonNull(specialStockIndicator)) {
            sql.append(" and special_stock_indicator = :specialStockIndicator ");
            params.put("specialStockIndicator", specialStockIndicator);
        } else {
            sql.append(" and special_stock_indicator is null ");
        }

        if (Objects.nonNull(specialStockNbr)) {
            sql.append(" and special_stock_nbr = :specialStockNbr ");
            params.put("specialStockNbr", specialStockNbr);
        } else {
            sql.append(" and special_stock_nbr is null ");
        }

        if (Objects.nonNull(inventoryArea)) {
            sql.append(" and inventory_area = :inventoryArea ");
            params.put("inventoryArea", inventoryArea);
        } else {
            sql.append(" and inventory_area is null ");
        }

        return DatabaseExecuter.queryBeanList(sql, params, AsnItemExtEntity.class);
    }

    public List<AsnItemExtEntity> queryByPeriod(String startTime, String endTime) {
        StringBuilder sql = new StringBuilder();
        sql.append("select * from wms_asn_item ")
                .append(" where is_active = 1 ")
                .append(" and create_datetime >= :startTime and create_datetime <= :endTime ");

        Map<String, Object> params = new HashMap<>();
        params.put("startTime", startTime);
        params.put("endTime", endTime);

        return DatabaseExecuter.queryBeanList(sql, params, AsnItemExtEntity.class);
    }

}
