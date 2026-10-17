<template>
  <div v-if="article" class="article-page">
    <div class="container">
      <NuxtLink to="/" class="back-link"><ArrowLeft :size="14" /> 返回文章列表</NuxtLink>

      <header class="article-header">
        <div class="article-kicker">
          <span v-if="article.status !== 1" class="status-pill" :class="article.status === 0 ? 'draft' : 'offline'">
            {{ statusLabel(article.status) }}预览
          </span>
          <span v-for="tag in article.tags" :key="tag" class="tag">{{ tag }}</span>
        </div>
        <h1 class="article-h1">{{ article.title }}</h1>
        <p v-if="article.summary" class="article-deck">{{ article.summary }}</p>
        <div class="byline">
          <NuxtLink v-if="article.author" :to="`/u/${article.author.id}`" class="byline-author">
            <Avatar :user="article.author" size="sm" />
            {{ article.author.nickname }}
          </NuxtLink>
          <span>{{ formatDate(article.publishTime || article.createdAt) }}</span>
          <span>{{ article.readingMinutes }} 分钟阅读</span>
          <span>{{ formatNumber(article.viewCount) }} 次阅读</span>
        </div>
      </header>

      <div class="article-layout">
        <div class="article-main">
          <div class="interaction-bar" style="display: flex; flex-wrap: wrap; gap: 10px; margin-bottom: 26px">
            <button class="like-btn" :class="{ liked: article.liked }" type="button" @click="toggleLike">
              <Heart :size="15" :fill="article.liked ? 'currentColor' : 'none'" />
              {{ article.liked ? '已喜欢' : '喜欢' }} · {{ article.likeCount }}
            </button>
            <button class="like-btn" type="button" @click="copyLink">
              <Link2 :size="15" /> 复制链接
            </button>
            <a class="like-btn" href="#comments">
              <MessageCircle :size="15" /> {{ article.commentCount }} 条讨论
            </a>
          </div>

          <MarkdownContent :html="article.contentHtml" />

          <div class="article-end">
            <span>— 完 —</span>
            <span class="line" />
            <span>写于 {{ formatDate(article.publishTime || article.createdAt) }}</span>
          </div>

          <div v-if="article.author" class="author-card">
            <Avatar :user="article.author" size="lg" />
            <div class="author-card-copy">
              <NuxtLink :to="`/u/${article.author.id}`" class="author-card-name">
                {{ article.author.nickname }}
              </NuxtLink>
              <p class="author-card-bio">持续写作，保持好奇。这里记录工程实践、阅读与生活中的长期问题。</p>
            </div>
            <NuxtLink :to="`/u/${article.author.id}`" class="btn btn-ghost btn-sm">访问空间</NuxtLink>
          </div>

          <CommentSection
            :article-id="article.id"
            :initial-count="article.commentCount"
            @count-change="article.commentCount = $event"
          />
        </div>
        <TocNav :items="article.toc" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft, Heart, Link2, MessageCircle } from 'lucide-vue-next'
import type { ArticleDetail } from '~/types'
import { useAuthStore } from '~/stores/auth'

const route = useRoute()
const api = useApi()
const auth = useAuthStore()
const toast = useToast()
const config = useRuntimeConfig()
const id = Number(route.params.id)

const { data: fetched, error } = await useAsyncData<ArticleDetail>(`article-${id}`, async () => {
  try {
    return await api.get<ArticleDetail>(`/articles/${id}`)
  } catch (err) {
    const status = (err as { status?: number }).status || 404
    throw createError({ statusCode: status, statusMessage: '文章不存在或已下架' })
  }
})

if (error.value || !fetched.value) {
  throw createError({ statusCode: 404, statusMessage: '文章不存在或已下架' })
}

// useAsyncData 返回 shallowRef，点赞等局部更新需要显式的深响应式副本。
const article = ref<ArticleDetail | null>(fetched.value)

const pageUrl = computed(() => `${config.public.siteUrl}/articles/${article.value?.id}`)

useSeoMeta({
  title: () => article.value?.title || '文章',
  description: () => article.value?.summary || '',
  ogTitle: () => article.value?.title || '',
  ogDescription: () => article.value?.summary || '',
  ogType: 'article',
  articlePublishedTime: () => article.value?.publishTime || '',
  articleModifiedTime: () => article.value?.updatedAt || '',
  articleTag: () => article.value?.tags || []
})

useHead(() => ({
  link: [{ rel: 'canonical', href: pageUrl.value }],
  script: article.value ? [{
    type: 'application/ld+json',
    innerHTML: JSON.stringify({
      '@context': 'https://schema.org',
      '@type': 'BlogPosting',
      headline: article.value.title,
      description: article.value.summary,
      datePublished: article.value.publishTime,
      dateModified: article.value.updatedAt,
      author: { '@type': 'Person', name: article.value.author?.nickname },
      mainEntityOfPage: pageUrl.value
    }).replace(/</g, '\\u003c')
  }] : []
}))

async function toggleLike() {
  if (!article.value) return
  if (!auth.isAuthenticated) {
    await navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return
  }
  try {
    const result = article.value.liked
      ? await api.del<{ liked: boolean; likeCount: number }>('/likes', { targetType: 1, targetId: article.value.id })
      : await api.put<{ liked: boolean; likeCount: number }>('/likes', { targetType: 1, targetId: article.value.id })
    article.value.liked = result.liked
    article.value.likeCount = result.likeCount
  } catch (err) {
    toast.error(err instanceof Error ? err.message : '操作失败')
  }
}

async function copyLink() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    toast.success('链接已复制')
  } catch {
    toast.error('复制失败，请从地址栏复制')
  }
}
</script>
