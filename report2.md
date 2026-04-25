# WMS 项目 DevOps 设计方案

> 项目：wms.pj878 v7.0.0.4 | Spring Boot 2.6.7 | Java 11 | 打包方式：WAR

---

## 一、测试用例设计

### 1.1 单元测试

基于 `spring-boot-starter-test`（已在 pom.xml 中引入），覆盖核心业务层。

**优先覆盖高风险方法（结合 report.md 空指针分析）：**

```java
// PjInboundServiceImpl - serial_no 为 null 时的防御测试
@Test
void testSerialNoNullSafety() {
    AsnDetailDto dto = new AsnDetailDto();
    dto.setSerial_no(null); // 模拟 null
    assertThrows(BusinessException.class, () -> inboundService.processSerialNo(dto));
}

// PickExtServiceImpl - createDispatchInfoWithReturn 返回 null 时
@Test
void testDispatchInfoNullReturn() {
    when(dispatchInfoService.createDispatchInfoWithReturn(any(), any())).thenReturn(null);
    assertThrows(BusinessException.class, () -> pickExtService.doDispatch(...));
}

// 列表越界防御
@Test
void testEmptyAsnItemList() {
    assertThrows(BusinessException.class,
        () -> inboundService.processAsnItems(Collections.emptyList()));
}
```

**测试分层建议：**

| 层级 | 框架 | 覆盖目标 |
|------|------|----------|
| Service 单元测试 | JUnit 5 + Mockito | PjInboundServiceImpl、PickExtServiceImpl 核心方法 |
| Controller 切片测试 | `@WebMvcTest` | PickExtController、SkuExtController 入参校验 |
| Mapper 测试 | `@MybatisTest` + H2 | AsnItemExtMapper、RequisitionItemExtMapper |
| ERP 集成测试 | WireMock | DataSyncController HTTP 回调模拟 |

### 1.2 集成测试

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:wms_test;MODE=MySQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  redis:
    host: localhost
    port: 6379
  flyway:
    enabled: true
    locations: classpath:sql/h2
```

### 1.3 测试覆盖率目标

- Service 层：≥ 70%
- Controller 层：≥ 60%（重点验证入参校验和异常响应）
- 核心高风险方法（见 report.md）：100%

---

## 二、Dockerfile

项目打包产物为分离式结构（`config/` + `lib/` + WAR），Dockerfile 按此结构设计。

```dockerfile
# ---- 构建阶段 ----
FROM maven:3.8.6-openjdk-11-slim AS builder
WORKDIR /build

# 优先复制 pom.xml，利用 Docker 层缓存加速依赖下载
COPY pom.xml .
RUN mvn dependency:go-offline -B \
    --settings /usr/share/maven/ref/settings.xml \
    -Dmaven.repo.remote=http://nexus.foeris.com:4667/content/groups/public/

COPY src ./src
RUN mvn package -DskipTests -B

# ---- 运行阶段 ----
FROM openjdk:11-jre-slim
WORKDIR /app

# 创建非 root 用户运行
RUN groupadd -r wms && useradd -r -g wms wms

# 复制构建产物（分离式结构）
COPY --from=builder /build/target/wms.pj878-V7.0.0.4/WEB-INF/lib       ./lib/
COPY --from=builder /build/target/config/                                ./config/
COPY --from=builder /build/target/wms.pj878-V7.0.0.4.war                ./app.war

# 日志目录
RUN mkdir -p /app/app-log && chown -R wms:wms /app

USER wms

EXPOSE 8080

# 通过环境变量注入配置，覆盖 application.yml 中的硬编码值
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Dfile.encoding=UTF-8"
ENV SPRING_PROFILES_ACTIVE=prod

ENTRYPOINT ["sh", "-c", \
  "java $JAVA_OPTS \
   -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE \
   -Dsystem_name=wms-pj878 \
   -cp 'config/:lib/*' \
   org.springframework.boot.loader.WarLauncher"]
```

> 注意：`application.yml` 中 Redis/MySQL/RabbitMQ 密码均为明文，生产环境必须通过环境变量覆盖（见第四节）。

---

## 三、CI/CD 流水线（GitHub Actions）

```yaml
# .github/workflows/ci.yml
name: WMS CI/CD

on:
  push:
    branches: [master, develop]
  pull_request:
    branches: [master]

env:
  IMAGE_NAME: wms-pj878
  REGISTRY: ghcr.io/${{ github.repository_owner }}

jobs:
  # ---- 构建 & 测试 ----
  build-and-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 11
        uses: actions/setup-java@v4
        with:
          java-version: '11'
          distribution: 'temurin'
          cache: maven

      - name: Configure private Maven repo
        run: |
          mkdir -p ~/.m2
          cat > ~/.m2/settings.xml <<EOF
          <settings>
            <mirrors>
              <mirror>
                <id>foreris-public</id>
                <mirrorOf>*</mirrorOf>
                <url>http://nexus.foeris.com:4667/content/groups/public/</url>
              </mirror>
            </mirrors>
          </settings>
          EOF

      - name: Build & Test
        run: mvn verify -B

      - name: Upload test report
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: surefire-reports
          path: target/surefire-reports/

  # ---- 构建镜像 & 推送（仅 master）----
  docker-build-push:
    needs: build-and-test
    if: github.ref == 'refs/heads/master'
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write
    steps:
      - uses: actions/checkout@v4

      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=sha,prefix=sha-
            type=raw,value=latest

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          cache-from: type=gha
          cache-to: type=gha,mode=max

  # ---- 部署到生产（手动触发）----
  deploy-prod:
    needs: docker-build-push
    if: github.ref == 'refs/heads/master'
    runs-on: ubuntu-latest
    environment: production        # 需要在 GitHub 仓库设置中配置审批人
    steps:
      - name: Deploy via SSH
        uses: appleboy/ssh-action@v1
        with:
          host: ${{ secrets.PROD_HOST }}
          username: ${{ secrets.PROD_USER }}
          key: ${{ secrets.PROD_SSH_KEY }}
          script: |
            cd /opt/wms
            docker compose pull
            docker compose up -d --no-deps wms
            docker compose ps
```

---

## 四、环境变量管理

### 4.1 变量分类

| 类别 | 变量名 | 说明 | 示例值 |
|------|--------|------|--------|
| 数据库 | `DB_URL` | MySQL 连接串 | `jdbc:mysql://db:3306/wms878` |
| 数据库 | `DB_USERNAME` | 数据库用户名 | `wms_user` |
| 数据库 | `DB_PASSWORD` | 数据库密码 | *(Secret)* |
| Redis | `REDIS_HOST` | Redis 地址 | `redis` |
| Redis | `REDIS_PASSWORD` | Redis 密码 | *(Secret)* |
| RabbitMQ | `RABBITMQ_HOST` | MQ 地址 | `rabbitmq` |
| RabbitMQ | `RABBITMQ_USERNAME` | MQ 用户名 | `admin` |
| RabbitMQ | `RABBITMQ_PASSWORD` | MQ 密码 | *(Secret)* |
| ES | `ES_URIS` | Elasticsearch 地址 | `http://es:9200` |
| ES | `ES_PASSWORD` | ES 密码 | *(Secret)* |
| 应用 | `SPRING_PROFILES_ACTIVE` | 激活的 Profile | `prod` |
| 应用 | `JAVA_OPTS` | JVM 参数 | `-Xms512m -Xmx1024m` |
| 监控 | `SW_AGENT_NAME` | SkyWalking Agent 名称 | `wms-pj878` |
| 监控 | `SW_AGENT_COLLECTOR_BACKEND_SERVICES` | OAP 地址 | `skywalking-oap:11800` |

### 4.2 application-prod.yml（覆盖默认配置）

```yaml
# src/main/resources/application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  redis:
    host: ${REDIS_HOST:redis}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
  rabbitmq:
    host: ${RABBITMQ_HOST:rabbitmq}
    username: ${RABBITMQ_USERNAME:admin}
    password: ${RABBITMQ_PASSWORD}
  elasticsearch:
    rest:
      uris: ${ES_URIS:http://es:9200}
      password: ${ES_PASSWORD}
```

### 4.3 docker-compose.yml（本地/测试环境）

```yaml
version: '3.8'
services:
  wms:
    image: wms-pj878:latest
    ports:
      - "8080:8080"
    env_file:
      - .env          # 本地开发用，不提交到 git
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JAVA_OPTS: "-Xms512m -Xmx1024m -XX:+UseG1GC"
    volumes:
      - ./logs:/app/app-log
    depends_on:
      - mysql
      - redis
      - rabbitmq

  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: wms878
      MYSQL_USER: wms_user
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    command: redis-server --requirepass ${REDIS_PASSWORD}

  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}

volumes:
  mysql_data:
```

> `.env` 文件加入 `.gitignore`，生产环境通过 Kubernetes Secret 或 Vault 注入。

---

## 五、日志收集（ELK）

项目已集成 **SkyWalking gRPC 日志上报**（logback-spring.xml:75），同时支持本地文件滚动。ELK 方案作为补充或替代。

### 5.1 当前日志架构

```
应用 (Logback)
  ├── Console Appender          → 容器标准输出
  ├── AsyncInfoFile Appender    → app-log/info.log (50MB/7天)
  ├── AsyncErrorFile Appender   → app-log/error.log (50MB/7天)
  └── grpc-log Appender         → SkyWalking OAP (已配置)
```

### 5.2 ELK 补充方案（Filebeat → Logstash → ES → Kibana）

```yaml
# filebeat.yml（部署在宿主机或 sidecar）
filebeat.inputs:
  - type: log
    enabled: true
    paths:
      - /app/app-log/*.log
    fields:
      app: wms-pj878
      env: prod
    fields_under_root: true
    multiline:
      pattern: '^\d{4}-\d{2}-\d{2}'
      negate: true
      match: after

output.logstash:
  hosts: ["logstash:5044"]
```

```
# Logstash pipeline（wms.conf）
input {
  beats { port => 5044 }
}
filter {
  grok {
    match => {
      "message" => "%{TIMESTAMP_ISO8601:timestamp} \[%{DATA:trace_id}\] \[%{DATA:thread}\] %{LOGLEVEL:level} %{DATA:logger} - %{GREEDYDATA:msg}"
    }
  }
  date { match => ["timestamp", "yyyy-MM-dd HH:mm:ss.SSS"] }
}
output {
  elasticsearch {
    hosts => ["http://es:9200"]
    index => "wms-pj878-%{+YYYY.MM.dd}"
  }
}
```

### 5.3 Kibana 推荐看板

| 看板 | 关键指标 |
|------|----------|
| 错误监控 | ERROR 日志数/分钟、Top 10 异常类 |
| 业务监控 | 入库/拣货接口 QPS、响应时间 P99 |
| ERP 同步 | 同步成功率、失败重试次数 |
| 慢 SQL | Druid 慢查询日志（已配置 5000ms 阈值） |

### 5.4 日志规范建议

当前 `logback-spring.xml` 中 `cn.zdjc` 包级别为 DEBUG，生产环境建议调整：

```xml
<!-- 生产环境覆盖，放入 application-prod.yml -->
logging:
  level:
    cn.zdjc: INFO
    com.foreris: INFO
    io.netty: WARN
```

---

## 六、部署脚本

### deploy.sh（适用于传统服务器部署）

```bash
#!/bin/bash
set -euo pipefail

APP_NAME="wms-pj878"
APP_DIR="/opt/wms"
LOG_DIR="${APP_DIR}/app-log"
BACKUP_DIR="${APP_DIR}/backup"
NEW_WAR="$1"   # 传入新 WAR 包路径

if [ -z "$NEW_WAR" ]; then
  echo "Usage: $0 <path-to-war>"
  exit 1
fi

echo "[1/5] 备份当前版本..."
mkdir -p "$BACKUP_DIR"
[ -f "${APP_DIR}/app.war" ] && cp "${APP_DIR}/app.war" "${BACKUP_DIR}/app.war.$(date +%Y%m%d%H%M%S)"

echo "[2/5] 停止当前服务..."
PID=$(cat "${APP_DIR}/app.pid" 2>/dev/null || echo "")
if [ -n "$PID" ] && kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  timeout 30 bash -c "while kill -0 $PID 2>/dev/null; do sleep 1; done"
  echo "  服务已停止 (PID: $PID)"
fi

echo "[3/5] 部署新版本..."
cp "$NEW_WAR" "${APP_DIR}/app.war"

echo "[4/5] 启动服务..."
source "${APP_DIR}/.env"
nohup java $JAVA_OPTS \
  -Dspring.profiles.active=prod \
  -Dsystem_name=$APP_NAME \
  -cp "${APP_DIR}/config/:${APP_DIR}/lib/*" \
  org.springframework.boot.loader.WarLauncher \
  > "${LOG_DIR}/startup.log" 2>&1 &
echo $! > "${APP_DIR}/app.pid"

echo "[5/5] 健康检查..."
sleep 15
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health || echo "000")
if [ "$HTTP_CODE" = "200" ]; then
  echo "  部署成功，服务健康 (HTTP $HTTP_CODE)"
else
  echo "  健康检查失败 (HTTP $HTTP_CODE)，执行回滚..."
  LATEST_BACKUP=$(ls -t "${BACKUP_DIR}"/app.war.* | head -1)
  cp "$LATEST_BACKUP" "${APP_DIR}/app.war"
  echo "  已回滚到: $LATEST_BACKUP"
  exit 1
fi
```

---

## 七、运维手册

### 7.1 服务启停

```bash
# 启动
cd /opt/wms && bash deploy.sh /path/to/wms.pj878-V7.0.0.4.war

# 停止
kill $(cat /opt/wms/app.pid)

# Docker 方式
docker compose -f /opt/wms/docker-compose.yml up -d wms
docker compose -f /opt/wms/docker-compose.yml down wms
```

### 7.2 日志查看

```bash
# 实时查看启动日志
tail -f /opt/wms/app-log/info.log

# 查看最近 ERROR
tail -100 /opt/wms/app-log/error.log

# Docker 容器日志
docker logs -f --tail=200 wms-pj878
```

### 7.3 健康检查端点

| 端点 | 说明 |
|------|------|
| `GET /actuator/health` | 服务整体健康状态 |
| `GET /actuator/info` | 版本信息 |
| `GET /druid/index.html` | Druid 连接池监控（需鉴权） |

### 7.4 数据库维护

```bash
# Flyway 迁移状态查看
mvn flyway:info -Dflyway.url=$DB_URL -Dflyway.user=$DB_USERNAME -Dflyway.password=$DB_PASSWORD

# 手动触发迁移（通常由应用启动自动执行）
mvn flyway:migrate
```

### 7.5 常见故障排查

| 现象 | 排查步骤 |
|------|----------|
| 启动失败 | 检查 `app-log/startup.log`；确认 MySQL/Redis/RabbitMQ 连通性 |
| ERP 同步异常 | 查看 `DataSyncController` 相关 ERROR 日志；检查 ERP HTTP 接口可达性 |
| WebSocket 断连 | 确认 Tomcat 版本与 `javax.websocket-api:1.1` 兼容；检查反向代理超时配置 |
| 拣货任务卡住 | 检查 Redis 分布式锁是否未释放：`redis-cli keys "lock:*"` |
| 慢查询告警 | Druid 监控面板查看慢 SQL（阈值 5000ms）；检查相关 Mapper XML |
| 内存溢出 | 调整 `JAVA_OPTS` 中 `-Xmx`；检查 EasyExcel 大文件导出是否未分批 |

### 7.6 版本回滚

```bash
# 传统部署回滚
BACKUP=$(ls -t /opt/wms/backup/app.war.* | head -1)
bash /opt/wms/deploy.sh "$BACKUP"

# Docker 回滚到上一个镜像
docker compose pull wms  # 拉取指定 tag
docker compose up -d --no-deps wms
```

### 7.7 监控告警建议

| 指标 | 告警阈值 | 工具 |
|------|----------|------|
| JVM 堆内存使用率 | > 85% | SkyWalking / Prometheus |
| ERROR 日志数 | > 10条/分钟 | Kibana Alert |
| 接口响应时间 P99 | > 3s | SkyWalking |
| MySQL 连接池使用率 | > 80% | Druid 监控 |
| Redis 连接失败 | 任意次 | 应用日志 + 告警 |
