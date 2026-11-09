<template>
  <div class="admin-page">
    <div class="page-meta">
      <p class="page-desc">查看校园用户资料，启用或禁用账号</p>
      <span class="meta-count">共 {{ total }} 位用户</span>
    </div>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 昵称 / 邮箱"
        clearable
        style="width: 280px"
        @keyup.enter="search"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="search">搜索</el-button>
    </div>

    <el-card shadow="never" class="panel">
      <el-table v-loading="loading" :data="users" stripe empty-text="暂无用户">
        <el-table-column label="用户" min-width="220">
          <template #default="{ row }">
            <div class="user-cell clickable" @click="openDetail(row)">
              <UserAvatar :src="row.avatar" :name="row.nickname || row.username" :size="40" />
              <div class="user-text">
                <div class="nick">{{ row.nickname || row.username || '未设置昵称' }}</div>
                <div class="uname">@{{ row.username }} · ID {{ row.id }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="联系方式" min-width="200">
          <template #default="{ row }">
            <div class="contact">
              <span>{{ row.email || '未填邮箱' }}</span>
              <span class="muted">{{ row.phone || '未填手机' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="角色" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.role === 'admin' ? 'danger' : 'info'" effect="plain">
              {{ row.role === 'admin' ? '管理员' : '用户' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status === 1" type="danger" link @click="toggleStatus(row.id, 0)">禁用</el-button>
            <el-button v-else type="success" link @click="toggleStatus(row.id, 1)">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div v-if="total > size" class="pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :page-size="size"
          :current-page="page"
          :total="total"
          @current-change="(p) => { page = p; load() }"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="detailVisible"
      title="用户详情"
      width="560px"
      destroy-on-close
      class="detail-dialog"
    >
      <div v-loading="detailLoading" class="detail-body">
        <template v-if="detail">
          <div class="detail-hero">
            <UserAvatar :src="detail.avatar" :name="detail.nickname || detail.username" :size="72" />
            <div>
              <h3>{{ detail.nickname || detail.username }}</h3>
              <p>@{{ detail.username }} · ID {{ detail.id }}</p>
              <div class="hero-tags">
                <el-tag size="small" :type="detail.status === 1 ? 'success' : 'danger'">
                  {{ detail.status === 1 ? '正常' : '禁用' }}
                </el-tag>
                <el-tag size="small" :type="detail.role === 'admin' ? 'danger' : 'info'" effect="plain">
                  {{ detail.role === 'admin' ? '管理员' : '普通用户' }}
                </el-tag>
              </div>
            </div>
          </div>

          <div class="stat-row">
            <div class="stat"><strong>{{ detail.productCount ?? 0 }}</strong><span>发布商品</span></div>
            <div class="stat"><strong>{{ detail.buyOrderCount ?? 0 }}</strong><span>买入订单</span></div>
            <div class="stat"><strong>{{ detail.sellOrderCount ?? 0 }}</strong><span>卖出订单</span></div>
          </div>

          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="邮箱">{{ detail.email || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ detail.phone || '未填写' }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ fmtDateTime(detail.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ fmtDateTime(detail.updateTime) }}</el-descriptions-item>
          </el-descriptions>
        </template>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button
          v-if="detail?.status === 1"
          type="danger"
          @click="toggleStatus(detail.id, 0).then(() => { detailVisible = false })"
        >
          禁用账号
        </el-button>
        <el-button
          v-else-if="detail"
          type="success"
          @click="toggleStatus(detail.id, 1).then(() => { detailVisible = false })"
        >
          启用账号
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { http } from '../../api/index.js'
import UserAvatar from '../../components/UserAvatar.vue'
import { fmtDateTime } from '../../utils/media.js'

const users = ref([])
const keyword = ref('')
const page = ref(1)
const size = ref(16)
const total = ref(0)
const loading = ref(false)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

onMounted(load)

async function load() {
  loading.value = true
  try {
    const res = await http.get('/admin/users', {
      page: page.value,
      size: size.value,
      keyword: keyword.value || undefined,
    })
    if (res.code === 200 && res.data) {
      users.value = res.data.records || []
      total.value = res.data.total || 0
      if (users.value.length === 0 && total.value > 0 && page.value > 1) {
        page.value = Math.max(1, Math.ceil(total.value / size.value) - 1)
        load()
        return
      }
    }
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const res = await http.get(`/admin/users/${row.id}`)
    if (res.code === 200 && res.data) {
      detail.value = res.data.userInfo || res.data
    } else {
      ElMessage.error(res.message || '加载详情失败')
      detailVisible.value = false
    }
  } catch {
    ElMessage.error('加载详情失败')
    detailVisible.value = false
  } finally {
    detailLoading.value = false
  }
}

async function toggleStatus(id, status) {
  try {
    await http.patch(`/admin/users/${id}/status?status=${status}`)
    ElMessage.success('已更新')
    load()
  } catch {
    ElMessage.error('操作失败')
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
.meta-count {
  font-size: 13px;
  color: #64748b;
  background: #fff;
  border: 1px solid #e2e8f0;
  padding: 4px 10px;
  border-radius: 999px;
  white-space: nowrap;
}
.toolbar {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}
.panel {
  border-radius: 12px;
  border: 1px solid #e8ecf1;
}
.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}
.user-cell.clickable {
  cursor: pointer;
}
.user-text .nick {
  font-weight: 600;
  color: #1f2937;
  line-height: 1.3;
}
.user-text .uname {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}
.contact {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 13px;
  color: #334155;
}
.contact .muted {
  font-size: 12px;
  color: #94a3b8;
}
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
.detail-body {
  min-height: 160px;
}
.detail-hero {
  display: flex;
  gap: 16px;
  align-items: center;
  margin-bottom: 18px;
}
.detail-hero h3 {
  margin: 0;
  font-size: 18px;
  color: #1f2937;
}
.detail-hero p {
  margin: 4px 0 8px;
  font-size: 13px;
  color: #94a3b8;
}
.hero-tags {
  display: flex;
  gap: 6px;
}
.stat-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 16px;
}
.stat {
  background: #f8fafc;
  border: 1px solid #eef2f7;
  border-radius: 10px;
  padding: 12px 10px;
  text-align: center;
}
.stat strong {
  display: block;
  font-size: 20px;
  color: #e1251b;
  line-height: 1.2;
}
.stat span {
  font-size: 12px;
  color: #94a3b8;
}
</style>
