<template>
  <div class="editor-page">
    <div class="container">
      <div class="editor-top">
        <NuxtLink to="/manage" class="back-link" style="margin: 0"><ArrowLeft :size="14" /> 返回管理</NuxtLink>
        <div class="editor-actions">
          <span v-if="loaded" class="status-pill" :class="statusClass">{{ statusLabel(currentStatus) }}</span>
          <button class="btn btn-ghost btn-sm" type="button" @click="saveDraft" :disabled="saving">
            {{ saving ? '保存中…' : isPublished ? '保存修改' : '保存草稿' }}
          </button>
          <button class="btn btn-primary btn-sm" type="button" @click="publish" :disabled="saving">
            {{ isPublished ? '更新并查看' : '发布文章' }}
          </button>
        </div>
      </div>

      <div v-if="!loaded" class="editor-loading">正在打开文稿…</div>

      <template v-else>
        <input v-model="form.title" class="editor-title-input" maxlength="80" placeholder="给这篇文章一个标题…">
        <div class="editor-meta">
          <div>
            <textarea
              v-model="form.summary"
              class="textarea"
              maxlength="255"
              placeholder="写一句摘要，留白时自动截取正文前 140 字。"
              style="min-height: 92px"
            />
            <div class="editor-info" style="display: flex; flex-wrap: wrap; gap: 14px; margin-top: 8px">
              <span>{{ form.contentMd.length }} 字符</span>
              <span>{{ form.tags.length }}/5 个标签</span>
              <span>Markdown · 表格 · Mermaid · 图片外链</span>
            </div>
          </div>
          <div class="editor-panel">
            <label class="field-label" for="tag-input">标签</label>
            <input
              id="tag-input"
              v-model="tagInput"
              class="input"
              maxlength="32"
              placeholder="输入后回车添加"
              @keydown="onTagKeydown"
            >
            <div class="tag-editor">
              <span v-for="tag in form.tags" :key="tag" class="tag-chip">
                {{ tag }}
                <button type="button" aria-label="移除标签" @click="removeTag(tag)">×</button>
              </span>
            </div>
            <p class="editor-hint">发布前至少需要 1 个标签。图片请使用图床外链。</p>
          </div>
        </div>

        <div class="editor-actions" style="margin-bottom: 12px">
          <span class="small muted">编辑模式</span>
          <button class="btn btn-ghost btn-sm" type="button" @click="setMode('ir')">所见即所得</button>
          <button class="btn btn-ghost btn-sm" type="button" @click="setMode('sv')">分屏预览</button>
          <span style="flex: 1" />
          <button v-if="isPublished" class="btn btn-danger btn-sm" type="button" @click="takeOffline">下架</button>
          <button v-if="articleId" class="btn btn-danger btn-sm" type="button" @click="removeArticle">删除</button>
        </div>

        <div class="quick-insert">
          <button type="button" @click="insertTemplate('mermaid')">插入 Mermaid</button>
          <button type="button" @click="insertTemplate('table')">插入表格</button>
          <button type="button" @click="insertTemplate('code')">插入代码块</button>
          <button type="button" @click="insertTemplate('image')">插入图片链接</button>
        </div>

        <div class="editor-shell">
          <ClientOnly>
            <MarkdownEditor
              ref="editorRef"
              v-model="form.contentMd"
              :theme="theme"
            />
            <template #fallback>
              <div class="editor-loading">编辑器加载中…</div>
            </template>
          </ClientOnly>
        </div>
        <p class="editor-hint">
          支持 GFM 表格、代码高亮、Mermaid、KaTeX 公式与图片 Lightbox。发布前请确认外链图片可公开访问。
        </p>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ArrowLeft } from 'lucide-vue-next'
import type { ArticleDetail } from '~/types'

const props = defineProps<{ articleId?: number }>()
const api = useApi()
const toast = useToast()
const { theme } = useTheme()
const router = useRouter()
const editorRef = ref<{ setMode: (mode: 'ir' | 'sv') => void; insertValue: (value: string) => void } | null>(null)

const loaded = ref(false)
const saving = ref(false)
const tagInput = ref('')
const currentStatus = ref(0)
const loadedArticle = ref<ArticleDetail | null>(null)
const form = reactive({ title: '', summary: '', contentMd: '', tags: [] as string[] })
let savedSnapshot = ''

const isPublished = computed(() => currentStatus.value === 1)
const statusClass = computed(() => currentStatus.value === 0 ? 'draft' : currentStatus.value === 2 ? 'offline' : 'published')

if (props.articleId) {
  const { data, error } = await useAsyncData<ArticleDetail>(`editor-${props.articleId}`, () =>
    api.get<ArticleDetail>(`/articles/${props.articleId}`))
  if (error.value || !data.value) {
    throw createError({ statusCode: 404, statusMessage: '文章不存在' })
  }
  loadedArticle.value = data.value
  form.title = data.value.title
  form.summary = data.value.summary || ''
  form.contentMd = data.value.contentMd || ''
  form.tags = [...data.value.tags]
  currentStatus.value = data.value.status
}

loaded.value = true
savedSnapshot = snapshot()

function snapshot() {
  return JSON.stringify({ title: form.title, summary: form.summary, content: form.contentMd, tags: form.tags })
}

function isDirty() {
  return loaded.value && snapshot() !== savedSnapshot
}

function onTagKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' || event.key === ',') {
    event.preventDefault()
    addTag()
  }
}

function addTag() {
  const tag = tagInput.value.replace(/^#+/, '').trim()
  if (!tag) return
  if (form.tags.length >= 5) {
    toast.error('最多添加 5 个标签')
    return
  }
  if (!form.tags.some(item => item.toLowerCase() === tag.toLowerCase())) {
    form.tags.push(tag)
  }
  tagInput.value = ''
}

function removeTag(tag: string) {
  form.tags = form.tags.filter(item => item !== tag)
}

function setMode(mode: 'ir' | 'sv') {
  editorRef.value?.setMode(mode)
}

function insertTemplate(type: 'mermaid' | 'table' | 'code' | 'image') {
  const templates = {
    mermaid: '\n```mermaid\nflowchart LR\n    A[开始] --> B[结束]\n```\n',
    table: '\n| 列一 | 列二 |\n| --- | --- |\n| 内容 | 内容 |\n',
    code: '\n```java\nSystem.out.println("Hello, world");\n```\n',
    image: '\n![图片说明](https://example.com/image.jpg)\n'
  }
  editorRef.value?.insertValue(templates[type])
}

function validateForPublish() {
  if (!form.title.trim()) {
    toast.error('请先填写文章标题')
    return false
  }
  if (!form.contentMd.trim()) {
    toast.error('发布前请填写文章正文')
    return false
  }
  if (!form.tags.length) {
    toast.error('发布前请至少添加 1 个标签')
    return false
  }
  return true
}

async function createOrUpdate(publishNow: boolean) {
  const payload = {
    title: form.title,
    summary: form.summary,
    contentMd: form.contentMd,
    tags: form.tags,
    status: props.articleId ? undefined : (publishNow ? 1 : 0)
  }
  if (!props.articleId) {
    const ref = await api.post<{ id: number; status: number }>('/articles', payload)
    return ref.id
  }
  await api.put(`/articles/${props.articleId}`, payload)
  if (publishNow && currentStatus.value !== 1) {
    await api.post(`/articles/${props.articleId}/status`, { action: 'publish' })
  }
  return props.articleId
}

async function saveDraft() {
  if (saving.value) return
  if (!form.title.trim()) {
    toast.error('请先填写文章标题')
    return
  }
  saving.value = true
  try {
    const id = await createOrUpdate(false)
    savedSnapshot = snapshot()
    toast.success(isPublished.value ? '修改已保存' : '草稿已保存')
    if (!props.articleId) {
      await navigateTo(`/write/${id}`, { replace: true })
    }
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function publish() {
  if (saving.value || !validateForPublish()) return
  saving.value = true
  try {
    const id = await createOrUpdate(true)
    savedSnapshot = snapshot()
    toast.success('文章已发布')
    await navigateTo(`/articles/${id}`)
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '发布失败')
  } finally {
    saving.value = false
  }
}

async function takeOffline() {
  if (!props.articleId || !window.confirm('下架后读者将无法访问这篇文章，确定继续吗？')) return
  try {
    await api.post(`/articles/${props.articleId}/status`, { action: 'offline' })
    currentStatus.value = 2
    toast.success('文章已下架')
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '操作失败')
  }
}

async function removeArticle() {
  if (!props.articleId || !window.confirm('确定删除这篇文章吗？此操作不会出现在公开页面。')) return
  try {
    await api.post(`/articles/${props.articleId}/status`, { action: 'delete' })
    toast.success('文章已删除')
    await navigateTo('/manage')
  } catch (error) {
    toast.error(error instanceof Error ? error.message : '删除失败')
  }
}

function beforeUnload(event: BeforeUnloadEvent) {
  if (!isDirty()) return
  event.preventDefault()
  event.returnValue = ''
}

onMounted(() => window.addEventListener('beforeunload', beforeUnload))
onBeforeUnmount(() => window.removeEventListener('beforeunload', beforeUnload))

onBeforeRouteLeave(() => {
  if (!isDirty()) return true
  return window.confirm('还有未保存的修改，确定离开吗？')
})
</script>
