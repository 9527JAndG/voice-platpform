# Docker 部署指南

## 快速开始

### 1. 准备环境

确保已安装：
- Docker (20.10+)
- Docker Compose (2.0+)

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 文件，修改数据库密码等配置
# Windows
notepad .env

# Linux/Mac
nano .env
```

### 3. 构建并启动服务

```bash
# 构建并启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 只查看应用日志
docker-compose logs -f app
```

### 4. 验证部署

```bash
# 检查服务状态
docker-compose ps

# 测试健康检查
curl http://localhost:8080/actuator/health

# 测试 API
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=client_credentials&client_id=aligenie-client&client_secret=aligenie-secret&scope=device:control,device:read"
```

## 常用命令

### 启动服务

```bash
# 启动所有服务
docker-compose up -d

# 只启动数据库
docker-compose up -d mysql

# 重新构建并启动
docker-compose up -d --build
```

### 停止服务

```bash
# 停止所有服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 停止并删除容器和数据卷（会删除数据库数据）
docker-compose down -v
```

### 查看日志

```bash
# 查看所有服务日志
docker-compose logs

# 实时查看日志
docker-compose logs -f

# 查看最近 100 行日志
docker-compose logs --tail=100

# 只查看应用日志
docker-compose logs -f app

# 只查看数据库日志
docker-compose logs -f mysql
```

### 进入容器

```bash
# 进入应用容器
docker-compose exec app sh

# 进入数据库容器
docker-compose exec mysql bash

# 在数据库容器中执行 SQL
docker-compose exec mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} smarthomedb
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 只重启应用
docker-compose restart app
```

### 查看资源使用

```bash
# 查看容器资源使用情况
docker stats

# 查看磁盘使用
docker system df
```

## 数据管理

### 备份数据库

```bash
# 备份数据库
docker-compose exec mysql mysqldump -uroot -p${MYSQL_ROOT_PASSWORD} smarthomedb > backup.sql

# 或使用环境变量
docker-compose exec mysql sh -c 'mysqldump -uroot -p$MYSQL_ROOT_PASSWORD smarthomedb' > backup.sql
```

### 恢复数据库

```bash
# 恢复数据库
docker-compose exec -T mysql mysql -uroot -p${MYSQL_ROOT_PASSWORD} smarthomedb < backup.sql
```

### 查看数据卷

```bash
# 列出所有数据卷
docker volume ls

# 查看数据卷详情
docker volume inspect voice-platform_mysql-data
```

## 配置说明

### 环境变量

在 `.env` 文件中配置：

```env
# MySQL 配置
MYSQL_ROOT_PASSWORD=your_secure_password
MYSQL_DATABASE=smarthomedb
MYSQL_USER=voiceuser
MYSQL_PASSWORD=your_user_password
MYSQL_PORT=3306

# 应用配置
APP_PORT=8080
SPRING_PROFILES_ACTIVE=prod

# JWT 密钥
JWT_SECRET=your_jwt_secret_key_here
```

### 端口映射

默认端口映射：
- 应用：`8080:8080`
- MySQL：`3306:3306`

修改端口：
```bash
# 在 .env 文件中修改
APP_PORT=9090
MYSQL_PORT=3307
```

### 资源限制

在 `docker-compose.yml` 中添加资源限制：

```yaml
services:
  app:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 512M
```

## 生产环境部署

### 1. 安全配置

```bash
# 修改默认密码
# 编辑 .env 文件，设置强密码
MYSQL_ROOT_PASSWORD=your_very_secure_password
JWT_SECRET=your_very_long_and_random_jwt_secret_key
```

### 2. 使用外部数据库

如果使用外部 MySQL 数据库：

```yaml
# docker-compose.yml
services:
  app:
    environment:
      DB_HOST: your-mysql-host.com
      DB_PORT: 3306
      DB_NAME: smarthomedb
      DB_USERNAME: your_username
      DB_PASSWORD: your_password
```

然后移除 mysql 服务：
```bash
docker-compose up -d app
```

### 3. 配置反向代理

使用 Nginx 作为反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 4. 配置 HTTPS

使用 Let's Encrypt：

```bash
# 安装 certbot
apt-get install certbot python3-certbot-nginx

# 获取证书
certbot --nginx -d your-domain.com
```

### 5. 监控和日志

```bash
# 配置日志轮转
# 在 docker-compose.yml 中添加
services:
  app:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

## 故障排查

### 应用无法启动

```bash
# 查看详细日志
docker-compose logs app

# 检查数据库连接
docker-compose exec app nc -zv mysql 3306

# 检查环境变量
docker-compose exec app env | grep DB_
```

### 数据库连接失败

```bash
# 检查数据库是否就绪
docker-compose exec mysql mysqladmin ping -h localhost -uroot -p

# 检查数据库日志
docker-compose logs mysql

# 重启数据库
docker-compose restart mysql
```

### 端口被占用

```bash
# Windows
netstat -ano | findstr :8080

# Linux/Mac
lsof -i :8080

# 修改端口
# 在 .env 中设置 APP_PORT=9090
```

### 容器内存不足

```bash
# 增加内存限制
# 在 docker-compose.yml 中修改 JAVA_OPTS
JAVA_OPTS: "-Xms256m -Xmx512m"
```

## 更新部署

### 更新应用代码

```bash
# 1. 拉取最新代码
git pull

# 2. 重新构建镜像
docker-compose build app

# 3. 重启应用
docker-compose up -d app

# 4. 查看日志确认启动成功
docker-compose logs -f app
```

### 滚动更新（零停机）

```bash
# 1. 构建新镜像
docker-compose build app

# 2. 启动新容器
docker-compose up -d --no-deps --scale app=2 app

# 3. 等待新容器就绪
sleep 30

# 4. 停止旧容器
docker-compose up -d --no-deps --scale app=1 app
```

## 清理

### 清理未使用的资源

```bash
# 清理未使用的镜像
docker image prune -a

# 清理未使用的容器
docker container prune

# 清理未使用的数据卷
docker volume prune

# 清理所有未使用的资源
docker system prune -a --volumes
```

### 完全清理项目

```bash
# 停止并删除所有容器和数据卷
docker-compose down -v

# 删除镜像
docker rmi voice-platform_app
docker rmi mysql:8.0
```

## 性能优化

### 1. JVM 调优

```yaml
# docker-compose.yml
environment:
  JAVA_OPTS: >-
    -Xms1g -Xmx2g
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
    -XX:+HeapDumpOnOutOfMemoryError
    -XX:HeapDumpPath=/app/logs/
```

### 2. 数据库优化

```yaml
# docker-compose.yml
mysql:
  command:
    - --max_connections=200
    - --innodb_buffer_pool_size=1G
    - --query_cache_size=0
```

### 3. 使用多阶段构建缓存

```bash
# 使用 BuildKit
DOCKER_BUILDKIT=1 docker-compose build
```

## 支持

如有问题，请查看：
- 应用日志：`docker-compose logs app`
- 数据库日志：`docker-compose logs mysql`
- 健康检查：`curl http://localhost:8080/actuator/health`
