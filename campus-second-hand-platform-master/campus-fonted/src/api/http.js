import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const API_BASE = import.meta.env.VITE_API_BASE || '/api/v1'

export function setToken(token) {
  if (token) localStorage.setItem('token', token)
  else localStorage.removeItem('token')
}

export function getToken() {
  return localStorage.getItem('token')
}

function clearSessionAndRedirect() {
  try {
    const userStore = useUserStore()
    userStore.logout()
  } catch {
    setToken(null)
    localStorage.removeItem('userInfo')
  }
  if (window.location.pathname !== '/login' && window.location.pathname !== '/admin/login') {
    window.location.href = '/login'
  }
}

/**
 * Unified fetch wrapper. Returns { code, message, data } style payloads.
 */
export async function api(url, options = {}) {
  const { method = 'GET', body, params, isFormData } = options
  const token = getToken()
  const headers = {}
  if (!isFormData) headers['Content-Type'] = 'application/json'
  if (token) headers['Authorization'] = `Bearer ${token}`

  let fullUrl = `${API_BASE}${url}`
  if (params) {
    const qs = new URLSearchParams()
    Object.entries(params).forEach(([k, v]) => {
      if (v !== undefined && v !== null && v !== '') qs.append(k, v)
    })
    const str = qs.toString()
    if (str) fullUrl += '?' + str
  }

  const config = { method: method.toUpperCase(), headers }
  if (body && method.toUpperCase() !== 'GET') {
    config.body = isFormData ? body : JSON.stringify(body)
  }

  try {
    const res = await fetch(fullUrl, config)
    // 登录/注册接口本身的 401 不应当成“会话过期”
    const isAuthEndpoint = url.startsWith('/auth/login') || url.startsWith('/auth/register') || url.startsWith('/admin/login')
    if (res.status === 401 && !isAuthEndpoint) {
      clearSessionAndRedirect()
      return { code: 401, message: '登录已过期，请重新登录', data: null }
    }
    const data = await res.json()
    return data
  } catch (err) {
    if (err.name === 'AbortError') return { code: 0, cancelled: true }
    const message = err.message || '网络错误'
    try {
      ElMessage.error(message)
    } catch {
      /* ElMessage may be unavailable during early bootstrap */
    }
    return { code: 500, message, data: null }
  }
}

export const http = {
  get: (url, params) => api(url, { method: 'GET', params }),
  post: (url, body) => api(url, { method: 'POST', body }),
  put: (url, body) => api(url, { method: 'PUT', body }),
  patch: (url, body) => api(url, { method: 'PATCH', body }),
  del: (url) => api(url, { method: 'DELETE' }),
  upload: (url, formData) => api(url, { method: 'POST', body: formData, isFormData: true }),
}
