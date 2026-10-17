<template>
  <BlogEditor />
</template>

<script setup lang="ts">
definePageMeta({ middleware: ['admin'] })
useSeoMeta({ title: '写文章', robots: 'noindex' })
</script>
