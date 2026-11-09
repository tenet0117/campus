import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { guest: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { guest: true },
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/Login.vue'),
    meta: { guest: true, adminGuest: true },
  },
  {
    path: '/admin',
    component: () => import('../views/admin/Layout.vue'),
    meta: { auth: true, roles: ['admin'] },
    children: [
      { path: '', redirect: { name: 'AdminDashboard' } },
      { path: 'dashboard', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue') },
      { path: 'products', name: 'AdminProducts', component: () => import('../views/admin/Products.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/Orders.vue') },
      { path: 'categories', name: 'AdminCategories', component: () => import('../views/admin/Categories.vue') },
    ],
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    meta: { auth: true },
    children: [
      { path: '', redirect: '/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/dashboard/index.vue') },
      {
        path: 'product/list',
        name: 'ProductList',
        component: () => import('../views/product/list.vue'),
        meta: { public: true, auth: false },
      },
      { path: 'product/publish', name: 'ProductPublish', component: () => import('../views/product/publish.vue') },
      { path: 'product/my', name: 'MyProducts', component: () => import('../views/product/my.vue') },
      { path: 'product/edit/:id', name: 'ProductEdit', component: () => import('../views/product/edit.vue') },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('../views/product/detail.vue'),
        meta: { public: true, auth: false },
      },
      { path: 'order', name: 'Order', component: () => import('../views/order/index.vue') },
      { path: 'order/checkout/:productId', name: 'OrderCheckout', component: () => import('../views/order/checkout.vue') },
      { path: 'message', name: 'Message', component: () => import('../views/message/index.vue') },
      { path: 'favorite', name: 'Favorite', component: () => import('../views/favorite/index.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/profile/index.vue') },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0, left: 0 }
  },
})

router.beforeEach((to) => {
  const user = useUserStore()

  // Guest-only pages
  if (to.meta.guest) {
    if (to.meta.adminGuest) {
      if (user.isLoggedIn && user.isAdmin) return { path: '/admin/dashboard' }
      return true
    }
    if (user.isLoggedIn) return { path: '/dashboard' }
    return true
  }

  const isPublic = to.matched.some((r) => r.meta.public === true)
  const requiresAuth = to.matched.some((r) => r.meta.auth === true) && !isPublic

  if (requiresAuth && !user.isLoggedIn) {
    return { path: '/login', replace: true, query: { redirect: to.fullPath } }
  }

  const rolesRecord = to.matched.find((r) => Array.isArray(r.meta.roles) && r.meta.roles.length)
  if (rolesRecord) {
    const roles = rolesRecord.meta.roles
    const role = user.userInfo?.role
    if (!user.isLoggedIn || !roles.includes(role)) {
      return { path: '/login', replace: true }
    }
  }

  return true
})

export default router
