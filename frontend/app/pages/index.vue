<template>
  <div class="home">
    <section class="hero container">
      <div>
        <span class="eyebrow">Personal notes · since 2026</span>
        <h1 class="hero-title">
          把思考写成<br>
          <em>可以漫步的路。</em>
        </h1>
        <p class="hero-text">
          {{ site?.description || '一个关于代码、设计与长期主义的个人空间。' }}
        </p>
        <div class="hero-actions">
          <a href="#latest" class="btn btn-primary">开始阅读 <ArrowDown :size="15" /></a>
          <NuxtLink v-if="site" :to="`/u/${site.owner.id}`" class="btn btn-ghost">认识作者</NuxtLink>
        </div>
      </div>
      <aside v-if="site" class="hero-aside">
        <p class="hero-note">
          写作不是为了证明知道，而是为了看清自己还不知道什么。
        </p>
        <div class="hero-author">
          <Avatar :user="site.owner" size="lg" />
          <div>
            <div class="hero-author-name">{{ site.owner.nickname }}</div>
            <div class="small muted">独立写作者 · 全栈工程师</div>
          </div>
        </div>
        <div class="hero-stats">
          <div class="stat">
            <div class="stat-value">{{ site.stats.articleCount }}</div>
            <div class="stat-label">篇文章</div>
          </div>
          <div class="stat">
            <div class="stat-value">{{ formatNumber(site.stats.likeCount) }}</div>
            <div class="stat-label">次喜欢</div>
          </div>
          <div class="stat">
            <div class="stat-value">{{ formatNumber(site.stats.viewCount) }}</div>
            <div class="stat-label">次阅读</div>
          </div>
        </div>
      </aside>
    </section>

    <section id="latest" class="section container">
      <div class="section-head">
        <div>
          <span class="eyebrow">Latest writing</span>
          <h2 class="section-title">{{ keyword ? `搜索「${keyword}」` : '最近写下的' }}</h2>
        </div>
        <form class="search-box" @submit.prevent="search">
          <input v-model="searchInput" type="search" placeholder="搜索标题或摘要…" aria-label="搜索文章">
          <button type="submit" aria-label="搜索"><Search :size="15" /></button>
        </form>
      </div>

      <div v-if="pending && !articles.length" class="article-list">
        <div v-for="i in 3" :key="i" class="article-card">
          <div class="skeleton" style="height: 18px; width: 72%; margin-bottom: 12px" />
          <div class="skeleton" style="height: 13px; width: 92%; margin-bottom: 8px" />
          <div class="skeleton" style="height: 13px; width: 55%" />
        </div>
      </div>

      <div v-else-if="articles.length" class="article-list">
        <ArticleCard v-for="(article, index) in articles" :key="article.id" :article="article" :index="index" />
      </div>

      <EmptyState v-else title="没有找到相关文章" description="换一个关键词，或者回到全部文章。" />

      <div v-if="hasMore" class="load-more">
        <button class="btn btn-ghost" type="button" :disabled="loadingMore" @click="loadMore">
          {{ loadingMore ? '加载中…' : '继续往下读' }}
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ArrowDown, Search } from 'lucide-vue-next'
import type { ArticleCard, PageResult, SiteView } from '~/types'

const api = useApi()
const route = useRoute()
const keyword = computed(() => String(route.query.q || '').trim())
const searchInput = ref(keyword.value)
const articles = ref<ArticleCard[]>([])
const cursor = ref<string | null>(null)
const hasMore = ref(false)
const loadingMore = ref(false)

const { data: site } = await useAsyncData<SiteView>('site-info', () => api.get<SiteView>('/site'), { lazy: true })

const { data: feed, pending } = await useAsyncData<PageResult<ArticleCard>>(
  'home-feed',
  () => api.get<PageResult<ArticleCard>>('/feed', { keyword: keyword.value || undefined, size: 10 }),
  { watch: [keyword] }
)

watch(feed, (value) => {
  if (!value) return
  articles.value = value.list
  cursor.value = value.nextCursor
  hasMore.value = value.hasMore
}, { immediate: true })

watch(keyword, (value) => { searchInput.value = value })

function search() {
  const query = searchInput.value.trim()
  navigateTo({ path: '/', query: query ? { q: query } : {} })
}

async function loadMore() {
  if (!hasMore.value || loadingMore.value) return
  loadingMore.value = true
  try {
    const page = await api.get<PageResult<ArticleCard>>('/feed', {
      keyword: keyword.value || undefined,
      cursor: cursor.value || undefined,
      size: 10
    })
    articles.value = [...articles.value, ...page.list]
    cursor.value = page.nextCursor
    hasMore.value = page.hasMore
  } finally {
    loadingMore.value = false
  }
}

useSeoMeta({
  title: '文章与笔记',
  description: site.value?.description || '一个关于代码、设计与长期主义的个人空间。',
  ogTitle: site.value?.title || '墨迹 · Inkwell',
  ogDescription: site.value?.description || '个人博客'
})
</script>
