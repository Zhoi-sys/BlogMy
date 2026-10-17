<template>
  <div class="comment-composer">
    <div v-if="showSuggestions && suggestions.length" class="mention-popover">
      <button
        v-for="user in suggestions"
        :key="user.id"
        type="button"
        class="mention-option"
        @mousedown.prevent="chooseMention(user)"
      >
        <Avatar :user="user" size="sm" />
        <span>
          <span class="mention-option-name">{{ user.nickname }}</span>
          <span class="mention-option-id"> @{{ user.username }}</span>
        </span>
      </button>
    </div>
    <textarea
      ref="inputRef"
      v-model="content"
      class="composer-textarea"
      :placeholder="placeholder"
      maxlength="1000"
      @input="detectMention"
      @keydown.ctrl.enter="submit"
      @blur="hideSuggestionsSoon"
    />
    <div class="composer-foot">
      <span class="composer-hint">
        {{ content.length }}/1000 · 输入 @ 可提及他人 · Ctrl/⌘ + Enter 发送
      </span>
      <div class="composer-tools">
        <button v-if="replyTo" type="button" class="btn btn-ghost btn-sm" @click="$emit('cancel')">取消</button>
        <button type="button" class="btn btn-accent btn-sm" :disabled="submitting || !content.trim()" @click="submit">
          {{ submitting ? '发送中…' : replyTo ? '回复' : '发表评论' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { CommentView, UserSummary } from '~/types'

const props = withDefaults(defineProps<{
  articleId: number
  replyTo?: CommentView | null
  autofocus?: boolean
}>(), {
  replyTo: null,
  autofocus: false
})

const emit = defineEmits<{
  created: [comment: CommentView]
  cancel: []
}>()

const api = useApi()
const toast = useToast()
const content = ref('')
const submitting = ref(false)
const suggestions = ref<UserSummary[]>([])
const selectedMentions = ref<UserSummary[]>([])
const showSuggestions = ref(false)
const inputRef = ref<HTMLTextAreaElement | null>(null)
let mentionQuery = ''
let searchTimer: ReturnType<typeof setTimeout> | null = null
let blurTimer: ReturnType<typeof setTimeout> | null = null

const placeholder = computed(() => props.replyTo
  ? `回复 @${props.replyTo.author?.nickname || '这位朋友'}…`
  : '写下你的想法，理性讨论，友善交流…')

onMounted(() => {
  if (props.replyTo?.author) {
    const user = props.replyTo.author
    content.value = `@${user.nickname} `
    selectedMentions.value = [user]
    nextTick(() => inputRef.value?.focus())
  } else if (props.autofocus) {
    inputRef.value?.focus()
  }
})

function currentMentionToken() {
  const el = inputRef.value
  if (!el) return null
  const before = content.value.slice(0, el.selectionStart)
  const match = before.match(/@([^\s@]{0,20})$/)
  if (!match) return null
  return { query: match[1] || '', start: before.length - match[0].length, end: el.selectionStart }
}

function detectMention() {
  const token = currentMentionToken()
  if (!token) {
    showSuggestions.value = false
    return
  }
  mentionQuery = token.query
  if (searchTimer) clearTimeout(searchTimer)
  if (!token.query.trim()) {
    showSuggestions.value = false
    return
  }
  searchTimer = setTimeout(() => searchMention(token.query), 220)
}

async function searchMention(query: string) {
  if (!query.trim() || query !== mentionQuery) return
  try {
    const users = await api.get<UserSummary[]>('/users/search', { keyword: query })
    if (query !== mentionQuery) return
    suggestions.value = users.filter(user => !selectedMentions.value.some(item => item.id === user.id))
    showSuggestions.value = suggestions.value.length > 0
  } catch {
    showSuggestions.value = false
  }
}

function chooseMention(user: UserSummary) {
  const token = currentMentionToken()
  if (!token) return
  const text = content.value
  const prefix = text.slice(0, token.start) + `@${user.nickname} `
  const next = prefix + text.slice(token.end)
  content.value = next
  if (!selectedMentions.value.some(item => item.id === user.id)) {
    selectedMentions.value = [...selectedMentions.value, user]
  }
  showSuggestions.value = false
  nextTick(() => {
    const el = inputRef.value
    if (!el) return
    el.focus()
    el.setSelectionRange(prefix.length, prefix.length)
  })
}

function hideSuggestionsSoon() {
  blurTimer = setTimeout(() => { showSuggestions.value = false }, 120)
}

async function submit() {
  if (submitting.value) return
  const value = content.value.trim()
  if (!value) {
    toast.error('评论内容不能为空')
    return
  }
  if (blurTimer) clearTimeout(blurTimer)
  showSuggestions.value = false
  submitting.value = true
  try {
    const comment = await api.post<CommentView>('/comments', {
      articleId: props.articleId,
      content: value,
      parentId: props.replyTo?.id || null,
      mentionUserIds: selectedMentions.value.map(user => user.id)
    })
    emit('created', comment)
    content.value = ''
    selectedMentions.value = []
    toast.success(props.replyTo ? '回复已发送' : '评论已发布')
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '发送失败')
  } finally {
    submitting.value = false
  }
}
</script>
