<template>
  <div class="page-pagination" v-if="total > 0">
    <el-pagination
      background
      layout="total, prev, pager, next"
      :total="total"
      :page-size="pageSize"
      :current-page="currentPage"
      @current-change="$emit('update:currentPage', $event); $emit('change', $event)"
    />
  </div>
</template>

<script setup>
defineProps({
  total: { type: Number, default: 0 },
  pageSize: { type: Number, default: 16 },
  currentPage: { type: Number, default: 1 },
})

defineEmits(['update:currentPage', 'change'])
</script>

<style scoped>
.page-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
