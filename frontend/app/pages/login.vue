<template>
  <div class="auth-shell">
    <section class="auth-art">
      <div class="brand" style="color: var(--paper)">
        <span class="brand-mark" style="background: var(--paper); color: var(--ink)">墨</span>
        <span class="brand-name">墨迹</span>
      </div>
      <h1 class="auth-art-title">欢迎回来，<br><em>继续写下去。</em></h1>
      <p class="auth-art-note">
        这里保存着长期思考的痕迹。登录后可以参与讨论、用 @ 找到同频的人，也可以进入自己的空间。
      </p>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <span class="eyebrow">Member access</span>
        <h2 class="auth-title">登录</h2>
        <p class="auth-sub">使用你的用户名与密码继续。</p>
        <form @submit.prevent="submit">
          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
          <div class="field">
            <label class="field-label" for="username">用户名</label>
            <input id="username" v-model="form.username" class="input" autocomplete="username" placeholder="your_name" required>
          </div>
          <div class="field">
            <label class="field-label" for="password">密码</label>
            <input id="password" v-model="form.password" class="input" type="password" autocomplete="current-password" placeholder="至少 8 位" required>
          </div>
          <button class="btn btn-primary" type="submit" style="width: 100%; margin-top: 8px" :disabled="loading">
            {{ loading ? '登录中…' : '登录' }}
          </button>
        </form>
        <p class="auth-switch">还没有账号？<NuxtLink to="/register">创建一个</NuxtLink></p>
        <div class="demo-hint">
          首次部署时，系统会根据服务端配置自动创建博主账号。普通读者可自助注册后参与评论与点赞。
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ middleware: [] })
useSeoMeta({ title: '登录', robots: 'noindex' })

const auth = useAuthStore()
const route = useRoute()
const toast = useToast()
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const errorMessage = ref('')

if (auth.isAuthenticated) {
  await navigateTo('/')
}

function safeRedirect() {
  const target = String(route.query.redirect || '/')
  return target.startsWith('/') && !target.startsWith('//') ? target : '/'
}

async function submit() {
  if (loading.value) return
  loading.value = true
  errorMessage.value = ''
  try {
    await auth.login({ username: form.username, password: form.password })
    toast.success('登录成功')
    await navigateTo(safeRedirect())
  } catch (error) {
    const err = error as { data?: { msg?: string }; message?: string }
    errorMessage.value = err.data?.msg || err.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}
</script>
