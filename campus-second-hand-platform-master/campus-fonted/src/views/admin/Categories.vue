<template>
  <div class="admin-page">
    <div class="page-meta">
      <p class="page-desc">维护商品分类名称与排序</p>
      <el-button type="primary" @click="openCreate">新增分类</el-button>
    </div>

    <el-card shadow="never" class="panel">
      <el-table v-loading="loading" :data="categories" stripe empty-text="暂无分类">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="分类" min-width="180">
          <template #default="{ row }">
            <div class="cat-cell">
              <span class="cat-mark">{{ (row.name || '?')[0] }}</span>
              <span class="cat-name">{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column label="商品数" width="100">
          <template #default="{ row }">{{ row.productCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="startEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="editing ? '编辑分类' : '新增分类'"
      width="420px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @submit.prevent>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如：数码产品" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" controls-position="right" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { http } from '../../api/index.js'
import { fmtDateTime } from '../../utils/media.js'

const categories = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const editing = ref(null)
const formRef = ref(null)
const form = reactive({ name: '', sortOrder: 0 })
const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
}

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await http.get('/categories')
    if (res.code === 200) categories.value = res.data || []
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  form.name = ''
  form.sortOrder = 0
  dialogVisible.value = true
}

function startEdit(c) {
  editing.value = c.id
  form.name = c.name
  form.sortOrder = c.sortOrder ?? 0
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await http.put(`/categories/${editing.value}`, { name: form.name, sortOrder: form.sortOrder })
      ElMessage.success('已更新')
    } else {
      await http.post('/categories', { name: form.name, sortOrder: form.sortOrder })
      ElMessage.success('已添加')
    }
    dialogVisible.value = false
    load()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function doDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该分类？', '提示', { type: 'warning' })
    await http.del(`/categories/${id}`)
    ElMessage.success('已删除')
    load()
  } catch {
    /* cancel */
  }
}
</script>

<style scoped>
.page-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}
.page-desc {
  margin: 0;
  font-size: 13px;
  color: #94a3b8;
}
.panel {
  border-radius: 12px;
  border: 1px solid #e8ecf1;
}
.cat-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.cat-mark {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #ff7a70 0%, #e1251b 100%);
  color: #fff;
  font-weight: 700;
  font-size: 13px;
  display: grid;
  place-items: center;
  flex-shrink: 0;
}
.cat-name {
  font-weight: 600;
  color: #1f2937;
}
</style>
