@echo off
REM ============================================
REM 测试数据导入脚本 (Windows)
REM ============================================
REM 说明：
REM 1. 本脚本用于导入测试数据到 MySQL 数据库
REM 2. 请先确保数据库已创建并配置正确
REM 3. 请先替换 test-data.sql 中的 YOUR_* 占位符
REM ============================================

setlocal enabledelayedexpansion

REM 数据库配置
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=smarthomedb
set DB_USER=root
set DB_PASSWORD=

REM 测试数据文件
set TEST_DATA_FILE=src\main\resources\test-data.sql

echo ============================================
echo 测试数据导入脚本
echo ============================================
echo.

REM 检查测试数据文件是否存在
if not exist "%TEST_DATA_FILE%" (
    echo [错误] 测试数据文件不存在: %TEST_DATA_FILE%
    exit /b 1
)

REM 检查是否已替换占位符
findstr /C:"YOUR_ALIGENIE_CLIENT_ID" "%TEST_DATA_FILE%" >nul 2>&1
if !errorlevel! equ 0 (
    echo [警告] 检测到未替换的占位符 YOUR_*
    echo [警告] 建议先替换为实际的平台配置
    echo.
    set /p CONTINUE="是否继续导入？(Y/N): "
    if /i not "!CONTINUE!"=="Y" (
        echo 已取消导入
        exit /b 0
    )
)

REM 提示输入数据库密码
echo 请输入数据库密码（如果没有密码请直接按回车）：
set /p DB_PASSWORD=
echo.

REM 测试数据库连接
echo 正在测试数据库连接...
if "%DB_PASSWORD%"=="" (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -e "SELECT 1;" >nul 2>&1
) else (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% -e "SELECT 1;" >nul 2>&1
)

if !errorlevel! neq 0 (
    echo [错误] 无法连接到数据库
    echo 请检查数据库配置和密码是否正确
    exit /b 1
)

echo [成功] 数据库连接成功
echo.

REM 检查数据库是否存在
echo 正在检查数据库 %DB_NAME%...
if "%DB_PASSWORD%"=="" (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -e "SHOW DATABASES LIKE '%DB_NAME%';" | findstr "%DB_NAME%" >nul 2>&1
) else (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% -e "SHOW DATABASES LIKE '%DB_NAME%';" | findstr "%DB_NAME%" >nul 2>&1
)

if !errorlevel! neq 0 (
    echo [错误] 数据库 %DB_NAME% 不存在
    echo 请先运行 schema.sql 创建数据库
    exit /b 1
)

echo [成功] 数据库 %DB_NAME% 存在
echo.

REM 询问是否清空现有数据
echo [警告] 导入测试数据会覆盖现有数据
set /p CLEAR_DATA="是否清空现有数据？(Y/N): "
echo.

REM 导入测试数据
echo 正在导入测试数据...
if "%DB_PASSWORD%"=="" (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% %DB_NAME% < "%TEST_DATA_FILE%"
) else (
    mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% < "%TEST_DATA_FILE%"
)

if !errorlevel! neq 0 (
    echo [错误] 导入测试数据失败
    exit /b 1
)

echo [成功] 测试数据导入成功
echo.

REM 显示导入统计
echo 正在统计导入数据...
if "%DB_PASSWORD%"=="" (
    for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% %DB_NAME% -N -e "SELECT COUNT(*) FROM oauth_clients;"') do set OAUTH_COUNT=%%i
    for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% %DB_NAME% -N -e "SELECT COUNT(*) FROM users;"') do set USER_COUNT=%%i
    for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% %DB_NAME% -N -e "SELECT COUNT(*) FROM devices;"') do set DEVICE_COUNT=%%i
    for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% %DB_NAME% -N -e "SELECT COUNT(*) FROM devices WHERE status='online';"') do set ONLINE_COUNT=%%i
) else (
    for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -N -e "SELECT COUNT(*) FROM oauth_clients;"') do set OAUTH_COUNT=%%i
    for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -N -e "SELECT COUNT(*) FROM users;"') do set USER_COUNT=%%i
    for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -N -e "SELECT COUNT(*) FROM devices;"') do set DEVICE_COUNT=%%i
    for /f %%i in ('mysql -h %DB_HOST% -P %DB_PORT% -u %DB_USER% -p%DB_PASSWORD% %DB_NAME% -N -e "SELECT COUNT(*) FROM devices WHERE status='online';"') do set ONLINE_COUNT=%%i
)

set /a OFFLINE_COUNT=%DEVICE_COUNT%-%ONLINE_COUNT%

echo ============================================
echo 导入统计
echo ============================================
echo OAuth 客户端：%OAUTH_COUNT% 个
echo 测试用户：%USER_COUNT% 个
echo 扫地机器人：%DEVICE_COUNT% 台
echo 在线设备：%ONLINE_COUNT% 台
echo 离线设备：%OFFLINE_COUNT% 台
echo ============================================
echo.

echo [成功] 测试数据导入完成！
echo.
echo 下一步：
echo 1. 启动应用：start.bat 或 mvn spring-boot:run
echo 2. 使用 Postman 测试接口
echo 3. 在音箱 App 中进行语音测试
echo.
echo 测试账号：
echo   用户名：testuser
echo   密码：password123
echo.
echo 参考文档：
echo   - 测试数据说明.md
echo   - OAuth配置指南.md
echo   - 使用说明.md
echo.

pause
