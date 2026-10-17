import type { ArticleCard, PageResult } from '~/types'

function escapeXml(value: string) {
  return value.replace(/[<>&'"]/g, char => ({
    '<': '&lt;',
    '>': '&gt;',
    '&': '&amp;',
    "'": '&apos;',
    '"': '&quot;'
  }[char] as string))
}

export default defineEventHandler(async (event) => {
  const config = useRuntimeConfig(event)
  const base = config.apiBase
  const site = config.public.siteUrl.replace(/\/$/, '')
  const articles: ArticleCard[] = []
  let cursor: string | null = null

  do {
    const page: { code: number; data: PageResult<ArticleCard> } =
      await $fetch<{ code: number; data: PageResult<ArticleCard> }>('/feed', {
        baseURL: base,
        query: { size: 30, cursor: cursor || undefined }
      })
    articles.push(...(page.data?.list || []))
    cursor = page.data?.hasMore ? page.data.nextCursor : null
  } while (cursor && articles.length < 5000)

  const urls = [
    `${site}/`,
    ...articles.map(article => `${site}/articles/${article.id}`)
  ]

  setHeader(event, 'Content-Type', 'application/xml; charset=utf-8')
  return `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urls.map(url => `  <url><loc>${escapeXml(url)}</loc></url>`).join('\n')}
</urlset>`
})
