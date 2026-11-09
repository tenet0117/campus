import { http } from './http'

export function login(account, password) {
  return http.post('/auth/login', { account, password })
}

export function register(payload) {
  return http.post('/auth/register', payload)
}

export function updateProfile(body) {
  return http.put('/auth/profile', body)
}
