import { defineStore } from 'pinia'
import type { AuthTokens, UserView } from '~/types'

interface LoginPayload {
  username: string
  password: string
}

export const useAuthStore = defineStore('auth', () => {
  const config = useRuntimeConfig()
  const accessCookie = useCookie<string | null>('inkwell_at', {
    sameSite: 'lax',
    secure: !import.meta.dev,
    maxAge: 60 * 60 * 2,
    path: '/'
  })
  const refreshCookie = useCookie<string | null>('inkwell_rt', {
    sameSite: 'lax',
    secure: !import.meta.dev,
    maxAge: 60 * 60 * 24 * 7,
    path: '/'
  })
  const userCookie = useCookie<UserView | null>('inkwell_user', {
    sameSite: 'lax',
    secure: !import.meta.dev,
    maxAge: 60 * 60 * 24 * 7,
    path: '/'
  })

  const user = ref<UserView | null>(null)
  const accessToken = ref('')
  const refreshToken = ref('')
  const initialized = ref(false)
  let refreshPromise: Promise<boolean> | null = null

  const isAuthenticated = computed(() => Boolean(accessToken.value))
  const isAdmin = computed(() => user.value?.role === 9)
  const authHeader = computed(() => (accessToken.value ? { Authorization: `Bearer ${accessToken.value}` } : {}))

  function hydrate() {
    if (initialized.value) return
    accessToken.value = accessCookie.value || ''
    refreshToken.value = refreshCookie.value || ''
    user.value = userCookie.value || null
    initialized.value = true
  }

  function persist(tokens: AuthTokens) {
    accessToken.value = tokens.accessToken
    refreshToken.value = tokens.refreshToken
    user.value = tokens.user
    accessCookie.value = tokens.accessToken
    refreshCookie.value = tokens.refreshToken
    userCookie.value = tokens.user
  }

  function clear() {
    accessToken.value = ''
    refreshToken.value = ''
    user.value = null
    accessCookie.value = null
    refreshCookie.value = null
    userCookie.value = null
  }

  function baseURL() {
    return import.meta.server ? config.apiBase : config.public.apiBase
  }

  async function login(payload: LoginPayload) {
    const result = await $fetch<{ code: number; msg: string; data: AuthTokens }>('/auth/login', {
      baseURL: baseURL(),
      method: 'POST',
      body: payload
    })
    if (result.code !== 0) throw new Error(result.msg)
    persist(result.data)
    return result.data.user
  }

  async function register(payload: LoginPayload) {
    const result = await $fetch<{ code: number; msg: string; data: AuthTokens }>('/auth/register', {
      baseURL: baseURL(),
      method: 'POST',
      body: payload
    })
    if (result.code !== 0) throw new Error(result.msg)
    persist(result.data)
    return result.data.user
  }

  async function refresh(): Promise<boolean> {
    if (!refreshToken.value) return false
    if (refreshPromise) return refreshPromise
    refreshPromise = (async () => {
      try {
        const result = await $fetch<{ code: number; msg: string; data: AuthTokens }>('/auth/refresh', {
          baseURL: baseURL(),
          method: 'POST',
          body: { refreshToken: refreshToken.value }
        })
        if (result.code !== 0) throw new Error(result.msg)
        persist(result.data)
        return true
      } catch {
        clear()
        return false
      } finally {
        refreshPromise = null
      }
    })()
    return refreshPromise
  }

  async function fetchMe() {
    if (!accessToken.value) return null
    try {
      const result = await $fetch<{ code: number; msg: string; data: UserView }>('/auth/me', {
        baseURL: baseURL(),
        headers: { Authorization: `Bearer ${accessToken.value}` }
      })
      if (result.code === 0) {
        user.value = result.data
        userCookie.value = result.data
        return result.data
      }
    } catch {
      // 401 由 useApi 统一刷新；此处保持静默。
    }
    return null
  }

  async function logout() {
    try {
      if (accessToken.value) {
        await $fetch('/auth/logout', {
          baseURL: baseURL(),
          method: 'POST',
          headers: { Authorization: `Bearer ${accessToken.value}` },
          body: refreshToken.value ? { refreshToken: refreshToken.value } : undefined
        })
      }
    } catch {
      // 本地状态无论如何都要清理。
    } finally {
      clear()
    }
  }

  return {
    user,
    accessToken,
    refreshToken,
    initialized,
    isAuthenticated,
    isAdmin,
    authHeader,
    hydrate,
    login,
    register,
    refresh,
    fetchMe,
    logout,
    clear
  }
})
