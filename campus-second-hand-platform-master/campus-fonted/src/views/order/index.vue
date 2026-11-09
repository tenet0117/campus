<template>
  <div class="order-page">
    <div class="page-top">
      <div>
        <h1>我的订单</h1>
        <p class="sub">卖家在这里确认接单；订单会展示买家填写的联系与地址信息</p>
      </div>
      <router-link v-if="pendingAccept > 0" to="/order?status=1" class="pending-tip">
        {{ pendingAccept }} 笔待你接单
      </router-link>
    </div>

    <el-radio-group v-model="filterStatus" class="filter-tabs" @change="load">
      <el-radio-button value="">全部</el-radio-button>
      <el-radio-button value="1">待接单</el-radio-button>
      <el-radio-button value="2">进行中</el-radio-button>
      <el-radio-button value="3">已完成</el-radio-button>
      <el-radio-button value="4">已取消</el-radio-button>
    </el-radio-group>

    <div v-if="loading" class="loading-wrap"><el-skeleton :rows="3" animated /></div>
    <EmptyState v-else-if="!orders.length" description="暂无订单" />
    <div v-else class="list">
      <article v-for="o in orders" :key="o.id" class="order-card">
        <router-link :to="'/product/' + o.productId" class="cover">
          <img v-if="coverOf(o)" :src="coverOf(o)" alt="" />
          <div v-else class="cover-empty">商品</div>
        </router-link>
        <div class="body">
          <div class="row-top">
            <div>
              <div class="order-no">订单号 {{ o.orderNo }}</div>
              <router-link :to="'/product/' + o.productId" class="product-link">
                {{ o.productTitle || ('商品 #' + o.productId) }}
              </router-link>
              <div class="meta">
                <span class="role-tag">{{ roleOf(o) }}</span>
                <span v-if="isSeller(o) && o.buyerNickname">买家 {{ o.buyerNickname }}</span>
                <span v-if="isBuyer(o) && o.sellerNickname">卖家 {{ o.sellerNickname }}</span>
                <span v-if="o.status === 2">{{ settleHint(o) }}</span>
              </div>
              <div v-if="o.receiverName || o.receiverPhone" class="contact">
                <span class="trade">{{ o.tradeTypeText || (o.tradeType === 2 ? '快递配送' : '校内面交') }}</span>
                <span>{{ o.receiverName }} {{ o.receiverPhone }}</span>
                <span v-if="addressOf(o)">{{ addressOf(o) }}</span>
                <span v-if="o.remark">备注：{{ o.remark }}</span>
              </div>
            </div>
            <div class="side">
              <div class="price">¥{{ o.amount }}</div>
              <StatusTag :status="o.status" type="order" />
              <div class="time">{{ fmt(o.createTime) }}</div>
            </div>
          </div>

          <div class="actions">
            <el-button
              v-if="o.status === 1 && isSeller(o)"
              type="primary"
              size="small"
              @click="doAccept(o)"
            >
              确认接单
            </el-button>
            <span v-if="o.status === 1 && isBuyer(o)" class="wait">已绑定该商品，等待卖家确认接单</span>

            <el-button
              v-if="o.status === 2 && isBuyer(o) && !Number(o.buyerConfirm)"
              type="success"
              size="small"
              @click="doSettleBuyer(o)"
            >
              确认完成
            </el-button>
            <el-button
              v-if="o.status === 2 && isSeller(o) && !Number(o.sellerConfirm)"
              type="success"
              size="small"
              @click="doSettleSeller(o)"
            >
              确认完成
            </el-button>
            <span
              v-if="o.status === 2 && ((isBuyer(o) && Number(o.buyerConfirm)) || (isSeller(o) && Number(o.sellerConfirm)))"
              class="wait"
            >
              已确认，等待对方确认
            </span>

            <el-button
              v-if="[1, 2].includes(o.status)"
              type="danger"
              size="small"
              plain
              @click="doCancel(o)"
            >
              取消订单
            </el-button>
          </div>
        </div>
      </article>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../../api/index.js'
import { useUserStore } from '../../stores/user'
import StatusTag from '../../components/StatusTag.vue'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const userStore = useUserStore()
const orders = ref([])
const filterStatus = ref('')
const loading = ref(false)
const userId = ref(null)

const pendingAccept = computed(
  () => orders.value.filter((o) => o.status === 1 && isSeller(o)).length
)

onMounted(() => {
  userId.value = userStore.userId
  if (route.query.status != null && route.query.status !== '') {
    filterStatus.value = String(route.query.status)
  }
  load()
})

watch(
  () => route.query.status,
  (v) => {
    if (v != null && String(v) !== filterStatus.value) {
      filterStatus.value = String(v)
      load()
    }
  }
)

function isBuyer(o) {
  return Number(o.buyerId) === Number(userId.value)
}
function isSeller(o) {
  return Number(o.sellerId) === Number(userId.value)
}
function roleOf(o) {
  if (isBuyer(o)) return '我是买家'
  if (isSeller(o)) return '我是卖家'
  return ''
}
function settleHint(o) {
  const b = Number(o.buyerConfirm) ? '买家已确认' : '待买家确认'
  const s = Number(o.sellerConfirm) ? '卖家已确认' : '待卖家确认'
  return `${b} · ${s}`
}

function addressOf(o) {
  return [o.campus, o.building, o.addressDetail].filter(Boolean).join(' · ')
}

function resolveUrl(raw) {
  if (!raw || typeof raw !== 'string') return ''
  if (raw.startsWith('http') || raw.startsWith('data:') || raw.startsWith('/api/') || raw.startsWith('/')) return raw
  return `/api/v1/files/view/${raw}`
}

function coverOf(o) {
  return resolveUrl(o.productImage)
}

async function load() {
  loading.value = true
  try {
    const res = filterStatus.value
      ? await http.get(`/orders/my/status/${filterStatus.value}`)
      : await http.get('/orders/my')
    if (res.code === 200) orders.value = res.data || []
    else ElMessage.error(res.message || '加载失败')
  } catch {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

async function doAccept(o) {
  try {
    await ElMessageBox.confirm(
      `确认接受「${o.productTitle || '该商品'}」的订单？\n联系人：${o.receiverName || '-'} ${o.receiverPhone || ''}\n地址：${addressOf(o) || '未填写'}`,
      '确认接单'
    )
    const res = await http.patch(`/orders/${o.id}/confirm/seller`)
    if (res.code === 200) {
      ElMessage.success('已接单')
      load()
    } else ElMessage.error(res.message || '操作失败')
  } catch {
    /* cancel */
  }
}

async function doSettleBuyer(o) {
  try {
    await ElMessageBox.confirm('确认已当面完成交易？', '确认完成')
    const res = await http.patch(`/orders/${o.id}/confirm/buyer`)
    if (res.code === 200) {
      ElMessage.success('已确认')
      load()
    } else ElMessage.error(res.message || '操作失败')
  } catch {
    /* cancel */
  }
}

async function doSettleSeller(o) {
  try {
    await ElMessageBox.confirm('确认已当面完成交易？', '确认完成')
    const res = await http.patch(`/orders/${o.id}/confirm/seller`)
    if (res.code === 200) {
      ElMessage.success('已确认')
      load()
    } else ElMessage.error(res.message || '操作失败')
  } catch {
    /* cancel */
  }
}

async function doCancel(o) {
  try {
    const { value } = await ElMessageBox.prompt('取消原因（选填）', '取消订单', {
      inputPlaceholder: '请输入原因',
      confirmButtonText: '确定取消',
    })
    const res = await http.patch(`/orders/${o.id}/cancel`, { reason: value || '' })
    if (res.code === 200) {
      ElMessage.success('已取消，商品将恢复在售')
      load()
    } else ElMessage.error(res.message || '操作失败')
  } catch {
    /* cancel */
  }
}

function fmt(t) {
  return t ? new Date(t).toLocaleString('zh-CN') : ''
}
</script>

<style scoped>
.order-page {
  max-width: 880px;
  margin: 0 auto 40px;
}
.page-top {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 14px;
  padding-bottom: 12px;
  border-bottom: 3px solid var(--jd-red);
}
.page-top h1 {
  font-size: 24px;
  font-weight: 900;
}
.page-top .sub {
  margin-top: 4px;
  font-size: 13px;
  color: #999;
}
.pending-tip {
  background: #fff1f0;
  color: #e1251b;
  font-size: 13px;
  font-weight: 700;
  padding: 8px 12px;
  border-radius: 999px;
  white-space: nowrap;
}
.filter-tabs {
  margin-bottom: 16px;
}
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.order-card {
  display: grid;
  grid-template-columns: 88px 1fr;
  gap: 12px;
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 1px 6px rgba(0, 0, 0, 0.04);
}
.cover {
  width: 88px;
  height: 88px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
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
  color: #bbb;
  font-size: 12px;
}
.row-top {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.order-no {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 4px;
  font-family: ui-monospace, monospace;
}
.product-link {
  color: #222;
  font-weight: 700;
  text-decoration: none;
}
.product-link:hover {
  color: #e1251b;
}
.meta {
  margin-top: 6px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  font-size: 12px;
  color: #8c8c8c;
}
.role-tag {
  background: #f5f5f5;
  padding: 1px 6px;
  border-radius: 4px;
}
.contact {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #595959;
  line-height: 1.5;
}
.contact .trade {
  display: inline-block;
  width: fit-content;
  background: #fff7e6;
  color: #d46b08;
  padding: 0 6px;
  border-radius: 4px;
  font-weight: 600;
}
.side {
  text-align: right;
  flex-shrink: 0;
}
.price {
  font-size: 18px;
  font-weight: 700;
  color: #e1251b;
  margin-bottom: 6px;
}
.time {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 4px;
}
.actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}
.wait {
  font-size: 13px;
  color: #999;
}
.loading-wrap {
  padding: 16px 0;
}
@media (max-width: 640px) {
  .order-card {
    grid-template-columns: 72px 1fr;
  }
  .cover {
    width: 72px;
    height: 72px;
  }
}
</style>
