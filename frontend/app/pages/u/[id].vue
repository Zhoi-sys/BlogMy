<template>
  <div v-if="profile" class="profile-page">
    <div class="container">
      <header class="profile-hero">
        <div class="profile-main">
          <Avatar :user="profile.user" size="xl" />
          <div>
            <span class="eyebrow">Personal space</span>
            <h1 class="profile-name">{{ profile.user.nickname }}</h1>
            <div class="profile-handle">@{{ profile.user.username }} · 加入于 {{ formatDate(profile.user.createdAt) }}</div>
            <p class="profile-bio">{{ profile.user.bio || '这个空间的主人还没有写下简介。' }}</p>
          </div>
        </div>
        <div style="display: flex; flex-direction: column; gap: 14px; align-items: flex-end">
          <div v-if="isOwner" class="editor-actions">
            <button class="btn btn-ghost btn-sm" type="button" @click="beginEdit">编辑资料</button>
            <NuxtLink v-if="auth.isAdmin" to="/write" class="btn btn-primary btn-sm">写新文章</NuxtLink>
          </div>
          <div class="profile-stats">
            <div class="stat">
              <div class="stat-value">{{ profile.stats.articleCount }}</div>
              <div class="stat-label">文章</div>
            </div>
            <div class="stat">
              <div class="stat-value">{{ formatNumber(profile.stats.likeCount) }}</div>
              <div class="stat-label">获赞</div>
            </div>
            <div class="stat">
              <div class="stat-value">{{ formatNumber(profile.stats.viewCount) }}</div>
              <div class="stat-label">阅读</div>
            </div>
          </div>
        </div>
      </header>

      <form v-if="editing" class="editor-panel" style="margin-top: 24px; max-width: 620px" @submit.prevent="saveProfile">
        <div class="field">
          <label class="field-label" for="nickname">昵称</label>
          <input id="nickname" v-model="editForm.nickname" class="input" maxlength="32" required>
        </div>
        <div class="field">
          <label class="field-label" for="bio">简介</label>
          <textarea id="bio" v-model="editForm.bio" class="textarea" maxlength="255" placeholder="介绍一下你自己…" />
        </div>
        <div class="editor-actions">
          <button class="btn btn-primary btn-sm" type="submit" :disabled="saving">{{ saving ? '保存中…' : '保存资料' }}</button>
          <button class="btn btn-ghost btn-sm" type="button" @click="editing = false">取消</button>
        </div>
      </form>

      <div class="tabs">
        <button class="tab" :class="{ active: status === 1 }" type="button" @click="switchTab(1)">文章</button>
        <template v-if="isOwner">
          <button class="tab" :class="{ active: status === 0 }" type="button" @click="switchTab(0)">草稿</button>
          <button class="tab" :class="{ active: status === 2 }" type="button" @click="switchTab(2)">已下架</button>
        </template>
        <NuxtLink v-if="isOwner && auth.isAdmin" to="/manage" class="tab" style="margin-left: auto; text-decoration: none">
          管理全部
        </NuxtLink>
      </div>

      <div v-if="pending" class="article-list">
        <div v-for="i in 3" :key="i" class="article-card">
          <div class="skeleton" style="height: 18px; width: 62%; margin-bottom: 12px" />
          <div class="skeleton" style="height: 13px; width: 88%" />
        </div>
      </div>
      <div v-else-if="articles.length" class="article-list">
        <ArticleCard v-for="(article, index) in articles" :key="article.id" :article="article" :index="index" />
      </div>
      <EmptyState
        v-else
        :title="status === 1 ? '还没有公开文章' : status === 0 ? '草稿箱是空的' : '没有已下架的文章'"
        :description="isOwner ? '从写下一个标题开始。' : '作者还在酝酿新的内容。'"
      />
      <div v-if="hasMore" class="load-more">
        <button class="btn btn-ghost" type="button" :disabled="pending" @click="loadMore">
          {{ pending ? '加载中…' : '加载更多' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'
import type { ArticleCard, PageResult, UserProfile, UserView } from '~/types'

const route = useRoute()
const api = useApi()
const auth = useAuthStore()
const toast = useToast()
const id = Number(route.params.id)
const status = ref(1)
const articles = ref<ArticleCard[]>([])
const cursor = ref<string | null>(null)
const hasMore = ref(false)
const pending = ref(false)
const editing = ref(false)
const saving = ref(false)
const editForm = reactive({ nickname: '', bio: '' })

const { data: fetchedProfile, error } = await useAsyncData<UserProfile>(
  `profile-${id}`,
  () => api.get<UserProfile>(`/users/${id}`)
)
if (error.value || !fetchedProfile.value) {
  throw createError({ statusCode: 404, statusMessage: '用户不存在' })
}

// 与文章页一致：为资料编辑提供深响应式副本。
const profile = ref<UserProfile | null>(fetchedProfile.value)

const isOwner = computed(() => auth.user?.id === profile.value?.user.id)
const pageTitle = computed(() => `${profile.value?.user.nickname} 的个人空间`)

const initial = await api.get<PageResult<ArticleCard>>(`/users/${id}/articles`, { status: 1, size: 10 }).catch(() => null)
if (initial) applyPage(initial)

function applyPage(page: PageResult<ArticleCard>, append = false) {
  articles.value = append ? [...articles.value, ...page.list] : page.list
  cursor.value = page.nextCursor
  hasMore.value = page.hasMore
}

async function load(statusValue = status.value, append = false) {
  pending.value = true
  try {
    const page = await api.get<PageResult<ArticleCard>>(`/users/${id}/articles`, {
      status: statusValue,
      cursor: append ? cursor.value || undefined : undefined,
      size: 10
    })
    applyPage(page, append)
  } catch (err) {
    toast.error(err instanceof Error ? err.message : '文章加载失败')
  } finally {
    pending.value = false
  }
}

function switchTab(next: number) {
  if (status.value === next) return
  status.value = next
  cursor.value = null
  articles.value = []
  load(next)
}

function loadMore() {
  load(status.value, true)
}

function beginEdit() {
  if (!auth.user) return
  editForm.nickname = auth.user.nickname
  editForm.bio = auth.user.bio || ''
  editing.value = true
}

async function saveProfile() {
  saving.value = true
  try {
    const user = await api.put<UserView>('/users/me', { nickname: editForm.nickname, bio: editForm.bio })
    if (profile.value) profile.value.user = user
    if (auth.user) {
      auth.user.nickname = user.nickname
      auth.user.bio = user.bio
    }
    editing.value = false
    toast.success('资料已更新')
  } catch (err) {
    toast.error(err instanceof Error ? err.message : '保存失败')
  } finally {
    saving.value = false
  }
}

watch(isOwner, (value) => {
  if (value && !editing.value) beginEditSilent()
}, { immediate: true })

function beginEditSilent() {
  if (!auth.user) return
  editForm.nickname = auth.user.nickname
  editForm.bio = auth.user.bio || ''
}

useSeoMeta({
  title: () => pageTitle.value,
  description: () => profile.value?.user.bio || `${profile.value?.user.nickname} 的个人空间`,
  ogTitle: () => pageTitle.value,
  ogDescription: () => profile.value?.user.bio || ''
})
</script>
