<template>
  <div class="admin-page">
    <div class="page-meta">
      <p class="page-desc">审核与上下架校园闲置商品</p>
      <span class="meta-count">共 {{ total }} 件商品</span>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索商品标题"
        clearable
        style="width: 240px"
        @keyup.enter="search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 140px" @change="search">
        <el-option :value="0" label="已下架" />
        <el-option :value="1" label="在售" />
        <el-option :value="2" label="已预订" />
        <el-option :value="3" label="已售出" />
      </el-select>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <el-card shadow="never" class="panel">
      <el-table v-loading="loading" :data="products" stripe empty-text="暂无商品">
        <el-table-column label="商品" min-width="280">
          <template #default="{ row }">
            <div class="prod-cell clickable" @click="openDetail(row)">
              <div class="thumb">
                <img v-if="coverOf(row)" :src="coverOf(row)" :alt="row.title" />
                <span v-else class="thumb-empty">无图</span>
              </div>
              <div class="prod-text">
                <div class="title">{{ row.title }}</div>
                <div class="sub">ID {{ row.id }} · 浏览 {{ row.viewCount || 0 }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="110">
          <template #default="{ row }"><span class="price">¥{{ row.price }}</span></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><StatusTag :status="row.status" /></template>
        </el-table-column>
        <el-table-column label="发布时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" type="warning" link @click="toggleStatus(row.id, 0)">下架</el-button>
            <el-button v-if="row.status === 0" type="success" link @click="toggleStatus(row.id, 1)">上架</el-button>
            <el-button type="danger" link @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="total > size" class="pager">
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

    <el-dialog
      v-model="detailVisible"
      title="商品详情"
      width="680px"
      destroy-on-close
    >
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detail">
          <div class="detail-top">
            <div class="gallery">
              <img
                v-if="activeImage"
                :src="activeImage"
                :alt="detail.title"
                class="main-img"
              />
              <div v-else class="main-empty">暂无图片</div>
              <div v-if="imageList.length > 1" class="thumbs">
                <button
                  v-for="(img, i) in imageList"
                  :key="img + i"
                  type="button"
                  class="thumb-btn"
                  :class="{ active: i === activeIdx }"
                  @click="activeIdx = i"
                >
                  <img :src="img" alt="" />
                </button>
              </div>
            </div>
            <div class="info">
              <h3>{{ detail.title }}</h3>
              <div class="price-line">
                <span class="price">¥{{ detail.price }}</span>
                <span v-if="detail.originalPrice" class="origin">原价 ¥{{ detail.originalPrice }}</span>
              </div>
              <div class="chips">
                <StatusTag :status="detail.status" />
                <el-tag v-if="detail.categoryName" size="small" effect="plain">{{ detail.categoryName }}</el-tag>
                <el-tag v-if="detail.condition" size="small" type="warning" effect="plain">{{ detail.condition }}</el-tag>
              </div>
              <div class="seller" v-if="detail.username || detail.userId">
                <UserAvatar :src="detail.userAvatar" :name="detail.username" :size="28" />
                <div>
                  <div class="seller-name">{{ detail.username || ('用户 #' + detail.userId) }}</div>
                  <div class="seller-sub">卖家 ID {{ detail.userId }}</div>
                </div>
              </div>
              <p class="meta">浏览 {{ detail.viewCount || 0 }} · 发布于 {{ fmtDateTime(detail.createTime) }}</p>
            </div>
          </div>

          <el-descriptions :column="2" border size="small" class="desc-block">
            <el-descriptions-item label="品牌">{{ detail.brand || '-' }}</el-descriptions-item>
            <el-descriptions-item label="型号">{{ detail.model || '-' }}</el-descriptions-item>
            <el-descriptions-item label="颜色">{{ detail.color || '-' }}</el-descriptions-item>
            <el-descriptions-item label="尺寸">{{ detail.size || '-' }}</el-descriptions-item>
            <el-descriptions-item label="购入时间">{{ detail.purchaseTime || '-' }}</el-descriptions-item>
            <el-descriptions-item label="购入地点">{{ detail.purchaseLocation || '-' }}</el-descriptions-item>
            <el-descriptions-item label="可议价">{{ detail.isNegotiable ? '是' : '否' }}</el-descriptions-item>
            <el-descriptions-item label="发票 / 保修">
              {{ detail.includeInvoice ? '有发票' : '无发票' }}
              ·
              {{ detail.includeWarranty ? '有保修' : '无保修' }}
            </el-descriptions-item>
            <el-descriptions-item label="卖家电话" :span="2">{{ detail.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="卖家邮箱" :span="2">{{ detail.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="交易说明" :span="2">{{ detail.tradeNotes || '-' }}</el-descriptions-item>
            <el-descriptions-item label="商品描述" :span="2">
              <div class="desc-text">{{ detail.description || '暂无描述' }}</div>
            </el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detail?.status === 1"
          type="warning"
          @click="toggleStatus(detail.id, 0).then(() => { detailVisible = false })"
        >
          下架
        </el-button>
        <el-button
          v-if="detail?.status === 0"
          type="success"
          @click="toggleStatus(detail.id, 1).then(() => { detailVisible = false })"
        >
          上架
        </el-button>
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
import UserAvatar from '../../components/UserAvatar.vue'
import { fmtDateTime, productCoverSrc, resolveMediaUrl } from '../../utils/media.js'

const products = ref([])
const keyword = ref('')
const statusFilter = ref(null)
const page = ref(1)
const size = ref(16)
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)
const activeIdx = ref(0)

const imageList = computed(() => {
  const p = detail.value
  if (!p) return []
  let images = p.images
  if (typeof images === 'string') {
    try {
      images = JSON.parse(images)
    } catch {
      images = [images]
    }
  }
  if (Array.isArray(images) && images.length) {
    return images
      .map((item) => resolveMediaUrl(typeof item === 'string' ? item : item?.url || item?.path || ''))
      .filter(Boolean)
  }
  const cover = productCoverSrc(p)
  return cover ? [cover] : []
})

const activeImage = computed(() => imageList.value[activeIdx.value] || '')

onMounted(load)

function coverOf(row) {
  return productCoverSrc(row)
}

async function load() {
  loading.value = true
  try {
    const res = await http.get('/admin/products', {
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
      status: statusFilter.value != null ? statusFilter.value : undefined,
    })
    if (res.code === 200 && res.data) {
      products.value = res.data.records || []
      total.value = res.data.total || 0
      if (products.value.length === 0 && total.value > 0 && page.value > 1) {
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
  activeIdx.value = 0
  try {
    const res = await http.get(`/admin/products/${row.id}`)
    if (res.code === 200 && res.data) {
      detail.value = res.data.product || res.data
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

async function toggleStatus(id, status) {
  try {
    await http.patch(`/admin/products/${id}/status?status=${status}`)
    ElMessage.success('已更新')
    load()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function doDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' })
    await http.del(`/admin/products/${id}`)
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
  width: 52px;
  height: 52px;
  border-radius: 8px;
  overflow: hidden;
  background: #f1f5f9;
  flex-shrink: 0;
  display: grid;
  place-items: center;
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
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.prod-text .sub {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
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
  min-height: 200px;
}
.detail-top {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 18px;
  margin-bottom: 16px;
}
.gallery .main-img,
.main-empty {
  width: 100%;
  height: 220px;
  border-radius: 10px;
  object-fit: cover;
  background: #f1f5f9;
}
.main-empty {
  display: grid;
  place-items: center;
  color: #94a3b8;
  font-size: 13px;
}
.thumbs {
  display: flex;
  gap: 6px;
  margin-top: 8px;
  flex-wrap: wrap;
}
.thumb-btn {
  width: 44px;
  height: 44px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 6px;
  overflow: hidden;
  cursor: pointer;
  background: #f1f5f9;
}
.thumb-btn.active {
  border-color: #e1251b;
}
.thumb-btn img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.info h3 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #1f2937;
  line-height: 1.35;
}
.price-line {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 10px;
}
.price-line .price {
  font-size: 22px;
}
.origin {
  font-size: 13px;
  color: #94a3b8;
  text-decoration: line-through;
}
.chips {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.seller {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  background: #f8fafc;
  border-radius: 10px;
  margin-bottom: 10px;
}
.seller-name {
  font-weight: 600;
  font-size: 13px;
  color: #1f2937;
}
.seller-sub {
  font-size: 12px;
  color: #94a3b8;
}
.meta {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}
.desc-block {
  margin-top: 4px;
}
.desc-text {
  white-space: pre-wrap;
  line-height: 1.55;
  color: #334155;
}
@media (max-width: 720px) {
  .detail-top {
    grid-template-columns: 1fr;
  }
}
</style>
