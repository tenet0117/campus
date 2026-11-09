<template>
  <div class="register-page">
    <div class="register-card">
      <div class="card-top">
        <span class="logo">CM</span>
        <h1>CampusMarket</h1>
        <p>校园二手交易平台</p>
      </div>

      <h2 class="card-title">创建账户</h2>
      <p class="card-sub">加入 CampusMarket，开启校园交易</p>

      <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleRegister">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            size="large"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            type="email"
            placeholder="邮箱"
            size="large"
            clearable
            :prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="form.nickname"
            placeholder="昵称（选填）"
            size="large"
            clearable
            :prefix-icon="Avatar"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码（至少 6 位）"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" native-type="submit">
          创 建 账 户
        </el-button>
      </el-form>

      <div class="card-foot">
        <router-link to="/login">已有账号？返回登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Message, Avatar, Lock } from '@element-plus/icons-vue'
import { http } from '../api/index.js'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: '', email: '', nickname: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
}

async function handleRegister() {
  try { await formRef.value?.validate() } catch { return }
  loading.value = true
  try {
    const res = await http.post('/auth/register', {
      username: form.username,
      email: form.email,
      nickname: form.nickname || form.username,
      password: form.password,
    })
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch {
    ElMessage.error('网络错误，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  background: linear-gradient(135deg, #042f2e 0%, #0f766e 40%, #14b8a6 100%);
  position: relative;
  overflow: hidden;
}

.register-page::before {
  content: '';
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(255,255,255,.08) 0%, transparent 70%);
  top: -200px;
  right: -100px;
  pointer-events: none;
}
.register-page::after {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(20,184,166,.2) 0%, transparent 70%);
  bottom: -80px;
  left: -60px;
  pointer-events: none;
}

/* ── 卡片 ── */
.register-card {
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
  margin-bottom: 28px;
}

.logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #14b8a6, #0f766e);
  color: #fff;
  font-size: 22px;
  font-weight: 900;
  box-shadow: 0 8px 24px rgba(15,118,110,.3);
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

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
  margin: 0 0 4px;
}

.card-sub {
  font-size: 13px;
  color: #9ca3af;
  margin: 0 0 20px;
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
  font-size: 14px;
}

.card-foot a {
  color: #0f766e;
  font-weight: 600;
  text-decoration: none;
}

.card-foot a:hover {
  text-decoration: underline;
}

@media (max-width: 480px) {
  .register-card {
    padding: 36px 28px 30px;
    border-radius: 20px;
  }
}
</style>
