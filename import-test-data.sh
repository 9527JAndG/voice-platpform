#!/bin/bash

# ============================================
# 测试数据导入脚本
# ============================================
# 说明：
# 1. 本脚本用于导入测试数据到 MySQL 数据库
# 2. 请先确保数据库已创建并配置正确
# 3. 请先替换 test-data.sql 中的 YOUR_* 占位符
# ============================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 数据库配置
DB_HOST="localhost"
DB_PORT="3306"
DB_NAME="smarthomedb"
DB_USER="root"
DB_PASSWORD=""

# 测试数据文件
TEST_DATA_FILE="src/main/resources/test-data.sql"

echo "============================================"
echo "测试数据导入脚本"
echo "============================================"
echo ""

# 检查测试数据文件是否存在
if [ ! -f "$TEST_DATA_FILE" ]; then
    echo -e "${RED}错误：测试数据文件不存在: $TEST_DATA_FILE${NC}"
    exit 1
fi

# 检查是否已替换占位符
if grep -q "YOUR_ALIGENIE_CLIENT_ID" "$TEST_DATA_FILE" || \
   grep -q "YOUR_DUEROS_CLIENT_ID" "$TEST_DATA_FILE" || \
   grep -q "YOUR_MIAI_CLIENT_ID" "$TEST_DATA_FILE" || \
   grep -q "YOUR_ALEXA_CLIENT_ID" "$TEST_DATA_FILE" || \
   grep -q "YOUR_GOOGLE_CLIENT_ID" "$TEST_DATA_FILE"; then
    echo -e "${YELLOW}警告：检测到未替换的占位符（YOUR_*）${NC}"
    echo -e "${YELLOW}建议先替换为实际的平台配置，或者跳过平台配置仅导入设备数据${NC}"
    echo ""
    read -p "是否继续导入？(y/n): " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "已取消导入"
        exit 0
    fi
fi

# 提示输入数据库密码
echo "请输入数据库密码（如果没有密码请直接按回车）："
read -s DB_PASSWORD
echo ""

# 测试数据库连接
echo "正在测试数据库连接..."
if [ -z "$DB_PASSWORD" ]; then
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "SELECT 1;" > /dev/null 2>&1
else
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" -e "SELECT 1;" > /dev/null 2>&1
fi

if [ $? -ne 0 ]; then
    echo -e "${RED}错误：无法连接到数据库${NC}"
    echo "请检查数据库配置和密码是否正确"
    exit 1
fi

echo -e "${GREEN}数据库连接成功${NC}"
echo ""

# 检查数据库是否存在
echo "正在检查数据库 $DB_NAME..."
if [ -z "$DB_PASSWORD" ]; then
    DB_EXISTS=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -e "SHOW DATABASES LIKE '$DB_NAME';" | grep "$DB_NAME")
else
    DB_EXISTS=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" -e "SHOW DATABASES LIKE '$DB_NAME';" | grep "$DB_NAME")
fi

if [ -z "$DB_EXISTS" ]; then
    echo -e "${RED}错误：数据库 $DB_NAME 不存在${NC}"
    echo "请先运行 schema.sql 创建数据库"
    exit 1
fi

echo -e "${GREEN}数据库 $DB_NAME 存在${NC}"
echo ""

# 询问是否清空现有数据
echo -e "${YELLOW}警告：导入测试数据会覆盖现有数据${NC}"
read -p "是否清空现有数据？(y/n): " -n 1 -r
echo ""
CLEAR_DATA=$REPLY

# 导入测试数据
echo "正在导入测试数据..."
if [ -z "$DB_PASSWORD" ]; then
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" < "$TEST_DATA_FILE"
else
    mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" < "$TEST_DATA_FILE"
fi

if [ $? -ne 0 ]; then
    echo -e "${RED}错误：导入测试数据失败${NC}"
    exit 1
fi

echo -e "${GREEN}测试数据导入成功${NC}"
echo ""

# 显示导入统计
echo "正在统计导入数据..."
if [ -z "$DB_PASSWORD" ]; then
    OAUTH_COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -N -e "SELECT COUNT(*) FROM oauth_clients;")
    USER_COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -N -e "SELECT COUNT(*) FROM users;")
    DEVICE_COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -N -e "SELECT COUNT(*) FROM devices;")
    ONLINE_COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" "$DB_NAME" -N -e "SELECT COUNT(*) FROM devices WHERE status='online';")
else
    OAUTH_COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -N -e "SELECT COUNT(*) FROM oauth_clients;")
    USER_COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -N -e "SELECT COUNT(*) FROM users;")
    DEVICE_COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -N -e "SELECT COUNT(*) FROM devices;")
    ONLINE_COUNT=$(mysql -h "$DB_HOST" -P "$DB_PORT" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" -N -e "SELECT COUNT(*) FROM devices WHERE status='online';")
fi

echo "============================================"
echo "导入统计"
echo "============================================"
echo "OAuth 客户端：$OAUTH_COUNT 个"
echo "测试用户：$USER_COUNT 个"
echo "扫地机器人：$DEVICE_COUNT 台"
echo "在线设备：$ONLINE_COUNT 台"
echo "离线设备：$((DEVICE_COUNT - ONLINE_COUNT)) 台"
echo "============================================"
echo ""

echo -e "${GREEN}测试数据导入完成！${NC}"
echo ""
echo "下一步："
echo "1. 启动应用：./start.sh 或 mvn spring-boot:run"
echo "2. 使用 Postman 测试接口"
echo "3. 在音箱 App 中进行语音测试"
echo ""
echo "测试账号："
echo "  用户名：testuser"
echo "  密码：password123"
echo ""
echo "参考文档："
echo "  - 测试数据说明.md"
echo "  - OAuth配置指南.md"
echo "  - 使用说明.md"
