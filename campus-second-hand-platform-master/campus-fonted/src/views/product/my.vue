<template>
  <div class="my-page">
    <div class="page-top">
      <div>
        <h1>我的发布</h1>
        <p class="sub">管理在售、预订与下架的闲置商品</p>
      </div>
      <router-link to="/product/publish" class="pub-btn">
        <el-icon><Plus /></el-icon>
        发布新商品
      </router-link>
    </div>

    <div class="tabs">
      <button
        v-for="t in tabs"
        :key="String(t.value)"
        type="button"
        class="tab"
        :class="{ active: filterStatus === t.value }"
        @click="setFilter(t.value)"
      >
        {{ t.label }}
      </button>
    </div>

    <div v-if="loading" class="spinner" />

    <div v-else-if="products.length" class="list">
      <article v-for="p in products" :key="p.id" class="item-card">
        <router-link :to="'/product/' + p.id" class="cover">
          <img v-if="coverOf(p)" :src="coverOf(p)" :alt="p.title" />
          <div v-else class="cover-empty">暂无图</div>
          <StatusTag class="status" :status="p.status" />
        </router-link>

        <div class="body">
          <div class="body-main">
            <router-link :to="'/product/' + p.id" class="title">{{ p.title }}</router-link>
            <div class="meta">
              <span v-if="p.condition">{{ p.condition }}</span>
              <span>{{ p.viewCount || 0 }} 浏览</span>
              <span>{{ fmt(p.createTime) }}</span>
            </div>
            <div class="price">
              <span class="yen">¥</span>
              <span class="num">{{ p.price }}</span>
              <span v-if="p.originalPrice" class="origin">¥{{ p.originalPrice }}</span>
            </div>
          </div>

          <div class="actions">
            <button type="button" class="link" @click="router.push('/product/' + p.id)">查看</button>
            <button
              v-if="p.status !== 3"
              type="button"
              class="link"
              @click="router.push('/product/edit/' + p.id)"
            >
              修改
            </button>
            <button
              v-if="p.status === 1"
              type="button"
              class="link"
              @click="changeStatus(p, 0, '确认下架该商品？')"
            >
              下架
            </button>
            <button
              v-if="p.status === 0"
              type="button"
              class="link primary"
              @click="changeStatus(p, 1, '确认重新上架？')"
            >
              上架
            </button>
            <button
              v-if="p.status === 2"
              type="button"
              class="link primary"
              @click="router.push('/order?status=1')"
            >
              处理订单
            </button>
            <button
              v-if="p.status === 2"
              type="button"
              class="link"
              @click="changeStatus(p, 1, '确认取消预约？相关未完成订单将一并取消。')"
            >
              取消预约
            </button>
            <button
              v-if="p.status === 3"
              type="button"
              class="link primary"
              @click="router.push('/product/publish')"
            >
              重新发布
            </button>
            <button
              v-if="p.status !== 0"
              type="button"
              class="link danger"
              @click="doDelete(p)"
            >
              删除
            </button>
            <button
              v-else
              type="button"
              class="link danger"
              @click="doHardDelete(p)"
            >
              彻底删除
            </button>
          </div>
        </div>
      </article>
    </div>

    <EmptyState v-else description="还没有发布商品">
      <router-link to="/product/publish" class="pub-btn sm">去发布一件闲置吧~</router-link>
    </EmptyState>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../../api/index.js'
import StatusTag from '../../components/StatusTag.vue'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const products = ref([])
const filterStatus = ref('')
const loading = ref(false)

const tabs = [
  { label: '全部', value: '' },
  { label: '在售', value: 1 },
  { label: '已预订', value: 2 },
  { label: '已售出', value: 3 },
  { label: '已下架', value: 0 },
]

onMounted(load)

function setFilter(v) {
  filterStatus.value = v
  load()
}

async function load() {
  loading.value = true
  try {
    const params = { page: 0, size: 100 }
    if (filterStatus.value !== '') params.status = filterStatus.value
    const res = await http.get('/products/my', params)
    if (res.code === 200 && res.data) {
      products.value = res.data.records || res.data.content || (Array.isArray(res.data) ? res.data : [])
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function resolveUrl(raw) {
  if (!raw || typeof raw !== 'string') return ''
  if (raw.startsWith('http') || raw.startsWith('data:') || raw.startsWith('/api/')) return raw
  if (raw.startsWith('/')) return raw
  return `/api/v1/files/view/${raw}`
}

function coverOf(p) {
  if (!p) return ''
  let images = p.images
  if (typeof images === 'string') {
    try {
      images = JSON.parse(images)
    } catch {
      return resolveUrl(images)
    }
  }
  if (Array.isArray(images) && images.length) {
    const first = images[0]
    return resolveUrl(typeof first === 'string' ? first : first?.url || first?.path || '')
  }
  return resolveUrl(p.coverImage || p.imageUrl || '')
}

function fmt(t) {
  if (!t) return ''
  const d = new Date(t)
  const m = `${d.getMonth() + 1}`.padStart(2, '0')
  const day = `${d.getDate()}`.padStart(2, '0')
  const h = `${d.getHours()}`.padStart(2, '0')
  const min = `${d.getMinutes()}`.padStart(2, '0')
  return `${d.getFullYear()}-${m}-${day} ${h}:${min}`
}

async function changeStatus(p, status, tip) {
  try {
    await ElMessageBox.confirm(tip, '提示', { type: 'warning', confirmButtonText: '确定', cancelButtonText: '取消' })
  } catch {
    return
  }
  try {
    await http.patch(`/products/${p.id}/status?status=${status}`)
    ElMessage.success('操作成功')
    load()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function doDelete(p) {
  try {
    await ElMessageBox.confirm(`将「${p.title}」移至已下架？`, '删除商品', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await http.del(`/products/${p.id}`)
    ElMessage.success('已移至下架')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}

async function doHardDelete(p) {
  try {
    await ElMessageBox.confirm('彻底删除后不可恢复，确定继续？', '危险操作', {
      type: 'error',
      confirmButtonText: '彻底删除',
      cancelButtonText: '取消',
    })
  } catch {
    return
  }
  try {
    await http.del(`/products/${p.id}?hard=true`)
    ElMessage.success('已彻底删除')
    load()
  } catch {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.my-page {
  max-width: 960px;
  margin: 0 auto 40px;
}

.page-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding-bottom: 14px;
  border-bottom: 3px solid var(--jd-red);
}

.page-top h1 {
  font-size: 24px;
  font-weight: 900;
  color: var(--gray-900);
}

.page-top .sub {
  margin-top: 4px;
  font-size: 13px;
  color: var(--gray-500);
}

.pub-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 40px;
  padding: 0 18px;
  border-radius: 20px;
  background: var(--jd-red);
  color: #fff;
  font-size: 14px;
  font-weight: 700;
  white-space: nowrap;
}
.pub-btn:hover { background: var(--jd-red-hover); }
.pub-btn.sm { height: 36px; padding: 0 16px; margin-top: 12px; }

.tabs {
  display: flex;
  gap: 4px;
  overflow-x: auto;
  margin-bottom: 14px;
  padding: 4px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.tab {
  flex-shrink: 0;
  border: none;
  background: transparent;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  color: var(--gray-600);
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.tab em {
  font-style: normal;
  font-size: 11px;
  color: #fff;
  background: var(--jd-red);
  border-radius: 8px;
  padding: 0 6px;
  line-height: 16px;
}
.tab.active {
  background: var(--jd-red-light);
  color: var(--jd-red);
  font-weight: 700;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-card {
  display: grid;
  grid-template-columns: 112px 1fr;
  gap: 14px;
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.04);
}

.cover {
  position: relative;
  width: 112px;
  height: 112px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--gray-100);
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.cover-empty {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  color: var(--gray-400);
  font-size: 12px;
}
.status {
  position: absolute;
  left: 6px;
  top: 6px;
}

.body {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.title {
  font-size: 16px;
  font-weight: 700;
  color: var(--gray-900);
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.title:hover { color: var(--jd-red); }

.meta {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  font-size: 12px;
  color: var(--gray-500);
}

.price {
  margin-top: 8px;
  color: var(--jd-red);
  font-weight: 800;
  line-height: 1;
}
.price .yen { font-size: 13px; margin-right: 1px; }
.price .num { font-size: 22px; }
.price .origin {
  margin-left: 8px;
  font-size: 12px;
  font-weight: 400;
  color: var(--gray-400);
  text-decoration: line-through;
}

.actions {
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid var(--gray-100);
  display: flex;
  flex-wrap: wrap;
  gap: 4px 2px;
  justify-content: flex-end;
}

.link {
  border: none;
  background: transparent;
  padding: 4px 10px;
  font-size: 13px;
  color: var(--gray-700);
  border-radius: 4px;
}
.link:hover { background: var(--gray-100); color: var(--jd-red); }
.link.primary { color: var(--jd-red); font-weight: 600; }
.link.danger { color: #c0392b; }
.link.danger:hover { background: #fff1f0; }

@media (max-width: 640px) {
  .page-top {
    flex-direction: column;
    align-items: stretch;
  }
  .item-card {
    grid-template-columns: 88px 1fr;
    gap: 10px;
    padding: 12px;
  }
  .cover {
    width: 88px;
    height: 88px;
  }
  .price .num { font-size: 18px; }
  .actions { justify-content: flex-start; }
}
</style>
