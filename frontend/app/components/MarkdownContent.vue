<template>
  <div>
    <div ref="root" class="article-body" v-html="html" @click="handleRootClick" />
    <Teleport to="body">
      <div v-if="lightboxSrc" class="lightbox" @click="lightboxSrc = ''">
        <img :src="lightboxSrc" alt="文章图片预览">
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import type { MermaidConfig } from 'mermaid'

const props = defineProps<{ html: string }>()
const root = ref<HTMLElement | null>(null)
const lightboxSrc = ref('')
const { theme } = useTheme()
let mermaidReady = false
let mermaidInstance: typeof import('mermaid').default | null = null
let enhancementVersion = 0

async function enhance() {
  const container = root.value
  if (!container) return
  const version = ++enhancementVersion

  await highlightCode(container)
  if (version !== enhancementVersion) return
  addCopyButtons(container)
  await renderMermaid(container)
  if (version !== enhancementVersion) return
  await renderMath(container)
}

async function highlightCode(container: HTMLElement) {
  const blocks = Array.from(container.querySelectorAll<HTMLElement>('pre code'))
    .filter(code => !code.classList.contains('language-mermaid') && !code.dataset.highlighted)
  if (!blocks.length) return
  try {
    const { default: hljs } = await import('highlight.js')
    for (const block of blocks) {
      try {
        hljs.highlightElement(block)
        block.dataset.highlighted = 'yes'
      } catch {
        // 未知语言保持纯文本。
      }
    }
  } catch {
    // 高亮库加载失败不影响正文阅读。
  }
}

function addCopyButtons(container: HTMLElement) {
  container.querySelectorAll('pre.code-block').forEach((pre) => {
    if (pre.querySelector('.code-copy') || pre.classList.contains('mermaid-source')) return
    const button = document.createElement('button')
    button.type = 'button'
    button.className = 'code-copy'
    button.textContent = '复制'
    button.addEventListener('click', async (event) => {
      event.stopPropagation()
      const code = pre.querySelector('code')?.textContent || ''
      try {
        await navigator.clipboard.writeText(code)
        button.textContent = '已复制'
        window.setTimeout(() => { button.textContent = '复制' }, 1500)
      } catch {
        button.textContent = '复制失败'
      }
    })
    pre.appendChild(button)
  })
}

async function renderMermaid(container: HTMLElement) {
  const blocks = Array.from(container.querySelectorAll<HTMLElement>('pre > code.language-mermaid'))
  if (!blocks.length) return
  try {
    if (!mermaidInstance) {
      const module = await import('mermaid')
      mermaidInstance = module.default
    }
    if (!mermaidReady && mermaidInstance) {
      const config: MermaidConfig = {
        startOnLoad: false,
        securityLevel: 'strict',
        theme: theme.value === 'dark' ? 'dark' : 'neutral',
        fontFamily: 'Inter, "PingFang SC", sans-serif'
      }
      mermaidInstance.initialize(config)
      mermaidReady = true
    }
    let index = 0
    for (const code of blocks) {
      const pre = code.parentElement
      if (!pre || pre.closest('.mermaid-block')) continue
      const source = code.textContent || ''
      const wrapper = document.createElement('div')
      wrapper.className = 'mermaid-block'
      const stage = document.createElement('div')
      stage.className = 'mermaid-stage'
      stage.textContent = '图表渲染中…'
      const sourcePre = pre.cloneNode(true) as HTMLElement
      sourcePre.classList.add('mermaid-source')
      sourcePre.style.display = 'none'
      wrapper.append(stage, sourcePre)
      pre.replaceWith(wrapper)

      try {
        const { svg } = await mermaidInstance.render(`inkwell-mermaid-${Date.now()}-${index++}`, source)
        stage.innerHTML = svg
        sourcePre.style.display = 'none'
      } catch {
        stage.remove()
        sourcePre.style.display = 'block'
        const error = document.createElement('div')
        error.className = 'mermaid-error'
        error.textContent = '图表渲染失败，已保留 Mermaid 源码。请检查语法或稍后重试。'
        wrapper.appendChild(error)
      }
    }
  } catch {
    // Mermaid 加载失败时保留源码块。
  }
}

async function renderMath(container: HTMLElement) {
  if (!container.textContent?.includes('$')) return
  try {
    const renderMathInElement = (await import('katex/contrib/auto-render')).default
    renderMathInElement(container, {
      delimiters: [
        { left: '$$', right: '$$', display: true },
        { left: '$', right: '$', display: false },
        { left: '\\(', right: '\\)', display: false },
        { left: '\\[', right: '\\]', display: true }
      ],
      throwOnError: false,
      ignoredTags: ['script', 'noscript', 'style', 'textarea', 'pre', 'code']
    })
  } catch {
    // 公式库不可用时保留原始 LaTeX。
  }
}

function handleRootClick(event: MouseEvent) {
  const target = event.target as HTMLElement
  if (target.tagName === 'IMG') {
    lightboxSrc.value = (target as HTMLImageElement).src
  }
}

onMounted(async () => {
  await nextTick()
  await enhance()
})

watch(() => props.html, async () => {
  await nextTick()
  await enhance()
})
</script>
