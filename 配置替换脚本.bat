@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

REM ============================================
REM 多平台智能音箱对接项目 - 配置替换脚本 (Windows)
REM ============================================
REM 说明：
REM 本脚本用于快速替换 test-data.sql 中的平台配置占位符
REM 使用方法：
REM   1. 编辑本脚本，填写实际的平台配置信息
REM   2. 双击运行或执行：配置替换脚本.bat
REM   3. 导入生成的 test-data-configured.sql
REM ============================================

echo ============================================
echo 多平台智能音箱对接项目 - 配置替换脚本
echo ============================================
echo.

REM ============================================
REM 配置信息（请修改为实际值）
REM ============================================

REM 天猫精灵配置
set ALIGENIE_CLIENT_ID=YOUR_ALIGENIE_CLIENT_ID
set ALIGENIE_CLIENT_SECRET=YOUR_ALIGENIE_CLIENT_SECRET

REM 小度音箱配置
set DUEROS_CLIENT_ID=YOUR_DUEROS_CLIENT_ID
set DUEROS_CLIENT_SECRET=YOUR_DUEROS_CLIENT_SECRET

REM 小爱同学配置
set MIAI_CLIENT_ID=YOUR_MIAI_CLIENT_ID
set MIAI_CLIENT_SECRET=YOUR_MIAI_CLIENT_SECRET

REM ============================================
REM 检查配置是否已修改
REM ============================================

set HAS_ERROR=0

if "%ALIGENIE_CLIENT_ID%"=="YOUR_ALIGENIE_CLIENT_ID" (
    echo [警告] 天猫精灵 CLIENT_ID 未配置
    set HAS_ERROR=1
)

if "%DUEROS_CLIENT_ID%"=="YOUR_DUEROS_CLIENT_ID" (
    echo [警告] 小度音箱 CLIENT_ID 未配置
    set HAS_ERROR=1
)

if "%MIAI_CLIENT_ID%"=="YOUR_MIAI_CLIENT_ID" (
    echo [警告] 小爱同学 CLIENT_ID 未配置
    set HAS_ERROR=1
)

if %HAS_ERROR%==1 (
    echo.
    echo [错误] 请先编辑本脚本，填写实际的平台配置信息！
    echo.
    echo 配置步骤：
    echo 1. 右键编辑本脚本：配置替换脚本.bat
    echo 2. 修改 ALIGENIE_CLIENT_ID 等变量的值
    echo 3. 保存后重新运行本脚本
    echo.
    pause
    exit /b 1
)

REM ============================================
REM 显示配置信息
REM ============================================

echo 当前配置信息：
echo ----------------------------------------
echo 天猫精灵：
echo   Client ID: %ALIGENIE_CLIENT_ID:~0,20%...
echo   Client Secret: %ALIGENIE_CLIENT_SECRET:~0,20%...
echo.
echo 小度音箱：
echo   Client ID: %DUEROS_CLIENT_ID:~0,20%...
echo   Client Secret: %DUEROS_CLIENT_SECRET:~0,20%...
echo.
echo 小爱同学：
echo   Client ID: %MIAI_CLIENT_ID:~0,20%...
echo   Client Secret: %MIAI_CLIENT_SECRET:~0,20%...
echo ----------------------------------------
echo.

REM ============================================
REM 执行替换
REM ============================================

set INPUT_FILE=src\main\resources\test-data.sql
set OUTPUT_FILE=src\main\resources\test-data-configured.sql

REM 检查输入文件是否存在
if not exist "%INPUT_FILE%" (
    echo [错误] 找不到文件 %INPUT_FILE%
    pause
    exit /b 1
)

echo 正在替换配置...
echo.

REM 使用 PowerShell 进行替换
powershell -Command "(Get-Content '%INPUT_FILE%') -replace 'YOUR_ALIGENIE_CLIENT_ID', '%ALIGENIE_CLIENT_ID%' -replace 'YOUR_ALIGENIE_CLIENT_SECRET', '%ALIGENIE_CLIENT_SECRET%' -replace 'YOUR_DUEROS_CLIENT_ID', '%DUEROS_CLIENT_ID%' -replace 'YOUR_DUEROS_CLIENT_SECRET', '%DUEROS_CLIENT_SECRET%' -replace 'YOUR_MIAI_CLIENT_ID', '%MIAI_CLIENT_ID%' -replace 'YOUR_MIAI_CLIENT_SECRET', '%MIAI_CLIENT_SECRET%' | Set-Content '%OUTPUT_FILE%' -Encoding UTF8"

if %ERRORLEVEL% EQU 0 (
    echo [成功] 天猫精灵配置已替换
    echo [成功] 小度音箱配置已替换
    echo [成功] 小爱同学配置已替换
    echo.
    echo [完成] 配置替换完成！
    echo.
    echo 生成的文件：%OUTPUT_FILE%
    echo.
) else (
    echo [错误] 配置替换失败！
    pause
    exit /b 1
)

REM ============================================
REM 导入数据库
REM ============================================

echo 是否立即导入数据库？(Y/N)
set /p ANSWER=

if /i "%ANSWER%"=="Y" (
    echo.
    echo 请输入 MySQL 配置信息：
    
    set /p DB_HOST=主机 (默认: localhost): 
    if "%DB_HOST%"=="" set DB_HOST=localhost
    
    set /p DB_PORT=端口 (默认: 3306): 
    if "%DB_PORT%"=="" set DB_PORT=3306
    
    set /p DB_USER=用户名 (默认: root): 
    if "%DB_USER%"=="" set DB_USER=root
    
    set /p DB_PASSWORD=密码: 
    
    set /p DB_NAME=数据库名 (默认: smarthomedb):
    if "%DB_NAME%"=="" set DB_NAME=smarthomedb
    
    echo.
    echo 正在导入数据...
    
    mysql -h%DB_HOST% -P%DB_PORT% -u%DB_USER% -p%DB_PASSWORD% %DB_NAME% < "%OUTPUT_FILE%"
    
    if %ERRORLEVEL% EQU 0 (
        echo [成功] 数据导入成功！
    ) else (
        echo [错误] 数据导入失败！
        echo 请检查 MySQL 配置是否正确
        pause
        exit /b 1
    )
) else (
    echo.
    echo 跳过数据库导入。
    echo 稍后可以手动导入：
    echo mysql -u root -p smarthomedb ^< %OUTPUT_FILE%
)

echo.
echo ============================================
echo 配置完成！
echo ============================================
echo.
echo 下一步：
echo 1. 启动应用：mvn spring-boot:run
echo 2. 测试接口：使用 Postman 测试集合
echo 3. 语音测试：在各平台 App 中进行语音控制
echo.
echo 详细说明请查看：平台配置说明.md
echo.

pause
