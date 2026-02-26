@echo off
chcp 65001 >nul
echo ========================================
echo Alexa 控制器快速测试脚本 v2.0
echo ========================================
echo.
echo 更新内容:
echo - 新增 AcceptGrant 授权测试
echo - 新增 AdjustMode 模式调整测试
echo - 新增 Edge 模式测试
echo - 更新所有测试用例
echo.

REM 设置变量
set BASE_URL=http://localhost:8080
set TOKEN=YOUR_ACCESS_TOKEN_HERE

echo [提示] 请确保:
echo 1. 应用已启动 (mvn spring-boot:run 或 start.bat)
echo 2. 数据库已导入测试数据 (import-test-data.bat)
echo 3. 已获取有效的 access_token (通过 OAuth 流程)
echo 4. Alexa 配置已设置 (application.yml 或环境变量)
echo.
echo 当前使用的 Token: %TOKEN%
echo.
echo [重要] 如需修改 Token:
echo 1. 编辑此脚本，修改 TOKEN 变量
echo 2. 或者在运行前设置环境变量: set TOKEN=your_token
echo.
pause

echo.
echo ========================================
echo 测试 0: AcceptGrant 授权接受 (新增)
echo ========================================
echo 说明: 测试用授权码换取 Alexa Access Token
echo 注意: 这是首次授权时调用，成功后会保存 Token 到数据库
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.Authorization\",\"name\":\"AcceptGrant\",\"payloadVersion\":\"3\",\"messageId\":\"test-acceptgrant-001\"},\"payload\":{\"grant\":{\"type\":\"OAuth2.AuthorizationCode\",\"code\":\"test_authorization_code_123\"},\"grantee\":{\"type\":\"BearerToken\",\"token\":\"test_grantee_token_456\"}}}}"
echo.
echo 验证: 检查数据库 alexa_tokens 表是否有新记录
echo SQL: SELECT * FROM alexa_tokens WHERE user_id = 1;
echo.
pause

echo.
echo ========================================
echo 测试 1: 设备发现 (Discovery)
echo ========================================
echo 说明: 返回用户的所有设备列表
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.Discovery\",\"name\":\"Discover\",\"payloadVersion\":\"3\",\"messageId\":\"test-discovery-001\"},\"payload\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"}}}}"
echo.
echo 验证: 检查返回的设备列表是否包含所有必需的 capabilities
echo.
pause

echo.
echo ========================================
echo 测试 2: 开机 (TurnOn)
echo ========================================
echo 说明: 打开设备 device-001
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.PowerController\",\"name\":\"TurnOn\",\"payloadVersion\":\"3\",\"messageId\":\"test-turnon-001\",\"correlationToken\":\"test-corr-001\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo 验证: 检查响应中 powerState 是否为 ON
echo SQL: SELECT power_state FROM devices WHERE device_id = 'device-001';
echo.
pause

echo.
echo ========================================
echo 测试 3: 关机 (TurnOff)
echo ========================================
echo 说明: 关闭设备 device-001
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.PowerController\",\"name\":\"TurnOff\",\"payloadVersion\":\"3\",\"messageId\":\"test-turnoff-001\",\"correlationToken\":\"test-corr-002\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo 验证: 检查响应中 powerState 是否为 OFF
echo.
pause

echo.
echo ========================================
echo 测试 4: 设置模式为 Auto
echo ========================================
echo 说明: 设置清扫模式为自动模式
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.ModeController\",\"name\":\"SetMode\",\"payloadVersion\":\"3\",\"messageId\":\"test-setmode-001\",\"correlationToken\":\"test-corr-003\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{\"mode\":\"Auto\"}}}"
echo.
echo 验证: 检查响应中 mode 是否为 Auto
echo.
pause

echo.
echo ========================================
echo 测试 5: 设置模式为 Spot
echo ========================================
echo 说明: 设置清扫模式为定点清扫
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.ModeController\",\"name\":\"SetMode\",\"payloadVersion\":\"3\",\"messageId\":\"test-setmode-002\",\"correlationToken\":\"test-corr-004\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{\"mode\":\"Spot\"}}}"
echo.
echo 验证: 检查响应中 mode 是否为 Spot
echo.
pause

echo.
echo ========================================
echo 测试 6: 设置模式为 Edge (新增)
echo ========================================
echo 说明: 设置清扫模式为沿边清扫
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.ModeController\",\"name\":\"SetMode\",\"payloadVersion\":\"3\",\"messageId\":\"test-setmode-003\",\"correlationToken\":\"test-corr-005\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{\"mode\":\"Edge\"}}}"
echo.
echo 验证: 检查响应中 mode 是否为 Edge
echo.
pause

echo.
echo ========================================
echo 测试 7: 调整模式 (AdjustMode - 新增)
echo ========================================
echo 说明: 循环切换到下一个清扫模式
echo 模式顺序: Auto → Spot → Edge → Auto
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.ModeController\",\"name\":\"AdjustMode\",\"payloadVersion\":\"3\",\"messageId\":\"test-adjustmode-001\",\"correlationToken\":\"test-corr-006\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{\"modeDelta\":1}}}"
echo.
echo 验证: 检查响应中 mode 是否切换到下一个模式
echo.
pause

echo.
echo ========================================
echo 测试 8: 状态报告 (ReportState)
echo ========================================
echo 说明: 查询设备当前所有状态
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa\",\"name\":\"ReportState\",\"payloadVersion\":\"3\",\"messageId\":\"test-reportstate-001\",\"correlationToken\":\"test-corr-007\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo 验证: 检查响应包含 powerState, mode, connectivity 三个属性
echo.
pause

echo.
echo ========================================
echo 测试 9: 错误测试 - 设备不存在
echo ========================================
echo 说明: 测试访问不存在的设备
echo 预期: 返回 NO_SUCH_ENDPOINT 错误
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.PowerController\",\"name\":\"TurnOn\",\"payloadVersion\":\"3\",\"messageId\":\"test-error-001\",\"correlationToken\":\"test-corr-008\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"nonexistent_device\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo 验证: 检查响应中 type 是否为 NO_SUCH_ENDPOINT
echo.
pause

echo.
echo ========================================
echo 测试 10: 错误测试 - 无效Token
echo ========================================
echo 说明: 测试使用无效的访问令牌
echo 预期: 返回 INVALID_AUTHORIZATION_CREDENTIAL 错误
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.PowerController\",\"name\":\"TurnOn\",\"payloadVersion\":\"3\",\"messageId\":\"test-error-002\",\"correlationToken\":\"test-corr-009\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"INVALID_TOKEN_12345\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{}}}"
echo.
echo 验证: 检查响应中 type 是否为 INVALID_AUTHORIZATION_CREDENTIAL
echo.
pause

echo.
echo ========================================
echo 测试 11: 错误测试 - 无效模式
echo ========================================
echo 说明: 测试设置不支持的清扫模式
echo 预期: 返回 INVALID_VALUE 错误
echo.
curl -X POST %BASE_URL%/alexa ^
  -H "Content-Type: application/json" ^
  -d "{\"directive\":{\"header\":{\"namespace\":\"Alexa.ModeController\",\"name\":\"SetMode\",\"payloadVersion\":\"3\",\"messageId\":\"test-error-003\",\"correlationToken\":\"test-corr-010\"},\"endpoint\":{\"scope\":{\"type\":\"BearerToken\",\"token\":\"%TOKEN%\"},\"endpointId\":\"device-001\",\"cookie\":{}},\"payload\":{\"mode\":\"InvalidMode\"}}}"
echo.
echo 验证: 检查响应中 type 是否为 INVALID_VALUE
echo.
pause

echo.
echo ========================================
echo 测试完成!
echo ========================================
echo.
echo 测试总结:
echo - 总测试数: 11 个
echo - 功能测试: 8 个 (AcceptGrant, Discovery, Power, Mode, State)
echo - 错误测试: 3 个 (设备不存在, 无效Token, 无效模式)
echo.
echo 如果所有测试都返回 JSON 格式的响应,说明接口正常工作
echo.
echo 下一步:
echo 1. 检查数据库中的数据是否正确更新
echo 2. 查看应用日志确认请求处理流程
echo 3. 使用 Postman 导入 Alexa_Test_Requests.json 进行详细测试
echo 4. 在 Alexa App 中测试真实的语音控制
echo.
echo 数据库检查命令:
echo SELECT * FROM devices WHERE user_id = 1;
echo SELECT * FROM alexa_tokens WHERE user_id = 1;
echo SELECT * FROM oauth_access_tokens WHERE client_id = 'alexa-client';
echo.
echo 相关文档:
echo - Alexa功能测试指南.md
echo - Alexa控制器使用说明.md
echo - Alexa完善工作完成总结.md
echo.
pause
