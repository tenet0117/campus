<template>
  <el-tag :type="tagType" size="small" effect="light">{{ label }}</el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: [Number, String], default: null },
  /** 'product' | 'order' | custom map */
  type: { type: String, default: 'product' },
  map: { type: Object, default: null },
  typeMap: { type: Object, default: null },
})

const PRODUCT_MAP = { 0: '已下架', 1: '在售', 2: '已预订', 3: '已售出' }
const PRODUCT_TYPE = { 0: 'danger', 1: 'success', 2: 'warning', 3: 'info' }
const ORDER_MAP = { 0: '已取消', 1: '待接单', 2: '进行中', 3: '已完成', 4: '已取消' }
const ORDER_TYPE = { 0: 'danger', 1: 'warning', 2: 'info', 3: 'success', 4: 'danger' }

const label = computed(() => {
  if (props.map) return props.map[props.status] ?? String(props.status ?? '')
  const dict = props.type === 'order' ? ORDER_MAP : PRODUCT_MAP
  return dict[props.status] ?? String(props.status ?? '')
})

const tagType = computed(() => {
  if (props.typeMap) return props.typeMap[props.status] || 'info'
  const dict = props.type === 'order' ? ORDER_TYPE : PRODUCT_TYPE
  return dict[props.status] || 'info'
})
</script>
