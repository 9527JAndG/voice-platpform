@echo off
chcp 65001 >nul
echo ========================================
echo 天猫精灵功能测试脚本
echo ========================================
echo.

set BASE_URL=http://localhost:8080
set ACCESS_TOKEN=

echo 步骤 1: 获取访问令牌
echo ----------------------------------------
curl -X POST "%BASE_URL%/oauth2/token" ^
  -H "Content-Type: application/x-www-form-urlencoded" ^
  -d "grant_type=client_credentials&client_id=aligenie-client&client_secret=aligenie-secret&scope=device:control,device:read"
echo.
echo.
echo 请复制上面返回的 access_token，然后按任意键继续...
pause >nul

set /p ACCESS_TOKEN=请输入 access_token: 
echo.

echo 步骤 2: 测试设备发现
echo ----------------------------------------
curl -X POST "%BASE_URL%/aligenie/discovery" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Discovery\",\"name\":\"DiscoveryDevices\",\"messageId\":\"test-msg-001\",\"payloadVersion\":\"1\"},\"payload\":{}}"
echo.
echo.

echo 步骤 3: 测试开机命令
echo ----------------------------------------
curl -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"TurnOn\",\"messageId\":\"test-msg-002\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"robot_001\"}}"
echo.
echo.

echo 步骤 4: 测试关机命令
echo ----------------------------------------
curl -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"TurnOff\",\"messageId\":\"test-msg-003\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"robot_001\"}}"
echo.
echo.

echo 步骤 5: 测试暂停命令
echo ----------------------------------------
curl -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"Pause\",\"messageId\":\"test-msg-004\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"robot_001\"}}"
echo.
echo.

echo 步骤 6: 测试继续命令
echo ----------------------------------------
curl -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"Continue\",\"messageId\":\"test-msg-005\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"robot_001\"}}"
echo.
echo.

echo 步骤 7: 测试设置模式命令
echo ----------------------------------------
curl -X POST "%BASE_URL%/aligenie/control" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"SetMode\",\"messageId\":\"test-msg-006\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"robot_001\",\"mode\":\"auto\"}}"
echo.
echo.

echo 步骤 8: 测试设备查询
echo ----------------------------------------
curl -X POST "%BASE_URL%/aligenie/query" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Query\",\"name\":\"Query\",\"messageId\":\"test-msg-007\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"robot_001\"}}"
echo.
echo.

echo ========================================
echo 测试完成！
echo ========================================
pause
