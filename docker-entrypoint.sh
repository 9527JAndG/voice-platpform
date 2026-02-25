#!/bin/sh
set -e

# 等待数据库就绪
echo "等待数据库连接..."
while ! nc -z ${DB_HOST:-mysql} ${DB_PORT:-3306}; do
  echo "数据库未就绪，等待中..."
  sleep 2
done
echo "数据库已就绪！"

# 启动应用
echo "启动应用..."
exec java ${JAVA_OPTS} \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod} \
  -jar app.jar
