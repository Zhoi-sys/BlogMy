export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: { enabled: false },
  ssr: true,
  modules: ['@pinia/nuxt'],
  css: ['~/assets/css/main.css', 'katex/dist/katex.min.css'],
  runtimeConfig: {
    apiBase: process.env.NUXT_API_BASE || 'http://localhost:8080/api',
    public: {
      apiBase: process.env.NUXT_PUBLIC_API_BASE || 'http://localhost:8080/api',
      siteUrl: process.env.NUXT_PUBLIC_SITE_URL || 'http://localhost:3000'
    }
  },
  app: {
    head: {
      htmlAttrs: { lang: 'zh-CN' },
      title: '墨迹 · Inkwell',
      titleTemplate: '%s · 墨迹',
      meta: [
        { name: 'viewport', content: 'width=device-width, initial-scale=1, viewport-fit=cover' },
        { name: 'theme-color', content: '#f4f0e8' },
        { name: 'description', content: '一个关于代码、设计与长期主义的个人空间。' }
      ],
      link: [
        { rel: 'icon', type: 'image/svg+xml', href: '/favicon.svg' },
        { rel: 'alternate', type: 'application/rss+xml', title: '墨迹 RSS', href: '/rss.xml' }
      ],
      script: [
        {
          key: 'theme-bootstrap',
          innerHTML: `(function(){try{var t=localStorage.getItem('inkwell-theme');if(!t){t=window.matchMedia('(prefers-color-scheme: dark)').matches?'dark':'light'}document.documentElement.dataset.theme=t}catch(e){}})();`
        }
      ]
    }
  },
  devServer: { port: 3000 },
  nitro: { compressPublicAssets: true },
  typescript: { strict: true, typeCheck: false },
  vite: {
    optimizeDeps: {
      include: ['highlight.js', 'mermaid', 'katex', 'katex/contrib/auto-render', 'lucide-vue-next', 'vditor']
    }
  }
})
