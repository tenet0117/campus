<template>
  <div class="mall-home">
    <!-- 主视觉区：分类 + Banner -->
    <section class="hero-row">
      <aside class="cat-panel">
        <h3>商品分类</h3>
        <ul>
          <li
            v-for="c in categories"
            :key="c.id"
            @click="goCategory(c.id)"
          >
            <span>{{ c.name }}</span>
            <em>›</em>
          </li>
        </ul>
      </aside>
      <div class="banner">
        <div class="banner-slide">
          <div class="banner-copy">
            <p class="eyebrow">CampusMarket</p>
            <h1>校园闲置<br />一站流转</h1>
            <p class="desc">同校交易 · 当面验货 · 放心出手</p>
            <router-link to="/product/list" class="banner-cta">去逛逛</router-link>
          </div>
          <div class="banner-art" aria-hidden="true" />
        </div>
      </div>
      <aside class="user-panel">
        <div class="user-card">
          <div class="avatar">
            <img v-if="avatarSrc" :src="avatarSrc" alt="" />
            <template v-else>{{ avatarLetter }}</template>
          </div>
          <p class="hello">{{ greet }}</p>
          <div class="user-actions" v-if="userStore.isLoggedIn">
            <router-link to="/order">订单</router-link>
            <router-link to="/favorite">收藏</router-link>
<!--            <router-link to="/message">消息</router-link>-->
          </div>
          <div class="user-actions" v-else>
            <router-link to="/login" class="login-btn">登录</router-link>
            <router-link to="/register">注册</router-link>
          </div>
        </div>
        <div class="promo-box">
          <h4>今日推荐</h4>
          <p>教材数码低价出，同宿舍可当面交易</p>
          <router-link to="/product/publish">立即发布闲置 ›</router-link>
        </div>
      </aside>
    </section>

    <!-- 快捷入口 -->
    <section class="quick-row">
      <router-link
        v-for="q in quickLinks"
        :key="q.to"
        :to="q.to"
        class="quick-item"
      >
        <span class="qi">{{ q.icon }}</span>
        <span>{{ q.label }}</span>
      </router-link>
    </section>

    <!-- 热门商品 -->
    <section class="floor">
      <div class="floor-head">
        <h2>热卖好物</h2>
        <router-link to="/product/list">更多 ›</router-link>
      </div>
      <div v-if="loading" class="spinner" />
      <div v-else class="product-grid">
        <ProductCard v-for="p in popular" :key="p.id" :product="p" />
      </div>
      <EmptyState v-if="!loading && !popular.length" description="暂无热门商品，去发布第一件闲置吧" />
    </section>

    <!-- 最新上架 -->
    <section class="floor">
      <div class="floor-head">
        <h2>最新上架</h2>
        <router-link to="/product/list">查看全部 ›</router-link>
      </div>
      <div class="product-grid">
        <ProductCard v-for="p in latest" :key="p.id" :product="p" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { http } from '../../api/index.js'
import { useUserStore } from '../../stores/user'
import ProductCard from '../../components/ProductCard.vue'
import EmptyState from '../../components/EmptyState.vue'
import { resolveAvatarUrl } from '../../utils/media.js'

const router = useRouter()
const userStore = useUserStore()
const categories = ref([])
const popular = ref([])
const latest = ref([])
const loading = ref(false)

const greet = computed(() => {
  if (!userStore.isLoggedIn) return '欢迎来到校园商城'
  const name = userStore.userInfo?.nickname || userStore.userInfo?.username || '同学'
  return `Hi，${name}`
})
const avatarLetter = computed(() => (greet.value.replace(/^Hi，/, '') || '?')[0])
const avatarSrc = computed(() => resolveAvatarUrl(userStore.userInfo?.avatar))

const quickLinks = [
  { label: '热卖好物', icon: '🔥', to: '/dashboard' },
  { label: '全部商品', icon: '🛒', to: '/product/list' },
  { label: '发布闲置', icon: '📸', to: '/product/publish' },
  { label: '我的订单', icon: '📦', to: '/order' },
  { label: '我的收藏', icon: '⭐', to: '/favorite' },
  { label: '站内消息', icon: '💬', to: '/message' },
]

function goCategory(id) {
  router.push({ path: '/product/list', query: { categoryId: id } })
}

onMounted(async () => {
  loading.value = true
  try {
    const [cRes, pRes, lRes] = await Promise.all([
      http.get('/categories'),
      http.get('/products/popular', { page: 0, size: 10 }),
      http.get('/products/latest', { page: 0, size: 10 }),
    ])
    if (cRes.code === 200) categories.value = (cRes.data || []).slice(0, 12)
    if (pRes.code === 200) {
      const d = pRes.data
      popular.value = d?.records || d?.content || (Array.isArray(d) ? d : [])
    }
    if (lRes.code === 200) {
      const d = lRes.data
      latest.value = d?.records || d?.content || (Array.isArray(d) ? d : [])
    }
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero-row {
  display: grid;
  grid-template-columns: 200px 1fr 210px;
  gap: 12px;
  margin-bottom: 16px;
}
.cat-panel, .user-panel, .banner {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
}
.cat-panel h3 {
  margin: 0;
  padding: 12px 16px;
  font-size: 14px;
  background: linear-gradient(90deg, #e1251b, #ff6600);
  color: #fff;
}
.cat-panel ul { list-style: none; padding: 6px 0; margin: 0; }
.cat-panel li {
  display: flex;
  justify-content: space-between;
  padding: 9px 16px;
  font-size: 13px;
  color: #333;
  cursor: pointer;
}
.cat-panel li:hover { background: #fff1f0; color: #e1251b; }
.cat-panel em { color: #ccc; font-style: normal; }

.banner-slide {
  position: relative;
  min-height: 280px;
  background: linear-gradient(120deg, #1a1a1a 0%, #3d0a0a 45%, #e1251b 100%);
  color: #fff;
  padding: 36px 40px;
  overflow: hidden;
}
.banner-copy { position: relative; z-index: 1; max-width: 360px; }
.eyebrow {
  font-size: 12px;
  letter-spacing: 2px;
  opacity: .75;
  margin-bottom: 10px;
}
.banner-copy h1 {
  font-size: 36px;
  font-weight: 900;
  line-height: 1.2;
  margin-bottom: 12px;
}
.desc { opacity: .85; margin-bottom: 20px; font-size: 14px; }
.banner-cta {
  display: inline-block;
  background: #fff;
  color: #e1251b;
  font-weight: 700;
  padding: 10px 22px;
  border-radius: 20px;
}
.banner-art {
  position: absolute;
  right: -40px;
  bottom: -40px;
  width: 260px;
  height: 260px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,.25), transparent 70%);
}

.user-card {
  padding: 20px 16px 14px;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}
.avatar {
  width: 52px;
  height: 52px;
  margin: 0 auto 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #e1251b, #ff6600);
  color: #fff;
  font-size: 20px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.hello { font-size: 14px; color: #333; margin-bottom: 12px; }
.user-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  font-size: 12px;
  color: #666;
}
.user-actions a:hover { color: #e1251b; }
.login-btn {
  background: #e1251b;
  color: #fff !important;
  padding: 4px 14px;
  border-radius: 12px;
}
.promo-box {
  padding: 14px 16px 18px;
  background: linear-gradient(180deg, #fff7f0, #fff);
}
.promo-box h4 { font-size: 14px; color: #e1251b; margin-bottom: 6px; }
.promo-box p { font-size: 12px; color: #888; margin-bottom: 8px; line-height: 1.5; }
.promo-box a { font-size: 12px; color: #e1251b; font-weight: 600; }

.quick-row {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  margin-bottom: 18px;
  background: #fff;
  border-radius: 8px;
  padding: 14px 8px;
}
.quick-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}
.quick-item:hover { color: #e1251b; }
.qi { font-size: 22px; }

.floor {
  background: #fff;
  border-radius: 8px;
  padding: 16px 16px 20px;
  margin-bottom: 16px;
}
.floor-head {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 14px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f5f5f5;
}
.floor-head h2 {
  font-size: 18px;
  font-weight: 800;
  color: #222;
  position: relative;
  padding-left: 10px;
}
.floor-head h2::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  background: #e1251b;
  border-radius: 2px;
}
.floor-head a { font-size: 13px; color: #999; }
.floor-head a:hover { color: #e1251b; }

.product-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 12px;
}

@media (max-width: 1100px) {
  .hero-row { grid-template-columns: 180px 1fr; }
  .user-panel { display: none; }
  .product-grid { grid-template-columns: repeat(4, 1fr); }
}
@media (max-width: 768px) {
  .hero-row { grid-template-columns: 1fr; }
  .cat-panel { display: none; }
  .quick-row { grid-template-columns: repeat(3, 1fr); }
  .product-grid { grid-template-columns: repeat(2, 1fr); }
  .banner-copy h1 { font-size: 28px; }
}
</style>
