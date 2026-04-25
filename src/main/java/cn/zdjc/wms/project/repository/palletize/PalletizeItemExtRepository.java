package cn.zdjc.wms.project.repository.palletize;

import cn.zdjc.wms.definition.domain.entity.PalletizeItem;
import cn.zdjc.wms.definition.infrastructure.enums.PalletizeFormStatus;
import cn.zdjc.wms.project.domain.entity.palletize.PalletizeItemExtEntity;
import cn.zdjc.wms.project.domain.entity.sku.SkuExtEntity;
import cn.zdjc.wms.project.domain.query.palletize.PalletizeItemExtQuery;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.exception.BusinessException;
import com.foreris.eris.common.map.MapWrap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public class PalletizeItemExtRepository extends DomainRepository<PalletizeItemExtEntity> {
    public List<PalletizeItemExtEntity> queryByAsnFormNoAndItemNo(String formNo, String itemNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PalletizeItem.class))
                .append(" from ").append(SqlUtl.getTable(PalletizeItem.class))
                .append(" where is_active = 1 ")
                .append(" and asn_form_no = :formNo ")
                .append(" and asn_item_no = :itemNo ");
        Map<String, Object> params = new HashMap<>();
        params.put("formNo", formNo);
        params.put("itemNo", itemNo);

        return DatabaseExecuter.queryBeanList(sql, params, PalletizeItemExtEntity.class);
    }

    public List<PalletizeItemExtEntity> queryByPalletizeItemAndForm(PalletizeItemExtQuery query) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PalletizeItemExtEntity.class, " s "))
                .append(" from ").append(SqlUtl.getTable(PalletizeItemExtEntity.class)).append(" s ")
                .append(" join wms_palletize_form u on s.palletize_form_id = u.id ")
                .append(" where 1 = 1 ");
        Map<String, Object> params = new HashMap<>();
        List<PalletizeFormStatus> palletizeFormStatusList = query.getPalletizeFormStatusList();
        if(CollectionUtils.isNotEmpty(palletizeFormStatusList)){
            sql.append(" and u.palletize_form_status in( :palletizeFormStatus) ");
            params.put("palletizeFormStatus", palletizeFormStatusList);
        }
        String containerCode = query.getContainerCode();
        if(StringUtils.isNotBlank(containerCode)){
            sql.append(" and s.container_code = :containerCode ");
            params.put("containerCode", containerCode);
        }
        List<String> containerCodeList = query.getContainerCodeList();
        if(CollectionUtils.isNotEmpty(containerCodeList)){
            sql.append(" and u.container_code in( :containerCodeList) ");
            params.put("containerCodeList", containerCodeList);
        }

        return DatabaseExecuter.queryBeanList(sql, params, PalletizeItemExtEntity.class);
    }

    public List<PalletizeItemExtEntity> queryByStorageMaterialId(String storageMaterialId) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(PalletizeItem.class))
                .append(" from ").append(SqlUtl.getTable(PalletizeItem.class))
                .append(" where is_active = 1 ")
                .append(" and storage_material_id = :storageMaterialId ");
        Map<String, Object> params = new HashMap<>();
        params.put("storageMaterialId", storageMaterialId);

        return DatabaseExecuter.queryBeanList(sql, params, PalletizeItemExtEntity.class);
    }

}
