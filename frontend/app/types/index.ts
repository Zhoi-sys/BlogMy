export interface ApiResult<T> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  nextCursor: string | null
  hasMore: boolean
}

export interface UserSummary {
  id: number
  username: string
  nickname: string
  avatarUrl: string
  role: number
}

export interface UserView extends UserSummary {
  bio: string
  createdAt: string
}

export interface UserStats {
  articleCount: number
  likeCount: number
  viewCount: number
  commentCount: number
}

export interface UserProfile {
  user: UserView
  stats: UserStats
}

export interface ArticleCard {
  id: number
  title: string
  summary: string
  tags: string[]
  status: number
  publishTime: string | null
  createdAt: string
  updatedAt: string
  viewCount: number
  likeCount: number
  commentCount: number
  readingMinutes: number
  author: UserSummary
}

export interface TocItem {
  level: number
  id: string
  text: string
}

export interface ArticleDetail extends Omit<ArticleCard, 'tags'> {
  contentHtml: string
  contentMd?: string
  tags: string[]
  toc: TocItem[]
  liked: boolean
  canEdit: boolean
}

export interface CommentView {
  id: number
  articleId: number
  parentId: number
  author: UserSummary | null
  contentHtml: string
  deleted: boolean
  likeCount: number
  liked: boolean
  replyToUserId: number | null
  replyToNickname: string | null
  replyCount: number
  previewReplies: CommentView[]
  createdAt: string
}

export interface NotificationView {
  id: number
  type: number
  actor: UserSummary | null
  articleId: number
  articleTitle: string | null
  commentId: number
  contentPreview: string
  read: boolean
  createdAt: string
}

export interface SiteView {
  title: string
  description: string
  siteUrl: string
  owner: UserSummary
  stats: UserStats
}

export interface AuthTokens {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserView
}

export interface LikeResult {
  liked: boolean
  likeCount: number
}
