<template>
  <footer class="site-footer">
    <div class="container footer-inner">
      <div>
        <div class="serif" style="font-size: 1rem; color: var(--ink-soft)">墨迹 · Inkwell</div>
        <div style="margin-top: 4px">写作、代码与长期主义。© {{ year }}</div>
      </div>
      <div class="footer-links">
        <NuxtLink to="/rss.xml" external>RSS</NuxtLink>
        <NuxtLink to="/sitemap.xml" external>Sitemap</NuxtLink>
        <NuxtLink v-if="auth.isAdmin" to="/write">开始写作</NuxtLink>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

const auth = useAuthStore()
const year = new Date().getFullYear()
</script>
