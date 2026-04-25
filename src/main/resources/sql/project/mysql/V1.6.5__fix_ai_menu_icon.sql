-- 修复AI智能平台菜单图标 (fa fa-robot 在 FA4 中不存在)
UPDATE sys_menu SET icon = 'fa fa-cubes'
WHERE id = 'a1a2a3c6-7762-4daa-9ee5-5d8f49ec8a9e' AND icon = 'fa fa-robot';
