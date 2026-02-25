#!/bin/bash

# ============================================
# 多平台智能音箱对接项目 - 配置替换脚本
# ============================================
# 说明：
# 本脚本用于快速替换 smarthomedb.sql 中的平台配置占位符
# 使用方法：
#   1. 编辑本脚本，填写实际的平台配置信息
#   2. 执行：bash 配置替换脚本.sh
#   3. 导入生成的 test-data-configured.sql
# ============================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}============================================${NC}"
echo -e "${GREEN}多平台智能音箱对接项目 - 配置替换脚本${NC}"
echo -e "${GREEN}============================================${NC}"
echo ""

# ============================================
# 配置信息（请修改为实际值）
# ============================================

# 天猫精灵配置
ALIGENIE_CLIENT_ID="YOUR_ALIGENIE_CLIENT_ID"
ALIGENIE_CLIENT_SECRET="YOUR_ALIGENIE_CLIENT_SECRET"

# 小度音箱配置
DUEROS_CLIENT_ID="YOUR_DUEROS_CLIENT_ID"
DUEROS_CLIENT_SECRET="YOUR_DUEROS_CLIENT_SECRET"

# 小爱同学配置
MIAI_CLIENT_ID="YOUR_MIAI_CLIENT_ID"
MIAI_CLIENT_SECRET="YOUR_MIAI_CLIENT_SECRET"

# ============================================
# 检查配置是否已修改
# ============================================

check_config() {
    local has_error=0
    
    if [ "$ALIGENIE_CLIENT_ID" = "YOUR_ALIGENIE_CLIENT_ID" ]; then
        echo -e "${YELLOW}警告: 天猫精灵 CLIENT_ID 未配置${NC}"
        has_error=1
    fi
    
    if [ "$DUEROS_CLIENT_ID" = "YOUR_DUEROS_CLIENT_ID" ]; then
        echo -e "${YELLOW}警告: 小度音箱 CLIENT_ID 未配置${NC}"
        has_error=1
    fi
    
    if [ "$MIAI_CLIENT_ID" = "YOUR_MIAI_CLIENT_ID" ]; then
        echo -e "${YELLOW}警告: 小爱同学 CLIENT_ID 未配置${NC}"
        has_error=1
    fi
    
    if [ $has_error -eq 1 ]; then
        echo ""
        echo -e "${RED}请先编辑本脚本，填写实际的平台配置信息！${NC}"
        echo ""
        echo "配置步骤："
        echo "1. 编辑本脚本：vim 配置替换脚本.sh"
        echo "2. 修改 ALIGENIE_CLIENT_ID 等变量的值"
        echo "3. 保存后重新执行本脚本"
        echo ""
        exit 1
    fi
}

# ============================================
# 执行替换
# ============================================

replace_config() {
    local input_file="src/main/resources/smarthomedb.sql"
    local output_file="src/main/resources/test-data-configured.sql"
    
    # 检查输入文件是否存在
    if [ ! -f "$input_file" ]; then
        echo -e "${RED}错误: 找不到文件 $input_file${NC}"
        exit 1
    fi
    
    echo "正在替换配置..."
    echo ""
    
    # 复制文件并替换占位符
    cp "$input_file" "$output_file"
    
    # 替换天猫精灵配置
    sed -i "s/YOUR_ALIGENIE_CLIENT_ID/$ALIGENIE_CLIENT_ID/g" "$output_file"
    sed -i "s/YOUR_ALIGENIE_CLIENT_SECRET/$ALIGENIE_CLIENT_SECRET/g" "$output_file"
    echo -e "${GREEN}✓${NC} 天猫精灵配置已替换"
    
    # 替换小度音箱配置
    sed -i "s/YOUR_DUEROS_CLIENT_ID/$DUEROS_CLIENT_ID/g" "$output_file"
    sed -i "s/YOUR_DUEROS_CLIENT_SECRET/$DUEROS_CLIENT_SECRET/g" "$output_file"
    echo -e "${GREEN}✓${NC} 小度音箱配置已替换"
    
    # 替换小爱同学配置
    sed -i "s/YOUR_MIAI_CLIENT_ID/$MIAI_CLIENT_ID/g" "$output_file"
    sed -i "s/YOUR_MIAI_CLIENT_SECRET/$MIAI_CLIENT_SECRET/g" "$output_file"
    echo -e "${GREEN}✓${NC} 小爱同学配置已替换"
    
    echo ""
    echo -e "${GREEN}配置替换完成！${NC}"
    echo ""
    echo "生成的文件：$output_file"
    echo ""
}

# ============================================
# 显示配置信息
# ============================================

show_config() {
    echo "当前配置信息："
    echo "----------------------------------------"
    echo "天猫精灵："
    echo "  Client ID: ${ALIGENIE_CLIENT_ID:0:20}..."
    echo "  Client Secret: ${ALIGENIE_CLIENT_SECRET:0:20}..."
    echo ""
    echo "小度音箱："
    echo "  Client ID: ${DUEROS_CLIENT_ID:0:20}..."
    echo "  Client Secret: ${DUEROS_CLIENT_SECRET:0:20}..."
    echo ""
    echo "小爱同学："
    echo "  Client ID: ${MIAI_CLIENT_ID:0:20}..."
    echo "  Client Secret: ${MIAI_CLIENT_SECRET:0:20}..."
    echo "----------------------------------------"
    echo ""
}

# ============================================
# 导入数据库
# ============================================

import_database() {
    local sql_file="src/main/resources/test-data-configured.sql"
    
    echo "是否立即导入数据库？(y/n)"
    read -r answer
    
    if [ "$answer" = "y" ] || [ "$answer" = "Y" ]; then
        echo ""
        echo "请输入 MySQL 配置信息："
        read -p "主机 (默认: localhost): " db_host
        db_host=${db_host:-localhost}
        
        read -p "端口 (默认: 3306): " db_port
        db_port=${db_port:-3306}
        
        read -p "用户名 (默认: root): " db_user
        db_user=${db_user:-root}
        
        read -sp "密码: " db_password
        echo ""
        
        read -p "数据库名 (默认: smarthomedb): " db_name
        db_name=${db_name:-smarthomedb}
        
        echo ""
        echo "正在导入数据..."
        
        mysql -h"$db_host" -P"$db_port" -u"$db_user" -p"$db_password" "$db_name" < "$sql_file"
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ 数据导入成功！${NC}"
        else
            echo -e "${RED}✗ 数据导入失败！${NC}"
            exit 1
        fi
    else
        echo ""
        echo "跳过数据库导入。"
        echo "稍后可以手动导入："
        echo "mysql -u root -p smarthomedb < $sql_file"
    fi
    
    echo ""
}

# ============================================
# 主流程
# ============================================

main() {
    # 检查配置
    check_config
    
    # 显示配置
    show_config
    
    # 执行替换
    replace_config
    
    # 导入数据库
    import_database
    
    echo -e "${GREEN}============================================${NC}"
    echo -e "${GREEN}配置完成！${NC}"
    echo -e "${GREEN}============================================${NC}"
    echo ""
    echo "下一步："
    echo "1. 启动应用：mvn spring-boot:run"
    echo "2. 测试接口：使用 Postman 测试集合"
    echo "3. 语音测试：在各平台 App 中进行语音控制"
    echo ""
    echo "详细说明请查看：平台配置说明.md"
    echo ""
}

# 执行主流程
main
