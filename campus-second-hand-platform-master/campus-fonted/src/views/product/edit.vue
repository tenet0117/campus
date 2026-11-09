<template>
  <div v-if="pageLoading" class="spinner" />
  <div v-else class="publish-page">
    <div class="publish-hero">
      <div class="publish-hero-inner">
        <h1>修改商品</h1>
        <p>更新信息后保存，在售商品会立即对买家可见</p>
      </div>
    </div>

    <div class="publish-shell">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        require-asterisk-position="right"
        class="publish-form"
        @submit.prevent
      >
        <section class="block">
          <div class="block-head">
            <h2>商品图片</h2>
            <span>最多 9 张，第一张将作为封面</span>
          </div>
          <div class="upload-grid">
            <div v-for="(img, i) in uploadedImages" :key="i" class="thumb">
              <img :src="img" alt="" />
              <span v-if="i === 0" class="cover-badge">封面</span>
              <button type="button" class="thumb-del" aria-label="删除" @click="removeImage(i)">×</button>
            </div>
            <label v-if="uploadedImages.length < 9" class="thumb add">
              <input type="file" accept="image/*" multiple hidden @change="handleUpload" />
              <span class="plus">+</span>
              <span class="hint">{{ uploading ? '上传中…' : '添加图片' }}</span>
            </label>
          </div>
        </section>

        <section class="block">
          <div class="block-head">
            <h2>基本信息</h2>
          </div>
          <el-form-item label="商品标题" prop="title">
            <el-input
              v-model="form.title"
              maxlength="100"
              show-word-limit
              size="large"
              placeholder="例：九成新 iPad Air 5 64G 星空灰"
            />
          </el-form-item>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="分类" prop="categoryId">
                <el-select v-model="form.categoryId" placeholder="选择分类" size="large" style="width: 100%">
                  <el-option v-for="c in categories" :key="c.id" :label="c.name" :value="c.id" />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="成色" prop="condition">
                <el-select v-model="form.condition" placeholder="选择成色" size="large" clearable style="width: 100%">
                  <el-option v-for="c in conditions" :key="c" :label="c" :value="c" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="商品描述" prop="description">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="5"
              maxlength="2000"
              show-word-limit
              placeholder="说明购买时间、使用情况、配件是否齐全、瑕疵位置等，方便买家快速决策"
            />
          </el-form-item>
        </section>

        <section class="block">
          <div class="block-head">
            <h2>价格</h2>
          </div>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="售价" prop="price">
                <el-input v-model.number="form.price" type="number" size="large" placeholder="0.00">
                  <template #prefix><span class="yen">¥</span></template>
                </el-input>
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="原价（选填）">
                <el-input
                  v-model.number="form.originalPrice"
                  type="number"
                  size="large"
                  placeholder="入手价，便于展示优惠"
                >
                  <template #prefix><span class="yen muted">¥</span></template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
        </section>

        <section class="block">
          <div class="block-head">
            <h2>补充信息</h2>
            <span>选填，写得越全越容易成交</span>
          </div>
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12">
              <el-form-item label="品牌">
                <el-input v-model="form.brand" size="large" placeholder="如 Apple / Nike" clearable />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="型号">
                <el-input v-model="form.model" size="large" placeholder="如 AirPods Pro 2" clearable />
              </el-form-item>
            </el-col>
          </el-row>
          <el-form-item label="标签">
            <el-input
              v-model="form.tags"
              size="large"
              placeholder="用逗号分隔，如：MacBook, 苹果, 笔记本"
              clearable
            />
          </el-form-item>
          <el-form-item label="交易备注">
            <el-input
              v-model="form.tradeNotes"
              type="textarea"
              :rows="2"
              placeholder="校区面交地点、可议价、仅限同校等"
            />
          </el-form-item>
        </section>

        <div class="publish-actions">
          <el-button size="large" @click="router.back()">取消</el-button>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="handleSubmit">
            保存修改
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { http } from '../../api/index.js'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const pageLoading = ref(true)
const uploading = ref(false)
const categories = ref([])
const conditions = ['全新', '九五新', '九成新', '八成新', '七成新']
const productId = route.params.id

const form = reactive({
  title: '',
  categoryId: null,
  price: null,
  originalPrice: null,
  description: '',
  brand: '',
  model: '',
  condition: null,
  tags: '',
  tradeNotes: '',
})

const rules = {
  title: [{ required: true, message: '请填写标题', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [
    { required: true, message: '请填写售价', trigger: 'blur' },
    {
      validator: (_r, v, cb) => {
        if (v == null || v === '' || Number(v) <= 0) cb(new Error('售价需大于 0'))
        else cb()
      },
      trigger: 'blur',
    },
  ],
  description: [{ required: true, message: '请填写描述', trigger: 'blur' }],
}

const uploadedImages = ref([])
const uploadedPaths = ref([])

function resolveUploadUrl(filePath) {
  if (!filePath) return ''
  if (filePath.startsWith('http') || filePath.startsWith('/') || filePath.startsWith('data:')) return filePath
  return `/api/v1/files/view/${filePath}`
}

function fillFromProduct(p) {
  form.title = p.title || ''
  form.categoryId = p.categoryId ?? null
  form.price = p.price != null ? Number(p.price) : null
  form.originalPrice = p.originalPrice != null ? Number(p.originalPrice) : null
  form.description = p.description || ''
  form.brand = p.brand || ''
  form.model = p.model || ''
  form.condition = p.condition || null
  form.tradeNotes = p.tradeNotes || ''
  if (Array.isArray(p.tags)) form.tags = p.tags.join(',')
  else if (typeof p.tags === 'string') form.tags = p.tags
  else form.tags = ''

  let imgs = p.images
  if (typeof imgs === 'string') {
    try {
      imgs = JSON.parse(imgs)
    } catch {
      imgs = imgs ? [imgs] : []
    }
  }
  if (!Array.isArray(imgs)) imgs = []
  uploadedPaths.value = imgs.map((x) => (typeof x === 'string' ? x : x?.url || x?.path || '')).filter(Boolean)
  uploadedImages.value = uploadedPaths.value.map(resolveUploadUrl)
}

onMounted(async () => {
  pageLoading.value = true
  try {
    const [cRes, pRes] = await Promise.all([
      http.get('/categories'),
      http.get(`/products/${productId}`),
    ])
    if (cRes.code === 200) categories.value = cRes.data || []
    if (pRes.code === 200 && pRes.data) fillFromProduct(pRes.data)
    else {
      ElMessage.error(pRes.message || '商品不存在')
      router.replace('/product/my')
    }
  } catch {
    ElMessage.error('加载失败')
    router.replace('/product/my')
  } finally {
    pageLoading.value = false
  }
})

async function handleUpload(e) {
  const files = e.target.files
  if (!files?.length) return
  uploading.value = true
  let ok = 0
  for (const file of files) {
    if (uploadedImages.value.length >= 9) break
    const fd = new FormData()
    fd.append('file', file)
    fd.append('category', 'products')
    try {
      const res = await http.upload('/files/upload/image', fd)
      if (res.code === 200 && res.data) {
        const path = res.data.filePath || res.data.url
        uploadedPaths.value.push(path)
        uploadedImages.value.push(resolveUploadUrl(path))
        ok += 1
      }
    } catch {
      /* continue */
    }
  }
  uploading.value = false
  e.target.value = ''
  if (ok) ElMessage.success(`已上传 ${ok} 张图片`)
  else ElMessage.error('图片上传失败，请重试')
}

function removeImage(i) {
  uploadedImages.value.splice(i, 1)
  uploadedPaths.value.splice(i, 1)
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (!uploadedPaths.value.length) {
    ElMessage.warning('请至少保留一张商品图片')
    return
  }

  loading.value = true
  try {
    const body = {
      title: form.title.trim(),
      categoryId: form.categoryId,
      price: form.price,
      originalPrice: form.originalPrice || null,
      description: form.description.trim(),
      brand: form.brand || null,
      model: form.model || null,
      condition: form.condition || null,
      tags: form.tags
        ? form.tags.split(/[,，]/).map((s) => s.trim()).filter(Boolean)
        : null,
      tradeNotes: form.tradeNotes || null,
      images: uploadedPaths.value,
    }
    const res = await http.put(`/products/${productId}`, body)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      router.push('/product/my')
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.publish-page {
  max-width: 880px;
  margin: 0 auto 48px;
  padding: 0 12px;
}

.publish-hero {
  margin: 0 0 16px;
  padding: 20px 4px 8px;
  border-bottom: 3px solid var(--jd-red);
}

.publish-hero-inner {
  max-width: 880px;
}

.publish-hero h1 {
  font-size: 26px;
  font-weight: 900;
  color: var(--gray-900);
  letter-spacing: 0.02em;
  margin-bottom: 6px;
}

.publish-hero p {
  font-size: 14px;
  color: var(--gray-500);
  max-width: 36em;
}

.publish-shell {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 28px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.publish-form {
  padding: 8px 0 0;
}

.block {
  padding: 22px 28px 8px;
  border-bottom: 1px solid var(--gray-200);
}

.block-head {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 16px;
}

.block-head h2 {
  font-size: 16px;
  font-weight: 700;
  color: var(--gray-900);
}

.block-head span {
  font-size: 12px;
  color: var(--gray-500);
}

.yen {
  color: var(--jd-red);
  font-weight: 700;
}

.yen.muted {
  color: var(--gray-500);
  font-weight: 500;
}

.upload-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}

.thumb {
  position: relative;
  width: 104px;
  height: 104px;
  border-radius: 8px;
  overflow: hidden;
  background: var(--gray-100);
  border: 1px solid var(--gray-200);
}

.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.thumb-del {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 14px;
  line-height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-badge {
  position: absolute;
  left: 0;
  bottom: 0;
  padding: 2px 8px;
  font-size: 11px;
  color: #fff;
  background: var(--jd-red);
  border-radius: 0 6px 0 0;
}

.thumb.add {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  border: 1.5px dashed var(--gray-300);
  background: #fafafa;
  cursor: pointer;
  transition: border-color 0.2s, background 0.2s, color 0.2s;
  color: var(--gray-500);
}

.thumb.add:hover {
  border-color: var(--jd-red);
  background: var(--jd-red-light);
  color: var(--jd-red);
}

.thumb.add .plus {
  font-size: 28px;
  line-height: 1;
  font-weight: 300;
}

.thumb.add .hint {
  font-size: 12px;
}

.publish-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 28px 28px;
  background: linear-gradient(180deg, #fff, #fafafa);
  border-top: 1px solid var(--gray-200);
}

.publish-actions .submit {
  min-width: 160px;
  font-weight: 700;
}

@media (max-width: 640px) {
  .publish-hero h1 {
    font-size: 22px;
  }

  .block {
    padding: 18px 16px 4px;
  }

  .publish-actions {
    padding: 16px;
    flex-direction: column-reverse;
  }

  .publish-actions .el-button {
    width: 100%;
  }
}
</style>
