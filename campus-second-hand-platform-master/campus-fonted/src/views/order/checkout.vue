<template>
  <div class="checkout-page">
    <div class="crumbs">
      <router-link to="/dashboard">首页</router-link>
      <span>/</span>
      <router-link v-if="product" :to="'/product/' + product.id">商品详情</router-link>
      <span>/</span>
      <strong>确认订单</strong>
    </div>

    <div v-if="loading" class="spinner" />
    <EmptyState v-else-if="!product" description="商品不存在或已下架" />
    <template v-else>
      <div class="layout">
        <div class="main">
          <section class="card">
            <h2>收货信息</h2>
            <el-form label-width="96px" class="form" @submit.prevent>
              <el-form-item label="交易方式" required>
                <el-radio-group v-model="form.tradeType">
                  <el-radio :value="1">校内面交</el-radio>
                  <el-radio :value="2">快递配送</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="收货人" required>
                <el-input v-model="form.receiverName" maxlength="50" placeholder="真实姓名或常用称呼" />
              </el-form-item>
              <el-form-item label="联系电话" required>
                <el-input v-model="form.receiverPhone" maxlength="20" placeholder="11 位手机号" />
              </el-form-item>
              <template v-if="form.tradeType === 1">
                <el-form-item label="校区" required>
                  <el-input v-model="form.campus" maxlength="50" placeholder="如：主校区 / 东校区" />
                </el-form-item>
                <el-form-item label="楼栋宿舍" required>
                  <el-input v-model="form.building" maxlength="100" placeholder="如：6 号楼 312" />
                </el-form-item>
                <el-form-item label="补充地点">
                  <el-input v-model="form.addressDetail" maxlength="200" placeholder="门口 / 快递柜等（选填）" />
                </el-form-item>
              </template>
              <template v-else>
                <el-form-item label="校区">
                  <el-input v-model="form.campus" maxlength="50" placeholder="选填" />
                </el-form-item>
                <el-form-item label="详细地址" required>
                  <el-input
                    v-model="form.addressDetail"
                    type="textarea"
                    :rows="2"
                    maxlength="200"
                    placeholder="省市区 + 街道门牌号"
                  />
                </el-form-item>
              </template>
              <el-form-item label="订单备注">
                <el-input
                  v-model="form.remark"
                  type="textarea"
                  :rows="2"
                  maxlength="500"
                  placeholder="方便面交时间、特殊要求等（选填）"
                />
              </el-form-item>
            </el-form>
          </section>
        </div>

        <aside class="side">
          <section class="card product-card">
            <h2>商品信息</h2>
            <div class="product-row">
              <img v-if="cover" :src="cover" alt="" />
              <div v-else class="cover-empty">无图</div>
              <div>
                <div class="title">{{ product.title }}</div>
                <div class="price">¥{{ product.price }}</div>
              </div>
            </div>
            <div class="pay-row">
              <span>应付金额</span>
              <strong>¥{{ product.price }}</strong>
            </div>
            <el-button type="danger" class="submit" :loading="submitting" @click="submit">
              提交订单
            </el-button>
            <p class="hint">提交后商品将预订给卖家，请等待卖家确认接单。</p>
          </section>
        </aside>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../../api/index.js'
import { useUserStore } from '../../stores/user'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(true)
const submitting = ref(false)
const product = ref(null)

const form = ref({
  tradeType: 1,
  receiverName: '',
  receiverPhone: '',
  campus: '',
  building: '',
  addressDetail: '',
  remark: '',
})

const cover = computed(() => {
  const imgs = product.value?.images
  if (!imgs) return ''
  if (Array.isArray(imgs)) return imgs[0] || ''
  if (typeof imgs === 'string') {
    try {
      const arr = JSON.parse(imgs)
      return Array.isArray(arr) ? arr[0] : imgs
    } catch {
      return imgs
    }
  }
  return ''
})

function prefill() {
  const u = userStore.userInfo || {}
  form.value.receiverName = u.nickname || u.username || ''
  form.value.receiverPhone = u.phone || ''
}

function validate() {
  const f = form.value
  if (!f.receiverName?.trim()) return '请填写收货人'
  if (!/^1\d{10}$/.test((f.receiverPhone || '').trim())) return '请填写正确的手机号'
  if (f.tradeType === 1) {
    if (!f.campus?.trim()) return '请填写校区'
    if (!f.building?.trim() && !f.addressDetail?.trim()) return '请填写楼栋或详细地点'
  } else if (!f.addressDetail?.trim()) {
    return '请填写详细收货地址'
  }
  return ''
}

async function submit() {
  const err = validate()
  if (err) {
    ElMessage.warning(err)
    return
  }
  submitting.value = true
  try {
    const res = await http.post('/orders', {
      productId: product.value.id,
      tradeType: form.value.tradeType,
      receiverName: form.value.receiverName.trim(),
      receiverPhone: form.value.receiverPhone.trim(),
      campus: form.value.campus?.trim() || null,
      building: form.value.building?.trim() || null,
      addressDetail: form.value.addressDetail?.trim() || null,
      remark: form.value.remark?.trim() || null,
    })
    if (res.code === 200) {
      ElMessage.success('下单成功，等待卖家接单')
      router.replace('/order?status=1')
    } else {
      ElMessage.error(res.message || '下单失败')
    }
  } catch {
    ElMessage.error('网络错误')
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  const id = route.params.productId
  if (!userStore.isLoggedIn) {
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  prefill()
  loading.value = true
  try {
    const res = await http.get(`/products/${id}`)
    if (res.code === 200) {
      product.value = res.data
      if (product.value?.status !== 1) {
        ElMessage.warning('该商品当前不可购买')
      }
    }
  } catch {
    product.value = null
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.checkout-page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 16px 16px 40px;
}
.crumbs {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #888;
  margin-bottom: 16px;
}
.crumbs a {
  color: #666;
}
.crumbs a:hover {
  color: #e1251b;
}
.layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 16px;
  align-items: start;
}
.card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 12px;
  padding: 18px 20px 8px;
}
.card h2 {
  font-size: 16px;
  font-weight: 800;
  margin: 0 0 14px;
  color: #222;
}
.product-card {
  padding-bottom: 18px;
  position: sticky;
  top: 72px;
}
.product-row {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.product-row img,
.cover-empty {
  width: 72px;
  height: 72px;
  border-radius: 8px;
  object-fit: cover;
  background: #f5f5f5;
  flex-shrink: 0;
}
.cover-empty {
  display: grid;
  place-items: center;
  color: #bbb;
  font-size: 12px;
}
.title {
  font-size: 14px;
  font-weight: 700;
  color: #222;
  line-height: 1.4;
}
.price {
  margin-top: 8px;
  color: #e1251b;
  font-size: 18px;
  font-weight: 800;
}
.pay-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  padding: 12px 0;
  border-top: 1px dashed #eee;
  margin-bottom: 12px;
  font-size: 14px;
  color: #666;
}
.pay-row strong {
  color: #e1251b;
  font-size: 22px;
}
.submit {
  width: 100%;
  height: 42px;
  font-weight: 700;
}
.hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: #999;
  line-height: 1.5;
}
.spinner {
  width: 28px;
  height: 28px;
  margin: 48px auto;
  border: 3px solid #eee;
  border-top-color: #e1251b;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
@media (max-width: 860px) {
  .layout {
    grid-template-columns: 1fr;
  }
  .product-card {
    position: static;
  }
}
</style>
