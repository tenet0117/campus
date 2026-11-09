<template>
  <div class="admin-login">
    <el-card class="admin-login-card" shadow="always">
      <h1>管理后台</h1>
      <p class="subtitle">校园二手交易平台</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent="handleLogin">
        <el-form-item label="管理员账号" prop="username">
          <el-input v-model="form.username" placeholder="请输入管理员账号" size="large" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
          管理员登录
        </el-button>
      </el-form>
      <p class="hint">测试账号：admin / admin123</p>
      <router-link class="back-link" to="/login">‹ 返回用户登录</router-link>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../../api/index.js'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const form = reactive({ username: 'admin', password: 'admin123' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await http.post('/admin/login', form)
    if (res.code === 200 && res.data) {
      const info = res.data.adminInfo || res.data
      // Prefer backend role; admin login endpoint implies admin when role omitted
      userStore.login({
        ...info,
        token: res.data.token,
        role: info.role || res.data.role || 'admin',
      })
      ElMessage.success('登录成功')
      router.push('/admin/dashboard')
    } else {
      ElMessage.error(res.message || '登录失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a0505 0%, #4a0a0a 45%, #e1251b 100%);
  padding: 20px;
}
.admin-login-card {
  width: 100%;
  max-width: 400px;
  padding: 24px 16px;
  border-radius: 12px;
}
h1 { font-size: 24px; font-weight: 700; text-align: center; color: #222; }
.subtitle { text-align: center; color: #8c8c8c; font-size: 14px; margin-bottom: 24px; }
.submit-btn { width: 100%; margin-top: 8px; }
.hint {
  margin-top: 16px;
  text-align: center;
  font-size: 13px;
  color: #888;
  background: #fff1f0;
  padding: 10px;
  border-radius: 8px;
}
.back-link {
  display: block;
  text-align: center;
  margin-top: 14px;
  font-size: 13px;
  color: #e1251b;
  font-weight: 600;
}
</style>
