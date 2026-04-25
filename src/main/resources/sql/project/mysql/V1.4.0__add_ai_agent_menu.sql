-- AI Agent平台菜单
-- 父菜单: AI智能平台
-- 子菜单: AI Agent平台 -> /ai-external (重定向到 http://localhost:8080/app/ai)

-- UUID
SET @parent_id = 'a1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e';
SET @child_id = 'b1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e';

-- 父菜单: AI智能平台
INSERT INTO sys_menu(
    id, parent_id, tree_index, child_count, full_child_count,
    tree_level, tree_code, full_tree_code, function_id,
    name, uri, param, description, type, icon, sort,
    is_active, create_by, create_datetime, last_modify_by, last_modify_datetime,
    system_type, is_hidden, target
)
SELECT @parent_id, NULL, @parent_id, 1, 1, 1, ' ', ' ', NULL,
       'AI智能平台', '  ', ' ', ' ', 'menu', 'fa fa-cubes', 500,
       1, 'system', NOW(), 'system', NOW(), 0, 0, ' '
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = @parent_id);

-- 子菜单: AI Agent平台
INSERT INTO sys_menu(
    id, parent_id, tree_index, child_count, full_child_count,
    tree_level, tree_code, full_tree_code, function_id,
    name, uri, param, description, type, icon, sort,
    is_active, create_by, create_datetime, last_modify_by, last_modify_datetime,
    system_type, is_hidden, target
)
SELECT @child_id, @parent_id, @child_id, 0, 0, 2, ' ', ' ', NULL,
       'AI Agent平台', '/wms/view/ai-external', ' ', ' ', 'menu', 'fa fa-cogs', 1,
       1, 'system', NOW(), 'system', NOW(), 0, 0, '_blank'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = @child_id);
