<template>
  <div class="admin-page">
    <div class="page-meta">
      <p class="page-desc">查看交易订单、收货信息与状态</p>
      <span class="meta-count">共 {{ total }} 笔订单</span>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索订单号"
        clearable
        style="width: 240px"
        @keyup.enter="search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 140px" @change="search">
        <el-option :value="1" label="待接单" />
        <el-option :value="2" label="进行中" />
        <el-option :value="3" label="已完成" />
        <el-option :value="4" label="已取消" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <el-card shadow="never" class="panel">
      <el-table v-loading="loading" :data="orders" stripe empty-text="暂无订单">
        <el-table-column label="订单 / 商品" min-width="260">
          <template #default="{ row }">
            <div class="prod-cell clickable" @click="openDetail(row)">
              <div class="thumb">
                <img v-if="coverOf(row)" :src="coverOf(row)" :alt="row.productTitle" />
                <span v-else class="thumb-empty">无图</span>
              </div>
              <div class="prod-text">
                <div class="title">{{ row.productTitle || ('商品 #' + row.productId) }}</div>
                <div class="sub">{{ row.orderNo }} · ID {{ row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="买卖双方" min-width="140">
          <template #default="{ row }">
            <div class="party">
              <span><em>买</em>{{ row.buyerNickname || row.buyerId }}</span>
              <span><em>卖</em>{{ row.sellerNickname || row.sellerId }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="收货信息" min-width="200">
          <template #default="{ row }">
            <div class="addr">
              <div>
                {{ row.tradeTypeText || (row.tradeType === 2 ? '快递' : '面交') }}
                · {{ row.receiverName || '-' }} {{ row.receiverPhone || '' }}
              </div>
              <div class="muted">
                {{ [row.campus, row.building, row.addressDetail].filter(Boolean).join(' ') || '未填地址' }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="金额" width="100">
          <template #default="{ row }"><span class="price">¥{{ row.amount }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="order" />
          </template>
        </el-table-column>
        <el-table-column label="时间" width="160">
          <template #default="{ row }">{{ fmtDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">详情</el-button>
            <el-button type="danger" link @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="totalPages > 1" class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :page-size="size"
          :current-page="page"
          :total="total"
          @current-change="(p) => { page = p; load() }"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="订单详情" width="620px" destroy-on-close>
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detail">
          <div class="order-hero">
            <div class="thumb large">
              <img v-if="coverOf(detail)" :src="coverOf(detail)" :alt="detail.productTitle" />
              <span v-else class="thumb-empty">无图</span>
            </div>
            <div>
              <h3>{{ detail.productTitle || ('商品 #' + detail.productId) }}</h3>
              <p class="sub">订单号 {{ detail.orderNo }}</p>
              <div class="chips">
                <StatusTag :status="detail.status" type="order" />
                <span class="price">¥{{ detail.amount }}</span>
              </div>
            </div>
          </div>

          <el-descriptions :column="2" border size="small">
            <el-descriptions-item label="买家">
              {{ detail.buyerNickname || detail.buyerUsername || detail.buyerId }}
            </el-descriptions-item>
            <el-descriptions-item label="卖家">
              {{ detail.sellerNickname || detail.sellerUsername || detail.sellerId }}
            </el-descriptions-item>
            <el-descriptions-item label="买家电话">{{ detail.buyerPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="卖家电话">{{ detail.sellerPhone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交易方式">
              {{ detail.tradeTypeText || (detail.tradeType === 2 ? '快递' : '面交') }}
            </el-descriptions-item>
            <el-descriptions-item label="收货人">
              {{ detail.receiverName || '-' }} {{ detail.receiverPhone || '' }}
            </el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">
              {{ [detail.campus, detail.building, detail.addressDetail].filter(Boolean).join(' ') || '未填地址' }}
            </el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ fmtDateTime(detail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ fmtDateTime(detail.completeTime) || '-' }}</el-descriptions-item>
            <el-descriptions-item v-if="detail.cancelReason" label="取消原因" :span="2">
              {{ detail.cancelReason }}
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { http } from '../../api/index.js'
import StatusTag from '../../components/StatusTag.vue'
import { fmtDateTime, productCoverSrc } from '../../utils/media.js'

const orders = ref([])
const keyword = ref('')
const statusFilter = ref(null)
const page = ref(1)
const size = ref(16)
const total = ref(0)
const loading = ref(false)
const totalPages = computed(() => Math.ceil(total.value / size.value) || 1)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

onMounted(load)

function coverOf(row) {
  return productCoverSrc(row)
}

async function load() {
  loading.value = true
  try {
    const res = await http.get('/admin/management/orders', {
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value != null ? statusFilter.value : undefined,
    })
    if (res.code === 200 && res.data) {
      orders.value = res.data.records || []
      total.value = res.data.total || 0
      if (orders.value.length === 0 && total.value > 0 && page.value > 1) {
        page.value = Math.max(1, Math.ceil(total.value / size.value) - 1)
        load()
        return
      }
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await http.get(`/admin/management/orders/${row.id}`)
    if (res.code === 200 && res.data) {
      detail.value = res.data.order || res.data
    } else {
      ElMessage.error(res.message || '加载详情失败')
      detailVisible.value = false
    }
  } catch {
    ElMessage.error('加载详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function doDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该订单？', '提示', { type: 'warning' })
    await http.del(`/admin/management/orders/${id}`)
    ElMessage.success('已删除')
    load()
  } catch {
    /* cancel */
  }
}
</script>

<style scoped>
.page-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.page-desc {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}
.meta-count {
  font-size: 13px;
  color: #64748b;
  background: #fff;
  border: 1px solid #e2e8f0;
  padding: 4px 10px;
  border-radius: 999px;
  white-space: nowrap;
}
.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.panel {
  border-radius: 12px;
  border: 1px solid #e8ecf1;
}
.prod-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}
.prod-cell.clickable {
  cursor: pointer;
}
.thumb {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f5f9;
  flex-shrink: 0;
  display: grid;
  place-items: center;
}
.thumb.large {
  width: 72px;
  height: 72px;
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-empty {
  font-size: 11px;
  color: #94a3b8;
}
.prod-text .title {
  font-weight: 600;
  color: #1f2937;
  line-height: 1.35;
}
.prod-text .sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 3px;
}
.party {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #334155;
}
.party em {
  font-style: normal;
  display: inline-block;
  width: 18px;
  height: 18px;
  line-height: 18px;
  text-align: center;
  border-radius: 4px;
  background: #fff1f0;
  color: #e1251b;
  font-size: 11px;
  font-weight: 700;
  margin-right: 6px;
}
.addr {
  font-size: 13px;
  color: #334155;
  line-height: 1.4;
}
.addr .muted {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}
.price {
  color: #e1251b;
  font-weight: 700;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.detail-body {
  min-height: 160px;
}
.order-hero {
  display: flex;
  gap: 14px;
  align-items: center;
  margin-bottom: 16px;
}
.order-hero h3 {
  margin: 0 0 4px;
  font-size: 16px;
  color: #1f2937;
}
.order-hero .sub {
  margin: 0 0 8px;
  font-size: 12px;
  color: #94a3b8;
}
.order-hero .chips {
  display: flex;
  align-items: center;
  gap: 10px;
}
.order-hero .price {
  font-size: 18px;
}
</style>
