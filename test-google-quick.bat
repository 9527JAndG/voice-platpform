@echo off
chcp 65001 >nul
echo ========================================
echo Google Assistant 快速测试脚本 v2.0
echo ========================================
echo.

REM 配置
set BASE_URL=http://localhost:8080
set USERNAME=user1
set PASSWORD=password
set CLIENT_ID=test_client
set CLIENT_SECRET=test_secret

echo [步骤 1] 获取 Access Token...
echo.

curl -s -X POST "%BASE_URL%/oauth2/token" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "grant_type=password&username=%USERNAME%&password=%PASSWORD%&client_id=%CLIENT_ID%&client_secret=%CLIENT_SECRET%" > token_response.json

REM 提取 access_token（简化版本，实际需要 JSON 解析工具）
for /f "tokens=2 delims=:," %%a in ('findstr "access_token" token_response.json') do (
    set TOKEN=%%a
)
set TOKEN=%TOKEN:"=%
set TOKEN=%TOKEN: =%

if "%TOKEN%"=="" (
    echo ❌ 获取 Token 失败
    type token_response.json
    pause
    exit /b 1
)

echo ✓ Token 获取成功
echo Token: %TOKEN:~0,20%...
echo.
echo ========================================
echo.

REM 测试 1: SYNC - 设备发现
echo [测试 1] SYNC - 设备发现
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-sync-001\",\"inputs\":[{\"intent\":\"action.devices.SYNC\"}]}"
echo.
echo ========================================
echo.

REM 测试 2: QUERY - 查询设备状态
echo [测试 2] QUERY - 查询设备状态
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-query-001\",\"inputs\":[{\"intent\":\"action.devices.QUERY\",\"payload\":{\"devices\":[{\"id\":\"robot_001\"}]}}]}"
echo.
echo ========================================
echo.

REM 测试 3: EXECUTE - 打开设备
echo [测试 3] EXECUTE - 打开设备
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-001\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.OnOff\",\"params\":{\"on\":true}}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 4: EXECUTE - 关闭设备
echo [测试 4] EXECUTE - 关闭设备
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-002\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.OnOff\",\"params\":{\"on\":false}}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 5: EXECUTE - 启动清扫
echo [测试 5] EXECUTE - 启动清扫
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-003\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.StartStop\",\"params\":{\"start\":true}}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 6: EXECUTE - 停止清扫
echo [测试 6] EXECUTE - 停止清扫
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-004\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.StartStop\",\"params\":{\"start\":false}}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 7: EXECUTE - 暂停清扫
echo [测试 7] EXECUTE - 暂停清扫
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-005\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.PauseUnpause\",\"params\":{\"pause\":true}}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 8: EXECUTE - 回充
echo [测试 8] EXECUTE - 回充
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-006\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.Dock\"}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 9: EXECUTE - 设置清扫模式（自动）
echo [测试 9] EXECUTE - 设置清扫模式（自动）
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-007\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.SetModes\",\"params\":{\"updateModeSettings\":{\"clean_mode\":\"auto\"}}}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 10: EXECUTE - 设置清扫模式（定点）
echo [测试 10] EXECUTE - 设置清扫模式（定点）
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-008\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.SetModes\",\"params\":{\"updateModeSettings\":{\"clean_mode\":\"spot\"}}}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 11: EXECUTE - 定位设备
echo [测试 11] EXECUTE - 定位设备
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-execute-009\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"robot_001\"}],\"execution\":[{\"command\":\"action.devices.commands.Locate\",\"params\":{\"silent\":false}}]}]}}]}"
echo.
echo ========================================
echo.

REM 测试 12: DISCONNECT - 账号解绑
echo [测试 12] DISCONNECT - 账号解绑
curl -s -X POST "%BASE_URL%/google/fulfillment" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TOKEN%" ^
  -d "{\"requestId\":\"test-disconnect-001\",\"inputs\":[{\"intent\":\"action.devices.DISCONNECT\"}]}"
echo.
echo ========================================
echo.

REM 清理临时文件
del token_response.json 2>nul

echo.
echo ✓ 所有测试完成！
echo.
echo 说明：
echo - 测试账号: %USERNAME%/%PASSWORD%
echo - 测试设备: robot_001
echo - 基础 URL: %BASE_URL%
echo.
pause
