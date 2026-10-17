<template>
  <aside v-if="items.length" class="article-toc">
    <p class="toc-title">本文目录</p>
    <ul class="toc-list">
      <li v-for="item in items" :key="item.id">
        <a
          :href="`#${item.id}`"
          class="toc-link"
          :class="[`level-${item.level}`, { active: activeId === item.id }]"
          @click="activeId = item.id"
        >
          {{ item.text }}
        </a>
      </li>
    </ul>
  </aside>
</template>

<script setup lang="ts">
import type { TocItem } from '~/types'

const props = defineProps<{ items: TocItem[] }>()
const activeId = ref('')
let ticking = false

function updateActive() {
  const headings = Array.from(document.querySelectorAll<HTMLElement>('.article-body h2[id], .article-body h3[id]'))
  if (!headings.length) return
  const threshold = 120
  let current = headings[0]?.id || ''
  for (const heading of headings) {
    if (heading.getBoundingClientRect().top <= threshold) {
      current = heading.id
    } else {
      break
    }
  }
  activeId.value = current
}

function onScroll() {
  if (ticking) return
  ticking = true
  requestAnimationFrame(() => {
    updateActive()
    ticking = false
  })
}

onMounted(() => {
  updateActive()
  window.addEventListener('scroll', onScroll, { passive: true })
})
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
watch(() => props.items, () => nextTick(updateActive), { deep: true })
</script>
