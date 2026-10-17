<template>
  <div :id="`comment-${rootState.id}`" class="comment">
    <Avatar v-if="rootState.author" :user="rootState.author" />
    <Avatar v-else name="已删除" />
    <div class="comment-body">
      <div class="comment-head">
        <span v-if="rootState.author" class="comment-author">{{ rootState.author.nickname }}</span>
        <span v-else class="comment-author muted">该评论已删除</span>
        <span class="comment-time">{{ relativeTime(rootState.createdAt) }}</span>
      </div>
      <div v-if="rootState.deleted" class="comment-deleted">这条评论已被作者删除，但回复仍然保留。</div>
      <div v-else class="comment-content" v-html="rootState.contentHtml" />
      <div class="comment-actions">
        <button
          class="comment-action"
          :class="{ active: rootState.liked }"
          type="button"
          :disabled="rootState.deleted"
          @click="toggleLike(rootState)"
        >
          <Heart :size="13" :fill="rootState.liked ? 'currentColor' : 'none'" />
          {{ rootState.likeCount || '赞' }}
        </button>
        <button
          v-if="!rootState.deleted && auth.isAuthenticated"
          class="comment-action"
          type="button"
          @click="startReply(rootState)"
        >
          <Reply :size="13" /> 回复
        </button>
        <button
          v-if="canDelete(rootState)"
          class="comment-action"
          type="button"
          @click="removeComment(rootState)"
        >
          <Trash2 :size="13" /> 删除
        </button>
      </div>

      <div v-if="replies.length" class="replies">
        <div v-for="reply in replies" :id="`comment-${reply.id}`" :key="reply.id" class="comment">
          <Avatar v-if="reply.author" :user="reply.author" size="sm" />
          <Avatar v-else name="已删除" size="sm" />
          <div class="comment-body">
            <div class="comment-head">
              <span v-if="reply.author" class="comment-author">{{ reply.author.nickname }}</span>
              <span v-else class="comment-author muted">该评论已删除</span>
              <span v-if="reply.replyToNickname" class="comment-reply-target">回复 @{{ reply.replyToNickname }}</span>
              <span class="comment-time">{{ relativeTime(reply.createdAt) }}</span>
            </div>
            <div v-if="reply.deleted" class="comment-deleted">该回复已删除。</div>
            <div v-else class="comment-content" v-html="reply.contentHtml" />
            <div class="comment-actions">
              <button
                class="comment-action"
                :class="{ active: reply.liked }"
                type="button"
                :disabled="reply.deleted"
                @click="toggleLike(reply)"
              >
                <Heart :size="12" :fill="reply.liked ? 'currentColor' : 'none'" />
                {{ reply.likeCount || '赞' }}
              </button>
              <button
                v-if="!reply.deleted && auth.isAuthenticated"
                class="comment-action"
                type="button"
                @click="startReply(reply)"
              >
                <Reply :size="12" /> 回复
              </button>
              <button
                v-if="canDelete(reply)"
                class="comment-action"
                type="button"
                @click="removeComment(reply)"
              >
                <Trash2 :size="12" /> 删除
              </button>
            </div>
          </div>
        </div>
      </div>

      <button
        v-if="replyCount > 3 || expanded"
        class="replies-toggle"
        type="button"
        :disabled="loadingReplies"
        @click="toggleReplies"
      >
        <template v-if="loadingReplies">加载中…</template>
        <template v-else-if="!expanded">展开其余 {{ Math.max(0, replyCount - replies.length) }} 条回复</template>
        <template v-else-if="replyHasMore">加载更多回复</template>
        <template v-else>收起回复</template>
      </button>

      <CommentComposer
        v-if="replyTo && auth.isAuthenticated"
        :article-id="articleId"
        :reply-to="replyTo"
        @created="onReplyCreated"
        @cancel="replyTo = null"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Heart, Reply, Trash2 } from 'lucide-vue-next'
import type { CommentView, PageResult } from '~/types'
import { useAuthStore } from '~/stores/auth'

const props = defineProps<{ root: CommentView; articleId: number }>()
const emit = defineEmits<{ changed: [delta: number] }>()

const api = useApi()
const auth = useAuthStore()
const toast = useToast()
const route = useRoute()

const rootState = ref<CommentView>({ ...props.root })
const replies = ref<CommentView[]>([...props.root.previewReplies])
const replyCount = ref(props.root.replyCount)
const replyTo = ref<CommentView | null>(null)
const expanded = ref(false)
const loadingReplies = ref(false)
const replyCursor = ref<string | null>(null)
const replyHasMore = ref(props.root.replyCount > props.root.previewReplies.length)

watch(() => props.root, (value) => {
  rootState.value = { ...value }
  replyCount.value = value.replyCount
  if (!expanded.value) {
    replies.value = [...value.previewReplies]
    replyHasMore.value = value.replyCount > value.previewReplies.length
  }
}, { deep: true })

function canDelete(comment: CommentView) {
  return Boolean(auth.user && comment.author && (comment.author.id === auth.user.id || auth.isAdmin))
}

function requireLogin() {
  if (!auth.isAuthenticated) {
    navigateTo(`/login?redirect=${encodeURIComponent(route.fullPath)}`)
    return false
  }
  return true
}

function startReply(comment: CommentView) {
  if (!requireLogin()) return
  replyTo.value = comment
  nextTick(() => {
    document.getElementById(`comment-${comment.id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  })
}

async function toggleReplies() {
  if (!expanded.value) {
    await loadMoreReplies(true)
    expanded.value = true
    return
  }
  if (replyHasMore.value) {
    await loadMoreReplies(false)
    return
  }
  expanded.value = false
  replies.value = [...props.root.previewReplies]
}

async function loadMoreReplies(reset: boolean) {
  if (loadingReplies.value) return
  loadingReplies.value = true
  try {
    const page = await api.get<PageResult<CommentView>>(`/comments/${props.root.id}/replies`, {
      cursor: reset ? undefined : replyCursor.value || undefined,
      size: 20
    })
    const existing = new Set(replies.value.map(item => item.id))
    const appended = page.list.filter(item => !existing.has(item.id))
    replies.value = reset ? page.list : [...replies.value, ...appended]
    replyCursor.value = page.nextCursor
    replyHasMore.value = page.hasMore
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '加载回复失败')
  } finally {
    loadingReplies.value = false
  }
}

async function toggleLike(comment: CommentView) {
  if (!requireLogin() || comment.deleted) return
  const nextLiked = !comment.liked
  try {
    const result = nextLiked
      ? await api.put<{ liked: boolean; likeCount: number }>('/likes', { targetType: 2, targetId: comment.id })
      : await api.del<{ liked: boolean; likeCount: number }>('/likes', { targetType: 2, targetId: comment.id })
    comment.liked = result.liked
    comment.likeCount = result.likeCount
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '操作失败')
  }
}

async function removeComment(comment: CommentView) {
  if (!window.confirm('确定删除这条评论吗？')) return
  try {
    await api.del(`/comments/${comment.id}`)
    toast.success('评论已删除')
    emit('changed', -1)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '删除失败')
  }
}

function onReplyCreated(comment: CommentView) {
  const exists = replies.value.some(item => item.id === comment.id)
  if (!exists) replies.value = [...replies.value, comment]
  replyCount.value += 1
  replyTo.value = null
  expanded.value = true
  emit('changed', 1)
}
</script>
