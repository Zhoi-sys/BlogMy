<template>
  <div class="empty">
    <div class="empty-icon"><FileText :size="24" /></div>
    <p style="margin: 0 0 6px; color: var(--ink-soft); font-family: var(--serif); font-size: 1.2rem">
      {{ title }}
    </p>
    <p style="margin: 0; font-size: 0.82rem">{{ description }}</p>
    <slot />
  </div>
</template>

<script setup lang="ts">
import { FileText } from 'lucide-vue-next'

withDefaults(defineProps<{ title?: string; description?: string }>(), {
  title: '这里还很安静',
  description: '新的内容正在路上。'
})
</script>
