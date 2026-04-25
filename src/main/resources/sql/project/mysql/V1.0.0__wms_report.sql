create table wms_material_report (
                                     id              char(36)         NOT NULL,
                                     house_code      varchar(100)     NOT NULL DEFAULT 0,
                                     serial_no       varchar(100)     NOT NULL,
                                     primary_qty     decimal(18, 3)   NOT NULL DEFAULT 0,
                                     enter_loc       varchar(100),
                                     enter_time      datetime(3),
                                     exit_loc        varchar(100),
                                     exit_time       datetime(3),
                                     category_code   varchar(100),
                                     category_name   varchar(100),
                                     sku_code        varchar(100),
                                     sku_name        varchar(100),
                                     factory_code    varchar(100),
                                     batch_no        varchar(100),
                                     is_active       tinyint(1)       NOT NULL DEFAULT 1,
                                     create_datetime datetime(3),
                                     create_by       varchar(100),
                                     last_modify_datetime datetime(3),
                                     last_modify_by  varchar(100),
                                     primary key (id) using btree,
                                     unique key serial_no (serial_no),
                                     key enter_loc (enter_loc),
                                     key enter_time (enter_time),
                                     key exit_loc (exit_loc),
                                     key exit_time (exit_time),
                                     key sku_code (sku_code),
                                     key house_code (house_code)
);

alter table wms_material_report add column container_code varchar(100) default null after primary_qty;

alter table wms_material_report add index container_code(container_code);

