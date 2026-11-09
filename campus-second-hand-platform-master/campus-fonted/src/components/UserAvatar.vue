<template>
  <el-avatar
    :key="avatarSrc || letter"
    :size="size"
    :src="avatarSrc || undefined"
    :style="fallbackStyle"
    class="user-avatar"
  >
    {{ letter }}
  </el-avatar>
</template>

<script setup>
import { computed } from 'vue'
import { resolveAvatarUrl, avatarLetter } from '../utils/media.js'

const props = defineProps({
  src: { type: String, default: '' },
  name: { type: String, default: '' },
  size: { type: [Number, String], default: 36 },
})

const avatarSrc = computed(() => resolveAvatarUrl(props.src))
const letter = computed(() => avatarLetter(props.name))
const fallbackStyle = computed(() =>
  avatarSrc.value
    ? undefined
    : {
        background: 'linear-gradient(135deg, #ff7a70 0%, #e1251b 100%)',
        color: '#fff',
        fontWeight: 700,
      }
)
</script>

<style scoped>
.user-avatar {
  flex-shrink: 0;
  font-size: 14px;
}
.user-avatar :deep(img) {
  object-fit: cover;
}
</style>
