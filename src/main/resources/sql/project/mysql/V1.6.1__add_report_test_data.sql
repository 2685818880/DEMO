-- 自定义报表测试数据
-- 提供 5 个开箱即用的示例报表，覆盖 TABLE / CHART / MIXED 三种类型
-- 适用于 MySQL

-- 1. 库存汇总报表 (TABLE)
INSERT INTO wms_report_config(report_name, report_code, report_type, category, config_json, status, remark, created_by, created_time)
SELECT '库存汇总报表', 'INV_SUMMARY', 'TABLE', '库存管理',
'{
  "sql": "SELECT s.sku_code, s.sku_name, s.primary_unit, SUM(s.primary_qty) as total_qty, SUM(s.available_qty) as available_qty, COUNT(DISTINCT s.batch_no) as batch_count, COUNT(DISTINCT s.house_code) as house_count FROM wms_storage_material s WHERE s.is_active = 1 AND (:houseCode IS NULL OR s.house_code = :houseCode) GROUP BY s.sku_code, s.sku_name, s.primary_unit ORDER BY total_qty DESC",
  "columns": [
    {"prop": "sku_code", "label": "物料编码", "width": 130},
    {"prop": "sku_name", "label": "物料名称", "width": 180},
    {"prop": "primary_unit", "label": "单位", "width": 70, "align": "center"},
    {"prop": "total_qty", "label": "总库存数", "width": 110, "align": "right", "format": "decimal2"},
    {"prop": "available_qty", "label": "可用数量", "width": 110, "align": "right", "format": "decimal2"},
    {"prop": "batch_count", "label": "批次数量", "width": 90, "align": "center"},
    {"prop": "house_count", "label": "仓库数", "width": 80, "align": "center"}
  ],
  "searchParams": [
    {"prop": "houseCode", "label": "仓库编码", "type": "string"}
  ],
  "pagination": {"enabled": true, "pageSize": 20},
  "export": {"enabled": true, "fileName": "库存汇总", "maxRows": 10000}
}',
1, '按SKU汇总各仓库库存数量', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_report_config WHERE report_code = 'INV_SUMMARY');

-- 2. 近期入库记录 (TABLE)
INSERT INTO wms_report_config(report_name, report_code, report_type, category, config_json, status, remark, created_by, created_time)
SELECT '近期入库记录', 'INBOUND_RECENT', 'TABLE', '入库管理',
'{
  "sql": "SELECT a.form_no, a.create_datetime, a.supplier_name, a.supplier_code, b.sku_code, b.sku_name, b.primary_qty, b.primary_unit, b.confirm_qty, a.asn_status FROM wms_asn a JOIN wms_asn_item b ON a.id = b.asn_id WHERE a.is_active = 1 AND b.is_active = 1 AND (:houseCode IS NULL OR a.house_code = :houseCode) AND a.create_datetime >= DATE_SUB(NOW(), INTERVAL :days DAY) ORDER BY a.create_datetime DESC",
  "columns": [
    {"prop": "form_no", "label": "入库单号", "width": 170},
    {"prop": "create_datetime", "label": "入库时间", "width": 170},
    {"prop": "supplier_name", "label": "供应商", "width": 150},
    {"prop": "supplier_code", "label": "供应商编码", "width": 120},
    {"prop": "sku_code", "label": "物料编码", "width": 130},
    {"prop": "sku_name", "label": "物料名称", "width": 180},
    {"prop": "primary_qty", "label": "入库数量", "width": 100, "align": "right", "format": "decimal2"},
    {"prop": "primary_unit", "label": "单位", "width": 70, "align": "center"},
    {"prop": "confirm_qty", "label": "已确认", "width": 100, "align": "right", "format": "decimal2"}
  ],
  "searchParams": [
    {"prop": "houseCode", "label": "仓库编码", "type": "string"},
    {"prop": "days", "label": "最近天数", "type": "number", "defaultValue": 7}
  ],
  "pagination": {"enabled": true, "pageSize": 20},
  "export": {"enabled": true, "fileName": "入库记录", "maxRows": 10000}
}',
1, '查询近期入库明细记录', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_report_config WHERE report_code = 'INBOUND_RECENT');

-- 3. 拣货工作站效率分析 (CHART - 柱状图)
INSERT INTO wms_report_config(report_name, report_code, report_type, category, config_json, status, remark, created_by, created_time)
SELECT '拣货工作站效率分析', 'PICK_STATION_ANALYSIS', 'CHART', '出库管理',
'{
  "sql": "SELECT p.pick_station, COUNT(*) as pick_count, SUM(p.primary_qty) as total_qty, COUNT(DISTINCT p.sku_code) as sku_count, COUNT(DISTINCT p.operator) as operator_count FROM wms_pick_item p WHERE p.pick_station IS NOT NULL AND p.create_datetime >= DATE_SUB(NOW(), INTERVAL :days DAY) GROUP BY p.pick_station ORDER BY pick_count DESC",
  "columns": [
    {"prop": "pick_station", "label": "工作站", "width": 120},
    {"prop": "pick_count", "label": "拣货次数", "width": 100, "align": "right"},
    {"prop": "total_qty", "label": "总数量", "width": 100, "align": "right", "format": "decimal2"},
    {"prop": "sku_count", "label": "SKU数", "width": 80, "align": "center"},
    {"prop": "operator_count", "label": "操作人数", "width": 100, "align": "center"}
  ],
  "searchParams": [
    {"prop": "days", "label": "最近天数", "type": "number", "defaultValue": 7}
  ],
  "charts": [
    {"chartType": "bar", "title": "各工作站拣货次数", "dimension": "pick_station", "metric": "pick_count"}
  ],
  "pagination": {"enabled": false},
  "export": {"enabled": true, "fileName": "拣货效率分析", "maxRows": 10000}
}',
1, '按工作站统计拣货效率，柱状图展示', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_report_config WHERE report_code = 'PICK_STATION_ANALYSIS');

-- 4. 仓库库存分布 (CHART - 饼图)
INSERT INTO wms_report_config(report_name, report_code, report_type, category, config_json, status, remark, created_by, created_time)
SELECT '仓库库存分布', 'WHSE_DISTRIBUTION', 'CHART', '库存管理',
'{
  "sql": "SELECT s.house_code, SUM(s.primary_qty) as total_qty, COUNT(DISTINCT s.sku_code) as sku_count FROM wms_storage_material s WHERE s.is_active = 1 AND s.primary_qty > 0 GROUP BY s.house_code ORDER BY total_qty DESC",
  "columns": [
    {"prop": "house_code", "label": "仓库编码", "width": 120},
    {"prop": "total_qty", "label": "库存数量", "width": 110, "align": "right", "format": "decimal2"},
    {"prop": "sku_count", "label": "SKU种类", "width": 100, "align": "center"}
  ],
  "searchParams": [],
  "charts": [
    {"chartType": "pie", "title": "各仓库库存占比", "dimension": "house_code", "metric": "total_qty"}
  ],
  "pagination": {"enabled": false},
  "export": {"enabled": true, "fileName": "仓库库存分布", "maxRows": 10000}
}',
1, '各仓库库存占比饼图', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_report_config WHERE report_code = 'WHSE_DISTRIBUTION');

-- 5. 库存质量分析 (MIXED - 表格 + 图表)
INSERT INTO wms_report_config(report_name, report_code, report_type, category, config_json, status, remark, created_by, created_time)
SELECT '库存质量分析', 'QUALITY_ANALYSIS', 'MIXED', '库存管理',
'{
  "sql": "SELECT s.quality_status, s.house_code, COUNT(*) as record_count, SUM(s.primary_qty) as total_qty, SUM(s.available_qty) as available_qty, COUNT(DISTINCT s.sku_code) as sku_count FROM wms_storage_material s WHERE s.is_active = 1 AND s.primary_qty > 0 AND (:qualityStatus IS NULL OR s.quality_status = :qualityStatus) GROUP BY s.quality_status, s.house_code ORDER BY s.quality_status, s.house_code",
  "columns": [
    {"prop": "quality_status", "label": "质量状态", "width": 100, "align": "center"},
    {"prop": "house_code", "label": "仓库编码", "width": 120},
    {"prop": "record_count", "label": "记录数", "width": 90, "align": "right"},
    {"prop": "total_qty", "label": "总数量", "width": 110, "align": "right", "format": "decimal2"},
    {"prop": "available_qty", "label": "可用数量", "width": 110, "align": "right", "format": "decimal2"},
    {"prop": "sku_count", "label": "SKU数", "width": 80, "align": "center"}
  ],
  "searchParams": [
    {"prop": "qualityStatus", "label": "质量状态", "type": "string"}
  ],
  "charts": [
    {"chartType": "pie", "title": "各质量状态占比", "dimension": "quality_status", "metric": "total_qty"}
  ],
  "pagination": {"enabled": true, "pageSize": 20},
  "export": {"enabled": true, "fileName": "库存质量分析", "maxRows": 10000}
}',
1, '按质量状态分析库存，饼图+表格混合展示', 'system', NOW()
WHERE NOT EXISTS (SELECT 1 FROM wms_report_config WHERE report_code = 'QUALITY_ANALYSIS');
