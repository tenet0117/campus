import { http } from './http'

export function unreadTotal() {
  return http.get('/messages/unread/total')
}

export function messagesPage(params) {
  return http.get('/messages/all/page', params)
}

export function markMessageRead(id) {
  return http.put(`/messages/${id}/read`)
}
