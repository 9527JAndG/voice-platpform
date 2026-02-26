@echo off
REM ========================================
REM 小爱同学快速测试脚本
REM ========================================
REM 版本: 2.0
REM 更新时间: 2026-02-25
REM 说明: 快速测试小米小爱同学智能家居控制 API
REM ========================================

setlocal enabledelayedexpansion

echo ========================================
echo 小爱同学 API 快速测试
echo ========================================
echo.

REM 配置
set BASE_URL=http://localhost:8080
set USERNAME=user1
set PASSWORD=password
set CLIENT_ID=miai-client
set CLIENT_SECRET=miai-secret

echo [1/14] 获取访问令牌...
curl -s -X POST "%BASE_URL%/oauth2/token" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "grant_type=password&username=%USERNAME%&password=%PASSWORD%&client_id=%CLIENT_ID%&client_secret=%CLIENT_SECRET%" ^
  > token_response.json

REM 提取 access_token (使用 PowerShell)
for /f "delims=" %%i in ('powershell -Command "(Get-Content token_response.json | ConvertFrom-Json).access_token"') do set ACCESS_TOKEN=%%i

if "%ACCESS_TOKEN%"=="" (
    echo ✗ 获取令牌失败
    type token_response.json
    del token_response.json
    exit /b 1
)

echo ✓ 令牌获取成功
echo.

REM ========================================
REM 测试 1: 健康检查
REM ========================================
echo [2/14] 测试健康检查...
curl -s -X GET "%BASE_URL%/miai/health" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 健康检查成功
    findstr /C:"xiaomi-miai" response.json >nul
    if !errorlevel!==0 (
        echo   - 平台标识正确
    )
) else (
    echo ✗ 健康检查失败
    type response.json
)
echo.

REM ========================================
REM 测试 2: 设备发现
REM ========================================
echo [3/14] 测试设备发现...
curl -s -X POST "%BASE_URL%/miai/discovery" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备发现成功
    findstr /C:"devices" response.json >nul
    if !errorlevel!==0 (
        echo   - 包含设备列表
    )
) else (
    echo ✗ 设备发现失败
    type response.json
)
echo.

REM ========================================
REM 测试 3: 设备开机
REM ========================================
echo [4/14] 测试设备开机...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"turn-on\",\"deviceId\":\"vacuum-001\",\"requestId\":\"req-turnon-001\"}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备开机成功
) else (
    echo ✗ 设备开机失败
    type response.json
)
echo.

REM ========================================
REM 测试 4: 设备状态查询
REM ========================================
echo [5/14] 测试设备状态查询...
curl -s -X POST "%BASE_URL%/miai/query" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"deviceId\":\"vacuum-001\",\"requestId\":\"req-query-001\"}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备查询成功
    findstr /C:"power_state" response.json >nul
    if !errorlevel!==0 (
        echo   - 包含电源状态
    )
) else (
    echo ✗ 设备查询失败
    type response.json
)
echo.

REM ========================================
REM 测试 5: 设备暂停
REM ========================================
echo [6/14] 测试设备暂停...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"pause\",\"deviceId\":\"vacuum-001\",\"requestId\":\"req-pause-001\"}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备暂停成功
) else (
    echo ✗ 设备暂停失败
    type response.json
)
echo.

REM ========================================
REM 测试 6: 设备继续
REM ========================================
echo [7/14] 测试设备继续...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"continue\",\"deviceId\":\"vacuum-001\",\"requestId\":\"req-continue-001\"}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备继续成功
) else (
    echo ✗ 设备继续失败
    type response.json
)
echo.

REM ========================================
REM 测试 7: 设置模式 - 自动模式
REM ========================================
echo [8/14] 测试设置模式 - 自动模式...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"set-mode\",\"deviceId\":\"vacuum-001\",\"requestId\":\"req-setmode-auto-001\",\"params\":{\"mode\":\"auto\"}}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设置自动模式成功
) else (
    echo ✗ 设置自动模式失败
    type response.json
)
echo.

REM ========================================
REM 测试 8: 设置模式 - 定点模式
REM ========================================
echo [9/14] 测试设置模式 - 定点模式...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"set-mode\",\"deviceId\":\"vacuum-001\",\"requestId\":\"req-setmode-spot-001\",\"params\":{\"mode\":\"spot\"}}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设置定点模式成功
) else (
    echo ✗ 设置定点模式失败
    type response.json
)
echo.

REM ========================================
REM 测试 9: 设置模式 - 沿边模式
REM ========================================
echo [10/14] 测试设置模式 - 沿边模式...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"set-mode\",\"deviceId\":\"vacuum-001\",\"requestId\":\"req-setmode-edge-001\",\"params\":{\"mode\":\"edge\"}}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设置沿边模式成功
) else (
    echo ✗ 设置沿边模式失败
    type response.json
)
echo.

REM ========================================
REM 测试 10: 设备关机
REM ========================================
echo [11/14] 测试设备关机...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"turn-off\",\"deviceId\":\"vacuum-001\",\"requestId\":\"req-turnoff-001\"}" ^
  > response.json

findstr /C:"\"code\":0" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备关机成功
) else (
    echo ✗ 设备关机失败
    type response.json
)
echo.

REM ========================================
REM 测试 11: 错误处理 - 设备不存在
REM ========================================
echo [12/14] 测试错误处理 - 设备不存在...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"turn-on\",\"deviceId\":\"non-existent\",\"requestId\":\"req-error-001\"}" ^
  > response.json

findstr /C:"\"code\":404" response.json >nul
if %errorlevel%==0 (
    echo ✓ 错误处理正确
) else (
    echo ✗ 错误处理失败
    type response.json
)
echo.

REM ========================================
REM 测试 12: 错误处理 - 无效模式
REM ========================================
echo [13/14] 测试错误处理 - 无效模式...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"intent\":\"set-mode\",\"deviceId\":\"vacuum-001\",\"requestId\":\"req-error-002\",\"params\":{\"mode\":\"invalid\"}}" ^
  > response.json

findstr /C:"\"code\":400" response.json >nul
if %errorlevel%==0 (
    echo ✓ 错误处理正确
) else (
    echo ✗ 错误处理失败
    type response.json
)
echo.

REM ========================================
REM 测试 13: 错误处理 - 缺少Intent参数
REM ========================================
echo [14/14] 测试错误处理 - 缺少Intent参数...
curl -s -X POST "%BASE_URL%/miai/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"deviceId\":\"vacuum-001\",\"requestId\":\"req-error-003\"}" ^
  > response.json

findstr /C:"\"code\":400" response.json >nul
if %errorlevel%==0 (
    echo ✓ 错误处理正确
) else (
    echo ✗ 错误处理失败
    type response.json
)
echo.

REM 清理临时文件
del token_response.json response.json 2>nul

echo ========================================
echo 测试完成
echo ========================================
echo.
echo 提示: 查看服务器日志以获取详细信息
echo.

endlocal
pause
