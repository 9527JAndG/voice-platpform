@echo off
REM Google Assistant 快速测试脚本
REM 使用 curl 命令测试 Google Assistant 控制器的主要功能

echo ========================================
echo Google Assistant 快速测试
echo ========================================
echo.

set BASE_URL=http://localhost:8080/google/fulfillment
set TOKEN=test-access-token-123

echo [测试 1] SYNC Intent - 设备发现
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-001\",\"inputs\":[{\"intent\":\"action.devices.SYNC\"}]}"
echo.
echo.

echo [测试 2] QUERY Intent - 状态查询
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-002\",\"inputs\":[{\"intent\":\"action.devices.QUERY\",\"payload\":{\"devices\":[{\"id\":\"device-001\"}]}}]}"
echo.
echo.

echo [测试 3] EXECUTE Intent - OnOff 开机
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-003\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"device-001\"}],\"execution\":[{\"command\":\"action.devices.commands.OnOff\",\"params\":{\"on\":true}}]}]}}]}"
echo.
echo.

echo [测试 4] EXECUTE Intent - OnOff 关机
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-004\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"device-001\"}],\"execution\":[{\"command\":\"action.devices.commands.OnOff\",\"params\":{\"on\":false}}]}]}}]}"
echo.
echo.

echo [测试 5] EXECUTE Intent - StartStop 启动
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-005\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"device-001\"}],\"execution\":[{\"command\":\"action.devices.commands.StartStop\",\"params\":{\"start\":true}}]}]}}]}"
echo.
echo.

echo [测试 6] EXECUTE Intent - PauseUnpause 暂停
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-006\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"device-001\"}],\"execution\":[{\"command\":\"action.devices.commands.PauseUnpause\",\"params\":{\"pause\":true}}]}]}}]}"
echo.
echo.

echo [测试 7] EXECUTE Intent - Dock 回充
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-007\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"device-001\"}],\"execution\":[{\"command\":\"action.devices.commands.Dock\"}]}]}}]}"
echo.
echo.

echo [测试 8] EXECUTE Intent - SetModes 设置模式
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-008\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"device-001\"}],\"execution\":[{\"command\":\"action.devices.commands.SetModes\",\"params\":{\"updateModeSettings\":{\"clean_mode\":\"auto\"}}}]}]}}]}"
echo.
echo.

echo [测试 9] EXECUTE Intent - Locate 定位设备
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-009\",\"inputs\":[{\"intent\":\"action.devices.EXECUTE\",\"payload\":{\"commands\":[{\"devices\":[{\"id\":\"device-001\"}],\"execution\":[{\"command\":\"action.devices.commands.Locate\",\"params\":{\"silent\":false}}]}]}}]}"
echo.
echo.

echo [测试 10] DISCONNECT Intent - 账号解绑
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-010\",\"inputs\":[{\"intent\":\"action.devices.DISCONNECT\"}]}"
echo.
echo.

echo [测试 11] 错误测试 - 无效Token
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer invalid-token" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-011\",\"inputs\":[{\"intent\":\"action.devices.SYNC\"}]}"
echo.
echo.

echo [测试 12] 错误测试 - 不支持的Intent
curl -X POST %BASE_URL% ^
  -H "Authorization: Bearer %TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"requestId\":\"req-012\",\"inputs\":[{\"intent\":\"action.devices.UNSUPPORTED\"}]}"
echo.
echo.

echo ========================================
echo 测试完成！
echo ========================================
pause
