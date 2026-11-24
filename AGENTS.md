# 项目约定（AGENTS.md）

> 本文件对 AI 编程助手具有约束力。任何与习惯或直觉冲突之处，以本文件为准。

## 1. 项目概览
- 类型：企业内部业务系统；约 200 名使用人员，峰值并发约 30。
- 技术栈：Java 17 + Spring Boot 3.x + Maven + PostgreSQL 15 + Redis 7 + Docker Compose。
- 架构：单体应用、按业务模块分包；不引入微服务、Kubernetes、分库分表。
- 环境：dev（开发/测试）、prod（生产）；内网 GitLab 单仓 monorepo。

## 2. 仓库结构
```
project-name/
├─ backend/                  # Spring Boot 后端
│   ├─ pom.xml
│   ├─ Dockerfile
│   └─ src/
├─ frontend/                 # Vue3 前端（如有）
├─ deploy/                   # Nginx 配置、部署脚本
├─ docs/                     # 设计文档、部署手册
├─ .gitlab-ci.yml            # CI/CD 流水线
├─ docker-compose.yml        # 本地/单机编排
├─ .env.example              # 环境变量样例（不含真实密钥）
├─ .gitignore
└─ AGENTS.md                 # 本文件
```

## 3. 后端包结构（强制）
```
com.company.project
├─ ProjectApplication.java
├─ common/      统一响应 Result、分页 PageResult、常量、工具类
├─ config/      配置类（Web、Redis、OpenAPI 等）
├─ exception/   业务异常 BizException + 全局异常处理
├─ security/    认证授权、JWT
└─ module/      按业务域分包
    └─ user/
        ├─ controller/   参数校验与请求编排
        ├─ service/      接口 + impl，事务边界
        ├─ mapper/       仅数据访问
        ├─ entity/       数据库实体
        ├─ dto/          入参
        ├─ vo/           出参
        └─ convert/      对象转换
```

分层纪律：
- Controller 只做参数校验与编排，不写业务逻辑；统一返回 `Result<T>`。
- Service 接口与实现分离；事务注解 @Transactional 加在 service 实现层。
- 禁止 Controller/Service 直接返回 entity；用 DTO/VO 隔离。
- Mapper 只做数据访问，不承载业务判断。

## 4. 编码规范
- 命名：类名 PascalCase，方法/变量 camelCase，常量 UPPER_SNAKE_CASE，包名全小写。
- 复杂 public 方法写 Javadoc；参数校验用 Jakarta Validation（@NotNull/@Size）+ Controller 层 @Valid。
- 业务异常抛自定义 BizException + 错误码；禁止吞异常或 printStackTrace；用 `@Slf4j` 记日志。
- 日志用 SLF4J 占位符 `{}`，禁止字符串拼接；密码/token 等敏感信息不进日志。
- 禁止 System.out.println；禁止魔法值（定义常量或枚举）。
- 单方法不超过 80 行，超过必须拆。

## 5. API 规范
- RESTful：GET 查、POST 增、PUT 全量更新、PATCH 部分更新、DELETE 删。
- 路径 `/api/v1/<资源>`，复数名词，如 `/api/v1/users`。
- 统一响应：{ code, message, data, timestamp }，成功 code=0。
- 分页：PageResult<T> { list, total, pageNum, pageSize }。
- 接口文档用 springdoc-openapi（@Operation/@Parameter），改接口必须同步更新注解。

## 6. 测试规范（强制）
- 框架：JUnit 5 + Mockito + Spring Boot Test。
- 新增/修改 service 方法必须写单元测试；新增接口必须写 MockMvc 集成测试。
- 测试类命名 XxxTest / XxxServiceTest；方法名 should_xxx_when_xxx。
- 覆盖率门禁 ≥ 80%，核心业务 ≥ 90%。
- 提交前 `mvn -f backend/pom.xml test` 必须全绿。

## 7. 配置与安全
- 环境差异走环境变量或 Spring profile（application-dev.yml / application-prod.yml）。
- 数据库地址、密码、JWT 密钥、Redis 密码绝不硬编码、绝不入库，走 .env / CI Variables。
- 生产环境关闭 Swagger 与 Actuator 敏感端点。
- 文件上传校验类型与大小；SQL 一律参数化，禁止字符串拼接。

## 8. 数据库规范
- 表名 snake_case 复数，如 sys_users；主键 id（bigint）。
- 每表含 created_at、updated_at、is_deleted（逻辑删除）。
- 字段加注释；索引 idx_表名_字段，唯一索引 uk_表名_字段。
- 表结构变更必须写 Flyway/Liquibase 迁移脚本，禁止手工改库。

## 9. Git 与协作
- main 受保护，禁止直接 push；从 main 切 `feature/xxx` 或 `fix/xxx`。
- Commit 用 Conventional Commits：feat: / fix: / docs: / test: / refactor: / chore:。
- 合并必经 MR，至少 1 人 approve + CI 全绿。
- 发布打 tag `vX.Y.Z`，tag 触发生产部署。

## 10. 给 AI 编程助手的额外要求
- 动手前先读相关现有代码，遵循既有模式，不要另起一套。
- 每次只做一件事，改动尽量小；不顺手重构无关代码。
- 改代码必带测试；改接口必更文档。
- 未经明确要求，不修改 main 分支配置、CI 配置、生产配置。
- 不确定的约定先问，不要猜。
- 生成配置时优先复用本仓库 `docker-compose.yml` / `.gitlab-ci.yml` 的既有结构。

## 11. 常用命令
- 构建：`mvn -f backend/pom.xml clean package -DskipTests`
- 测试：`mvn -f backend/pom.xml test`
- 本地启动：`cp .env.example .env && docker compose up -d`
- 查看日志：`docker compose logs -f app`
