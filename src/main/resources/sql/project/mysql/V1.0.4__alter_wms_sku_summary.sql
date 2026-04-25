CREATE OR REPLACE VIEW wms_sku_summary AS
SELECT
    `wms_storage_material`.`house_code` AS `house_code`,
    `wms_storage_material`.`category_code` AS `category_code`,
    `wms_storage_material`.`category_name` AS `category_name`,
    `wms_storage_material`.`sku_code` AS `sku_code`,
    `wms_storage_material`.`sku_name` AS `sku_name`,
    `wms_storage_material`.`package_level` AS `PACKAGE_LEVEL`,
    `wms_storage_material`.`factory_code` AS `factory_code`,
    `wms_storage_material`.`inventory_location` AS `inventory_location`,
    `wms_storage_material`.`batch_no` AS `batch_no`,
    `wms_storage_material`.`quality_status` AS `quality_status`,
    `wms_storage_material`.`inventory_status` AS `inventory_status`,
    `wms_storage_material`.`batch_status` AS `batch_status`,
    SUM(`wms_storage_material`.`primary_qty`) AS `primary_qty`,
    `wms_storage_material`.`primary_unit` AS `primary_unit`,
    SUM(`wms_storage_material`.`auxiliary_qty`) AS `auxiliary_qty`,
    `wms_storage_material`.`auxiliary_unit` AS `auxiliary_unit`,
    MIN(`wms_storage_material`.`create_datetime`) AS `create_datetime`,
    SUM(`wms_storage_material`.`available_qty`) AS `available_qty`,
    `wms_storage_material`.`owner_code` AS `owner_code`
FROM
    `wms_storage_material`
WHERE
    (`wms_storage_material`.`package_level` IN ('NORMAL', 'MERGE_MAIN'))
GROUP BY
    `wms_storage_material`.`house_code`,
    `wms_storage_material`.`category_code`,
    `wms_storage_material`.`category_name`,
    `wms_storage_material`.`sku_code`,
    `wms_storage_material`.`sku_name`,
    `wms_storage_material`.`factory_code`,
    `wms_storage_material`.`inventory_location`,
    `wms_storage_material`.`batch_no`,
    `wms_storage_material`.`quality_status`,
    `wms_storage_material`.`inventory_status`,
    `wms_storage_material`.`batch_status`,
    `wms_storage_material`.`primary_unit`,
    `wms_storage_material`.`auxiliary_unit`,
    `wms_storage_material`.`package_level`,
    `wms_storage_material`.`owner_code`
UNION ALL
SELECT
    `wms_storage_material`.`house_code` AS `house_code`,
    `wms_storage_material`.`category_code` AS `category_code`,
    `wms_storage_material`.`category_name` AS `category_name`,
    `wms_storage_material`.`sku_code` AS `sku_code`,
    `wms_storage_material`.`sku_name` AS `sku_name`,
    `wms_storage_material`.`package_level` AS `PACKAGE_LEVEL`,
    `wms_storage_material`.`factory_code` AS `factory_code`,
    `wms_storage_material`.`inventory_location` AS `inventory_location`,
    `wms_storage_material`.`batch_no` AS `batch_no`,
    `wms_storage_material`.`quality_status` AS `quality_status`,
    `wms_storage_material`.`inventory_status` AS `inventory_status`,
    `wms_storage_material`.`batch_status` AS `batch_status`,
    MAX(`wms_storage_material`.`primary_qty`) AS `primary_qty`,
    `wms_storage_material`.`primary_unit` AS `primary_unit`,
    MAX(`wms_storage_material`.`auxiliary_qty`) AS `auxiliary_qty`,
    `wms_storage_material`.`auxiliary_unit` AS `auxiliary_unit`,
    MIN(`wms_storage_material`.`create_datetime`) AS `create_datetime`,
    SUM(`wms_storage_material`.`available_qty`) AS `available_qty`,
    `wms_storage_material`.`owner_code` AS `owner_code`
FROM
    `wms_storage_material`
WHERE
    ((`wms_storage_material`.`package_level` = 'SPLIT_ITEM') AND (`wms_storage_material`.`kitting_flag` = 'Y'))
GROUP BY
    `wms_storage_material`.`house_code`,
    `wms_storage_material`.`category_code`,
    `wms_storage_material`.`category_name`,
    `wms_storage_material`.`sku_code`,
    `wms_storage_material`.`sku_name`,
    `wms_storage_material`.`factory_code`,
    `wms_storage_material`.`inventory_location`,
    `wms_storage_material`.`batch_no`,
    `wms_storage_material`.`quality_status`,
    `wms_storage_material`.`inventory_status`,
    `wms_storage_material`.`batch_status`,
    `wms_storage_material`.`primary_unit`,
    `wms_storage_material`.`auxiliary_unit`,
    `wms_storage_material`.`package_no`,
    `wms_storage_material`.`package_level`,
    `wms_storage_material`.`owner_code`;