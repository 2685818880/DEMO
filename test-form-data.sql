-- 表单设计器测试数据脚本
-- 数据库: MySQL
-- 连接: 127.0.0.1:3306/wms878
-- 用户: root/root

-- 先清空现有测试数据（可选）
-- DELETE FROM wms_form_config WHERE code IN ('user_registration', 'product_info', 'user_registration_api');

-- 用户注册表单
INSERT INTO wms_form_config (name, code, description, config_json, form_type, version, status, created_by, created_time)
VALUES (
    '用户注册表单',
    'user_registration',
    '用户注册信息收集表单',
    '{
        "fields": [
            {
                "__config__": {
                    "label": "用户名",
                    "tag": "el-input",
                    "tagIcon": "input",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": [],
                    "changeTag": true,
                    "document": "https://element.eleme.cn/#/zh-CN/component/input"
                },
                "__slot__": {},
                "placeholder": "请输入用户名",
                "style": { "width": "100%" },
                "clearable": true,
                "prefix-icon": "",
                "suffix-icon": "",
                "maxlength": null,
                "show-word-limit": false,
                "readonly": false,
                "disabled": false,
                "__vModel__": "username"
            },
            {
                "__config__": {
                    "label": "密码",
                    "tag": "el-input",
                    "tagIcon": "password",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": [],
                    "changeTag": true,
                    "document": "https://element.eleme.cn/#/zh-CN/component/input"
                },
                "__slot__": {},
                "placeholder": "请输入密码",
                "style": { "width": "100%" },
                "clearable": true,
                "show-password": true,
                "prefix-icon": "",
                "suffix-icon": "",
                "maxlength": null,
                "show-word-limit": false,
                "readonly": false,
                "disabled": false,
                "__vModel__": "password"
            },
            {
                "__config__": {
                    "label": "邮箱",
                    "tag": "el-input",
                    "tagIcon": "input",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": [
                        {
                            "pattern": "/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$/",
                            "message": "请输入有效的邮箱地址",
                            "trigger": "blur"
                        }
                    ],
                    "changeTag": true,
                    "document": "https://element.eleme.cn/#/zh-CN/component/input"
                },
                "__slot__": {},
                "placeholder": "请输入邮箱",
                "style": { "width": "100%" },
                "clearable": true,
                "prefix-icon": "el-icon-message",
                "suffix-icon": "",
                "maxlength": null,
                "show-word-limit": false,
                "readonly": false,
                "disabled": false,
                "__vModel__": "email"
            },
            {
                "__config__": {
                    "label": "性别",
                    "tag": "el-select",
                    "tagIcon": "select",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": [],
                    "changeTag": true,
                    "document": "https://element.eleme.cn/#/zh-CN/component/select"
                },
                "__slot__": {
                    "options": [
                        { "label": "男", "value": "male" },
                        { "label": "女", "value": "female" },
                        { "label": "其他", "value": "other" }
                    ]
                },
                "placeholder": "请选择性别",
                "style": { "width": "100%" },
                "clearable": true,
                "disabled": false,
                "multiple": false,
                "filterable": false,
                "__vModel__": "gender"
            },
            {
                "__config__": {
                    "label": "提交",
                    "tag": "el-button",
                    "tagIcon": "button",
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": [],
                    "changeTag": false,
                    "document": "https://element.eleme.cn/#/zh-CN/component/button"
                },
                "__slot__": {
                    "default": "提交注册"
                },
                "type": "primary",
                "icon": "el-icon-check",
                "round": false,
                "size": "medium",
                "plain": false,
                "circle": false,
                "disabled": false,
                "__vModel__": "submitBtn"
            }
        ],
        "formBtns": true
    }',
    'REGISTRATION',
    1,
    1,
    'admin',
    NOW()
);

-- 产品信息表单
INSERT INTO wms_form_config (name, code, description, config_json, form_type, version, status, created_by, created_time)
VALUES (
    '产品信息表单',
    'product_info',
    '产品基本信息收集表单',
    '{
        "fields": [
            {
                "__config__": {
                    "label": "产品名称",
                    "tag": "el-input",
                    "tagIcon": "input",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": [],
                    "changeTag": true
                },
                "__slot__": {},
                "placeholder": "请输入产品名称",
                "style": { "width": "100%" },
                "clearable": true,
                "__vModel__": "productName"
            },
            {
                "__config__": {
                    "label": "产品分类",
                    "tag": "el-select",
                    "tagIcon": "select",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": []
                },
                "__slot__": {
                    "options": [
                        { "label": "电子产品", "value": "electronics" },
                        { "label": "服装", "value": "clothing" },
                        { "label": "食品", "value": "food" },
                        { "label": "图书", "value": "books" }
                    ]
                },
                "placeholder": "请选择产品分类",
                "style": { "width": "100%" },
                "__vModel__": "category"
            },
            {
                "__config__": {
                    "label": "产品价格",
                    "tag": "el-input-number",
                    "tagIcon": "number",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": []
                },
                "placeholder": "请输入产品价格",
                "min": 0,
                "max": 999999,
                "step": 1,
                "precision": 2,
                "controls-position": "right",
                "__vModel__": "price"
            },
            {
                "__config__": {
                    "label": "产品描述",
                    "tag": "el-input",
                    "tagIcon": "textarea",
                    "required": false,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": []
                },
                "type": "textarea",
                "placeholder": "请输入产品描述",
                "autosize": { "minRows": 3, "maxRows": 6 },
                "style": { "width": "100%" },
                "maxlength": 500,
                "show-word-limit": true,
                "__vModel__": "description"
            }
        ],
        "formBtns": true
    }',
    'PRODUCT',
    1,
    1,
    'admin',
    NOW()
);

-- 订单信息表单
INSERT INTO wms_form_config (name, code, description, config_json, form_type, version, status, created_by, created_time)
VALUES (
    '订单信息表单',
    'order_info',
    '订单信息收集表单',
    '{
        "fields": [
            {
                "__config__": {
                    "label": "订单编号",
                    "tag": "el-input",
                    "tagIcon": "input",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": []
                },
                "placeholder": "请输入订单编号",
                "style": { "width": "100%" },
                "clearable": true,
                "__vModel__": "orderNo"
            },
            {
                "__config__": {
                    "label": "客户姓名",
                    "tag": "el-input",
                    "tagIcon": "input",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": []
                },
                "placeholder": "请输入客户姓名",
                "style": { "width": "100%" },
                "clearable": true,
                "__vModel__": "customerName"
            },
            {
                "__config__": {
                    "label": "订单日期",
                    "tag": "el-date-picker",
                    "tagIcon": "date",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": []
                },
                "placeholder": "请选择订单日期",
                "style": { "width": "100%" },
                "type": "date",
                "format": "yyyy-MM-dd",
                "value-format": "yyyy-MM-dd",
                "readonly": false,
                "disabled": false,
                "editable": false,
                "clearable": true,
                "__vModel__": "orderDate"
            },
            {
                "__config__": {
                    "label": "订单状态",
                    "tag": "el-select",
                    "tagIcon": "select",
                    "required": true,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": []
                },
                "__slot__": {
                    "options": [
                        { "label": "待付款", "value": "pending" },
                        { "label": "已付款", "value": "paid" },
                        { "label": "已发货", "value": "shipped" },
                        { "label": "已完成", "value": "completed" },
                        { "label": "已取消", "value": "cancelled" }
                    ]
                },
                "placeholder": "请选择订单状态",
                "style": { "width": "100%" },
                "__vModel__": "orderStatus"
            },
            {
                "__config__": {
                    "label": "备注",
                    "tag": "el-input",
                    "tagIcon": "textarea",
                    "required": false,
                    "layout": "colFormItem",
                    "span": 24,
                    "regList": []
                },
                "type": "textarea",
                "placeholder": "请输入备注信息",
                "autosize": { "minRows": 2, "maxRows": 4 },
                "style": { "width": "100%" },
                "maxlength": 200,
                "show-word-limit": true,
                "__vModel__": "remark"
            }
        ],
        "formBtns": true
    }',
    'ORDER',
    1,
    1,
    'admin',
    NOW()
);

-- 查询验证
SELECT '插入完成，共插入 ' || COUNT(*) || ' 条测试数据' AS result FROM wms_form_config WHERE code IN ('user_registration', 'product_info', 'order_info');