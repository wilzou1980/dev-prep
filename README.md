# dev-prep

> 企业内部业务系统脚手架：Java 17 + Spring Boot 3 + Maven + PostgreSQL 15 + Redis 7 + Docker Compose。
> 单体架构、按业务模块分包，不引入微服务 / Kubernetes / 分库分表。面向约 200 名使用人员、峰值并发约 30。

## 技术栈

| 层 | 选型 |
|---|---|
| 后端 | Java 17 / Spring Boot 3.3.5 / Maven |
| 数据库 | PostgreSQL 15（逻辑删除字段 `is_deleted`） |
| 缓存 | Redis 7 |
| 接口文档 | springdoc-openapi（Swagger UI） |
| 容器 | Docker + docker compose |
| CI/CD | GitHub Actions → GHCR → SSH 部署 |

## 目录结构

```
.
├─ backend/                          # Spring Boot 后端
│  ├─ pom.xml
│  ├─ Dockerfile
│  └─ src/
│     ├─ main/java/com/company/project/
│     │  ├─ ProjectApplication.java
│     │  ├─ common/                  # 统一响应 Result<T>
│     │  └─ module/hello/controller/ # Hello World 示例
│     ├─ main/resources/application.yml
│     └─ test/java/.../HelloControllerTest.java
├─ .github/workflows/ci.yml          # CI/CD 流水线
├─ docker-compose.yml                # db + redis + app 编排
├─ .env.example                      # 环境变量样例（不含真实密钥）
├─ .gitignore
└─ AGENTS.md                         # AI 编程助手约定（约束力高于习惯）
```

## 本地启动

### 方式一：直接跑 backend（只需 JDK 17 + Maven）

```bash
# 本机 Maven Central 慢时可用 -s backend/mvn-settings.xml（阿里云镜像，本地文件未入库）
mvn -f backend/pom.xml spring-boot:run
# 或先打包再运行
mvn -f backend/pom.xml clean package
java -jar backend/target/project-0.0.1-SNAPSHOT.jar
```

启动后访问 <http://localhost:8080/api/v1/hello>。

### 方式二：Docker Compose（含 PostgreSQL + Redis）

```bash
cp .env.example .env        # 修改其中所有 change-me 密码
docker compose up -d
docker compose logs -f app  # 查看应用日志
```

`app` 服务依赖 `db`、`redis` 健康后才启动。可使用 `IMAGE_TAG` 指定镜像（默认 `app-local`，会用 `backend/Dockerfile` 本地构建）。

### 常用命令

| 用途 | 命令 |
|---|---|
| 编译 | `mvn -f backend/pom.xml -DskipTests compile` |
| 测试 | `mvn -f backend/pom.xml test` |
| 打包 | `mvn -f backend/pom.xml clean package -DskipTests` |
| 本地起服务 | `mvn -f backend/pom.xml spring-boot:run` |
| 容器日志 | `docker compose logs -f app` |
| 停止容器 | `docker compose down` |

## 接口说明

统一响应格式（成功 `code=0`）：

```json
{ "code": 0, "message": "success", "data": "...", "timestamp": "2026-11-24T19:00:00" }
```

### Hello World 示例

- `GET /api/v1/hello?name=Harness`

  ```json
  { "code": 0, "message": "success", "data": "Hello, Harness!", "timestamp": "..." }
  ```

- `GET /api/v1/hello`（`name` 缺省为 `World`）

  ```json
  { "code": 0, "message": "success", "data": "Hello, World!", "timestamp": "..." }
  ```

Swagger UI：启动后访问 <http://localhost:8080/swagger-ui.html>。
新增 / 修改接口必须同步更新 `@Operation` / `@Parameter` 注解，并补 MockMvc 测试。

## 环境变量（.env）

复制 `.env.example` 为 `.env` 后按实际修改；`.env` 已被 `.gitignore` 忽略，**绝不入库**。

| 变量 | 说明 | 是否必填 |
|---|---|---|
| `POSTGRES_DB` / `POSTGRES_USER` | 数据库名 / 用户，默认 `appdb` / `appuser` | 否 |
| `POSTGRES_PASSWORD` | 数据库密码 | **是**（未设则 compose 启动失败） |
| `REDIS_PASSWORD` | Redis 密码 | 建议设置 |
| `APP_JWT_SECRET` | JWT 签名密钥，长随机串 | **是**（未设则 compose 启动失败） |
| `SPRING_PROFILES_ACTIVE` | Spring profile，默认 `dev` | 否 |
| `APP_PORT` | 对外端口，默认 `8080` | 否 |
| `IMAGE_TAG` | 部署镜像地址，CI 部署脚本注入 | 本地一般留空 |

## 必需的 GitHub Actions Secrets

在仓库 **Settings → Secrets and variables → Actions → New repository secret** 添加：

| Secret | 用途 |
|---|---|
| `DEV_HOST` | 开发 / 测试部署机 IP 或域名（deploy-dev 作业 SSH 目标） |
| `DEV_SSH_KEY` | 该机器 `deploy` 用户的 SSH **私钥**全文（含 BEGIN/END 行） |
| `PROD_HOST` | 生产机 IP 或域名（deploy-prod 作业 SSH 目标） |
| `PROD_SSH_KEY` | 生产机 `deploy` 用户的 SSH **私钥**全文 |

> `GITHUB_TOKEN` 由 GitHub 自动注入，用于推送镜像到 GHCR，无需手动配置。
> 注意：这里填的是**私钥**，不是添加到 GitHub 账户的那个公钥（`.pub`）。

配套还需在 **Settings → Environments** 建 `development` 与 `production` 两个 environment（可给 production 配审批人实现手动卡点），并在 **Settings → Branches** 给 `main` 开分支保护（要求 PR + 至少 1 人 approve + CI 全绿）。

## CI/CD 流水线

触发：push 到 `main`、推送 `v*` tag、PR、手动 `workflow_dispatch`。

```
compile-check（编译）
   └─ unit-test（mvn test + 上传报告）
         └─ build-image（推 GHCR，tag=commit sha 与 latest）
               ├─ deploy-dev（main 分支，environment development，手动放行）
               └─ deploy-prod（v* tag，environment production，手动放行）
```

镜像地址：`ghcr.io/<owner>/<repo>/app`。部署机需在 `/opt/app` 放置 `docker-compose.yml` 与 `.env`，流水线通过 SSH 执行 `docker compose pull / up -d app`。

## 开发约定

详见 [AGENTS.md](AGENTS.md)（对 AI 编程助手同样具有约束力）。要点：main 受保护、`feature/xxx` / `fix/xxx` 分支 + Pull Request、Conventional Commits、改代码必带测试、覆盖率门禁 ≥ 80%（核心 ≥ 90%）、表结构变更走 Flyway / Liquibase 迁移。
