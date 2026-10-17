import { useAuthStore } from '~/stores/auth'
import type { ApiResult } from '~/types'

export class ApiError extends Error {
  code: number
  status: number

  constructor(message: string, code = 50000, status = 500) {
    super(message)
    this.name = 'ApiError'
    this.code = code
    this.status = status
  }
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: Record<string, unknown> | string | null
  query?: Record<string, unknown>
  headers?: Record<string, string>
}

export function useApi() {
  const config = useRuntimeConfig()
  const auth = useAuthStore()

  function baseURL() {
    return import.meta.server ? config.apiBase : config.public.apiBase
  }

  function toApiError(error: unknown): ApiError {
    const err = error as {
      status?: number
      statusCode?: number
      data?: Partial<ApiResult<unknown>>
      message?: string
    }
    return new ApiError(
      err?.data?.msg || err?.message || '请求失败，请稍后再试',
      err?.data?.code || 50000,
      err?.status || err?.statusCode || 500
    )
  }

  async function request<T>(path: string, options: RequestOptions = {}, allowRetry = true): Promise<T> {
    const headers: Record<string, string> = { ...(options.headers || {}) }
    if (auth.accessToken) headers.Authorization = `Bearer ${auth.accessToken}`
    try {
      const result = await $fetch<ApiResult<T>>(path, {
        baseURL: baseURL(),
        method: options.method || 'GET',
        body: options.body,
        query: options.query,
        headers
      })
      if (result.code !== 0) throw new ApiError(result.msg, result.code, 200)
      return result.data
    } catch (error) {
      const normalized = toApiError(error)
      if (normalized.status === 401 && allowRetry && auth.refreshToken && !path.startsWith('/auth/')) {
        const refreshed = await auth.refresh()
        if (refreshed) return request<T>(path, options, false)
      }
      throw normalized
    }
  }

  return {
    get: <T>(path: string, query?: Record<string, unknown>) => request<T>(path, { query }),
    post: <T>(path: string, body?: Record<string, unknown>) => request<T>(path, { method: 'POST', body }),
    put: <T>(path: string, body?: Record<string, unknown>) => request<T>(path, { method: 'PUT', body }),
    del: <T>(path: string, body?: Record<string, unknown>) => request<T>(path, { method: 'DELETE', body }),
    request
  }
}
