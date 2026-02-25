@echo off
chcp 65001 >nul
echo ========================================
echo Alexa 控制器快速测试脚本
echo ========================================
echo.

REM 设置变量
set BASE_URL=http://localhost:8080
set TOKEN=test_token_123

echo [提示] 请确保:
echo 1. 应用已启动 (mvn spring-boot:run)
echo 2. 数据库已导入测试数据
echo 3. 已获取有效的 access_token
echo.
echo 当前使用的 Token: %TOKEN%
echo 如需修改,请编辑此脚本的 TOKEN 变量
echo.
pause

echo.
echo ========================================
echo 测试 1: 设备发现
echo ========================================
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.Discovery\",\"name\":\"Discover\",\"payloadVersion\":\"3\",\"messageId\":\"test-001\"},\"payload\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"}}}}"
echo.
echo.
pause

echo.
echo ========================================
echo 测试 2: 开机 (robot_001)
echo ========================================
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.PowerController\",\"name\":\"TurnOn\",\"payloadVersion\":\"3\",\"messageId\":\"test-002\",\"correlationToken\":\"test-corr-001\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"robot_001\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo.
pause

echo.
echo ========================================
echo 测试 3: 关机 (robot_001)
echo ========================================
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.PowerController\",\"name\":\"TurnOff\",\"payloadVersion\":\"3\",\"messageId\":\"test-003\",\"correlationToken\":\"test-corr-002\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"robot_001\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo.
pause

echo.
echo ========================================
echo 测试 4: 设置模式为 Spot
echo ========================================
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.ModeController\",\"name\":\"SetMode\",\"payloadVersion\":\"3\",\"messageId\":\"test-004\",\"correlationToken\":\"test-corr-003\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"robot_001\",\"cookie\":{}},\"payload\":{\"mode\":\"Spot\"}}}"
echo.
echo.
pause

echo.
echo ========================================
echo 测试 5: 状态报告
echo ========================================
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa\",\"name\":\"ReportState\",\"payloadVersion\":\"3\",\"messageId\":\"test-005\",\"correlationToken\":\"test-corr-004\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"robot_001\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo.
pause

echo.
echo ========================================
echo 测试 6: 错误测试 - 设备不存在
echo ========================================
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.PowerController\",\"name\":\"TurnOn\",\"payloadVersion\":\"3\",\"messageId\":\"test-006\",\"correlationToken\":\"test-corr-005\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"nonexistent\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo.
pause

echo.
echo ========================================
echo 测试完成!
echo ========================================
echo.
echo 请查看上面的响应结果
echo 如果看到 JSON 格式的响应,说明接口正常工作
echo.
echo 详细测试请使用 Postman 导入 Alexa_Test_Requests.json
echo.
pause
