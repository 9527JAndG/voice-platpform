@echo off
chcp 65001 >nul
echo ========================================
echo 小度音箱（DuerOS）快速测试脚本
echo ========================================
echo.

REM 设置变量
set BASE_URL=http://localhost:8080
set CLIENT_ID=xiaodu_client_id
set CLIENT_SECRET=xiaodu_client_secret
set DEVICE_ID=robot_001

echo [步骤 1] 获取访问令牌...
echo.
echo 请先访问以下URL进行授权：
echo %BASE_URL%/authorize?client_id=%CLIENT_ID%^&redirect_uri=https://dueros.baidu.com/callback^&state=test123^&response_type=code
echo.
echo 授权后，请输入获取到的 code:
set /p AUTH_CODE=

echo.
echo [步骤 2] 使用 code 换取 access_token...
curl -X POST "%BASE_URL%/token" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "grant_type=authorization_code" ^
  -d "client_id=%CLIENT_ID%" ^
  -d "client_secret=%CLIENT_SECRET%" ^
  -d "code=%AUTH_CODE%"

echo.
echo.
echo 请从上面的响应中复制 access_token 的值
set /p ACCESS_TOKEN=请输入 access_token: 

echo.
echo ========================================
echo 开始测试小度音箱接口
echo ========================================

echo.
echo [测试 1] 设备发现
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/discovery" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Discovery\",\"name\":\"DiscoverAppliancesRequest\",\"messageId\":\"test-discovery-001\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\"}}"

echo.
echo.
echo [测试 2] 开机
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/control" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Control\",\"name\":\"TurnOnRequest\",\"messageId\":\"test-control-001\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\",\"appliance\":{\"applianceId\":\"%DEVICE_ID%\"}}}"

echo.
echo.
echo [测试 3] 关机
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/control" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Control\",\"name\":\"TurnOffRequest\",\"messageId\":\"test-control-002\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\",\"appliance\":{\"applianceId\":\"%DEVICE_ID%\"}}}"

echo.
echo.
echo [测试 4] 暂停
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/control" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Control\",\"name\":\"PauseRequest\",\"messageId\":\"test-control-003\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\",\"appliance\":{\"applianceId\":\"%DEVICE_ID%\"}}}"

echo.
echo.
echo [测试 5] 继续
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/control" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Control\",\"name\":\"ContinueRequest\",\"messageId\":\"test-control-004\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\",\"appliance\":{\"applianceId\":\"%DEVICE_ID%\"}}}"

echo.
echo.
echo [测试 6] 设置模式 - 自动模式
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/control" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Control\",\"name\":\"SetModeRequest\",\"messageId\":\"test-control-005\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\",\"appliance\":{\"applianceId\":\"%DEVICE_ID%\"},\"additionalInfo\":{\"mode\":\"auto\"}}}"

echo.
echo.
echo [测试 7] 设置模式 - 定点模式
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/control" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Control\",\"name\":\"SetModeRequest\",\"messageId\":\"test-control-006\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\",\"appliance\":{\"applianceId\":\"%DEVICE_ID%\"},\"additionalInfo\":{\"mode\":\"spot\"}}}"

echo.
echo.
echo [测试 8] 设置模式 - 沿边模式
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/control" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Control\",\"name\":\"SetModeRequest\",\"messageId\":\"test-control-007\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\",\"appliance\":{\"applianceId\":\"%DEVICE_ID%\"},\"additionalInfo\":{\"mode\":\"edge\"}}}"

echo.
echo.
echo [测试 9] 查询设备状态
echo ----------------------------------------
curl -X POST "%BASE_URL%/dueros/control" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"DuerOS.ConnectedHome.Query\",\"name\":\"GetStateRequest\",\"messageId\":\"test-query-001\",\"payloadVersion\":\"1.0\"},\"payload\":{\"accessToken\":\"%ACCESS_TOKEN%\",\"appliance\":{\"applianceId\":\"%DEVICE_ID%\"}}}"

echo.
echo.
echo ========================================
echo 测试完成！
echo ========================================
echo.
pause
