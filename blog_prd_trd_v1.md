# 个人博客系统 —— 一期需求文档（PRD）与技术文档（TRD）

> 版本：v1.1（一期裁剪版）　日期：2026-10　技术栈：Spring Boot 3.x（Java 17）
> v1.1 变更：一期仅保留**文章**内容形态；**视频发布与图片上传（媒体中心）移出一期范围**，相关模块/接口/表已移除。

---

# 第一部分：PRD（产品需求文档）

## 1. 项目背景与目标

个人博客是博主发布技术文章并与读者互动的个人内容平台。一期目标是搭建 **可上线运行的 MVP**，覆盖「文章生产 → 内容消费 → 互动」的完整闭环。

**一期目标（成功标准）：**
- 博主可独立完成文章的发布、编辑与管理；
- 读者无需注册即可浏览首页与文章，注册登录后可点赞、评论、@ 回复；
- 文章渲染支持 Markdown、表格、代码块、Mermaid 图表等富格式；
- 全站核心页面首屏加载 < 2s，文章渲染 < 1s。

## 2. 角色定义

| 角色 | 说明 | 核心权限 |
|---|---|---|
| 游客 | 未登录用户 | 浏览首页、文章详情、个人空间；不可互动 |
| 注册用户（读者） | 登录用户 | 点赞、评论、@ 回复、查看自己的互动记录 |
| 博主（管理员） | 站点所有者 | 在读者权限之上：发布/编辑/下架文章、删除任意评论、管理个人空间 |

> 一期简化：单博主模式，博主账号通过初始化和后台配置产生，不做多博主与权限分级。

## 3. 功能需求（一期范围）

### 3.1 账户与登录（F-01）

| 编号 | 需求 | 优先级 |
|---|---|---|
| F-01-1 | 用户名+密码注册（用户名唯一、密码强度校验、密码 BCrypt 加密存储） | P0 |
| F-01-2 | 用户名+密码登录，登录成功签发 JWT（Access Token 2h + Refresh Token 7d） | P0 |
| F-01-3 | 登出（服务端将 Token 加入黑名单直至过期） | P1 |
| F-01-4 | 登录状态保持与过期自动刷新（Refresh Token 轮换） | P1 |
| F-01-5 | 博主账号初始化（首次启动时通过配置项创建管理员） | P0 |

### 3.2 首页内容查看（F-02）

| 编号 | 需求 | 优先级 |
|---|---|---|
| F-02-1 | 首页展示已发布文章列表，按发布时间倒序 | P0 |
| F-02-2 | 分页加载（游标分页，每页 10 篇，支持下拉加载更多） | P0 |
| F-02-3 | 文章卡片信息：标题、摘要、标签、发布时间、点赞数、评论数、阅读量 | P0 |
| F-02-4 | 关键词搜索（按标题/摘要，一期做简单 LIKE，预留 ES） | P1 |

### 3.3 个人空间（F-03）

| 编号 | 需求 | 优先级 |
|---|---|---|
| F-03-1 | 空间页展示：头像、昵称、简介、注册时间、获赞总数、文章总数 | P0 |
| F-03-2 | 空间文章列表：该用户发布的已发布文章（游客可看博主全部；普通用户看自己需登录） | P0 |
| F-03-3 | 博主空间额外展示：草稿箱、文章管理入口（编辑/下架/删除） | P0 |
| F-03-4 | 个人资料编辑（昵称、简介） | P1 |

> 说明：一期不提供头像上传，头像使用系统默认头像（或 Gravatar 镜像，按邮箱 Hash，二期再考虑自托管上传）。

### 3.4 文章发布（F-04）★ 一期核心

| 编号 | 需求 | 优先级 |
|---|---|---|
| F-04-1 | Markdown 编辑器（所见即所得 + 分屏预览双模式） | P0 |
| F-04-2 | 格式自动适配渲染：标题/列表/引用/链接、**表格**、**代码块（语法高亮）**、**Mermaid 图表（流程图/时序图/甘特图等）**、数学公式（KaTeX，可选）；图片一期仅支持**外链图片**（Markdown 图片语法引用外部 URL，编辑器内粘贴图片时提示使用图床外链） | P0 |
| F-04-3 | 文章元信息：标题（必填，≤80 字）、摘要（选填，默认截取正文前 100 字）、标签（一期 1~5 个字符串标签） | P0 |
| F-04-4 | 保存草稿 / 发布 / 下架 / 删除；发布后编辑生成「更新于」时间 | P0 |
| F-04-5 | 文章详情页：正文渲染、目录（TOC）生成、阅读量自增（防刷：同 IP+文章 1 小时计 1 次） | P0 |
| F-04-6 | Mermaid 渲染失败降级：显示源码块 + 友好提示，不阻塞整页 | P0 |

### 3.5 评论与点赞（F-05）

| 编号 | 需求 | 优先级 |
|---|---|---|
| F-05-1 | 登录用户可对文章点赞、取消点赞；一人一篇文章一次点赞（幂等） | P0 |
| F-05-2 | 一级评论：登录用户对文章发表评论（≤1000 字） | P0 |
| F-05-3 | 评论列表：按时间倒序（一期不做热度排序），分页加载，展示评论人昵称/头像/内容/时间/点赞数 | P0 |
| F-05-4 | 评论点赞（独立计数的轻量点赞） | P1 |
| F-05-5 | 删除规则：评论作者可删自己的评论；博主可删任意评论；删除采用软删除（保留子评论上下文，显示「该评论已删除」） | P0 |
| F-05-6 | 敏感词过滤（维护一份基础词库，命中拒绝或打码，一期简单实现） | P1 |

### 3.6 @ 回复评论（F-06）

| 编号 | 需求 | 优先级 |
|---|---|---|
| F-06-1 | 评论输入框支持 `@用户名`（输入 @ 弹出用户搜索联想，选择后生成结构化 mention） | P0 |
| F-06-2 | 回复某条评论时自动 @ 被回复人，形成二级对话关系（reply_to_comment_id） | P0 |
| F-06-3 | 二级评论聚合展示：一级评论下折叠展示其回复（默认展示 3 条，可展开更多） | P0 |
| F-06-4 | 被 @ 的用户收到通知（站内通知列表：「xxx 回复了你 / 在评论中提到了你」，一期不做推送/邮件） | P0 |
| F-06-5 | 通知已读/未读状态，未读数角标 | P1 |

## 4. 非功能需求

| 类别 | 指标 |
|---|---|
| 性能 | 核心接口 P95 < 300ms；文章详情（含渲染）P95 < 500ms；列表接口 P95 < 200ms |
| 并发 | 一期单实例支撑 200 QPS（个人博客场景足够） |
| 可用性 | 全年可用性 ≥ 99.5%（单机+定时备份即可） |
| 安全 | 全站 HTTPS；接口鉴权；SQL 注入/XSS/CSRF 防护；限流（登录、评论接口） |
| 兼容性 | 支持 Chrome/Edge/Safari/Firefox 最近两个大版本；响应式适配移动端（≥375px） |
| 数据 | 数据库每日全量备份（mysqldump），保留 7 天 |
| SEO | 文章页 og 标签 + sitemap.xml |

## 5. 一期不做（Out of Scope）

**视频发布与视频播放**（二期）；**图片/文件上传与媒体中心**（含头像上传，二期）；文章封面图上传（一期封面使用文章内第一张外链图自动截取，或不展示封面）；多博主/多租户、文章分类专栏与归档、关注/粉丝体系、收藏、转发、私信、富文本（非 Markdown）编辑器、邮件/短信通知、第三方登录（GitHub 等，二期）、全文搜索引擎、内容审核 AI 接入、主题换肤。

## 6. 里程碑

| 里程碑 | 内容 | 周期参考 |
|---|---|---|
| M1 | 项目脚手架、登录注册、JWT、基础用户体系 | 1 周 |
| M2 | 文章模块（编辑器+渲染+详情+管理） | 2 周 |
| M3 | 互动模块（评论/@回复/点赞/通知） | 1.5 周 |
| M4 | 首页/个人空间/联调/测试/上线 | 1 周 |

---

# 第二部分：TRD（技术需求文档）

## 1. 总体架构

```
┌────────────┐   HTTPS   ┌─────────────────────────────────────────┐
│  Web 前端   │ ◄──────► │  Spring Boot 应用（单体，模块化分包）       │
│ Vue3+Vite  │    REST   │  ┌──────────┬──────────┬──────────────┐ │
└────────────┘           │  │用户模块   │ 文章模块  │ 互动模块       │ │
                         │  │auth/user  │ article/ │ like/comment/│ │
                         │  │           │ feed     │ mention/notify│ │
                         │  └──────────┴──────────┴──────────────┘ │
                         └──────┬───────────────┬──────────────────┘
                                │               │
                    ┌───────────▼───┐   ┌───────▼────────┐
                    │   MySQL 8     │   │    Redis 7      │
                    │  (主数据)      │   │ 缓存/计数/Token │
                    └───────────────┘   └────────────────┘
```

> 一期**无对象存储依赖**，架构更简单：应用服务器 + MySQL + Redis 即可部署。
> 文章中的图片为一期外链图（img 标签直接引用第三方图床/公开 URL），后端不做任何文件存储与转发。

**技术选型：**

| 层 | 技术 | 版本（建议） | 说明 |
|---|---|---|---|
| 语言/框架 | Java + Spring Boot | 17 / 3.3.x | 单体应用，按业务模块分包，预留拆分 |
| 安全 | Spring Security + JJWT | 6.x / 0.12.x | JWT 无状态鉴权，Token 黑名单存 Redis |
| 持久层 | MyBatis-Plus | 3.5.x | 分页插件、逻辑删除、自动填充 |
| 数据库 | MySQL | 8.0 | InnoDB，utf8mb4 |
| 缓存 | Redis | 7.x | 点赞状态/计数缓存、首页列表缓存、限流、黑名单 |
| 前端 | Vue3 + Vite + TypeScript + Pinia | 3.x | SPA，响应式 |
| Markdown | Vditor（编辑）+ markdown-it（渲染） | — | 插件：表格、代码高亮 highlight.js、mermaid.js、KaTeX |
| 构建部署 | Docker + docker-compose | — | 一期单机部署；Nginx 反代+静态资源+HTTPS |

## 2. 模块划分（后端包结构）

```
com.blog
├── common/        # 统一返回体、全局异常、枚举、常量、工具
├── config/        # Security/MyBatisPlus/Redis/WebMvc/限流配置
├── auth/          # 登录注册、JWT 签发校验、Token 黑名单
├── user/          # 用户 CRUD、资料、空间聚合、@联想搜索
├── article/       # 文章 CRUD、草稿、上下架、标签
├── feed/          # 首页文章流查询
├── interaction/   # 点赞(like)、评论(comment)、@提及(mention)
└── notify/        # 站内通知
```

## 3. 数据库设计（核心表）

```sql
-- 用户表
user(id BIGINT PK, username VARCHAR(32) UNIQUE, password VARCHAR(100),
     nickname VARCHAR(32), avatar_url VARCHAR(255) DEFAULT '',  -- 一期默认头像/Gravatar，不上传
     bio VARCHAR(255), role TINYINT DEFAULT 1,           -- 1用户 9博主
     status TINYINT DEFAULT 1, create_time DATETIME, update_time DATETIME);

-- 文章表
article(id, author_id FK, title VARCHAR(80), summary VARCHAR(200),
        content_md MEDIUMTEXT,        -- 原始 Markdown
        content_html MEDIUMTEXT,      -- 服务端渲染好的 HTML（可选，用于 SEO）
        status TINYINT,               -- 0草稿 1发布 2下架
        view_count INT DEFAULT 0, like_count INT DEFAULT 0, comment_count INT DEFAULT 0,
        publish_time DATETIME, create_time, update_time, deleted TINYINT DEFAULT 0);

-- 文章标签（推荐独立表，便于按标签筛选扩展）
article_tag(id, article_id FK, tag_name VARCHAR(32),
            KEY idx_tag(tag_name));

-- 点赞表（一期仅文章与评论两种对象）
like(id, user_id, target_type TINYINT,   -- 1文章 2评论
     target_id BIGINT, create_time,
     UNIQUE KEY uk_user_target(user_id, target_type, target_id));

-- 评论表
comment(id, user_id, article_id FK, parent_id BIGINT DEFAULT 0,   -- 一级评论为 0
        reply_to_user_id BIGINT,          -- 被回复人（@提及对象）
        content VARCHAR(1000),            -- 存原文，@以结构化标记存储
        like_count INT DEFAULT 0,
        status TINYINT DEFAULT 1, deleted TINYINT DEFAULT 0,
        create_time, update_time);
-- 索引: idx_article(article_id, parent_id, create_time)

-- 提及表（@ 关系，用于通知与反查）
mention(id, comment_id FK, from_user_id, to_user_id, create_time, read TINYINT DEFAULT 0);

-- 通知表
notification(id, user_id, type TINYINT,    -- 1回复我 2提到我 3评论了我的文章
             actor_id, article_id, comment_id,
             content_preview VARCHAR(100), read TINYINT DEFAULT 0, create_time);
```

**关键设计说明：**
1. `@回复` 的 content 中 mention 以 `[@昵称](uid:123)` 形式存储，前端渲染时解析为可点击链接；
2. 计数（like_count/view_count/comment_count）**冗余存储在文章表**，配合 Redis 缓存 + 定时落库（如每 5 分钟 flush），避免高频写库；
3. 逻辑删除（deleted 字段）保护内容可恢复；评论软删后展示占位；
4. 评论树只有两级：一级 + 回复，避免深嵌套递归查询。

## 4. API 设计（REST 风格，统一返回体 `Result<T>{code,msg,data}`）

### 4.1 认证
```
POST /api/auth/register        注册        {username, password}
POST /api/auth/login           登录        {username, password} → {accessToken, refreshToken}
POST /api/auth/refresh         刷新令牌    {refreshToken}
POST /api/auth/logout          登出(黑名单)
```

### 4.2 用户与空间
```
GET  /api/users/{id}                 用户信息（空间头部）
GET  /api/users/{id}/articles?status=&cursor=&size=   空间文章列表（游标分页）
PUT  /api/users/me                   更新资料（昵称、简介）
GET  /api/users/search?keyword=      @ 联想搜索
```

### 4.3 文章
```
GET    /api/feed?keyword=&cursor=&size=      首页文章流（倒序，可选关键词过滤）
GET    /api/articles/{id}                    文章详情（阅读量+1，返回 like/comment 计数）
POST   /api/articles                         新建（草稿或发布）
PUT    /api/articles/{id}                    编辑
POST   /api/articles/{id}/status             {action: publish|offline|delete}
GET    /api/articles/manage?status=&cursor=  博主文章管理列表（草稿箱等）
```

### 4.4 互动
```
PUT    /api/likes                 {targetType, targetId}   点赞（重复调用幂等）
DELETE /api/likes                 {targetType, targetId}   取消
GET    /api/comments?articleId=&cursor=                     一级评论列表
POST   /api/comments              {articleId, content, parentId?, replyToUserId?}
DELETE /api/comments/{id}         删除（作者或博主）
GET    /api/notifications?unreadOnly=&cursor=               通知列表
PUT    /api/notifications/read    {ids} or all
```

### 4.5 分页与游标约定
- 列表响应：`{ list: [...], nextCursor: "eyJpZCI6MTAwfQ==", hasMore: true }`
- cursor = Base64(最后一条排序字段快照)，避免深分页 offset 性能问题。

## 5. 核心流程设计

### 5.1 登录鉴权流程
1. 登录成功 → 生成 `accessToken(2h, 含 uid/role)` + `refreshToken(7d, 随机串)`；
2. 每次请求经 `JwtAuthFilter` 校验签名与有效期；登出时 jti 写入 Redis 黑名单（TTL=剩余有效期）；
3. accessToken 过期 → 用 refreshToken 换新（旧 refreshToken 作废，实现轮换）；
4. 权限：普通接口需 USER 角色；文章写接口需 uid==作者 或 role=博主。

### 5.2 富格式渲染方案（文章自动适配）
- **编辑端**：Vditor 编辑器，自带 GFM 语法、表格、代码块、公式；
- **渲染端**：`markdown-it` + 插件链：
  - GFM 表格（内置）；
  - `highlight.js` 代码高亮；
  - Mermaid：自定义 fence 规则（mermaid 代码块交给 mermaid.js，进入视口懒渲染）；
  - `markdown-it-katex` 数学公式；
  - 图片：外链 img 懒加载，支持点击放大（viewer）；
- **容错**：mermaid.parse 失败时保留源码块并提示「图表渲染失败」；
- **XSS 防护**：渲染配置 `html: false`，输出经 DOMPurify 白名单过滤；
- **SEO（可选）**：发布时将渲染好的 HTML 存 `content_html` 字段供爬虫抓取，客户端仍用 md 实时渲染。

### 5.3 评论与 @ 回复流程
```
POST /api/comments
 1. 校验登录、目标文章存在且已发布
 2. 敏感词过滤；解析 content 中 mention 标记，与 replyToUserId 合并去重
 3. 落库 comment；若为回复则 parent_id = 一级评论 id（二级评论不再产生 parent）
 4. 事务内写 mention 记录 + notification（@我的 / 回复了我，含 content 摘要 100 字）
 5. Redis 自增 comment_count（文章表计数延迟落库）
```
- 展示：一级评论分页；每条一级评论携带前 3 条回复 + `replyCount`，「展开更多」单独分页请求；
- 幂等：点赞/取消用 `INSERT ... ON DUPLICATE KEY UPDATE` 或先查后写，唯一键兜底；网络重试不产生重复数据。

### 5.4 计数与缓存设计
| 数据 | 存储 | 策略 |
|---|---|---|
| like_count/comment_count/view_count | 文章表 + Redis | 写先更 Redis（HINCRBY），定时任务 5min 批量刷库 |
| 用户是否已点赞 | Redis SET like:{uid}:{type}:{id} | 请求时合并返回，避免查库 |
| 首页文章流 | MySQL 查询 + Redis 缓存 60s | 发布/下架时删缓存 |
| Token 黑名单 / 限流 | Redis | String + TTL；登录接口 5 次/分钟/IP |

## 6. 安全设计

- 密码 BCrypt(cost=10)；登录接口限流 + 图形验证码（连续失败 3 次触发）；
- JWT 密钥走环境变量/配置中心，禁止入库入仓；
- 所有写接口鉴权；越权校验封装为 `OwnershipChecker`（uid==author 或博主）；
- XSS：前端 DOMPurify + 后端对 user-generated content 转义存储；文章外链图片加 `referrerpolicy="no-referrer"`；
- SQL 注入：MyBatis-Plus 参数化，禁止 `${}` 拼接；
- CSRF：JWT 放 Header（Authorization: Bearer）天然规避；
- 接口通用防护：全局 RateLimiter（Bucket4j/Redis 滑窗），关键字段长度与格式校验（Bean Validation）。

## 7. 性能与容量评估（一期）

| 项 | 评估 |
|---|---|
| 数据规模 | 文章 ≤5k，评论 ≤5w —— MySQL 单表无压力，无需分库分表 |
| QPS | 单实例 200 QPS（4C8G + 连接池 HikariCP max=20 足够） |
| 缓存命中 | 首页 Feed 与详情计数命中率目标 ≥90% |
| 前端 | 路由级代码分割；编辑器/mermaid/highlight 按需动态 import |

## 8. 测试与上线

- 单测：Service 层覆盖 ≥70%（JaCoCo 校验）；核心幂等/事务流程必须覆盖；
- 接口测试：REST Assured 或 Apifox 用例集；
- 压测：Feed/详情/评论接口 JMeter 200 QPS 基线；
- 部署：docker-compose（app + mysql + redis + nginx）；Nginx 反代 `/api`、托管前端静态资源与 HTTPS；
- 监控：Spring Actuator + 日志采集；错误告警；
- 备份：MySQL 每日 cron 全量备份保留 7 天。

## 9. 风险与应对

| 风险 | 应对 |
|---|---|
| Mermaid/公式渲染在低端设备卡顿 | 图表懒渲染（进入视口才渲染），提供「源码模式」开关 |
| 外链图片失效或防盗链 | 前端图片加载失败占位图；提示作者使用稳定图床 |
| 计数与真实数据不一致 | 定时任务对账（文章 count vs like/comment 表），差异报警 |
| 评论恶意刷量 | 评论限流（10 条/分钟/用户）+ 敏感词 + 博主可一键清空 |

---

# 附录：一期接口清单速览

| 模块 | 接口数 | 说明 |
|---|---|---|
| auth | 4 | 注册/登录/刷新/登出 |
| user | 4 | 信息/空间文章/资料/搜索 |
| feed | 1 | 首页文章流 |
| article | 5 | 详情/新建/编辑/状态流转/管理列表 |
| like | 2 | 点赞/取消 |
| comment | 3 | 列表/发表/删除 |
| notification | 2 | 列表/已读 |
| **合计** | **21** | 均含统一返回体与错误码规范 |
