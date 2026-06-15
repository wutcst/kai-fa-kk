# 《芝麻开门》项目计划文档

## 1. 项目概述

《芝麻开门》是一个基于 Vue 3 + Spring Boot 的前后端分离 Web 图形化文字冒险游戏。

项目以课程样例 `world-of-zuul` 为起点，目前已经形成包含三关遗迹探索、金币、体力、负重、物品、商店、暗语、存档、排行榜和通关结算的完整游戏流程。游戏状态由后端统一维护，前端负责通过真实 API 展示状态并提供交互。

本项目采用 GitHub Issues、Milestones、Pull Requests、分支模型和 GitHub Actions 进行小组协同开发。当前核心玩法和前后端联调已基本完成，工作重点进入验收、体验优化、测试补充和发布材料整理阶段。

## 2. 小组分工

| 负责人 | 负责 Issue | 主要职责 |
|---|---|---|
| 谢恺燊 | #1、#2、#3、#4、#6、#7、#8、#9、#10、#14、#15、#17、#18 | 后端、测试、CI、文档主控、分支合并、项目管理、服务器部署 |
| 黄凯峰 | #1、#2、#3、#5、#11、#12、#13、#15、#16、#17 | 前端初始化、页面展示、接口调用、交互弹窗、展示素材 |

## 3. 里程碑计划

| Milestone | 时间 | 目标 | 版本 | 当前状态 |
|---|---:|---|---|---|
| M1 基础工程与项目规范 | Day 1 - Day 2 | 完成分支、目录、前后端初始化、模板、需求文档 | v0.1 | 已完成 |
| M2 核心玩法与前后端联调 | Day 3 - Day 7 | 完成主要游戏逻辑、REST API、前端交互和完整游戏流程 | v0.5 | 基本完成，已跑通三关、商店、暗语、存档和排行榜，并完成 #16、#17 增强 |
| M3 测试、CI、文档与发布 | Day 8 - Day 10 | 完成测试、CI、README、REPORT、视频、服务器部署和最终合并 | v1.0 | 进行中，持续补充验收测试、发布文档和服务器部署说明 |

当前实现进度：

- 后端已实现游戏会话、三关房间图、移动与返回、体力与负重、当前关重开、失败与通关、最终积分结算。
- 物品系统已实现定义、拾取、丢弃、补给与装备使用、宝物出售和结算；前端按物品语义展示操作，避免误导性效果说明。
- 商店系统已实现商品目录、购买、出售和继续探险，跨关只能通过营地商店流程完成。
- 暗语系统已实现机关长廊入口控制、正确暗语校验、线索链和错误次数递进提示。
- 账号与数据功能已实现注册、登录、SQLite 存档、存档列表、读取、排行榜和历史最高分。
- 前端已实现游戏 HUD、行动面板、背包、商店、帮助、地图查看、当前房间标记、提示和结果弹窗。
- 后端 JUnit、Checkstyle、前端 Vitest、ESLint、build 和 GitHub Actions 已建立。
- 已在阿里云轻量应用服务器上完成前后端部署验证，支持通过公网 IP 访问游戏页面并调用后端接口。

## 4. 分支模型

项目采用以下分支模型：

```text
main / master
└── develop
    ├── backend/dev
    │   ├── feature/backend-init
    │   ├── feature/backend-domain-models
    │   ├── feature/backend-game-session
    │   ├── feature/backend-move-cost
    │   ├── feature/backend-inventory
    │   └── feature/backend-special-rules
    │
    └── frontend/dev
    │   ├── feature/frontend-init
    │   ├── feature/frontend-game-layout
    │   ├── feature/frontend-api-client
    │   └── feature/frontend-game-actions
    │
    ├── fix/backend-game-session-review
    ├── fix/backend-shop-catalog-leaderboard
    ├── fix/frontend-ranking-empty-state
    └── fix/game-map-password-hints
   
```

合并路径：

- 后端功能分支合并到 `backend/dev`。
- 前端功能分支合并到 `frontend/dev`。
- `backend/dev` 和 `frontend/dev` 稳定后合并到 `develop`。
- `develop` 完成测试和文档后合并到 `main/master`。
- 发布阶段可创建 `release/v1.0` 或 tag `v1.0`。
- 修复任务使用聚焦的修复分支，验证后再按上述流程合并。

## 5. Issue 拆分

| Issue | 标题 | 负责人 | Milestone | 建议分支 | 时间 | 当前状态 |
|---|---|---|---|---|---|---|
| #1 | `[Init] 初始化仓库目录与分支模型` | 谢恺燊 | M1 | `feature/project-structure` | Day 1 | 已完成 |
| #2 | `[Docs] 编写项目需求与开发计划文档` | 谢恺燊 | M1 | `feature/docs-project-plan` | Day 1 | 已完成，持续同步 |
| #3 | `[Workflow] 创建 Issue、PR 模板与协作规范` | 谢恺燊 | M1 | `feature/workflow-template` | Day 1 | 已完成 |
| #4 | `[Backend] 初始化 Spring Boot 后端项目` | 谢恺燊 | M1 | `feature/backend-init` | Day 1 - Day 2 | 已完成 |
| #5 | `[Frontend] 初始化 Vue 3 前端项目` | 黄凯峰 | M1 | `feature/frontend-init` | Day 1 - Day 2 | 已完成 |
| #6 | `[Backend] 实现玩家、物品、房间核心模型` | 谢恺燊 | M2 | `feature/backend-domain-models` | Day 3 | 已完成 |
| #7 | `[Backend] 实现地图、游戏会话与初始状态` | 谢恺燊 | M2 | `feature/backend-game-session` | Day 3 - Day 4 | 已完成 |
| #8 | `[Backend] 实现移动、体力、门票与金额规则` | 谢恺燊 | M2 | `feature/backend-move-cost` | Day 4 | 已完成 |
| #9 | `[Backend] 实现背包、拾取、丢弃与使用物品` | 谢恺燊 | M2 | `feature/backend-inventory` | Day 5 | 已完成 |
| #10 | `[Backend] 实现暗语开门、救援、通关和失败机制` | 谢恺燊 | M2 | `feature/backend-special-rules` | Day 5 - Day 6 | 已完成 |
| #11 | `[Frontend] 实现游戏主界面与状态展示` | 黄凯峰 | M2 | `feature/frontend-game-layout` | Day 3 - Day 4 | 已完成 |
| #12 | `[Frontend] 实现 API 调用封装与基础交互` | 黄凯峰 | M2 | `feature/frontend-api-client` | Day 4 - Day 5 | 已完成 |
| #13 | `[Frontend] 实现完整游戏操作与提示弹窗` | 黄凯峰 | M2 | `feature/frontend-game-actions` | Day 5 - Day 7 | 已完成 |
| #14 | `[Test/CI] 完成测试、代码检查与自动化构建` | 谢恺燊 | M3 | `feature/test-and-ci` | Day 8 | 已完成基础检查，继续补充覆盖 |
| #15 | `[Docs/Release] 完成 README、REPORT、AI 说明、视频与最终发布` | 谢恺燊、黄凯峰 | M3 | `feature/final-docs-release` | Day 9 - Day 10 | 进行中 |
| #16 | `[Bug] 新增商店商品列表与排行榜接口` | 黄凯峰 | M2 | `backend/fix-shop-catalog-leaderboard` | 联调完善阶段 | 已完成 |
| #17 | `[Bug] 修复地图显示、暗语线索与背包操作提示` | 谢恺燊、黄凯峰 | M2 | `fix/game-map-password-hints` | 联调完善阶段 | 已完成 |
| #18 | `[Deploy] 完成服务器部署与公网访问配置` | 谢恺燊 | M3 | `feature/server-deployment-docs` | 发布准备阶段 | 进行中 |

## 6. 每日执行计划

| 天数 | 主要任务 | 对应 Issue | 当前结果 |
|---|---|---|---|
| Day 1 | 建目录、建分支、写项目计划、创建模板 | #1、#2、#3 | 已完成 |
| Day 2 | 初始化后端和前端工程 | #4、#5 | 已完成 |
| Day 3 | 后端模型、前端主界面 | #6、#11 | 已完成 |
| Day 4 | 后端会话与地图、移动规则、前端 API 封装 | #7、#8、#12 | 已完成 |
| Day 5 | 后端背包和物品、特殊规则开始、前端操作按钮 | #9、#10、#13 | 已完成 |
| Day 6 | 完成特殊规则、前后端首次联调 | #10、#13 | 已完成 |
| Day 7 | 修复联调问题，跑通完整游戏流程 | #8 - #13、#16、#17 | 已完成 |
| Day 8 | 补测试、配置 CI、自动打包 | #14 | 已完成基础配置，继续增加边界覆盖 |
| Day 9 | README、API 文档、架构文档、测试文档 | #15 | 进行中 |
| Day 10 | REPORT、AI 说明、视频、服务器部署、最终合并和 release | #15、#18 | 进行中 |

当前验收重点：

- 真实接口返回的完整 `GameState` 是前端状态刷新依据。
- 三关主流程、两个营地商店、机关长廊暗语和最终通关可连续演示。
- 保存、读取、放弃、重开、排行榜和历史最高分可用于展示。
- 结果弹窗、帮助说明、地图查看和当前房间标记可用于录屏。
- 仍需通过人工完整流程和异常场景验证发布稳定性。
- 服务器部署需保留可复现的环境配置、Nginx 反向代理、后端后台服务、SQLite 数据位置和发布更新步骤。

## 7. Pull Request 要求

每个 Pull Request 应包含：

- 本次修改内容。
- 关联 Issue，例如 `Closes #2`。
- 修改类型。
- 测试情况。
- 影响范围。
- 合并前检查。

合并要求：

- 后端功能合并前应运行 `mvn test`、`mvn checkstyle:check` 和 `mvn package`。
- 前端功能合并前应运行 `npm run lint`、`npm run test:run` 和 `npm run build`。
- 前后端联调功能应说明接口参数、返回状态和人工验证步骤。
- 文档类任务应检查内容完整性、链接正确性和与当前代码的一致性。
- 不得提交 `backend/data/`、`target/`、`node_modules/`、`dist/`、`.npm-cache/` 等运行时或生成目录。

## 8. 交付物

最终项目需要提交或准备以下交付物：

- 可运行的后端项目：已具备，发布前继续验证。
- 可运行的前端项目：已具备，发布前继续验证。
- README 项目说明：待按最终版本完善。
- REPORT.docx 或 REPORT.pdf：待完成。
- API 文档：需与最终接口保持同步。
- 架构文档：待完善。
- 测试说明：已有自动化基础，待整理结果。
- 服务器部署配置文档：已新增，需随最终公网访问地址继续完善。
- AI 使用说明：待完成。
- 5 - 10 分钟项目展示视频：待录制。
