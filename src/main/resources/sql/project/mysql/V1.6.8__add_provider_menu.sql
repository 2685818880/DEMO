-- API提供商管理菜单
-- 子菜单: API提供商管理 -> /ai/providers
-- 父菜单: AI智能平台 (id = 'a1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e')

SET @parent_id = 'a1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e';
SET @child_id = 'd1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e';

-- 更新父菜单的子计数
UPDATE sys_menu
SET child_count = 3, full_child_count = 3
WHERE id = @parent_id AND child_count < 3;

-- 子菜单: API提供商管理
INSERT INTO sys_menu(
    id, parent_id, tree_index, child_count, full_child_count,
    tree_level, tree_code, full_tree_code, function_id,
    name, uri, param, description, type, icon, sort,
    is_active, create_by, create_datetime, last_modify_by, last_modify_datetime,
    system_type, is_hidden, target
)
SELECT @child_id, @parent_id, @child_id, 0, 0, 2, ' ', ' ', NULL,
       'API提供商', '/wms/view/ai/providers', ' ', ' ', 'menu', 'fa fa-plug', 3,
       1, 'system', NOW(), 'system', NOW(), 0, 0, ' '
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = @child_id);
