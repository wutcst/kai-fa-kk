# 《芝麻开门》项目计划文档

## 1. 项目概述

《芝麻开门》是一个基于 Vue 3 + Spring Boot 的前后端分离 Web 图形化文字冒险游戏。

项目以课程样例 `world-of-zuul` 为起点，计划扩展为包含金额、体力、负重、物品、暗语、救援和通关机制的完整游戏系统。

本项目采用 GitHub Issues、Milestones、Pull Requests 和分支模型进行小组协同开发。

## 2. 小组分工

| 负责人 | 负责 Issue | 主要职责 |
|---|---|---|
| 谢恺燊 | #1、#2、#3、#4、#6、#7、#8、#9、#10、#14、#15 | 后端、测试、CI、文档主控、分支合并、项目管理 |
| 黄凯峰 | #5、#11、#12、#13、#15 | 前端初始化、页面展示、接口调用、交互弹窗、展示素材 |

说明：

- #15 为共同任务。
- 组长负责报告结构、AI 使用说明和最终合并。
- 组员负责页面截图、视频素材和前端运行说明。

## 3. 里程碑计划

| Milestone | 时间 | 目标 | 版本 |
|---|---:|---|---|
| M1 基础工程与项目规范 | Day 1 - Day 2 | 完成分支、目录、前后端初始化、模板、需求文档 | v0.1 |
| M2 核心玩法与前后端联调 | Day 3 - Day 7 | 完成主要游戏逻辑、REST API、前端交互和完整游戏流程 | v0.5 |
| M3 测试、CI、文档与发布 | Day 8 - Day 10 | 完成测试、CI、README、REPORT、视频和最终合并 | v1.0 |

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
        ├── feature/frontend-init
        ├── feature/frontend-game-layout
        ├── feature/frontend-api-client
        └── feature/frontend-game-actions
```

合并路径：

- 后端功能分支合并到 `backend/dev`。
- 前端功能分支合并到 `frontend/dev`。
- `backend/dev` 和 `frontend/dev` 稳定后合并到 `develop`。
- `develop` 完成测试和文档后合并到 `main/master`。
- 发布阶段可创建 `release/v1.0` 或 tag `v1.0`。

## 5. Issue 拆分

| Issue | 标题 | 负责人 | Milestone | 建议分支 | 时间 |
|---|---|---|---|---|---|
| #1 | `[Init] 初始化仓库目录与分支模型` | 组长 | M1 | `feature/project-structure` | Day 1 |
| #2 | `[Docs] 编写项目需求与开发计划文档` | 组长 | M1 | `feature/docs-project-plan` | Day 1 |
| #3 | `[Workflow] 创建 Issue、PR 模板与协作规范` | 组长 | M1 | `feature/workflow-template` | Day 1 |
| #4 | `[Backend] 初始化 Spring Boot 后端项目` | 组长 | M1 | `feature/backend-init` | Day 1 - Day 2 |
| #5 | `[Frontend] 初始化 Vue 3 前端项目` | 组员 | M1 | `feature/frontend-init` | Day 1 - Day 2 |
| #6 | `[Backend] 实现玩家、物品、房间核心模型` | 组长 | M2 | `feature/backend-domain-models` | Day 3 |
| #7 | `[Backend] 实现地图、游戏会话与初始状态` | 组长 | M2 | `feature/backend-game-session` | Day 3 - Day 4 |
| #8 | `[Backend] 实现移动、体力、门票与金额规则` | 组长 | M2 | `feature/backend-move-cost` | Day 4 |
| #9 | `[Backend] 实现背包、拾取、丢弃与使用物品` | 组长 | M2 | `feature/backend-inventory` | Day 5 |
| #10 | `[Backend] 实现暗语开门、救援、通关和失败机制` | 组长 | M2 | `feature/backend-special-rules` | Day 5 - Day 6 |
| #11 | `[Frontend] 实现游戏主界面与状态展示` | 组员 | M2 | `feature/frontend-game-layout` | Day 3 - Day 4 |
| #12 | `[Frontend] 实现 API 调用封装与基础交互` | 组员 | M2 | `feature/frontend-api-client` | Day 4 - Day 5 |
| #13 | `[Frontend] 实现完整游戏操作与提示弹窗` | 组员 | M2 | `feature/frontend-game-actions` | Day 5 - Day 7 |
| #14 | `[Test/CI] 完成测试、代码检查与自动化构建` | 组长 | M3 | `feature/test-and-ci` | Day 8 |
| #15 | `[Docs/Release] 完成 README、REPORT、AI 说明、视频与最终发布` | 组长 + 组员 | M3 | `feature/final-docs-release` | Day 9 - Day 10 |

## 6. 每日执行计划

| 天数 | 主要任务 | 对应 Issue |
|---|---|---|
| Day 1 | 建目录、建分支、写项目计划、创建模板 | #1、#2、#3 |
| Day 2 | 初始化后端和前端工程 | #4、#5 |
| Day 3 | 后端模型、前端主界面 | #6、#11 |
| Day 4 | 后端会话与地图、移动规则、前端 API 封装 | #7、#8、#12 |
| Day 5 | 后端背包和物品、特殊规则开始、前端操作按钮 | #9、#10、#13 |
| Day 6 | 完成特殊规则、前后端首次联调 | #10、#13 |
| Day 7 | 修复联调问题，跑通完整游戏流程 | #8 - #13 |
| Day 8 | 补测试、配置 CI、自动打包 | #14 |
| Day 9 | README、API 文档、架构文档、测试文档 | #15 |
| Day 10 | REPORT、AI 说明、视频、最终合并和 release | #15 |

## 7. Pull Request 要求

每个 Pull Request 应包含：

- 本次修改内容
- 关联 Issue，例如 `Closes #2`
- 修改类型
- 测试情况
- 影响范围
- 合并前检查

合并要求：

- 后端功能合并前应运行 `mvn test`。
- 前端功能合并前应运行 `npm run build`。
- 文档类任务应检查内容完整性和链接正确性。
- 不得提交 `target/`、`node_modules/`、`dist/` 等生成目录。

## 8. 交付物

最终项目需要提交或准备以下交付物：

- 可运行的后端项目。
- 可运行的前端项目。
- README 项目说明。
- REPORT.docx 或 REPORT.pdf。
- API 文档。
- 架构文档。
- 测试说明。
- AI 使用说明。
- 5 - 10 分钟项目展示视频。

