<template>
  <article class="article-card fade-in">
    <div class="article-index">{{ String(index + 1).padStart(2, '0') }}</div>
    <div class="article-copy">
      <div v-if="article.status !== 1" class="article-kicker">
        <span class="status-pill" :class="statusClass">{{ statusLabel(article.status) }}</span>
      </div>
      <h2 class="article-title">
        <NuxtLink :to="articlePath(article.id)">{{ article.title }}</NuxtLink>
      </h2>
      <p v-if="article.summary" class="article-summary">{{ article.summary }}</p>
      <div class="article-meta">
        <span>{{ formatDate(article.publishTime || article.createdAt) }}</span>
        <span v-if="article.author" class="dot">{{ article.author.nickname }}</span>
        <span class="dot">{{ article.readingMinutes }} 分钟阅读</span>
      </div>
      <div v-if="article.tags.length" class="article-tags">
        <span v-for="tag in article.tags" :key="tag" class="tag">{{ tag }}</span>
      </div>
    </div>
    <div class="article-card-side">
      <span class="article-readtime">{{ formatNumber(article.viewCount) }} 阅读</span>
      <div class="article-readtime" style="display: flex; gap: 10px">
        <span style="display: inline-flex; align-items: center; gap: 3px"><Heart :size="12" />{{ formatNumber(article.likeCount) }}</span>
        <span style="display: inline-flex; align-items: center; gap: 3px"><MessageCircle :size="12" />{{ formatNumber(article.commentCount) }}</span>
      </div>
      <NuxtLink :to="articlePath(article.id)" class="article-arrow" aria-label="阅读文章">
        <ArrowUpRight :size="16" />
      </NuxtLink>
    </div>
  </article>
</template>

<script setup lang="ts">
import { ArrowUpRight, Heart, MessageCircle } from 'lucide-vue-next'
import type { ArticleCard } from '~/types'

const props = withDefaults(defineProps<{
  article: ArticleCard
  index?: number
}>(), { index: 0 })

const statusClass = computed(() => {
  if (props.article.status === 0) return 'draft'
  if (props.article.status === 2) return 'offline'
  return 'published'
})
</script>
