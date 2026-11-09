<template>
  <div>
    <div class="page-header"><h2>站内消息</h2></div>
    <div v-if="loading" class="spinner"></div>
    <template v-else>
      <div v-if="!messages.length" class="empty"><div class="empty-icon">&#x2709;</div><p>暂无消息</p></div>
      <div class="card" v-for="m in messages" :key="m.id" style="margin-bottom:8px;padding:14px;cursor:pointer" @click="markRead(m)">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;gap:12px">
          <div style="flex:1">
            <div style="font-size:14px;font-weight:500;margin-bottom:4px">
              <span v-if="!m.isRead" style="display:inline-block;width:8px;height:8px;border-radius:50%;background:var(--danger);margin-right:6px;vertical-align:middle"></span>
              {{ m.content || '(无内容)' }}
            </div>
            <div style="font-size:12px;color:var(--gray-400)">来自: ID{{ m.fromUserId }} <span v-if="m.productId" style="margin-left:8px">商品: #{{ m.productId }}</span></div>
          </div>
          <div style="font-size:12px;color:var(--gray-400);flex-shrink:0;text-align:right">
            <span class="badge" :class="m.isRead ? 'badge-info' : 'badge-warning'" style="margin-bottom:4px;display:inline-block">{{ m.isRead ? '已读' : '未读' }}</span><br/>
            {{ fmt(m.createTime || m.sendTime) }}
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '../../api/index.js'

const messages = ref([]); const loading = ref(false)
onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await http.get('/messages/all/page', { page: 0, size: 100 })
    if (res.code === 200 && res.data) messages.value = (res.data.records || []).map(m => ({ ...m, isRead: !!m.read }))
  } catch {} finally { loading.value = false }
}
async function markRead(m) {
  if (m.isRead) return
  try { await http.put(`/messages/${m.id}/read`); m.isRead = true } catch {}
}
function fmt(t) { return t ? new Date(t).toLocaleString('zh-CN') : '' }
</script>
