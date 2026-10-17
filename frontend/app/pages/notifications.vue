<template>
  <div class="page-shell">
    <div class="container" style="max-width: 900px">
      <header class="page-head">
        <div>
          <span class="eyebrow">Inbox</span>
          <h1 class="page-title">通知</h1>
          <p class="page-sub">有人回复了你，或者在评论中提到了你。</p>
        </div>
        <button v-if="unreadCount > 0" class="btn btn-ghost btn-sm" type="button" :disabled="marking" @click="markAll">
          全部已读
        </button>
      </header>

      <div class="filter-row">
        <button class="filter-chip" :class="{ active: !unreadOnly }" type="button" @click="changeFilter(false)">全部</button>
        <button class="filter-chip" :class="{ active: unreadOnly }" type="button" @click="changeFilter(true)">
          未读 {{ unreadCount ? `(${unreadCount})` : '' }}
        </button>
      </div>

      <div v-if="loading && !loaded" style="padding: 28px 0">
        <div v-for="i in 4" :key="i" class="notice-item">
          <div class="skeleton" style="width: 36px; height: 36px; border-radius: 50%" />
          <div style="flex: 1">
            <div class="skeleton" style="height: 13px; width: 58%; margin-bottom: 8px" />
            <div class="skeleton" style="height: 11px; width: 38%" />
          </div>
        </div>
      </div>

      <div v-else-if="items.length" class="notice-list">
        <div
          v-for="item in items"
          :key="item.id"
          class="notice-item"
          :class="{ unread: !item.read }"
          role="link"
          tabindex="0"
          @click="open(item)"
          @keyup.enter="open(item)"
        >
          <Avatar v-if="item.actor" :user="item.actor" />
          <div class="notice-copy">
            <p class="notice-line">
              <strong>{{ item.actor?.nickname || '一位读者' }}</strong>
              {{ notificationLabel(item.type) }}
              <template v-if="item.articleTitle">：{{ item.articleTitle }}</template>
            </p>
            <p v-if="item.contentPreview" class="notice-preview">“{{ item.contentPreview }}”</p>
          </div>
          <span class="notice-time">{{ relativeTime(item.createdAt) }}</span>
        </div>
      </div>

      <EmptyState v-else title="没有新通知" description="当有人回复你或提到你时，会出现在这里。" />

      <div v-if="hasMore" class="load-more">
        <button class="btn btn-ghost" type="button" :disabled="loading" @click="load(true)">
          {{ loading ? '加载中…' : '加载更多' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { NotificationView, PageResult } from '~/types'

definePageMeta({ middleware: ['auth'] })
useSeoMeta({ title: '通知', robots: 'noindex' })

const api = useApi()
const toast = useToast()
const items = ref<NotificationView[]>([])
const unreadOnly = ref(false)
const cursor = ref<string | null>(null)
const hasMore = ref(false)
const loading = ref(false)
const loaded = ref(false)
const marking = ref(false)
const unreadCount = ref(0)

async function loadUnread() {
  try {
    const result = await api.get<{ count: number }>('/notifications/unread-count')
    unreadCount.value = result.count
  } catch {
    unreadCount.value = 0
  }
}

async function load(append = false, reset = false) {
  if (loading.value) return
  loading.value = true
  try {
    const page = await api.get<PageResult<NotificationView>>('/notifications', {
      unreadOnly: unreadOnly.value,
      cursor: append && !reset ? cursor.value || undefined : undefined,
      size: 20
    })
    items.value = append && !reset ? [...items.value, ...page.list] : page.list
    cursor.value = page.nextCursor
    hasMore.value = page.hasMore
    loaded.value = true
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '通知加载失败')
  } finally {
    loading.value = false
  }
}

function changeFilter(value: boolean) {
  if (unreadOnly.value === value && loaded.value) return
  unreadOnly.value = value
  cursor.value = null
  items.value = []
  load(false, true)
}

async function markAll() {
  marking.value = true
  try {
    const result = await api.put<{ count: number }>('/notifications/read', { all: true })
    unreadCount.value = result.count
    items.value = items.value.map(item => ({ ...item, read: true }))
    toast.success('已全部标记为已读')
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '操作失败')
  } finally {
    marking.value = false
  }
}

async function open(item: NotificationView) {
  if (!item.read) {
    try {
      const result = await api.put<{ count: number }>('/notifications/read', { ids: [item.id] })
      unreadCount.value = result.count
      item.read = true
    } catch {
      // 通知已读失败不阻塞跳转。
    }
  }
  if (item.articleId) {
    await navigateTo(`/articles/${item.articleId}#comment-${item.commentId}`)
  }
}

onMounted(() => {
  loadUnread()
  load(false, true)
})
</script>
