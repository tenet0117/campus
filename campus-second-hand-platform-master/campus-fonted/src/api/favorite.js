import { http } from './http'

export function myFavorites() {
  return http.get('/favorites/my')
}

export function favoriteCount() {
  return http.get('/favorites/count')
}

export function checkFavorite(productId) {
  return http.get(`/favorites/${productId}/check`)
}

export function addFavorite(productId) {
  return http.post(`/favorites/${productId}`)
}

export function removeFavorite(productId) {
  return http.del(`/favorites/${productId}`)
}
