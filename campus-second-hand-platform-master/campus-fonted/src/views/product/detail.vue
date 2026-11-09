<template>
  <div>
    <div v-if="loading" class="spinner"></div>
    <template v-else-if="product">
      <div class="card" style="margin-bottom:16px">
        <div style="display:flex;gap:24px;flex-wrap:wrap">
          <div style="flex:0 0 320px;height:260px;border-radius:4px;overflow:hidden;background:var(--gray-100);display:flex;align-items:center;justify-content:center">
            <div v-if="images.length" style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;overflow:hidden">
              <img :src="images[activeImg]" :alt="product.title" style="max-width:100%;max-height:100%;object-fit:contain" />
            </div>
            <div v-else class="img-placeholder" style="width:100%;height:100%;font-size:60px">&#x1f4e6;</div>
          </div>
          <div style="flex:1;min-width:280px">
            <h2 style="font-size:20px;margin-bottom:12px">{{ product.title }}</h2>
            <div style="margin-bottom:12px">
              <span style="font-size:28px" class="price">¥{{ product.price }}</span>
              <span v-if="product.originalPrice" style="font-size:16px;margin-left:8px" class="price-original">¥{{ product.originalPrice }}</span>
              <span :class="'badge ' + stBadge(product.status)" style="margin-left:12px;vertical-align:middle">{{ stMap[product.status] }}</span>
            </div>
            <p style="color:var(--gray-500);margin-bottom:8px;font-size:14px">{{ [product.brand, product.model, product.condition].filter(Boolean).join(' | ') }}</p>
            <p style="color:var(--gray-500);margin-bottom:8px;font-size:13px">浏览: {{ product.viewCount || 0 }}</p>
            <div v-if="product.tags" style="margin-bottom:12px">
              <span v-for="t in normTags(product.tags)" :key="t" class="badge badge-primary" style="margin-right:4px">{{ t }}</span>
            </div>
            <div style="display:flex;gap:8px;margin-top:16px">
              <!-- 买家操作：仅当在售时 -->
              <button class="btn btn-primary btn-lg" @click="doBuy" v-if="isOther && product.status === 1">立即购买</button>
              <span v-if="isOther && product.status === 2" style="color:var(--warning);font-size:14px">该商品已被预订</span>
              <span v-if="isOther && product.status === 3" style="color:var(--gray-400);font-size:14px">该商品已售出</span>
              <span v-if="isOther && product.status === 0" style="color:var(--gray-400);font-size:14px">该商品已下架</span>
              <button class="btn btn-outline btn-lg" @click="toggleFav" v-if="isOther">{{ faved ? '取消收藏' : '收藏' }}</button>
              <!-- 卖家操作 -->
              <button class="btn btn-warning btn-lg" @click="doOffShelf" v-if="isOwner && product.status === 1">下架</button>
              <button class="btn btn-success btn-lg" @click="doRelist" v-if="isOwner && product.status === 0">重新上架</button>
              <button class="btn btn-primary btn-lg" @click="doAcceptActive" v-if="isOwner && activeOrder && activeOrder.status === 1">确认接单</button>
              <router-link
                v-if="isOwner && product.status === 2"
                class="btn btn-outline btn-lg"
                to="/order?status=1"
              >查看订单</router-link>
              <button class="btn btn-outline btn-lg" @click="doCancelReserve" v-if="isOwner && product.status === 2">取消预约</button>
              <router-link
                v-if="isOwner && product.status === 3"
                class="btn btn-outline btn-lg"
                to="/product/publish"
              >重新发布</router-link>
            </div>
          </div>
        </div>
      </div>
      <div v-if="images.length > 1" style="display:flex;gap:8px;margin-bottom:16px;flex-wrap:wrap">
        <div v-for="(img, i) in images" :key="i" @click="activeImg = i" :style="{ width: '64px', height: '64px', border: i === activeImg ? '2px solid var(--primary)' : '2px solid var(--gray-200)', borderRadius: '4px', overflow: 'hidden', cursor: 'pointer' }">
          <img :src="img" style="width:100%;height:100%;object-fit:cover" />
        </div>
      </div>
      <div class="card" v-if="product.description">
        <h3 style="font-size:16px;margin-bottom:12px;font-weight:600">商品描述</h3>
        <p style="white-space:pre-wrap;font-size:14px;line-height:1.8;color:var(--gray-600)">{{ product.description }}</p>
        <div v-if="product.tradeNotes" style="margin-top:12px;padding:12px;background:var(--gray-50);border-radius:4px;font-size:13px;color:var(--gray-500)"><strong>交易备注：</strong>{{ product.tradeNotes }}</div>
      </div>
    </template>
    <div v-else class="empty"><div class="empty-icon">&#x274C;</div><p>商品不存在或已删除</p></div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { http } from '../../api/index.js'

const route = useRoute(); const router = useRouter()
const product = ref(null); const faved = ref(false); const loading = ref(false)
const activeImg = ref(0)
const activeOrder = ref(null)
const stMap = { 0: '已下架', 1: '在售', 2: '已预订', 3: '已售出' }
const userId = ref(null)

const images = computed(() => {
  if (!product.value?.images) return []
  const imgs = product.value.images
  if (Array.isArray(imgs)) return imgs.map(i => normalizeUrl(i))
  if (typeof imgs === 'string') {
    try { const arr = JSON.parse(imgs); return arr.map(i => normalizeUrl(i)) } catch {}
  }
  return []
})

const isOwner = computed(() => Number(userId.value) === Number(product.value?.userId))
const isOther = computed(() => !isOwner.value && userId.value)

onMounted(async () => {
  const u = JSON.parse(localStorage.getItem('userInfo') || 'null')
  userId.value = u?.userId ?? u?.id
  const id = route.params.id
  loading.value = true
  try {
    const [res, favRes] = await Promise.all([
      http.get(`/products/${id}`),
      userId.value ? http.get(`/favorites/${id}/check`) : Promise.resolve({ code: -1 })
    ])
    if (res.code === 200) product.value = res.data
    if (favRes.code === 200) faved.value = !!favRes.data
    if (userId.value && product.value?.status === 2) {
      const oRes = await http.get(`/orders/product/${id}/active`)
      if (oRes.code === 200) activeOrder.value = oRes.data
    }
  } catch {} finally { loading.value = false }
})

async function toggleFav() {
  if (!userId.value) return router.push('/login')
  try {
    if (faved.value) { const r = await http.del(`/favorites/${product.value.id}`); if (r.code === 200) faved.value = false }
    else { const r = await http.post(`/favorites/${product.value.id}`); if (r.code === 200) faved.value = true }
  } catch {}
}

async function doBuy() {
  if (!userId.value) return router.push('/login')
  router.push(`/order/checkout/${product.value.id}`)
}

async function doAcceptActive() {
  if (!activeOrder.value) return
  if (!confirm(`确认接受买家对该商品的订单？`)) return
  try {
    const res = await http.patch(`/orders/${activeOrder.value.id}/confirm/seller`)
    if (res.code === 200) {
      alert('已接单，请与买家当面交割后双方在订单里确认完成')
      router.push('/order?status=2')
    } else alert(res.message || '接单失败')
  } catch { alert('接单失败') }
}

async function doOffShelf() {
  if (!confirm('确认下架？')) return
  try { await http.patch(`/products/${product.value.id}/status?status=0`); product.value.status = 0 }
  catch {}
}

async function doRelist() {
  if (!confirm('确认重新上架？')) return
  try { await http.patch(`/products/${product.value.id}/status?status=1`); product.value.status = 1 }
  catch {}
}

async function doCancelReserve() {
  if (!confirm('确认取消预约？相关未完成订单将一并取消，商品恢复在售。')) return
  try {
    const res = await http.patch(`/products/${product.value.id}/status?status=1`)
    if (res.code === 200) {
      product.value.status = 1
      alert('已取消预约并关闭相关订单')
    } else alert(res.message || '操作失败')
  } catch {
    alert('操作失败')
  }
}

function stBadge(s) { return ({ 1: 'badge-success', 2: 'badge-warning', 3: 'badge-info', 0: 'badge-danger' })[s] || '' }
function normTags(tags) {
  if (Array.isArray(tags)) return tags.map(t => typeof t === 'string' ? t.trim() : t).filter(Boolean)
  if (typeof tags === 'string') return tags.split(',').map(s => s.trim()).filter(Boolean)
  return []
}
function normalizeUrl(url) {
  if (!url) return ''
  if (url.startsWith('http') || url.startsWith('data:') || url.startsWith('/api/')) return url
  if (url.startsWith('/')) return url
  return `/api/v1/files/view/${url}`
}
</script>
