import type { ArticleCard, PageResult, SiteView } from '~/types'

function escapeXml(value?: string | null) {
  return (value || '').replace(/[<>&'"]/g, char => ({
    '<': '&lt;',
    '>': '&gt;',
    '&': '&amp;',
    "'": '&apos;',
    '"': '&quot;'
  }[char] as string))
}

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const siteUrl = config.public.siteUrl.replace(/\/$/, '')
  const [site, feed] = await Promise.all([
    $fetch<{ data: SiteView }>('/site', { baseURL: config.apiBase }),
    $fetch<{ data: PageResult<ArticleCard> }>('/feed', { baseURL: config.apiBase, query: { size: 20 } })
  ])
  const articles = feed.data?.list || []
  const items = articles.map(article => `
    <item>
      <title>${escapeXml(article.title)}</title>
      <link>${siteUrl}/articles/${article.id}</link>
      <guid isPermaLink="true">${siteUrl}/articles/${article.id}</guid>
      <description>${escapeXml(article.summary)}</description>
      <pubDate>${new Date(`${article.publishTime || article.createdAt}+08:00`).toUTCString()}</pubDate>
    </item>`).join('')

  setHeader(event, 'Content-Type', 'application/rss+xml; charset=utf-8')
  return `<?xml version="1.0" encoding="UTF-8"?>
<rss version="2.0">
  <channel>
    <title>${escapeXml(site.data?.title || '墨迹')}</title>
    <link>${siteUrl}</link>
    <description>${escapeXml(site.data?.description)}</description>
    <language>zh-CN</language>${items}
  </channel>
</rss>`
})
