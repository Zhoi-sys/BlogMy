# API 速览（一期）

Base URL：`/api`。除公开接口外均需 `Authorization: Bearer <accessToken>`。

统一响应：

```json
{ "code": 0, "msg": "OK", "data": {} }
```

错误码：`40001` 参数错误、`40100` 未登录/令牌失效、`40300` 无权限、`40400` 不存在、`40900` 状态冲突、`42900` 请求过频、`50000` 服务异常。

## 认证

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/auth/register` | `{username,password}`，用户名 3-32 位字母/数字/下划线，密码 8-64 位含字母数字 |
| POST | `/auth/login` | `{username,password}` → accessToken / refreshToken / user |
| POST | `/auth/refresh` | `{refreshToken}`，旧 refreshToken 立即失效 |
| POST | `/auth/logout` | `Authorization` 必填；可选 `{refreshToken}` |
| GET | `/auth/me` | 当前用户 |

## 首页 / 文章

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/site` | 站点标题、简介、博主与统计（公开） |
| GET | `/feed?keyword=&cursor=&size=10` | 已发布文章流（公开） |
| GET | `/articles/{id}` | 详情；作者可预览草稿/下架，公开仅已发布 |
| GET | `/articles/manage?status=&cursor=&size=10` | 博主管理列表（ADMIN） |
| POST | `/articles` | 新建草稿/发布（ADMIN） |
| PUT | `/articles/{id}` | 编辑（ADMIN） |
| POST | `/articles/{id}/status` | `{action: publish|offline|delete}`（ADMIN） |

文章请求字段：`title`（≤80）、`summary`（可选，≤255）、`contentMd`、`tags`（发布时 1-5 个）、`status`（0 草稿 / 1 发布）。

文章详情响应中的 `contentHtml` 为服务端清洗后的安全 HTML；`toc` 为 `[{level,id,text}]`；`contentMd` 仅作者可见。

## 用户 / 空间

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/users/{id}` | 用户资料与公开统计（公开） |
| GET | `/users/{id}/articles?status=&cursor=&size=10` | 空间文章；非本人只能看 status=1 |
| PUT | `/users/me` | `{nickname,bio}` |
| GET | `/users/search?keyword=` | @ 联想，最多 8 条，不含自己 |

## 点赞

| 方法 | 路径 | 说明 |
|---|---|---|
| PUT | `/likes` | `{targetType:1 文章/2 评论,targetId}`，幂等 |
| DELETE | `/likes` | 同上，取消点赞 |

## 评论 / @ 回复

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/comments?articleId=&cursor=&size=10` | 一级评论，含前 3 条回复与 `replyCount` |
| GET | `/comments/{id}/replies?cursor=&size=10` | 展开更多回复（按对话时间升序） |
| POST | `/comments` | `{articleId,content,parentId?,replyToUserId?,mentionUserIds?}` |
| DELETE | `/comments/{id}` | 评论作者或博主；软删除并保留上下文 |

服务端将 `@昵称` 转为结构化标记，禁止客户端伪造 mention；回复某条评论时自动 @ 被回复人。

## 通知

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/notifications?unreadOnly=&cursor=&size=20` | 通知列表 |
| GET | `/notifications/unread-count` | 未读数 |
| PUT | `/notifications/read` | `{ids:[...]}` 或 `{all:true}`，返回最新未读数 |

通知类型：`1` 回复了我、`2` 在评论中提到了我、`3` 评论了我的文章。

## 游标分页

列表返回：

```json
{ "list": [], "nextCursor": "MjAyNi0xMC0xOFQwMDowMDowMHwx", "hasMore": true }
```

`nextCursor` 为 Base64URL 编码的时间 + id 快照；传回即取下一页。游标无效返回 400。
