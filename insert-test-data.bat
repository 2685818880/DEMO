@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM 表单设计器测试数据插入脚本 (Windows版本)
REM 使用方法: insert-test-data.bat [数据库密码]

set DB_HOST=127.0.0.1
set DB_PORT=3306
set DB_NAME=wms878
set DB_USER=root

REM 使用第一个参数作为密码，默认为root
if "%~1"=="" (
    set DB_PASS=root
) else (
    set DB_PASS=%~1
)

set SQL_FILE=test-form-data.sql

echo === 表单设计器测试数据插入脚本 ===
echo 数据库: %DB_HOST%:%DB_PORT%/%DB_NAME%
echo 用户: %DB_USER%
echo SQL文件: %SQL_FILE%
echo.

REM 检查mysql客户端是否在PATH中
where mysql >nul 2>nul
if errorlevel 1 (
    echo 错误: mysql客户端未找到
    echo 请将MySQL的bin目录添加到PATH环境变量
    echo 或使用其他方式执行SQL文件
    pause
    exit /b 1
)

REM 检查SQL文件是否存在
if not exist "%SQL_FILE%" (
    echo 错误: SQL文件不存在: %SQL_FILE%
    pause
    exit /b 1
)

echo 正在连接数据库...
echo.

REM 执行SQL文件
mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASS% %DB_NAME% < "%SQL_FILE%"

if %errorlevel% equ 0 (
    echo.
    echo ✅ 测试数据插入成功！
    echo.
    echo 已插入的测试表单:
    echo 1. 用户注册表单 (user_registration)
    echo 2. 产品信息表单 (product_info)
    echo 3. 订单信息表单 (order_info)
    echo.
    echo 请访问 http://localhost:8080/app/form-designer/list 查看结果
) else (
    echo.
    echo ❌ 测试数据插入失败
    echo 请检查:
    echo 1. 数据库服务是否运行
    echo 2. 数据库连接信息是否正确
    echo 3. 用户名和密码是否正确
)

pause