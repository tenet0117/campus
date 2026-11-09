<template>
  <el-container class="admin-layout">
    <el-aside width="232px" class="admin-sidebar">
      <div class="admin-brand">
        <span class="mark">CM</span>
        <div>
          <strong>CampusMarket</strong>
          <small>管理后台</small>
        </div>
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="transparent"
        text-color="rgba(255,255,255,.72)"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/admin/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/products">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/orders">
          <el-icon><List /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/categories">
          <el-icon><Folder /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
      </el-menu>
      <div class="sidebar-foot">
        <router-link to="/dashboard" class="back-mall">返回商城</router-link>
        <el-button class="logout" size="small" @click="logout">退出登录</el-button>
      </div>
    </el-aside>
    <el-container>
      <el-header class="admin-topbar" height="56px">
        <div class="crumb">{{ pageTitle }}</div>
        <div class="top-right">
          <UserAvatar
            :src="userStore.userInfo?.avatar"
            :name="displayName"
            :size="32"
          />
          <div class="admin-meta">
            <span class="role">管理员</span>
            <span class="name">{{ displayName }}</span>
          </div>
        </div>
      </el-header>
      <el-main ref="mainRef" class="admin-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../stores/user'
import UserAvatar from '../../components/UserAvatar.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeMenu = computed(() => route.path)
const mainRef = ref(null)
const displayName = computed(
  () => userStore.userInfo?.nickname || userStore.userInfo?.username || 'admin'
)

const titleMap = {
  '/admin/dashboard': '仪表盘',
  '/admin/users': '用户管理',
  '/admin/products': '商品管理',
  '/admin/orders': '订单管理',
  '/admin/categories': '分类管理',
}
const pageTitle = computed(() => titleMap[route.path] || '管理后台')

function logout() {
  userStore.logout()
  router.replace('/admin/login')
}

watch(
  () => route.fullPath,
  () => {
    mainRef.value?.$el?.scrollTo({ top: 0, behavior: 'instant' })
  }
)
</script>

<style scoped>
.admin-layout {
  height: 100vh;
  overflow: hidden;
  background: #f4f4f5;
}
.admin-sidebar {
  background: #1f1f1f;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #2a2a2a;
}
.admin-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 18px 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.admin-brand .mark {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  background: #e1251b;
  color: #fff;
  font-weight: 900;
  display: grid;
  place-items: center;
  font-size: 13px;
}
.admin-brand strong {
  display: block;
  color: #fff;
  font-size: 14px;
  line-height: 1.2;
}
.admin-brand small {
  color: rgba(255, 255, 255, 0.45);
  font-size: 12px;
}
.admin-sidebar :deep(.el-menu) {
  border-right: none;
  flex: 1;
  padding: 8px 8px 0;
}
.admin-sidebar :deep(.el-menu-item) {
  border-radius: 8px;
  margin-bottom: 4px;
  height: 44px;
}
.admin-sidebar :deep(.el-menu-item.is-active) {
  background: #e1251b !important;
}
.sidebar-foot {
  padding: 12px 14px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.back-mall {
  text-align: center;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.55);
  padding: 8px;
}
.back-mall:hover {
  color: #fff;
}
.logout {
  width: 100%;
  background: rgba(255, 255, 255, 0.08);
  border: none;
  color: #fff;
}
.admin-topbar {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #eee;
  padding: 0 24px;
}
.crumb {
  font-size: 16px;
  font-weight: 700;
  color: #222;
}
.top-right {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
}
.admin-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  line-height: 1.2;
}
.role {
  background: #fff1f0;
  color: #e1251b;
  padding: 1px 8px;
  border-radius: 999px;
  font-weight: 600;
  font-size: 11px;
  width: fit-content;
}
.name {
  color: #475569;
  font-weight: 600;
}
.admin-content {
  padding: 12px 16px 24px;
  background: #f1f5f9;
  overflow-y: auto;
}
</style>
