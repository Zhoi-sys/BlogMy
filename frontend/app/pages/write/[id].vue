<template>
  <BlogEditor :article-id="articleId" />
</template>

<script setup lang="ts">
definePageMeta({ middleware: ['admin'] })
useSeoMeta({ title: '编辑文章', robots: 'noindex' })
const route = useRoute()
const articleId = Number(route.params.id)
</script>
