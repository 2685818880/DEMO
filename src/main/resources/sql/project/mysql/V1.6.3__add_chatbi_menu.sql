-- ChatBI智能查询菜单
-- 子菜单: ChatBI智能查询 -> /ai/chat-bi
-- 父菜单: AI智能平台 (id = 'a1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e')

SET @parent_id = 'a1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e';
SET @child_id = 'c1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e';

-- 更新父菜单的子计数
UPDATE sys_menu
SET child_count = 2, full_child_count = 2
WHERE id = @parent_id AND child_count < 2;

-- 子菜单: ChatBI智能查询
INSERT INTO sys_menu(
    id, parent_id, tree_index, child_count, full_child_count,
    tree_level, tree_code, full_tree_code, function_id,
    name, uri, param, description, type, icon, sort,
    is_active, create_by, create_datetime, last_modify_by, last_modify_datetime,
    system_type, is_hidden, target
)
SELECT @child_id, @parent_id, @child_id, 0, 0, 2, ' ', ' ', NULL,
       'ChatBI智能查询', '/wms/view/ai/chat-bi', ' ', ' ', 'menu', 'fa fa-commenting', 2,
       1, 'system', NOW(), 'system', NOW(), 0, 0, ' '
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE id = @child_id);
