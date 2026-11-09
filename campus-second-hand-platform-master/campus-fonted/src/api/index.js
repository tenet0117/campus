/** Re-export HTTP helpers for backward compatibility with existing views. */
export { api, http, setToken, getToken } from './http.js'

export * as authApi from './auth.js'
export * as productApi from './product.js'
export * as orderApi from './order.js'
export * as adminApi from './admin.js'
export * as messageApi from './message.js'
export * as favoriteApi from './favorite.js'
export * as categoryApi from './category.js'
