<template>
  <div class="mall-shell">
    <!-- 顶部工具条 -->
    <div class="util-bar">
      <div class="mall-container util-inner">
        <span class="util-left">校园闲置 · 同校交易更安心</span>
        <div class="util-right">
          <template v-if="userStore.isLoggedIn">
            <span class="util-hi">Hi，{{ displayName }}</span>
            <router-link to="/profile">个人中心</router-link>
            <a href="javascript:;" @click.prevent="logout">退出</a>
          </template>
          <template v-else>
            <router-link to="/login" class="link-red">你好，请登录</router-link>
            <router-link to="/register">免费注册</router-link>
          </template>
          <router-link to="/order" class="msg">
            我的订单
            <em v-if="pendingSellerOrders">{{ pendingAcceptLabel }}</em>
          </router-link>
          <router-link to="/favorite">我的收藏</router-link>
<!--          <router-link to="/message" class="msg">-->
<!--            消息-->
<!--            <em v-if="appStore.unreadCount">{{ appStore.unreadCount > 99 ? '99+' : appStore.unreadCount }}</em>-->
<!--          </router-link>-->
          <router-link to="/admin/login" class="util-muted">管理后台</router-link>
        </div>
      </div>
    </div>

    <!-- 搜索头 -->
    <header class="mall-header">
      <div class="mall-container header-inner">
        <router-link to="/dashboard" class="brand">
          <span class="brand-mark">CM</span>
          <div class="brand-text">
            <strong>CampusMarket</strong>
            <small>校园二手商城</small>
          </div>
        </router-link>

        <div class="search-box">
          <el-input
            v-model="keyword"
            size="large"
            clearable
            placeholder="搜教材 / 数码 / 生活用品…"
            class="search-input"
            @keyup.enter="goSearch"
          />
          <button class="search-btn" type="button" @click="goSearch">搜索</button>
        </div>

        <div class="header-actions">
          <router-link to="/product/publish" class="action-pill primary">
            <el-icon><Plus /></el-icon>
            发布闲置
          </router-link>
          <router-link to="/product/my" class="action-pill">
            <el-icon><Box /></el-icon>
            我的发布
          </router-link>
        </div>
      </div>
    </header>

    <!-- 分类导航：首页推荐优先，分类浏览；全部商品在首页楼层入口 -->
    <nav class="cat-nav">
      <div class="mall-container cat-inner">
        <router-link
          to="/dashboard"
          class="cat-item hot"
          :class="{ active: route.path === '/dashboard' || route.path === '/' }"
        >
          首页推荐
        </router-link>
        <router-link
          v-for="c in categories.slice(0, 10)"
          :key="c.id"
          :to="{ path: '/product/list', query: { categoryId: c.id } }"
          class="cat-item"
          :class="{ active: route.path.startsWith('/product/list') && String(route.query.categoryId) === String(c.id) }"
        >
          {{ c.name }}
        </router-link>
      </div>
    </nav>

    <main ref="mainRef" class="mall-main">
      <div class="mall-container">
        <router-view />
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { http } from '../api/index.js'
import { useUserStore } from '../stores/user'
import { useAppStore } from '../stores/app'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
const keyword = ref('')
const categories = ref([])
const pendingSellerOrders = ref(0)
const mainRef = ref(null)

const displayName = computed(
  () => userStore.userInfo?.nickname || userStore.userInfo?.username || '同学'
)
const pendingAcceptLabel = computed(() =>
  pendingSellerOrders.value > 99 ? '99+' : String(pendingSellerOrders.value)
)

watch(
  () => route.query.keyword,
  (kw) => {
    if (typeof kw === 'string') keyword.value = kw
  },
  { immediate: true }
)

watch(
  () => route.fullPath,
  () => {
    mainRef.value?.scrollTo({ top: 0, behavior: 'instant' })
  }
)

onMounted(async () => {
  try {
    const res = await http.get('/categories')
    if (res.code === 200) categories.value = res.data || []
  } catch {
    /* ignore */
  }
  if (!userStore.isLoggedIn) return
  try {
    const res = await http.get('/messages/unread/total')
    if (res.code === 200 && res.data) {
      appStore.setUnreadCount(res.data.totalUnread || 0)
    }
  } catch {
    /* ignore */
  }
  try {
    // 卖家待接单：status=1 且当前用户为卖家
    const oRes = await http.get('/orders/seller/pending')
    if (oRes.code === 200 && Array.isArray(oRes.data)) {
      pendingSellerOrders.value = oRes.data.filter((o) => o.status === 1).length
    }
  } catch {
    /* ignore */
  }
})

function goSearch() {
  const q = { keyword: keyword.value || undefined }
  if (route.query.categoryId) q.categoryId = route.query.categoryId
  router.push({ path: '/product/list', query: q })
}

function logout() {
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.mall-shell {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f4f4f4;
  overflow: hidden;
}

.util-bar {
  background: #e3e4e5;
  font-size: 12px;
  color: #999;
  height: 30px;
  line-height: 30px;
}
.util-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.util-right {
  display: flex;
  align-items: center;
  gap: 14px;
}
.util-right a:hover,
.link-red { color: var(--jd-red); }
.util-hi { color: #666; }
.util-muted { color: #bbb; }
.msg { position: relative; }
.msg em {
  position: absolute;
  top: -6px;
  right: -14px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  border-radius: 8px;
  background: var(--jd-red);
  color: #fff;
  font-size: 10px;
  font-style: normal;
  line-height: 16px;
  text-align: center;
}

.mall-header {
  background: #fff;
  padding: 18px 0 14px;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.04);
}
.header-inner {
  display: flex;
  align-items: center;
  gap: 28px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}
.brand-mark {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #e1251b, #ff6600);
  color: #fff;
  font-weight: 900;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  letter-spacing: -1px;
}
.brand-text strong {
  display: block;
  font-size: 20px;
  font-weight: 900;
  color: #e1251b;
  line-height: 1.1;
}
.brand-text small {
  color: #999;
  font-size: 12px;
}

.search-box {
  flex: 1;
  display: flex;
  max-width: 640px;
  border: 2px solid var(--jd-red);
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
}
.search-box :deep(.el-input__wrapper) {
  box-shadow: none !important;
  border-radius: 0;
  padding-left: 14px;
}
.search-btn {
  width: 88px;
  background: var(--jd-red);
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  flex-shrink: 0;
}
.search-btn:hover { background: var(--jd-red-hover); }

.header-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}
.action-pill {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  height: 40px;
  padding: 0 14px;
  border: 1px solid #eee;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  background: #fff;
}
.action-pill:hover { border-color: #fcc; color: var(--jd-red); }
.action-pill.primary {
  background: var(--jd-red);
  border-color: var(--jd-red);
  color: #fff;
}
.action-pill.primary:hover { background: var(--jd-red-hover); }

.cat-nav {
  background: #fff;
  border-bottom: 2px solid var(--jd-red);
}
.cat-inner {
  display: flex;
  align-items: stretch;
  gap: 4px;
  overflow-x: auto;
  height: 40px;
}
.cat-item {
  flex-shrink: 0;
  padding: 0 16px;
  line-height: 40px;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}
.cat-item:hover,
.cat-item.active { color: var(--jd-red); }
.cat-item.hot { font-weight: 700; }
.cat-item.hot:not(.active) { color: var(--jd-orange); }

.mall-main {
  flex: 1;
  padding: 16px 0 40px;
  overflow-y: auto;
}

.mall-footer {
  background: #fff;
  border-top: 1px solid #eee;
  padding: 28px 0;
  text-align: center;
  color: #999;
  font-size: 13px;
}
.mall-footer .sub { margin-top: 6px; font-size: 12px; color: #bbb; }

@media (max-width: 900px) {
  .header-inner { flex-wrap: wrap; }
  .search-box { order: 3; max-width: none; width: 100%; }
  .header-actions { margin-left: auto; }
  .util-left { display: none; }
}
</style>
