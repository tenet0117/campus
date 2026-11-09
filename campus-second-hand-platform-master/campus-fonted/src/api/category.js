import { http } from './http'

export function listCategories() {
  return http.get('/categories')
}

export function createCategory(body) {
  return http.post('/categories', body)
}

export function updateCategory(id, body) {
  return http.put(`/categories/${id}`, body)
}

export function deleteCategory(id) {
  return http.del(`/categories/${id}`)
}
