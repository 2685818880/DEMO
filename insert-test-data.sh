#!/bin/bash

# 表单设计器测试数据插入脚本
# 使用方法: ./insert-test-data.sh [数据库密码]

DB_HOST="127.0.0.1"
DB_PORT="3306"
DB_NAME="wms878"
DB_USER="root"
DB_PASS="${1:-root}"  # 使用第一个参数作为密码，默认为root

SQL_FILE="test-form-data.sql"

echo "=== 表单设计器测试数据插入脚本 ==="
echo "数据库: ${DB_HOST}:${DB_PORT}/${DB_NAME}"
echo "用户: ${DB_USER}"
echo "SQL文件: ${SQL_FILE}"
echo ""

# 检查mysql客户端是否安装
if ! command -v mysql &> /dev/null; then
    echo "错误: mysql客户端未安装"
    echo "请安装MySQL客户端或使用其他方式执行SQL文件"
    exit 1
fi

# 检查SQL文件是否存在
if [ ! -f "$SQL_FILE" ]; then
    echo "错误: SQL文件不存在: $SQL_FILE"
    exit 1
fi

echo "正在连接数据库..."
echo ""

# 执行SQL文件
mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$SQL_FILE"

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ 测试数据插入成功！"
    echo ""
    echo "已插入的测试表单:"
    echo "1. 用户注册表单 (user_registration)"
    echo "2. 产品信息表单 (product_info)"
    echo "3. 订单信息表单 (order_info)"
    echo ""
    echo "请访问 http://localhost:8080/app/form-designer/list 查看结果"
else
    echo ""
    echo "❌ 测试数据插入失败"
    echo "请检查:"
    echo "1. 数据库服务是否运行"
    echo "2. 数据库连接信息是否正确"
    echo "3. 用户名和密码是否正确"
    exit 1
fi