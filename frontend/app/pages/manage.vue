<template>
  <div class="page-shell">
    <div class="container">
      <header class="page-head">
        <div>
          <span class="eyebrow">Writing desk</span>
          <h1 class="page-title">文章管理</h1>
          <p class="page-sub">草稿、已发布与已下架文章的统一工作台。</p>
        </div>
        <NuxtLink to="/write" class="btn btn-primary"><PenLine :size="15" /> 写新文章</NuxtLink>
      </header>

      <div class="filter-row">
        <button
          v-for="filter in filters"
          :key="String(filter.value)"
          class="filter-chip"
          :class="{ active: activeFilter === filter.value }"
          type="button"
          @click="changeFilter(filter.value)"
        >
          {{ filter.label }}
        </button>
      </div>

      <div v-if="pending" class="manage-list">
        <div v-for="i in 4" :key="i" class="manage-row">
          <div>
            <div class="skeleton" style="height: 17px; width: 55%; margin-bottom: 8px" />
            <div class="skeleton" style="height: 11px; width: 35%" />
          </div>
        </div>
      </div>

      <div v-else-if="articles.length" class="manage-list">
        <div v-for="article in articles" :key="article.id" class="manage-row">
          <div>
            <h2 class="manage-title">
              <NuxtLink :to="`/articles/${article.id}`">{{ article.title }}</NuxtLink>
            </h2>
            <div class="manage-meta">
              <span class="status-pill" :class="statusClass(article.status)">{{ statusLabel(article.status) }}</span>
              <span>更新于 {{ formatDateTime(article.updatedAt) }}</span>
              <span>{{ article.viewCount }} 阅读</span>
              <span>{{ article.likeCount }} 赞</span>
              <span>{{ article.commentCount }} 评论</span>
            </div>
          </div>
          <div class="manage-meta" style="align-self: center">
            <span v-for="tag in article.tags.slice(0, 2)" :key="tag" class="tag">{{ tag }}</span>
          </div>
          <div class="manage-actions">
            <NuxtLink :to="`/write/${article.id}`" class="btn btn-ghost">编辑</NuxtLink>
            <button
              v-if="article.status === 1"
              class="btn btn-ghost"
              type="button"
              @click="changeStatus(article, 'offline')"
            >下架</button>
            <button
              v-if="article.status !== 1"
              class="btn btn-soft"
              type="button"
              @click="changeStatus(article, 'publish')"
            >发布</button>
            <button class="btn btn-danger" type="button" @click="remove(article)">删除</button>
          </div>
        </div>
      </div>

      <EmptyState v-else title="这里还没有文章" description="写一篇新的，或者切换筛选条件看看。">
        <NuxtLink to="/write" class="btn btn-primary btn-sm" style="margin-top: 16px">开始写作</NuxtLink>
      </EmptyState>

      <div v-if="hasMore" class="load-more">
        <button class="btn btn-ghost" type="button" :disabled="pending" @click="load(true)">
          {{ pending ? '加载中…' : '加载更多' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { PenLine } from 'lucide-vue-next'
import type { ArticleCard, PageResult } from '~/types'

definePageMeta({ middleware: ['admin'] })
useSeoMeta({ title: '文章管理', robots: 'noindex' })

const api = useApi()
const toast = useToast()
const filters = [
  { label: '全部', value: null as number | null },
  { label: '已发布', value: 1 },
  { label: '草稿', value: 0 },
  { label: '已下架', value: 2 }
]
const activeFilter = ref<number | null>(null)
const articles = ref<ArticleCard[]>([])
const cursor = ref<string | null>(null)
const hasMore = ref(false)
const pending = ref(false)

const initial = await api.get<PageResult<ArticleCard>>('/articles/manage', { size: 10 }).catch(() => null)
if (initial) apply(initial)

function apply(page: PageResult<ArticleCard>, append = false) {
  articles.value = append ? [...articles.value, ...page.list] : page.list
  cursor.value = page.nextCursor
  hasMore.value = page.hasMore
}

async function load(append = false) {
  pending.value = true
  try {
    const page = await api.get<PageResult<ArticleCard>>('/articles/manage', {
      status: activeFilter.value ?? undefined,
      cursor: append ? cursor.value || undefined : undefined,
      size: 10
    })
    apply(page, append)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '加载失败')
  } finally {
    pending.value = false
  }
}

function changeFilter(value: number | null) {
  activeFilter.value = value
  cursor.value = null
  load(false)
}

function statusClass(status: number) {
  if (status === 0) return 'draft'
  if (status === 2) return 'offline'
  return 'published'
}

async function changeStatus(article: ArticleCard, action: 'publish' | 'offline') {
  try {
    await api.post(`/articles/${article.id}/status`, { action })
    toast.success(action === 'publish' ? '文章已发布' : '文章已下架')
    await load(false)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '操作失败')
  }
}

async function remove(article: ArticleCard) {
  if (!window.confirm(`确定删除《${article.title}》吗？删除后不会出现在管理列表中。`)) return
  try {
    await api.post(`/articles/${article.id}/status`, { action: 'delete' })
    toast.success('文章已删除')
    await load(false)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '删除失败')
  }
}
</script>
