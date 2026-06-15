# 《芝麻开门》服务器部署配置文档

## 1. 部署目标

本文档记录《芝麻开门》项目在阿里云轻量应用服务器上的部署方式。

部署完成后，用户可以通过服务器公网 IP 访问前端页面，前端通过 `/api` 请求调用后端 Spring Boot 服务，后端使用 SQLite 文件保存用户、存档和排行榜数据。

## 2. 参考资料

- 阿里云轻量应用服务器官方文档：https://help.aliyun.com/zh/simple-application-server/
- 阿里云轻量应用服务器产品页：https://www.aliyun.com/product/swas
- Nginx 反向代理官方文档：https://nginx.org/en/docs/http/ngx_http_proxy_module.html
- Spring Boot 部署官方文档：https://docs.spring.io/spring-boot/how-to/deployment/installing.html

## 3. 服务器基础配置

| 配置项 | 当前方案 |
|---|---|
| 云服务平台 | 阿里云轻量应用服务器 |
| 操作系统 | Ubuntu 22.04 |
| 推荐规格 | 2 vCPU / 1 GB 或以上 |
| 系统盘 | ESSD 云盘 30 GB 或以上 |
| 后端运行端口 | 8080，仅服务器内部访问 |
| 前端访问端口 | 80，通过 Nginx 对外提供 |
| 数据库 | SQLite 本地文件 |

服务器防火墙需要放行：

| 端口 | 协议 | 用途 |
|---|---|---|
| 22 | TCP | SSH 远程登录 |
| 80 | TCP | HTTP 网页访问 |
| 443 | TCP | HTTPS 访问，后续绑定证书时使用 |

`8080` 不需要对公网开放，后端服务由 Nginx 在服务器内部转发访问。

## 4. 服务器环境安装

登录服务器后，先更新软件源：

```bash
sudo apt update
```

安装 Java、Maven、Nginx、Git 和 curl：

```bash
sudo apt install -y openjdk-17-jdk maven nginx git curl
```

安装 Node.js 20：

```bash
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs
```

检查版本：

```bash
java -version
mvn -version
node -v
npm -v
nginx -v
git --version
```

## 5. 项目目录规划

服务器上统一使用 `/opt/sesame` 存放项目：

```text
/opt/sesame
└── app
    ├── backend
    ├── frontend
    ├── docs
    └── README.md
```

创建目录并授予当前用户权限：

```bash
sudo mkdir -p /opt/sesame
sudo chown -R admin:admin /opt/sesame
```

拉取项目代码：

```bash
cd /opt/sesame
git clone <仓库地址> app
cd /opt/sesame/app
git checkout develop
```

更新服务器代码时使用：

```bash
cd /opt/sesame/app
git fetch origin
git checkout develop
git pull --ff-only origin develop
```

## 6. 后端构建与运行

进入后端目录：

```bash
cd /opt/sesame/app/backend
```

打包后端：

```bash
mvn package -DskipTests
```

当前后端 Jar 文件名为：

```text
sesame-open-backend-0.1.0-SNAPSHOT.jar
```

临时启动后端：

```bash
java -jar target/sesame-open-backend-0.1.0-SNAPSHOT.jar
```

在另一个终端检查后端健康接口：

```bash
curl http://127.0.0.1:8080/api/health
```

如果返回以下内容，说明后端运行正常：

```json
{"status":"ok"}
```

## 7. 后端 systemd 服务配置

为了避免关闭 SSH 后后端停止运行，需要将后端配置为系统服务。

创建服务文件：

```bash
sudo nano /etc/systemd/system/sesame-backend.service
```

写入以下内容：

```ini
[Unit]
Description=Open Sesame Backend
After=network.target

[Service]
User=admin
WorkingDirectory=/opt/sesame/app/backend
ExecStart=/usr/bin/java -jar /opt/sesame/app/backend/target/sesame-open-backend-0.1.0-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable sesame-backend
sudo systemctl start sesame-backend
```

查看服务状态：

```bash
sudo systemctl status sesame-backend
```

查看后端日志：

```bash
sudo journalctl -u sesame-backend -f
```

## 8. 前端构建

进入前端目录：

```bash
cd /opt/sesame/app/frontend
```

安装依赖并构建：

```bash
npm install
npm run build
```

构建成功后，前端产物位于：

```text
/opt/sesame/app/frontend/dist
```

其中应包含：

```text
index.html
assets/
```

## 9. Nginx 配置

创建 Nginx 站点配置：

```bash
sudo nano /etc/nginx/sites-available/sesame
```

写入以下内容，将 `服务器公网IP` 替换为实际公网 IP：

```nginx
server {
    listen 80;
    server_name 服务器公网IP;

    root /opt/sesame/app/frontend/dist;
    index index.html;

    location /api/ {
        proxy_pass http://127.0.0.1:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

启用站点：

```bash
sudo ln -s /etc/nginx/sites-available/sesame /etc/nginx/sites-enabled/sesame
sudo rm -f /etc/nginx/sites-enabled/default
```

检查配置并重启 Nginx：

```bash
sudo nginx -t
sudo systemctl restart nginx
```

浏览器访问：

```text
http://服务器公网IP
```

## 10. SQLite 数据文件说明

后端默认数据库路径为：

```text
backend/data/sesame-open.db
```

部署到服务器后，实际数据库文件一般位于：

```text
/opt/sesame/app/backend/data/sesame-open.db
```

该文件会保存用户账号、游戏存档和排行榜数据。数据库属于运行时数据，不应提交到 GitHub。

如果需要查看数据库，可以在服务器安装 SQLite 命令行工具：

```bash
sudo apt install -y sqlite3
```

查看数据表：

```bash
cd /opt/sesame/app/backend
sqlite3 data/sesame-open.db
.tables
```

## 11. 发布更新流程

当 `develop` 分支有新版本需要部署时，按以下流程更新服务器：

```bash
cd /opt/sesame/app
git fetch origin
git checkout develop
git pull --ff-only origin develop
```

重新构建后端：

```bash
cd /opt/sesame/app/backend
mvn package -DskipTests
sudo systemctl restart sesame-backend
```

重新构建前端：

```bash
cd /opt/sesame/app/frontend
npm install
npm run build
sudo systemctl restart nginx
```

验证访问：

```bash
curl http://127.0.0.1:8080/api/health
```

并在浏览器访问公网 IP，检查登录、注册、开始游戏、保存和排行榜是否正常。

## 12. 常见问题

### 12.1 页面打不开

检查服务器防火墙是否放行 `80` 端口，并确认 Nginx 是否运行：

```bash
sudo systemctl status nginx
sudo nginx -t
```

### 12.2 页面能打开，但登录或注册失败

检查后端是否运行：

```bash
curl http://127.0.0.1:8080/api/health
sudo systemctl status sesame-backend
```

### 12.3 修改代码后页面还是旧版本

重新构建前端并重启 Nginx：

```bash
cd /opt/sesame/app/frontend
npm run build
sudo systemctl restart nginx
```

### 12.4 后端重启后数据还在吗

数据保存在 SQLite 文件中，只要没有删除 `backend/data/sesame-open.db`，用户、存档和排行榜数据会保留。
