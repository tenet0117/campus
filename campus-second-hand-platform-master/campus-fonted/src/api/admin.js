import { http } from './http'

export function adminLogin(body) {
  return http.post('/admin/login', body)
}

export function adminStatistics() {
  return http.get('/admin/statistics')
}

export function adminTradeStatistics(days = 30) {
  return http.get('/admin/statistics/trade', { days })
}

export function adminCategoryStatistics() {
  return http.get('/admin/statistics/category')
}

export function adminUsers(params) {
  return http.get('/admin/users', params)
}

export function adminUserStatus(id, status) {
  return http.patch(`/admin/users/${id}/status?status=${status}`)
}

export function adminProducts(params) {
  return http.get('/admin/products', params)
}

export function adminProductStatus(id, status) {
  return http.patch(`/admin/products/${id}/status?status=${status}`)
}

export function adminDeleteProduct(id) {
  return http.del(`/admin/products/${id}`)
}

export function adminOrders(params) {
  return http.get('/admin/management/orders', params)
}

export function adminDeleteOrder(id) {
  return http.del(`/admin/management/orders/${id}`)
}
