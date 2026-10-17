<template>
  <header class="site-header">
    <div class="container header-inner">
      <NuxtLink to="/" class="brand" aria-label="返回首页">
        <span class="brand-mark">墨</span>
        <span>
          <span class="brand-name">{{ site?.title || '墨迹' }}</span>
          <span class="brand-sub">Personal notes</span>
        </span>
      </NuxtLink>

      <nav class="nav" :class="{ open: navOpen }">
        <NuxtLink to="/" class="nav-link" @click="navOpen = false">首页</NuxtLink>
        <NuxtLink v-if="site" :to="`/u/${site.owner.id}`" class="nav-link" @click="navOpen = false">关于</NuxtLink>
        <NuxtLink v-if="auth.isAdmin" to="/manage" class="nav-link" @click="navOpen = false">文章管理</NuxtLink>
        <NuxtLink v-if="auth.isAuthenticated" to="/notifications" class="nav-link mobile-only" @click="navOpen = false">
          通知
        </NuxtLink>
        <NuxtLink v-if="!auth.isAuthenticated" to="/login" class="nav-link mobile-only" @click="navOpen = false">
          登录
        </NuxtLink>
      </nav>

      <div class="header-actions">
        <button class="icon-btn desktop-only" type="button" aria-label="切换主题" @click="toggleTheme">
          <Sun v-if="theme === 'dark'" :size="17" />
          <Moon v-else :size="17" />
        </button>

        <template v-if="auth.isAuthenticated">
          <NuxtLink v-if="auth.isAdmin" to="/write" class="btn btn-primary btn-sm desktop-only">
            <PenLine :size="14" />
            写文章
          </NuxtLink>
          <NuxtLink
            to="/notifications"
            class="icon-btn desktop-only"
            :class="{ 'has-dot': unreadCount > 0 }"
            aria-label="通知"
          >
            <Bell :size="17" />
          </NuxtLink>
          <NuxtLink :to="`/u/${auth.user?.id}`" class="desktop-only" aria-label="个人空间">
            <Avatar :user="auth.user" />
          </NuxtLink>
          <button class="icon-btn desktop-only" type="button" aria-label="退出登录" @click="handleLogout">
            <LogOut :size="16" />
          </button>
        </template>
        <template v-else>
          <NuxtLink to="/login" class="btn btn-ghost btn-sm desktop-only">登录</NuxtLink>
          <NuxtLink to="/register" class="btn btn-primary btn-sm desktop-only">加入</NuxtLink>
        </template>

        <button class="icon-btn mobile-toggle" type="button" aria-label="菜单" @click="navOpen = !navOpen">
          <X v-if="navOpen" :size="18" />
          <Menu v-else :size="18" />
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { Bell, LogOut, Menu, Moon, PenLine, Sun, X } from 'lucide-vue-next'
import { useAuthStore } from '~/stores/auth'
import type { SiteView } from '~/types'

const auth = useAuthStore()
const api = useApi()
const route = useRoute()
const toast = useToast()
const { theme, toggle } = useTheme()
const navOpen = ref(false)
const unreadCount = ref(0)

const { data: site } = await useAsyncData<SiteView>('site-info', () => api.get<SiteView>('/site'), {
  server: true,
  lazy: true
})

function toggleTheme() {
  toggle()
}

async function loadUnread() {
  if (!auth.isAuthenticated) {
    unreadCount.value = 0
    return
  }
  try {
    const result = await api.get<{ count: number }>('/notifications/unread-count')
    unreadCount.value = result.count
  } catch {
    unreadCount.value = 0
  }
}

async function handleLogout() {
  await auth.logout()
  unreadCount.value = 0
  toast.success('已退出登录')
  await navigateTo('/')
}

watch(() => route.fullPath, () => { navOpen.value = false })
watch(() => auth.isAuthenticated, loadUnread, { immediate: true })
onMounted(loadUnread)
</script>
