package cn.zdjc.wms.project.repository.location;

import cn.hutool.core.lang.Assert;
import cn.zdjc.warehouse.definition.infrastructure.enums.StorageStatus;
import cn.zdjc.wms.project.domain.entity.location.StorageLocationExtEntity;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.foeris.y.common.jdbc.SqlUtl;
import com.foeris.y.common.string.StringUtl;
import com.foeris.y.fairy.jdbc.connection.DatabaseExecuter;
import com.foeris.y.fairy.jdbc.ddd.DomainRepository;
import com.foreris.eris.common.exception.BusinessException;
import com.foreris.eris.common.map.MapWrap;
import org.springframework.stereotype.Repository;

import java.util.*;


@SuppressWarnings("deprecation")
@Repository
public class StorageLocationExtRepository extends DomainRepository<StorageLocationExtEntity> {


    public StorageLocationExtEntity getLocationByHouseIdAndLocNo(String houseId, String locNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1 ");
        Map<String, Object> params = new HashMap<>();
        if (Objects.nonNull(houseId)) {
            sql.append(" and house_id = :houseId ");
            params.put("houseId", houseId);
        }
        if (!StringUtl.isEmpty(locNo)) {
            sql.append(" and loc_no = :loc_no ");
            params.put("loc_no", locNo);
        }

        List<StorageLocationExtEntity> list = DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
        if (list.size() > 1) {
            throw new BusinessException("货位管理[{}]非唯一,请检查!", locNo);
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    public StorageLocationExtEntity getLocationByHouseCodeAndLocNo(String houseCode, String locNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1 ");
        Map<String, Object> params = new HashMap<>();
        if (!StringUtl.isEmpty(houseCode)) {
            sql.append(" and house_code = :houseCode ");
            params.put("houseCode", houseCode);
        }
        if (!StringUtl.isEmpty(locNo)) {
            sql.append(" and loc_no = :loc_no ");
            params.put("loc_no", locNo);
        }
        List<StorageLocationExtEntity> list = DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
        if (list.size() > 1) {
            throw new BusinessException("货位管理[{}]非唯一,请检查!", locNo);
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    /**
     * 获取指定仓库货位表记录的ID
     */
    public String getLocationIdByHouseCodeAndLocNo(String houseCode, String locNo) {
        StringBuilder sql = new StringBuilder();
        sql.append("select id from ").append(this.tableName)
                .append(" where is_active=1 ");
        Map<String, Object> params = new HashMap<>();
        if (!StringUtl.isEmpty(houseCode)) {
            sql.append(" and house_code = :houseCode ");
            params.put("houseCode", houseCode);
        }
        if (!StringUtl.isEmpty(locNo)) {
            sql.append(" and loc_no = :loc_no ");
            params.put("loc_no", locNo);
        }
        List<String> list = DatabaseExecuter.queryListValue(sql, params, String.class);
        if (list.size() > 1) {
            throw new BusinessException("货位管理[{}]非唯一,请检查!", locNo);
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    public List<StorageLocationExtEntity> getStorageLocation(List<String> locationIdList, Integer xPos) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1")
                .append(" and id in(:locationIdList) ")
                .append(" and x_pos =:xPos")
                .append(" order by z_pos asc, loc_no asc");
        Map<String, Object> params = new HashMap<>();
        params.put("locationIdList", locationIdList);
        params.put("xPos", xPos);
        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> getAvailableDepthStorageLocation(List<String> locationIdList, Double allowDepth, List<Integer> xPosList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1")
                .append(" and id in(:locationIdList) ")
                .append(" and allow_depth=:allowDepth")
                .append(" and x_pos in(:xPosList)")
                .append(" and available_depth > 0.000")
                .append(" order by z_pos asc, loc_no asc");
        Map<String, Object> params = new HashMap<>();
        params.put("locationIdList", locationIdList);
        params.put("allowDepth", allowDepth);
        params.put("xPosList", xPosList);
        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> getVoidDepthStorageLocation(List<String> locationIdList, List<Integer> xPosList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1")
                .append(" and id in(:locationIdList) ")
                .append(" and allow_depth = 0.000")
                .append(" and available_depth = 0.000")
                .append(" and x_pos in(:xPosList)")
                .append(" order by z_pos asc, loc_no asc");
        Map<String, Object> params = new HashMap<>();
        params.put("locationIdList", locationIdList);
        params.put("xPosList", xPosList);
        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }


	private void checkDepth(String loc, Double allowDepth, Double availableDepth) {
		Assert.notNull(allowDepth, "允许深度不允许null");
    	Assert.notNull(availableDepth, "可用深度不允许null");
    	Assert.isTrue(availableDepth < allowDepth || allowDepth == 0,
    			"货位 {} 允许深度 {} 必须大于可用深度 {}", loc, allowDepth, availableDepth);
	}

    public void updateLocStatus(String id, String locStatus) {
        String sql = "update $table set loc_status=:locStatus where id =:id ";
        DatabaseExecuter.update(sql, new MapWrap<String, Object>()
                .put("table", SqlUtl.getTable(StorageLocationExtEntity.class))
                .put("locStatus", locStatus)
                .put("id", id)
                .getMap());
    }

    public List<StorageLocationExtEntity> queryHaveContainerStorageLocation(List<Integer> xPosList, Integer zPos) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1")
                .append(" and x_pos in(:xPosList) ")
                .append(" and z_pos =:zPos")
                .append(" and container_code <> ''")
                .append(" order by x_pos asc, y_pos asc");
        Map<String, Object> params = new HashMap<>();
        params.put("xPosList", xPosList);
        params.put("zPos", zPos);
        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> queryContainerByLocNo(String houseCode, Collection<String> locNos) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1")
                .append(" and house_code =:houseCode ")
                .append(" and loc_no in(:locNos)")
                .append(" order by x_pos asc, z_pos asc, y_pos asc");
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);
        params.put("locNos", locNos);
        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> queryStorageLocationByContainer(String containerCode) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1")
                .append(" and container_code like :containerCode");
        Map<String, Object> params = new HashMap<>();
        params.put("containerCode", "%" + containerCode + "%");
        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> queryStorageLocationByPos(List<Integer> xPosList, List<Integer> yPosList, List<Integer> zPosList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1 ");

        Map<String, Object> params = new HashMap<>();

        if (ObjectUtils.isNotEmpty(xPosList)) {
            sql.append(" and x_pos in(:xPosList) ");
            params.put("xPosList", xPosList);
        }

        if (ObjectUtils.isNotEmpty(yPosList)) {
            sql.append(" and y_pos in(:yPosList) ");
            params.put("yPosList", yPosList);
        }

        if (ObjectUtils.isNotEmpty(zPosList)) {
            sql.append(" and z_pos in(:zPosList) ");
            params.put("zPosList", zPosList);
        }

        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> queryStorageLocationByPos(List<Integer> rowList,List<Integer> xPosList, List<Integer> yPosList, List<Integer> zPosList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1 ");

        Map<String, Object> params = new HashMap<>();
        if (ObjectUtils.isNotEmpty(rowList)) {
            sql.append(" and associated_roadway in(:rowList) ");
            params.put("rowList", rowList);
        }
        if (ObjectUtils.isNotEmpty(xPosList)) {
            sql.append(" and x_pos in(:xPosList) ");
            params.put("xPosList", xPosList);
        }

        if (ObjectUtils.isNotEmpty(yPosList)) {
            sql.append(" and y_pos in(:yPosList) ");
            params.put("yPosList", yPosList);
        }

        if (ObjectUtils.isNotEmpty(zPosList)) {
            sql.append(" and z_pos in(:zPosList) ");
            params.put("zPosList", zPosList);
        }

        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public int queryVoidStorageLocation(String houseCode, List<String> roadwayList) {
        String sql = "SELECT count(1)" +
                " FROM wms_storage_location wsl" +
                " WHERE wsl.is_active=1" +
                " and wsl.house_code=:houseCode" +
                " and wsl.loc_type='cubic'" +
                " and wsl.allow_depth=0.0" +
                " and wsl.available_depth=0.0";
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);

        if (ObjectUtils.isNotEmpty(roadwayList)) {
            sql += " and associated_roadway in (:roadwayList) ";
            params.put("roadwayList", roadwayList);
        }

        return DatabaseExecuter.queryInteger(sql, params);
    }

    public int queryNonVoidStorageLocation(String houseCode, Double allowDepth, List<String> roadwayList) {
        String sql = "SELECT count(1)" +
                " FROM wms_storage_location wsl" +
                " WHERE wsl.is_active=1" +
                " and wsl.house_code=:houseCode" +
                " and wsl.loc_type='cubic'" +
                " and wsl.allow_depth=:allowDepth";
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);
        params.put("allowDepth", allowDepth);

        if (ObjectUtils.isNotEmpty(roadwayList)) {
            sql += " and associated_roadway in (:roadwayList) ";
            params.put("roadwayList", roadwayList);
        }

        return DatabaseExecuter.queryInteger(sql, params);
    }

    public List<StorageLocationExtEntity> queryHaveContainerStorageLocation(String houseCode, List<String> roadwayList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1")
                .append(" and house_code=:houseCode")
                .append(" and allow_depth>0.0");
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);

        if (ObjectUtils.isNotEmpty(roadwayList)) {
            sql.append(" and associated_roadway in(:roadwayList)");
            params.put("roadwayList", roadwayList);
        }

        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> queryErrorStorageLocation(String houseCode, List<String> roadwayList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName)
                .append(" where is_active=1")
                .append(" and house_code=:houseCode")
                .append(" and loc_is_error=1")
                .append(" and loc_type='cubic'");
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);

        if (ObjectUtils.isNotEmpty(roadwayList)) {
            sql.append(" and associated_roadway in(:roadwayList)");
            params.put("roadwayList", roadwayList);
        }

        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> queryStorageLocationByRoadway(String houseCode, String associatedRoadway) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName).append(" where is_active=1 ")
                .append(" and loc_type = 'cubic'")
                .append(" and house_code = :houseCode");
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);

        if (ObjectUtils.isNotEmpty(associatedRoadway)) {
            params.put("associatedRoadway", associatedRoadway);
            sql.append(" and associated_roadway=:associatedRoadway");
        }

        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public List<StorageLocationExtEntity> queryStorageLocationByXPos(String houseCode, Integer xPos) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
                .append(" from ").append(this.tableName).append(" where is_active=1 ")
                .append(" and house_code = :houseCode")
                .append(" and x_pos = :xPos")
                .append(" and loc_type = 'cubic'")
                .append(" order by x_pos, y_pos, z_pos");
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);
        params.put("xPos", xPos);

        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }

    public int queryContainerCount(String houseCode, StorageStatus storageStatus) {
        String sql = "select count(wsli.container_code) " +
                "from wms_storage_location_inventory wsli inner join wms_storage_location wsl on wsli.location_code = wsl.loc_no " +
                "where wsli.house_code=:houseCode and wsli.container_status=:storageStatus and wsl.loc_type='cubic' ";

        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);
        params.put("storageStatus", storageStatus);

        return DatabaseExecuter.queryInteger(sql, params);
    }



    public List<StorageLocationExtEntity> queryStorageLocation(String houseCode, List<String> roadwayList) {
        StringBuilder sql = new StringBuilder();
        sql.append("select ").append(SqlUtl.getColumns(StorageLocationExtEntity.class))
	        .append(" from ").append(this.tableName)
	        .append(" where is_active=1 ")
	        .append(" and house_code = :houseCode")
	        .append(" and loc_type = 'cubic'");
        Map<String, Object> params = new HashMap<>();
        params.put("houseCode", houseCode);
        if (ObjectUtils.isNotEmpty(roadwayList)) {
            sql.append(" and associated_roadway in (:roadwayList)");
            params.put("roadwayList", roadwayList);
        }

        return DatabaseExecuter.queryBeanList(sql, params, StorageLocationExtEntity.class);
    }
    

}
