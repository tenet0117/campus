<template>
  <router-link :to="'/product/' + product.id" class="jd-card">
    <div class="cover">
      <img v-if="coverSrc" :src="coverSrc" :alt="product.title" loading="lazy" />
      <div v-else class="cover-empty">暂无图片</div>
      <span v-if="product.status === 2" class="ribbon">已预订</span>
      <span v-else-if="product.status === 3" class="ribbon sold">已售出</span>
    </div>
    <div class="info">
      <h3 class="title">{{ product.title }}</h3>
      <div class="price-line">
        <span class="yen">¥</span>
        <span class="num">{{ priceInt }}</span>
        <span v-if="priceDec" class="dec">.{{ priceDec }}</span>
        <span v-if="product.originalPrice" class="origin">¥{{ product.originalPrice }}</span>
      </div>
      <div class="foot">
        <span>{{ product.viewCount || 0 }}人看过</span>
        <span v-if="product.condition" class="tag">{{ product.condition }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  product: { type: Object, required: true },
})

function resolveUrl(raw) {
  if (!raw || typeof raw !== 'string') return ''
  if (raw.startsWith('http') || raw.startsWith('data:') || raw.startsWith('/api/')) return raw
  if (raw.startsWith('/')) return raw
  return `/api/v1/files/view/${raw}`
}

const coverSrc = computed(() => {
  const p = props.product
  if (!p) return ''
  if (p.coverImage) return resolveUrl(p.coverImage)
  if (p.imageUrl) return resolveUrl(p.imageUrl)
  let images = p.images
  if (typeof images === 'string') {
    try {
      images = JSON.parse(images)
    } catch {
      return resolveUrl(images)
    }
  }
  if (Array.isArray(images) && images.length) {
    const first = images[0]
    return resolveUrl(typeof first === 'string' ? first : first?.url || first?.path || '')
  }
  return ''
})

const priceInt = computed(() => {
  const n = Number(props.product?.price || 0)
  return Math.floor(n)
})
const priceDec = computed(() => {
  const n = Number(props.product?.price || 0)
  const d = Math.round((n - Math.floor(n)) * 100)
  return d ? String(d).padStart(2, '0') : ''
})
</script>

<style scoped>
.jd-card {
  display: block;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  transition: transform .2s, box-shadow .2s;
  border: 1px solid transparent;
}
.jd-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  border-color: #ffe1df;
}
.cover {
  position: relative;
  aspect-ratio: 1;
  background: #f7f7f7;
  overflow: hidden;
}
.cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform .35s;
}
.jd-card:hover .cover img { transform: scale(1.04); }
.cover-empty {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
  font-size: 13px;
}
.ribbon {
  position: absolute;
  top: 8px;
  left: 0;
  background: var(--jd-orange, #ff6600);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 0 4px 4px 0;
}
.ribbon.sold { background: #999; }
.info { padding: 10px 12px 14px; }
.title {
  font-size: 13px;
  font-weight: 500;
  color: #333;
  line-height: 1.45;
  height: 2.9em;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  margin-bottom: 8px;
}
.jd-card:hover .title { color: var(--jd-red, #e1251b); }
.price-line {
  color: var(--jd-red, #e1251b);
  display: flex;
  align-items: baseline;
  gap: 1px;
  margin-bottom: 6px;
}
.yen { font-size: 12px; font-weight: 700; }
.num { font-size: 20px; font-weight: 900; line-height: 1; }
.dec { font-size: 12px; font-weight: 700; }
.origin {
  margin-left: 6px;
  font-size: 12px;
  color: #bbb;
  text-decoration: line-through;
  font-weight: 400;
}
.foot {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #999;
}
.tag {
  background: #fff1f0;
  color: #e1251b;
  padding: 0 6px;
  border-radius: 2px;
  font-size: 11px;
}
</style>
