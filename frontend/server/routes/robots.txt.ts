export default defineEventHandler((event) => {
  const config = useRuntimeConfig(event)
  const site = config.public.siteUrl.replace(/\/$/, '')
  setHeader(event, 'Content-Type', 'text/plain; charset=utf-8')
  return `User-agent: *
Allow: /
Disallow: /manage
Disallow: /write
Disallow: /notifications

Sitemap: ${site}/sitemap.xml
`
})
