@echo off
REM 小爱同学（MiAI）快速测试脚本
REM 使用 curl 命令测试小爱同学控制器的主要功能

echo ========================================
echo 小爱同学（MiAI）快速测试
echo ========================================
echo.

set BASE_URL=http://localhost:8080/miai
set TOKEN=test-access-token-123

echo [测试 1] 健康检查
curl -X GET %BASE_URL%/health
echo.
echo.

echo [测试 2] 设备发现
curl -X POST %BASE_URL%/discovery ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json"
echo.
echo.

echo [测试 3] 设备开机
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"intent\":\"turn-on\",\"deviceId\":\"device-001\",\"requestId\":\"req-001\"}"
echo.
echo.

echo [测试 4] 设备关机
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"intent\":\"turn-off\",\"deviceId\":\"device-001\",\"requestId\":\"req-002\"}"
echo.
echo.

echo [测试 5] 设备暂停
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"intent\":\"pause\",\"deviceId\":\"device-001\",\"requestId\":\"req-003\"}"
echo.
echo.

echo [测试 6] 设备继续
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"intent\":\"continue\",\"deviceId\":\"device-001\",\"requestId\":\"req-004\"}"
echo.
echo.

echo [测试 7] 设置模式 - 自动模式
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"intent\":\"set-mode\",\"deviceId\":\"device-001\",\"requestId\":\"req-005\",\"params\":{\"mode\":\"auto\"}}"
echo.
echo.

echo [测试 8] 设置模式 - 定点模式
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"intent\":\"set-mode\",\"deviceId\":\"device-001\",\"requestId\":\"req-006\",\"params\":{\"mode\":\"spot\"}}"
echo.
echo.

echo [测试 9] 设备查询
curl -X POST %BASE_URL%/query ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"deviceId\":\"device-001\",\"requestId\":\"req-007\"}"
echo.
echo.

echo [测试 10] 错误测试 - 无效Token
curl -X POST %BASE_URL%/discovery ^
  -H "Authorization: Bearer invalid-token" ^
  -H "Content-Type: application/json"
echo.
echo.

echo [测试 11] 错误测试 - 无效模式
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"intent\":\"set-mode\",\"deviceId\":\"device-001\",\"requestId\":\"req-008\",\"params\":{\"mode\":\"invalid\"}}"
echo.
echo.

echo [测试 12] 错误测试 - 缺少参数
curl -X POST %BASE_URL%/control ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"deviceId\":\"device-001\",\"requestId\":\"req-009\"}"
echo.
echo.

echo ========================================
echo 测试完成！
echo ========================================
pause
