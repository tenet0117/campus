import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    unreadCount: 0,
  }),

  actions: {
    setUnreadCount(n) {
      this.unreadCount = Number(n) || 0
    },
  },
})
