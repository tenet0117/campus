<template>
  <div class="login-page">
    <div class="login-card">
      <div class="card-top">
        <span class="logo">CM</span>
        <h1>CampusMarket</h1>
        <p>校园二手交易平台</p>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
        <el-form-item prop="account">
          <el-input
            v-model="form.account"
            placeholder="用户名 / 邮箱"
            size="large"
            clearable
            autofocus
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            size="large"
            show-password
            :prefix-icon="Lock"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" native-type="submit">
          登 录
        </el-button>
      </el-form>

      <div class="card-foot">
        <p class="demo-account"><strong>体验账号</strong>&nbsp; dengshao / 123456</p>
        <router-link to="/admin/login">管理后台登录 ›</router-link>
        <router-link to="/register" class="register-link">还没有账号？立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { http } from '../api/index.js'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ account: 'dengshao', password: '123456' })
const rules = {
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const res = await http.post('/auth/login', { account: form.account, password: form.password })
    if (res.code === 200 && res.data) {
      userStore.login(res.data)
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/dashboard'
      router.push(typeof redirect === 'string' ? redirect : '/dashboard')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch {
    ElMessage.error('网络错误，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #1a0505 0%, #4a0a0a 40%, #e1251b 100%);
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-page::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,102,0,.2) 0%, transparent 70%);
  top: -200px;
  right: -100px;
  pointer-events: none;
}
.login-page::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,.08) 0%, transparent 70%);
  bottom: -80px;
  left: -60px;
  pointer-events: none;
}

/* ── 卡片 ── */
.login-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 24px;
  padding: 44px 40px 36px;
  box-shadow:
    0 1px 3px rgba(0,0,0,.08),
    0 8px 40px rgba(0,0,0,.25);
}

.card-top {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #e1251b, #ff6600);
  color: #fff;
  font-size: 22px;
  font-weight: 900;
  box-shadow: 0 8px 24px rgba(225,37,27,.25);
  margin-bottom: 16px;
}

.card-top h1 {
  font-size: 24px;
  font-weight: 800;
  color: #1f2937;
  margin: 0 0 6px;
  letter-spacing: -.5px;
}

.card-top p {
  font-size: 14px;
  color: #9ca3af;
  margin: 0;
}

/* ── 表单 ── */
.submit-btn {
  width: 100%;
  height: 46px;
  border-radius: 10px;
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 1px;
}

.card-foot {
  margin-top: 24px;
  text-align: center;
  font-size: 13px;
  color: #9ca3af;
  line-height: 2;
}

.card-foot .demo-account {
  margin: 0;
}

.card-foot .demo-account strong {
  color: #6b7280;
}

.card-foot a {
  display: block;
  color: #9ca3af;
  text-decoration: none;
}

.card-foot a:hover {
  color: #e1251b;
}

.card-foot .register-link {
  margin-top: 4px;
  color: #e1251b;
  font-weight: 600;
  font-size: 14px;
}

.card-foot .register-link:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .login-card {
    padding: 36px 28px 30px;
    border-radius: 20px;
  }
}
</style>
