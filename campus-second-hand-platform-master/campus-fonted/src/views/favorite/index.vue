<template>
  <div class="fav-page">
    <div class="page-top">
      <div>
        <h1>我的收藏</h1>
        <p class="sub">共 {{ favorites.length }} 件心仪闲置</p>
      </div>
      <router-link to="/product/list" class="browse-btn">去逛逛</router-link>
    </div>

    <div v-if="loading" class="spinner" />

    <div v-else-if="favorites.length" class="list">
      <article v-for="p in favorites" :key="p.id" class="item-card">
        <router-link :to="'/product/' + p.id" class="cover">
          <img v-if="coverOf(p)" :src="coverOf(p)" :alt="p.title" loading="lazy" />
          <div v-else class="cover-empty">暂无图</div>
          <StatusTag v-if="p.status != null && p.status !== 1" class="status" :status="p.status" />
        </router-link>

        <div class="body">
          <router-link :to="'/product/' + p.id" class="title">{{ p.title }}</router-link>
          <div class="meta">
            <span v-if="p.condition">{{ p.condition }}</span>
            <span>{{ p.viewCount || 0 }} 浏览</span>
          </div>
          <div class="row-bottom">
            <div class="price">
              <span class="yen">¥</span>
              <span class="num">{{ p.price }}</span>
              <span v-if="p.originalPrice" class="origin">¥{{ p.originalPrice }}</span>
            </div>
            <div class="actions">
              <button type="button" class="link" @click="router.push('/product/' + p.id)">查看</button>
              <button type="button" class="link danger" @click="doRemove(p)">取消收藏</button>
            </div>
          </div>
        </div>
      </article>
    </div>

    <EmptyState v-else description="还没有收藏，去首页挑几件心仪的吧">
      <router-link to="/dashboard" class="browse-btn sm">去首页看看</router-link>
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
const favorites = ref([])
const loading = ref(false)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await http.get('/favorites/my')
    if (res.code === 200) {
      const data = res.data
      favorites.value = Array.isArray(data) ? data : data?.records || data?.content || []
    }
  } catch {
    ElMessage.error('加载收藏失败')
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

async function doRemove(p) {
  try {
    await ElMessageBox.confirm(`取消收藏「${p.title}」？`, '提示', {
      confirmButtonText: '取消收藏',
      cancelButtonText: '再想想',
      type: 'warning',
    })
  } catch {
    return
  }
  try {
    await http.del(`/favorites/${p.id}`)
    favorites.value = favorites.value.filter((x) => x.id !== p.id)
    ElMessage.success('已取消收藏')
  } catch {
    ElMessage.error('操作失败')
  }
}
</script>

<style scoped>
.fav-page {
  max-width: 880px;
  margin: 0 auto 40px;
}

.page-top {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 12px;
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

.browse-btn {
  display: inline-flex;
  align-items: center;
  height: 36px;
  padding: 0 16px;
  border-radius: 18px;
  background: var(--jd-red);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}
.browse-btn:hover { background: var(--jd-red-hover); }
.browse-btn.sm { margin-top: 12px; }

.list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.item-card {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 12px;
  background: #fff;
  border-radius: 10px;
  padding: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}

.cover {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--gray-100);
  flex-shrink: 0;
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
  font-size: 12px;
  color: var(--gray-400);
}
.status {
  position: absolute;
  left: 4px;
  top: 4px;
}

.body {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.title {
  font-size: 15px;
  font-weight: 700;
  color: var(--gray-900);
  line-height: 1.35;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.title:hover { color: var(--jd-red); }

.meta {
  margin-top: 4px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: var(--gray-500);
}

.row-bottom {
  margin-top: auto;
  padding-top: 8px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 8px;
}

.price {
  color: var(--jd-red);
  font-weight: 800;
  line-height: 1;
}
.price .yen { font-size: 12px; }
.price .num { font-size: 18px; }
.price .origin {
  margin-left: 6px;
  font-size: 12px;
  font-weight: 400;
  color: var(--gray-400);
  text-decoration: line-through;
}

.actions {
  display: flex;
  gap: 2px;
  flex-shrink: 0;
}

.link {
  border: none;
  background: transparent;
  padding: 4px 8px;
  font-size: 13px;
  color: var(--gray-600);
  border-radius: 4px;
}
.link:hover { background: var(--gray-100); color: var(--jd-red); }
.link.danger { color: #c0392b; }
.link.danger:hover { background: #fff1f0; }

@media (max-width: 560px) {
  .item-card {
    grid-template-columns: 72px 1fr;
    gap: 10px;
    padding: 10px;
  }
  .cover {
    width: 72px;
    height: 72px;
  }
  .row-bottom {
    flex-direction: column;
    align-items: flex-start;
  }
  .actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>
