<template>
  <div class="profile-page">
    <!-- 头部资料卡 -->
    <section class="hero-card">
      <div class="hero-gradient" />
      <div class="hero-content">
        <label class="avatar-wrap" :class="{ uploading: avatarUploading }" title="点击更换头像">
          <img v-if="avatarSrc" :src="avatarSrc" alt="头像" />
          <span v-else class="avatar-letter">{{ avatarLetter }}</span>
          <span class="avatar-overlay">
            <span>{{ avatarUploading ? '上传中...' : '更换头像' }}</span>
          </span>
          <input
            type="file"
            accept="image/jpeg,image/png,image/webp,image/gif"
            hidden
            :disabled="avatarUploading"
            @change="handleAvatarChange"
          />
        </label>
        <h1 class="hero-name">{{ displayName }}</h1>
        <p class="hero-email">{{ user.email || '未绑定邮箱' }}</p>
        <div class="hero-tags">
          <span v-if="user.phone" class="tag">{{ user.phone }}</span>
          <span class="tag sub">ID {{ user.userId || user.id || '—' }}</span>
        </div>
      </div>

      <div class="stats-row">
        <div class="stat">
          <span class="stat-num">{{ stats.publish }}</span>
          <span class="stat-label">发布</span>
        </div>
        <div class="stat-divider" />
        <div class="stat">
          <span class="stat-num">{{ stats.favorite }}</span>
          <span class="stat-label">收藏</span>
        </div>
        <div class="stat-divider" />
        <div class="stat">
          <span class="stat-num">{{ stats.order }}</span>
          <span class="stat-label">订单</span>
        </div>
      </div>
    </section>

    <!-- 快捷入口列表 -->
    <section class="menu-card">
      <router-link v-for="(e, i) in entries" :key="e.to" :to="e.to" class="menu-item" :class="{ last: i === entries.length - 1 }">
        <span class="menu-icon" :style="{ background: e.bg, color: e.color }">{{ e.icon }}</span>
        <span class="menu-label">{{ e.label }}</span>
        <span class="menu-badge" v-if="e.badge">{{ e.badge }}</span>
        <span class="menu-arrow">›</span>
      </router-link>
    </section>

    <!-- 资料编辑 -->
    <section class="form-card">
      <div class="form-head">
        <span class="form-head-icon">✏️</span>
        <span>账户资料</span>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @submit.prevent
      >
        <div class="form-fields">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" size="large" maxlength="30" show-word-limit clearable placeholder="怎么称呼你" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" size="large" maxlength="11" clearable placeholder="便于同校面交联系" />
          </el-form-item>
          <el-form-item label="登录邮箱">
            <el-input :model-value="user.email" size="large" disabled />
          </el-form-item>
          <el-form-item label="新密码" prop="password">
            <el-input v-model="form.password" type="password" size="large" show-password placeholder="不修改请留空，至少 6 位" clearable />
          </el-form-item>
        </div>

        <el-button type="primary" size="large" class="save-btn" :loading="loading" @click="handleUpdate">
          保存修改
        </el-button>
      </el-form>
    </section>

    <button type="button" class="logout-btn" @click="handleLogout">退出登录</button>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../../api/index.js'
import { useUserStore } from '../../stores/user'
import { resolveAvatarUrl } from '../../utils/media.js'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const avatarUploading = ref(false)
const user = ref({})

const form = reactive({
  nickname: '',
  phone: '',
  password: '',
})

const rules = {
  nickname: [{ required: true, message: '请填写昵称', trigger: 'blur' }],
  phone: [{ pattern: /^1\d{10}$/, message: '请输入有效手机号', trigger: 'blur' }],
  password: [{ min: 6, message: '密码至少 6 位', trigger: 'blur' }],
}

const displayName = computed(() => user.value.nickname || user.value.username || '同学')
const avatarLetter = computed(() => (displayName.value || '?')[0].toUpperCase())
const avatarSrc = computed(() => resolveAvatarUrl(user.value.avatar))

const stats = computed(() => ({
  publish: user.value.productCount ?? '—',
  favorite: user.value.favoriteCount ?? '—',
  order: user.value.orderCount ?? '—',
}))

const entries = [
  { label: '我的订单', icon: '📦', to: '/order', bg: '#fff0ed', color: '#e1251b' },
  { label: '我的收藏', icon: '⭐', to: '/favorite', bg: '#fffbeb', color: '#d97706' },
  { label: '我的发布', icon: '🏷️', to: '/product/my', bg: '#eff6ff', color: '#2563eb' },
  { label: '站内消息', icon: '💬', to: '/message', bg: '#f0fdf4', color: '#16a34a' },
  { label: '发布闲置', icon: '📸', to: '/product/publish', bg: '#fdf2f8', color: '#db2777' },
  { label: '逛商城', icon: '🏠', to: '/dashboard', bg: '#f3f4f6', color: '#6b7280' },
]

function applyUser(u, { keepPassword } = {}) {
  user.value = u || {}
  form.nickname = u?.nickname || u?.username || ''
  form.phone = u?.phone || ''
  if (!keepPassword) form.password = ''
}

function persistUser(partial, opts) {
  const next = { ...userStore.userInfo, ...user.value, ...partial }
  applyUser(next, opts)
  userStore.setSession({ ...next, token: userStore.token })
}

onMounted(async () => {
  applyUser(userStore.userInfo || {})
  try {
    const res = await http.get('/auth/profile')
    if (res.code === 200 && res.data) {
      const data = res.data
      if (data.id != null && !data.userId) data.userId = data.id
      persistUser(data)
    }
  } catch {
    /* keep local */
  }
})

async function handleAvatarChange(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  if (!file.type.startsWith('image/')) return ElMessage.warning('请选择图片文件')
  if (file.size > 5 * 1024 * 1024) return ElMessage.warning('头像请小于 5MB')

  avatarUploading.value = true
  try {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('category', 'avatars')
    const up = await http.upload('/files/upload/image', fd)
    if (up.code !== 200 || !up.data) {
      ElMessage.error(up.message || '上传失败')
      return
    }
    const avatar = up.data.url || up.data.filePath
    const res = await http.put('/auth/profile', { avatar })
    if (res.code === 200) {
      persistUser({ avatar: res.data?.avatar || avatar }, { keepPassword: true })
      ElMessage.success('头像已更新')
    } else {
      ElMessage.error(res.message || '保存头像失败')
    }
  } catch {
    ElMessage.error('头像上传失败')
  } finally {
    avatarUploading.value = false
  }
}

async function handleUpdate() {
  try { await formRef.value.validate() } catch { return }

  loading.value = true
  try {
    const body = { nickname: form.nickname.trim(), phone: form.phone || null }
    if (form.password) body.password = form.password

    const res = await http.put('/auth/profile', body)
    if (res.code === 200) {
      persistUser({ ...body, ...(res.data || {}) })
      form.password = ''
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确认退出当前账号？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch {
    return
  }
  userStore.logout()
  router.replace('/login')
}
</script>

<style scoped>
.profile-page {
  max-width: 600px;
  margin: 0 auto 60px;
  padding: 0 12px;
}

/* ==================== 头部资料卡 ==================== */
.hero-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.04);
}

.hero-gradient {
  height: 100px;
  background: linear-gradient(135deg, #e1251b 0%, #ff5a3c 40%, #ff9a5a 100%);
}

.hero-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px 20px;
  margin-top: -44px;
  position: relative;
}

/* 头像 */
.avatar-wrap {
  width: 88px;
  height: 88px;
  border-radius: 50%;
  background: #fff;
  border: 4px solid #fff;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  display: grid;
  place-items: center;
  font-size: 32px;
  font-weight: 800;
  color: #e1251b;
  flex-shrink: 0;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  transition: transform 0.2s;
}
.avatar-wrap:hover {
  transform: scale(1.05);
}
.avatar-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-letter {
  line-height: 1;
}

.avatar-overlay {
  position: absolute;
  inset: 4px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: grid;
  place-items: center;
  opacity: 0;
  transition: opacity 0.2s;
  font-size: 12px;
  font-weight: 600;
}
.avatar-wrap:hover .avatar-overlay,
.avatar-wrap.uploading .avatar-overlay {
  opacity: 1;
}

.hero-name {
  margin: 12px 0 0;
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
}

.hero-email {
  margin: 4px 0 0;
  font-size: 13px;
  color: #9ca3af;
}

.hero-tags {
  margin-top: 10px;
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  justify-content: center;
}

.tag {
  font-size: 11px;
  padding: 2px 10px;
  border-radius: 999px;
  background: #fff1f0;
  color: #e1251b;
  font-weight: 500;
}
.tag.sub {
  background: #f3f4f6;
  color: #9ca3af;
}

/* 统计数据 */
.stats-row {
  display: flex;
  align-items: center;
  border-top: 1px solid #f3f4f6;
  margin: 0 24px;
  padding: 16px 0;
}

.stat {
  flex: 1;
  text-align: center;
}
.stat-num {
  display: block;
  font-size: 20px;
  font-weight: 800;
  color: #1f2937;
  line-height: 1.2;
}
.stat-label {
  display: block;
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

.stat-divider {
  width: 1px;
  height: 28px;
  background: #e5e7eb;
  flex-shrink: 0;
}

/* ==================== 菜单列表 ==================== */
.menu-card {
  background: #fff;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.04);
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 20px;
  text-decoration: none;
  border-bottom: 1px solid #f3f4f6;
  transition: background 0.15s;
}
.menu-item.last {
  border-bottom: none;
}
.menu-item:hover {
  background: #fafbfc;
}
.menu-item:active {
  background: #f3f4f6;
}

.menu-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  font-size: 18px;
  flex-shrink: 0;
}

.menu-label {
  flex: 1;
  font-size: 15px;
  color: #1f2937;
  font-weight: 500;
}

.menu-badge {
  font-size: 12px;
  color: #9ca3af;
  background: #f3f4f6;
  padding: 1px 8px;
  border-radius: 999px;
}

.menu-arrow {
  font-size: 20px;
  color: #d1d5db;
  font-weight: 300;
  line-height: 1;
}

/* ==================== 编辑表单 ==================== */
.form-card {
  background: #fff;
  border-radius: 16px;
  padding: 20px 22px 24px;
  margin-bottom: 12px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.04);
}

.form-head {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 18px;
  padding-bottom: 14px;
  border-bottom: 1px solid #f3f4f6;
}

.form-head-icon {
  font-size: 18px;
}

.form-fields {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-fields :deep(.el-form-item) {
  margin-bottom: 0;
}

.save-btn {
  width: 100%;
  margin-top: 20px;
  font-weight: 700;
  height: 46px;
  border-radius: 12px;
  font-size: 15px;
}

/* ==================== 退出登录 ==================== */
.logout-btn {
  display: block;
  width: 100%;
  margin-top: 4px;
  height: 48px;
  border-radius: 14px;
  border: none;
  background: #fff;
  color: #ef4444;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: all 0.2s;
}
.logout-btn:hover {
  background: #fef2f2;
  color: #dc2626;
}
.logout-btn:active {
  transform: scale(0.98);
}

/* ==================== 响应式 ==================== */
@media (max-width: 480px) {
  .profile-page {
    padding: 0;
  }
  .hero-card,
  .menu-card,
  .form-card,
  .logout-btn {
    border-radius: 0;
  }
  .hero-content {
    padding: 0 16px 16px;
  }
  .stats-row {
    margin: 0 16px;
  }
  .menu-item {
    padding: 14px 16px;
  }
  .form-card {
    padding: 16px;
  }
}
</style>
