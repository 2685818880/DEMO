DELIMITER $$

CREATE PROCEDURE `init_wms_indexes_safe`()
BEGIN
    -- 捕获并忽略 1061 错误（Duplicate key name：索引已存在），确保脚本可重复执行
    DECLARE CONTINUE HANDLER FOR 1061 BEGIN END;

    -- ================= wms_workstation =================
ALTER TABLE wms_workstation ADD INDEX idx_workstation_code (workstation_code);
ALTER TABLE wms_workstation ADD INDEX idx_workstation_status (workstation_status);
ALTER TABLE wms_workstation ADD INDEX idx_is_open (is_open);
ALTER TABLE wms_workstation ADD INDEX idx_create_datetime (create_datetime);
ALTER TABLE wms_workstation ADD INDEX idx_status_code (workstation_status, workstation_code);
ALTER TABLE wms_workstation ADD INDEX idx_ip_status (workstation_ip, workstation_status);

-- ================= wms_storage_material =================
ALTER TABLE wms_storage_material ADD INDEX idx_sku_code (sku_code);
ALTER TABLE wms_storage_material ADD INDEX idx_batch_no (batch_no);
ALTER TABLE wms_storage_material ADD INDEX idx_serial_no (serial_no);
ALTER TABLE wms_storage_material ADD INDEX idx_house_code (house_code);
ALTER TABLE wms_storage_material ADD INDEX idx_category_code (category_code);
ALTER TABLE wms_storage_material ADD INDEX idx_inventory_status (inventory_status);
ALTER TABLE wms_storage_material ADD INDEX idx_quality_status (quality_status);
ALTER TABLE wms_storage_material ADD INDEX idx_batch_status (batch_status);
ALTER TABLE wms_storage_material ADD INDEX idx_create_datetime (create_datetime);
ALTER TABLE wms_storage_material ADD INDEX idx_inventory_location (inventory_location);
ALTER TABLE wms_storage_material ADD INDEX idx_package_level (package_level);
ALTER TABLE wms_storage_material ADD INDEX idx_sku_status (sku_code, inventory_status);
ALTER TABLE wms_storage_material ADD INDEX idx_house_sku (house_code, sku_code);
ALTER TABLE wms_storage_material ADD INDEX idx_location_status (inventory_location, inventory_status);
ALTER TABLE wms_storage_material ADD INDEX idx_batch_quality (batch_no, quality_status);

-- ================= wms_sku =================
ALTER TABLE wms_sku ADD INDEX idx_sku_code (sku_code);
ALTER TABLE wms_sku ADD INDEX idx_category_code (category_code);

-- ================= wms_asn =================
ALTER TABLE wms_asn ADD INDEX idx_form_no (form_no);
ALTER TABLE wms_asn ADD INDEX idx_asn_status (asn_status);
ALTER TABLE wms_asn ADD INDEX idx_business_form_no (business_form_no);
ALTER TABLE wms_asn ADD INDEX idx_supplier_code (supplier_code);
ALTER TABLE wms_asn ADD INDEX idx_owner_code (owner_code);
ALTER TABLE wms_asn ADD INDEX idx_create_datetime (create_datetime);
ALTER TABLE wms_asn ADD INDEX idx_status_time (asn_status, create_datetime);
ALTER TABLE wms_asn ADD INDEX idx_form_supplier (form_no, supplier_code);
ALTER TABLE wms_asn ADD INDEX idx_business_status (business_form_no, asn_status);

-- ================= wms_outbound =================
ALTER TABLE wms_outbound ADD INDEX idx_form_no (form_no);
ALTER TABLE wms_outbound ADD INDEX idx_form_status (form_status);
ALTER TABLE wms_outbound ADD INDEX idx_form_type (form_type);
ALTER TABLE wms_outbound ADD INDEX idx_house_code (house_code);
ALTER TABLE wms_outbound ADD INDEX idx_container_code (container_code);
ALTER TABLE wms_outbound ADD INDEX idx_location_code (location_code);
ALTER TABLE wms_outbound ADD INDEX idx_out_task_no (out_task_no);
ALTER TABLE wms_outbound ADD INDEX idx_out_status (out_status);
ALTER TABLE wms_outbound ADD INDEX idx_create_datetime (create_datetime);
ALTER TABLE wms_outbound ADD INDEX idx_status_type (form_status, form_type);
ALTER TABLE wms_outbound ADD INDEX idx_house_status (house_code, form_status);
ALTER TABLE wms_outbound ADD INDEX idx_container_status (container_code, form_status);

-- ================= wms_pick_item =================
ALTER TABLE wms_pick_item ADD INDEX idx_workstation_code (workstation_code);
ALTER TABLE wms_pick_item ADD INDEX idx_sku_code (sku_code);
ALTER TABLE wms_pick_item ADD INDEX idx_outbound_id (outbound_id);
ALTER TABLE wms_pick_item ADD INDEX idx_pick_status (pick_status);
ALTER TABLE wms_pick_item ADD INDEX idx_order_container_code (order_container_code);
ALTER TABLE wms_pick_item ADD INDEX idx_create_datetime (create_datetime);
ALTER TABLE wms_pick_item ADD INDEX idx_status_workstation (pick_status, workstation_code);
ALTER TABLE wms_pick_item ADD INDEX idx_sku_status (sku_code, pick_status);

-- ================= wms_storage_location =================
ALTER TABLE wms_storage_location ADD INDEX idx_location_code (loc_no);
ALTER TABLE wms_storage_location ADD INDEX idx_loc_type (loc_type);
ALTER TABLE wms_storage_location ADD INDEX idx_house_code (house_code);

END$$

DELIMITER ;

-- 执行创建
CALL init_wms_indexes_safe();

-- 清理临时存储过程
DROP PROCEDURE IF EXISTS init_wms_indexes_safe;