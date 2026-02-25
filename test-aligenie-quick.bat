@echo off
REM 天猫精灵（AliGenie）快速测试脚本
REM 使用 curl 命令测试天猫精灵控制器的主要功能

echo ========================================
echo 天猫精灵（AliGenie）快速测试
echo ========================================
echo.

set BASE_URL=http://localhost:8080/aligenie
set TOKEN=test-access-token-123

echo [测试 1] 设备发现
curl -X POST %BASE_URL%/discovery ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Discovery\",\"name\":\"DiscoveryDevices\",\"messageId\":\"msg-001\",\"payloadVersion\":\"1\"},\"payload\":{}}"
echo.
echo.

echo [测试 2] 设备开机
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"TurnOn\",\"messageId\":\"msg-002\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"device-001\"}}"
echo.
echo.

echo [测试 3] 设备关机
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"TurnOff\",\"messageId\":\"msg-003\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"device-001\"}}"
echo.
echo.

echo [测试 4] 设备暂停
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"Pause\",\"messageId\":\"msg-004\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"device-001\"}}"
echo.
echo.

echo [测试 5] 设备继续
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"Continue\",\"messageId\":\"msg-005\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"device-001\"}}"
echo.
echo.

echo [测试 6] 设置模式 - 自动模式
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"SetMode\",\"messageId\":\"msg-006\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"device-001\",\"mode\":\"auto\"}}"
echo.
echo.

echo [测试 7] 设置模式 - 定点模式
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"SetMode\",\"messageId\":\"msg-007\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"device-001\",\"mode\":\"spot\"}}"
echo.
echo.

echo [测试 8] 设备查询
curl -X POST %BASE_URL%/query ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Query\",\"name\":\"Query\",\"messageId\":\"msg-008\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"device-001\"}}"
echo.
echo.

echo [测试 9] 错误测试 - 无效Token
curl -X POST %BASE_URL%/discovery ^
  -H "Authorization: Bearer invalid-token" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Discovery\",\"name\":\"DiscoveryDevices\",\"messageId\":\"msg-009\",\"payloadVersion\":\"1\"},\"payload\":{}}"
echo.
echo.

echo [测试 10] 错误测试 - 无效模式
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"header\":{\"namespace\":\"AliGenie.Iot.Device.Control\",\"name\":\"SetMode\",\"messageId\":\"msg-010\",\"payloadVersion\":\"1\"},\"payload\":{\"deviceId\":\"device-001\",\"mode\":\"invalid\"}}"
echo.
echo.

echo ========================================
echo 测试完成！
echo ========================================
pause
