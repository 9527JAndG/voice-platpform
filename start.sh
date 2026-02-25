#!/bin/bash

echo "========================================"
echo "天猫精灵智能家居服务启动脚本"
echo "========================================"
echo ""

echo "[1/3] 检查 Maven 环境..."
if ! command -v mvn &> /dev/null; then
    echo "[错误] 未检测到 Maven，请先安装 Maven"
    exit 1
fi
echo "[✓] Maven 环境正常"

echo ""
echo "[2/3] 检查 MySQL 连接..."
echo "请确保 MySQL 已启动并且已执行 schema.sql 脚本"
echo ""

echo "[3/3] 启动应用..."
echo ""
mvn spring-boot:run
