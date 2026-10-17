const dateFormatter = new Intl.DateTimeFormat('zh-CN', {
  timeZone: 'Asia/Shanghai',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit'
})

const dateTimeFormatter = new Intl.DateTimeFormat('zh-CN', {
  timeZone: 'Asia/Shanghai',
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit'
})

/** 后端返回 LocalDateTime（无时区），统一按站点时区 Asia/Shanghai 解析。 */
export function parseDate(value?: string | null): Date | null {
  if (!value) return null
  const normalized = /[zZ]|[+-]\d{2}:\d{2}$/.test(value) ? value : `${value}+08:00`
  const date = new Date(normalized)
  return Number.isNaN(date.getTime()) ? null : date
}

export function formatDate(value?: string | null): string {
  const date = parseDate(value)
  return date ? dateFormatter.format(date) : '—'
}

export function formatDateTime(value?: string | null): string {
  const date = parseDate(value)
  return date ? dateTimeFormatter.format(date) : '—'
}

export function relativeTime(value?: string | null): string {
  const date = parseDate(value)
  if (!date) return ''
  const seconds = Math.floor((Date.now() - date.getTime()) / 1000)
  if (seconds < 60) return '刚刚'
  if (seconds < 3600) return `${Math.floor(seconds / 60)} 分钟前`
  if (seconds < 86400) return `${Math.floor(seconds / 3600)} 小时前`
  if (seconds < 86400 * 7) return `${Math.floor(seconds / 86400)} 天前`
  return formatDate(value)
}

export function formatNumber(value?: number): string {
  const number = value ?? 0
  if (number >= 10000) return `${(number / 10000).toFixed(1).replace('.0', '')}w`
  if (number >= 1000) return `${(number / 1000).toFixed(1).replace('.0', '')}k`
  return String(number)
}

export function initials(name?: string): string {
  if (!name) return '?'
  const trimmed = name.trim()
  if (!trimmed) return '?'
  const chars = Array.from(trimmed)
  return chars.length > 1 ? chars.slice(0, 2).join('').toUpperCase() : chars[0]!.toUpperCase()
}

export function statusLabel(status: number): string {
  if (status === 0) return '草稿'
  if (status === 2) return '已下架'
  return '已发布'
}

export function notificationLabel(type: number): string {
  if (type === 1) return '回复了你'
  if (type === 2) return '在评论中提到了你'
  return '评论了你的文章'
}

export function articlePath(id: number): string {
  return `/articles/${id}`
}

export function profilePath(id: number): string {
  return `/u/${id}`
}
