@echo off
REM ========================================
REM 天猫精灵快速测试脚本
REM ========================================
REM 版本: 2.0
REM 更新时间: 2026-02-25
REM 说明: 快速测试天猫精灵智能家居控制 API
REM ========================================

setlocal enabledelayedexpansion

echo ========================================
echo 天猫精灵 API 快速测试
echo ========================================
echo.

REM 配置
set BASE_URL=http://localhost:8080
set USERNAME=user1
set PASSWORD=password
set CLIENT_ID=aligenie-client
set CLIENT_SECRET=aligenie-secret

echo [1/12] 获取访问令牌...
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
REM 测试 1: 设备发现
REM ========================================
echo [2/12] 测试设备发现...
curl -s -X POST "%BASE_URL%/aligenie/discovery" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Discovery\",\"name\":\"DiscoveryDevices\",\"messageId\":\"msg-discovery-001\",\"payloadVersion\":\"1\"},\"payload\":{}}" ^
  > response.json

findstr /C:"DiscoveryResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备发现成功
) else (
    echo ✗ 设备发现失败
    type response.json
)
echo.

REM ========================================
REM 测试 2: 设备开机
REM ========================================
echo [3/12] 测试设备开机...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"TurnOn\",\"messageId\":\"msg-turnon-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\"}}" ^
  > response.json

findstr /C:"TurnOnResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备开机成功
) else (
    echo ✗ 设备开机失败
    type response.json
)
echo.

REM ========================================
REM 测试 3: 设备状态查询
REM ========================================
echo [4/12] 测试设备状态查询...
curl -s -X POST "%BASE_URL%/aligenie/query" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Query\",\"name\":\"Query\",\"messageId\":\"msg-query-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\"}}" ^
  > response.json

findstr /C:"QueryResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备查询成功
    findstr /C:"powerstate" response.json >nul
    if !errorlevel!==0 (
        echo   - 包含电源状态
    )
) else (
    echo ✗ 设备查询失败
    type response.json
)
echo.

REM ========================================
REM 测试 4: 设备暂停
REM ========================================
echo [5/12] 测试设备暂停...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"Pause\",\"messageId\":\"msg-pause-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\"}}" ^
  > response.json

findstr /C:"PauseResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备暂停成功
) else (
    echo ✗ 设备暂停失败
    type response.json
)
echo.

REM ========================================
REM 测试 5: 设备继续
REM ========================================
echo [6/12] 测试设备继续...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"Continue\",\"messageId\":\"msg-continue-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\"}}" ^
  > response.json

findstr /C:"ContinueResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备继续成功
) else (
    echo ✗ 设备继续失败
    type response.json
)
echo.

REM ========================================
REM 测试 6: 设置模式 - 自动模式
REM ========================================
echo [7/12] 测试设置模式 - 自动模式...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"SetMode\",\"messageId\":\"msg-setmode-auto-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\",\"mode\":\"auto\"}}" ^
  > response.json

findstr /C:"SetModeResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设置自动模式成功
) else (
    echo ✗ 设置自动模式失败
    type response.json
)
echo.

REM ========================================
REM 测试 7: 设置模式 - 定点模式
REM ========================================
echo [8/12] 测试设置模式 - 定点模式...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"SetMode\",\"messageId\":\"msg-setmode-spot-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\",\"mode\":\"spot\"}}" ^
  > response.json

findstr /C:"SetModeResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设置定点模式成功
) else (
    echo ✗ 设置定点模式失败
    type response.json
)
echo.

REM ========================================
REM 测试 8: 设置模式 - 沿边模式
REM ========================================
echo [9/12] 测试设置模式 - 沿边模式...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"SetMode\",\"messageId\":\"msg-setmode-edge-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\",\"mode\":\"edge\"}}" ^
  > response.json

findstr /C:"SetModeResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设置沿边模式成功
) else (
    echo ✗ 设置沿边模式失败
    type response.json
)
echo.

REM ========================================
REM 测试 9: 设备关机
REM ========================================
echo [10/12] 测试设备关机...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"TurnOff\",\"messageId\":\"msg-turnoff-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\"}}" ^
  > response.json

findstr /C:"TurnOffResponse" response.json >nul
if %errorlevel%==0 (
    echo ✓ 设备关机成功
) else (
    echo ✗ 设备关机失败
    type response.json
)
echo.

REM ========================================
REM 测试 10: 错误处理 - 设备不存在
REM ========================================
echo [11/12] 测试错误处理 - 设备不存在...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"TurnOn\",\"messageId\":\"msg-error-001\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"non-existent\"}}" ^
  > response.json

findstr /C:"DEVICE_NOT_FOUND" response.json >nul
if %errorlevel%==0 (
    echo ✓ 错误处理正确
) else (
    echo ✗ 错误处理失败
    type response.json
)
echo.

REM ========================================
REM 测试 11: 错误处理 - 无效模式
REM ========================================
echo [12/12] 测试错误处理 - 无效模式...
curl -s -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"SetMode\",\"messageId\":\"msg-error-002\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"vacuum-001\",\"mode\":\"invalid\"}}" ^
  > response.json

findstr /C:"INVALID_MODE" response.json >nul
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
