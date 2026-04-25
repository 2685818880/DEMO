-- 自定义报表菜单
-- 父菜单: 自定义报表
-- 子菜单: 报表管理 -> /report-designer/list

-- UUID
SET @parent_id = 'd1e2f3c6-7762-4daa-9ee5-5d8f49ec8a9e';
SET @child_list_id = 'e1e2f3c6-7762-4daa-9ee5-5d8f49ec8a9e';

-- 父菜单: 自定义报表
INSERT INTO sys_menu(
    id, parent_id, tree_index, child_count, full_child_count,
    tree_level, tree_code, full_tree_code, function_id,
    name, uri, param, description, type, icon, sort,
    is_active, create_by, create_datetime, last_modify_by, last_modify_datetime,
    system_type, is_hidden, target
)
SELECT @parent_id, NULL, @parent_id, 1, 1, 1, ' ', ' ', NULL,
       '自定义报表', '  ', ' ', ' ', 'menu', 'fa fa-file-text', 400,
       1, 'system', NOW(), 'system', NOW(), 0, 0, ' '
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = @parent_id);

-- 子菜单: 报表管理
INSERT INTO sys_menu(
    id, parent_id, tree_index, child_count, full_child_count,
    tree_level, tree_code, full_tree_code, function_id,
    name, uri, param, description, type, icon, sort,
    is_active, create_by, create_datetime, last_modify_by, last_modify_datetime,
    system_type, is_hidden, target
)
SELECT @child_list_id, @parent_id, @child_list_id, 0, 0, 2, ' ', ' ', NULL,
       '报表管理', '/report-designer/list', ' ', ' ', 'menu', 'fa fa-list-alt', 1,
       1, 'system', NOW(), 'system', NOW(), 0, 0, ' '
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = @child_list_id);
