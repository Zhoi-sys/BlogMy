<template>
  <div class="auth-shell">
    <section class="auth-art">
      <div class="brand" style="color: var(--paper)">
        <span class="brand-mark" style="background: var(--paper); color: var(--ink)">墨</span>
        <span class="brand-name">墨迹</span>
      </div>
      <h1 class="auth-art-title">从一句话开始，<br><em>加入这场对话。</em></h1>
      <p class="auth-art-note">
        注册后可以点赞、评论、@ 回复，并在个人空间里查看自己的互动记录。
      </p>
    </section>
    <section class="auth-panel">
      <div class="auth-card">
        <span class="eyebrow">Create account</span>
        <h2 class="auth-title">创建账号</h2>
        <p class="auth-sub">只需要一个用户名和密码。</p>
        <form @submit.prevent="submit">
          <p v-if="errorMessage" class="form-error">{{ errorMessage }}</p>
          <div class="field">
            <label class="field-label" for="username">用户名</label>
            <input id="username" v-model="form.username" class="input" autocomplete="username" placeholder="3-32 位字母、数字或下划线" required>
            <p class="small muted" style="margin: 6px 0 0">用于登录与 @ 提及，注册后不可修改。</p>
          </div>
          <div class="field">
            <label class="field-label" for="password">密码</label>
            <input id="password" v-model="form.password" class="input" type="password" autocomplete="new-password" placeholder="至少 8 位，包含字母和数字" required>
          </div>
          <div class="field">
            <label class="field-label" for="confirm">确认密码</label>
            <input id="confirm" v-model="form.confirm" class="input" type="password" autocomplete="new-password" placeholder="再次输入密码" required>
          </div>
          <button class="btn btn-primary" type="submit" style="width: 100%; margin-top: 8px" :disabled="loading">
            {{ loading ? '创建中…' : '创建账号' }}
          </button>
        </form>
        <p class="auth-switch">已经有账号？<NuxtLink to="/login">直接登录</NuxtLink></p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '~/stores/auth'

definePageMeta({ middleware: [] })
useSeoMeta({ title: '注册', robots: 'noindex' })

const auth = useAuthStore()
const toast = useToast()
const form = reactive({ username: '', password: '', confirm: '' })
const loading = ref(false)
const errorMessage = ref('')

if (auth.isAuthenticated) {
  await navigateTo('/')
}

async function submit() {
  if (loading.value) return
  errorMessage.value = ''
  if (!/^[A-Za-z0-9_]{3,32}$/.test(form.username)) {
    errorMessage.value = '用户名需为 3-32 位字母、数字或下划线'
    return
  }
  if (form.password.length < 8 || !/[A-Za-z]/.test(form.password) || !/\d/.test(form.password)) {
    errorMessage.value = '密码需为 8 位以上，且同时包含字母和数字'
    return
  }
  if (form.password !== form.confirm) {
    errorMessage.value = '两次输入的密码不一致'
    return
  }
  loading.value = true
  try {
    await auth.register({ username: form.username, password: form.password })
    toast.success('账号创建成功')
    await navigateTo('/')
  } catch (error) {
    const err = error as { data?: { msg?: string }; message?: string }
    errorMessage.value = err.data?.msg || err.message || '注册失败，请稍后再试'
  } finally {
    loading.value = false
  }
}
</script>
