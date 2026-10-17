import { setActivePinia } from 'pinia'
import { useAuthStore } from '~/stores/auth'

/**
 * 在 SSR 与客户端都从 Cookie 恢复登录态。
 * 显式绑定 Pinia 实例，避免插件执行顺序导致的 active pinia 丢失。
 */
export default defineNuxtPlugin(async (nuxtApp) => {
  const pinia = (nuxtApp as unknown as { $pinia?: Parameters<typeof setActivePinia>[0] }).$pinia
  if (pinia) setActivePinia(pinia)
  const auth = useAuthStore(pinia)
  auth.hydrate()
  if (auth.accessToken && !auth.user) {
    await auth.fetchMe()
  }
})
