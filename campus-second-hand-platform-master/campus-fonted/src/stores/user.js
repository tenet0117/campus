import { defineStore } from 'pinia'

function safeParse(json) {
  try {
    return JSON.parse(json)
  } catch {
    return null
  }
}

function normalizeRole(role) {
  if (role == null || role === '') return null
  const r = String(role).toLowerCase().replace(/^role_/, '')
  if (r === 'admin' || r.includes('admin')) return 'admin'
  if (r === 'user' || r === 'member') return 'user'
  return r
}

function loadUserInfo() {
  const raw = localStorage.getItem('userInfo')
  if (!raw) return null
  const info = safeParse(raw)
  if (!info || typeof info !== 'object') return null
  if (info.role) info.role = normalizeRole(info.role)
  return info
}

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: loadUserInfo(),
  }),

  getters: {
    isLoggedIn: (s) => !!s.token,
    isAdmin: (s) => s.userInfo?.role === 'admin',
    userId: (s) => s.userInfo?.userId ?? s.userInfo?.id ?? null,
  },

  actions: {
    /** Persist session from backend login payload; role comes from response. */
    setSession(data = {}) {
      const token = data.token || ''
      const role = normalizeRole(data.role ?? data.userRole)
      const { token: _t, ...rest } = data
      this.token = token
      this.userInfo = { ...rest, ...(role ? { role } : {}) }
      if (token) localStorage.setItem('token', token)
      else localStorage.removeItem('token')
      try {
        localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
      } catch {
        /* ignore quota errors */
      }
    },

    login(data) {
      this.setSession(data)
    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
    },
  },
})
