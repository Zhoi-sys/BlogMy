<template>
  <section class="comments" aria-label="评论区">
    <div class="comments-head">
      <h2 class="comments-title">讨论 <span class="comments-count">{{ count }} 条</span></h2>
      <span class="small muted">友善、具体、有建设性</span>
    </div>

    <CommentComposer
      v-if="auth.isAuthenticated"
      :article-id="articleId"
      @created="onRootCreated"
    />
    <div v-else class="comment-login">
      <span>登录后即可参与讨论，也可以 @ 你欣赏的人。</span>
      <NuxtLink :to="`/login?redirect=${encodeURIComponent(route.fullPath)}`" class="btn btn-soft btn-sm">
        登录参与
      </NuxtLink>
    </div>

    <div v-if="loading && !loaded" style="padding: 26px 0">
      <div class="skeleton" style="height: 18px; width: 38%; margin-bottom: 12px" />
      <div class="skeleton" style="height: 13px; width: 82%; margin-bottom: 8px" />
      <div class="skeleton" style="height: 13px; width: 64%" />
    </div>

    <div v-else-if="!comments.length" class="empty" style="padding: 46px 10px">
      <p style="margin: 0; color: var(--ink-soft)">还没有评论，来留下第一个想法。</p>
    </div>

    <div v-else class="comment-list">
      <CommentThread
        v-for="root in comments"
        :key="root.id"
        :root="root"
        :article-id="articleId"
        @changed="onThreadChanged"
      />
    </div>

    <div v-if="hasMore" class="load-more">
      <button class="btn btn-ghost" type="button" :disabled="loading" @click="load(false)">
        {{ loading ? '加载中…' : '加载更多评论' }}
      </button>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { CommentView, PageResult } from '~/types'
import { useAuthStore } from '~/stores/auth'

const props = defineProps<{ articleId: number; initialCount: number }>()
const emit = defineEmits<{ countChange: [count: number] }>()
const api = useApi()
const auth = useAuthStore()
const route = useRoute()
const toast = useToast()
const comments = ref<CommentView[]>([])
const count = ref(props.initialCount)
const cursor = ref<string | null>(null)
const hasMore = ref(false)
const loading = ref(false)
const loaded = ref(false)

async function load(reset = false) {
  if (loading.value) return
  loading.value = true
  try {
    const page = await api.get<PageResult<CommentView>>('/comments', {
      articleId: props.articleId,
      cursor: reset ? undefined : cursor.value || undefined,
      size: 10
    })
    comments.value = reset ? page.list : [...comments.value, ...page.list]
    cursor.value = page.nextCursor
    hasMore.value = page.hasMore
    loaded.value = true
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '评论加载失败')
  } finally {
    loading.value = false
  }
}

function onRootCreated() {
  count.value += 1
  emit('countChange', count.value)
  load(true)
}

function onThreadChanged(delta: number) {
  count.value = Math.max(0, count.value + delta)
  emit('countChange', count.value)
  load(true)
}

watch(count, value => emit('countChange', value))

watch(() => props.articleId, () => {
  comments.value = []
  cursor.value = null
  hasMore.value = false
  loaded.value = false
  count.value = props.initialCount
  load(true)
})

onMounted(() => load(true))
</script>
