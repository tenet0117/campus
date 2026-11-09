<template>
  <div class="market-page">
    <div class="filter-bar">
      <div class="crumbs">
        <router-link to="/dashboard">首页</router-link>
        <span>/</span>
        <strong>{{ currentCatName }}</strong>
      </div>
      <div class="filters">
        <button
          class="chip"
          :class="{ on: !categoryId }"
          @click="pickCategory(null)"
        >
          全部
        </button>
        <button
          v-for="c in categories"
          :key="c.id"
          class="chip"
          :class="{ on: categoryId === c.id }"
          @click="pickCategory(c.id)"
        >
          {{ c.name }}
        </button>
      </div>
      <div class="result-meta" v-if="!loading">
        共 <em>{{ total }}</em> 件商品
        <span v-if="keyword">，关键词「{{ keyword }}」</span>
      </div>
    </div>

    <div v-if="loading" class="spinner" />
    <template v-else-if="products.length">
      <div class="product-grid">
        <ProductCard v-for="p in products" :key="p.id" :product="p" />
      </div>
      <PagePagination
        v-if="total > pageSize"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @change="onPageChange"
      />
    </template>
    <EmptyState v-else description="没有找到相关闲置，换个词试试或去发布吧" />
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../../api/index.js'
import ProductCard from '../../components/ProductCard.vue'
import PagePagination from '../../components/PagePagination.vue'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const products = ref([])
const categories = ref([])
const keyword = ref('')
const categoryId = ref(null)
const page = ref(1)
const pageSize = 20
const total = ref(0)
const loading = ref(false)

const currentCatName = computed(() => {
  if (!categoryId.value) return keyword.value ? '搜索结果' : '全部商品'
  return categories.value.find((c) => c.id === categoryId.value)?.name || '商品'
})

function syncFromRoute() {
  keyword.value = typeof route.query.keyword === 'string' ? route.query.keyword : ''
  const cid = route.query.categoryId
  categoryId.value = cid != null && cid !== '' ? Number(cid) : null
  page.value = 1
}

function pickCategory(id) {
  const q = { ...route.query }
  if (id == null) delete q.categoryId
  else q.categoryId = id
  router.push({ path: '/product/list', query: q })
}

async function load() {
  loading.value = true
  try {
    const params = { page: page.value, size: pageSize }
    if (keyword.value) params.keyword = keyword.value
    if (categoryId.value != null) params.categoryId = categoryId.value
    const res =
      keyword.value || categoryId.value != null
        ? await http.get('/products/search', params)
        : await http.get('/products', params)
    if (res.code === 200) {
      const data = res.data || {}
      products.value = data.records || data.content || (Array.isArray(data) ? data : [])
      total.value = data.total ?? data.totalElements ?? products.value.length
    } else {
      ElMessage.error(res.message || '加载失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

function onPageChange(p) {
  page.value = p || 1
  load()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

watch(
  () => [route.query.keyword, route.query.categoryId],
  () => {
    syncFromRoute()
    load()
  }
)

onMounted(async () => {
  try {
    const res = await http.get('/categories')
    if (res.code === 200) categories.value = res.data || []
  } catch {
    /* ignore */
  }
  syncFromRoute()
  load()
})
</script>

<style scoped>
.market-page {
  background: #fff;
  border-radius: 8px;
  padding: 16px 18px 24px;
}
.filter-bar { margin-bottom: 16px; }
.crumbs {
  font-size: 13px;
  color: #999;
  margin-bottom: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}
.crumbs strong { color: #333; }
.crumbs a:hover { color: #e1251b; }
.filters {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}
.chip {
  padding: 4px 14px;
  border-radius: 14px;
  border: 1px solid #eee;
  background: #fafafa;
  font-size: 13px;
  color: #666;
}
.chip:hover { border-color: #fcc; color: #e1251b; }
.chip.on {
  background: #fff1f0;
  border-color: #e1251b;
  color: #e1251b;
  font-weight: 600;
}
.result-meta {
  font-size: 13px;
  color: #999;
}
.result-meta em {
  color: #e1251b;
  font-style: normal;
  font-weight: 700;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}
@media (max-width: 1100px) {
  .product-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 768px) {
  .product-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
