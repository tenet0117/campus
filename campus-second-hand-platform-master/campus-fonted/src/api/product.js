import { http } from './http'

export function listProducts(params) {
  return http.get('/products', params)
}

export function searchProducts(params) {
  return http.get('/products/search', params)
}

export function getProduct(id) {
  return http.get(`/products/${id}`)
}

export function createProduct(body) {
  return http.post('/products', body)
}

export function myProducts(params) {
  return http.get('/products/my', params)
}

export function updateProduct(id, body) {
  return http.put(`/products/${id}`, body)
}

export function updateProductStatus(id, status) {
  return http.patch(`/products/${id}/status?status=${status}`)
}

export function deleteProduct(id, hard = false) {
  return http.del(hard ? `/products/${id}?hard=true` : `/products/${id}`)
}

export function productCountByUser(uid) {
  return http.get(`/products/count/user/${uid}`)
}

export function popularProducts(params) {
  return http.get('/products/popular', params)
}

export function uploadImage(formData) {
  return http.upload('/files/upload/image', formData)
}
