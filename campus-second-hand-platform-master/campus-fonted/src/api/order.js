import { http } from './http'

export function myOrders() {
  return http.get('/orders/my')
}

export function myOrdersByStatus(status) {
  return http.get(`/orders/my/status/${status}`)
}

export function myOrdersPage(params) {
  return http.get('/orders/my/page', params)
}

export function orderAmount() {
  return http.get('/orders/amount')
}

export function createOrder(body) {
  return http.post('/orders', body)
}

export function confirmOrderSeller(id) {
  return http.patch(`/orders/${id}/confirm/seller`)
}

export function confirmOrderBuyer(id) {
  return http.patch(`/orders/${id}/confirm/buyer`)
}

export function completeOrder(id) {
  return http.patch(`/orders/${id}/complete`)
}

export function cancelOrder(id, body) {
  return http.patch(`/orders/${id}/cancel`, body)
}
