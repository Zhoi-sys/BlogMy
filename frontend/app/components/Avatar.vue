<template>
  <span class="avatar" :class="sizeClass" :style="gradientStyle" :title="displayName">
    <img v-if="user?.avatarUrl" :src="user.avatarUrl" :alt="displayName" loading="lazy">
    <span v-else>{{ initials(displayName) }}</span>
  </span>
</template>

<script setup lang="ts">
import type { UserSummary } from '~/types'

const props = withDefaults(defineProps<{
  user?: UserSummary | null
  name?: string
  size?: 'sm' | 'md' | 'lg' | 'xl'
}>(), {
  user: null,
  name: '',
  size: 'md'
})

const displayName = computed(() => props.name || props.user?.nickname || props.user?.username || '访客')
const sizeClass = computed(() => props.size === 'md' ? '' : `avatar-${props.size}`)
const gradientStyle = computed(() => {
  const seed = Array.from(displayName.value).reduce((acc, char) => acc + char.charCodeAt(0), 0)
  const hue = (seed * 37) % 360
  return {
    background: `linear-gradient(135deg, hsl(${hue} 34% 33%), hsl(${(hue + 38) % 360} 42% 48%))`
  }
})
</script>
