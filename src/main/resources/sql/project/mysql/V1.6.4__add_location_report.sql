-- 库位库存明细报表
-- 基于 wms_storage_material + 容器 + 库位 多表关联

INSERT INTO wms_report_config(report_name, report_code, report_type, category, config_json, status, remark, created_by, created_time)
SELECT '库位库存明细', 'LOCATION_INV_DETAIL', 'TABLE', '库存管理',
'{
  "sql": "SELECT sm.house_code, wsl.loc_no AS location_code, wsl.loc_type, wcm.container_code, wcm.container_type_code, sm.sku_code, sm.sku_name, sm.batch_no, sm.serial_no, sm.primary_qty, sm.primary_unit, sm.available_qty, sm.quality_status, sm.batch_status, sm.inventory_status, sm.owner_name, sm.owner_code, sm.category_code, sm.category_name, sm.vendor_name, sm.package_no, sm.package_level, sm.product_date, sm.expire_date, sm.remark, wsl.x_pos, wsl.y_pos, wsl.z_pos FROM wms_storage_material sm LEFT JOIN wms_container_material wcm ON sm.id = wcm.storage_material_id LEFT JOIN wms_storage_location_inventory wsli ON wsli.container_code = wcm.container_code LEFT JOIN wms_storage_location wsl ON wsl.id = wsli.location_id WHERE sm.is_active = 1 AND (:houseCode IS NULL OR :houseCode = '''' OR sm.house_code = :houseCode) AND (:skuCode IS NULL OR :skuCode = '''' OR sm.sku_code LIKE CONCAT(''%'', :skuCode, ''%'')) AND (:qualityStatus IS NULL OR :qualityStatus = '''' OR sm.quality_status = :qualityStatus) AND (:locNo IS NULL OR :locNo = '''' OR wsl.loc_no LIKE CONCAT(''%'', :locNo, ''%'')) ORDER BY sm.house_code, wsl.loc_no, sm.sku_code",
  "columns": [
    {"prop": "house_code", "label": "仓库", "width": 80, "align": "center"},
    {"prop": "location_code", "label": "库位编码", "width": 130},
    {"prop": "loc_type", "label": "库位类型", "width": 90, "align": "center"},
    {"prop": "container_code", "label": "容器/托盘", "width": 130},
    {"prop": "container_type_code", "label": "容器类型", "width": 90, "align": "center"},
    {"prop": "sku_code", "label": "物料编码", "width": 120},
    {"prop": "sku_name", "label": "物料名称", "width": 160},
    {"prop": "batch_no", "label": "批次号", "width": 130},
    {"prop": "serial_no", "label": "序列号", "width": 150},
    {"prop": "primary_qty", "label": "库存数", "width": 90, "align": "right", "format": "decimal2"},
    {"prop": "primary_unit", "label": "单位", "width": 60, "align": "center"},
    {"prop": "available_qty", "label": "可用数", "width": 90, "align": "right", "format": "decimal2"},
    {"prop": "quality_status", "label": "质量状态", "width": 90, "align": "center"},
    {"prop": "batch_status", "label": "批次状态", "width": 90, "align": "center"},
    {"prop": "inventory_status", "label": "库存状态", "width": 90, "align": "center"},
    {"prop": "owner_name", "label": "货主", "width": 100},
    {"prop": "vendor_name", "label": "供应商", "width": 120},
    {"prop": "product_date", "label": "生产日期", "width": 100, "align": "center"},
    {"prop": "expire_date", "label": "失效日期", "width": 100, "align": "center"},
    {"prop": "x_pos", "label": "X坐标", "width": 70, "align": "center"},
    {"prop": "y_pos", "label": "Y坐标", "width": 70, "align": "center"},
    {"prop": "z_pos", "label": "Z坐标", "width": 70, "align": "center"},
    {"prop": "remark", "label": "备注", "width": 150}
  ],
  "searchParams": [
    {"prop": "houseCode", "label": "仓库编码", "type": "string"},
    {"prop": "skuCode", "label": "物料编码", "type": "string"},
    {"prop": "qualityStatus", "label": "质量状态", "type": "string"},
    {"prop": "locNo", "label": "库位编码", "type": "string"}
  ],
  "pagination": {"enabled": true, "pageSize": 20},
  "export": {"enabled": true, "fileName": "库位库存明细", "maxRows": 10000}
}',
1, '库存+库位+容器多表关联明细查询', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_report_config WHERE report_code = 'LOCATION_INV_DETAIL');
