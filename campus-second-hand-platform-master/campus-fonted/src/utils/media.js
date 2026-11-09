/** Resolve uploaded / relative media paths to a browser-usable URL. */
export function resolveMediaUrl(raw) {
  if (!raw || typeof raw !== 'string') return ''
  if (raw.startsWith('http') || raw.startsWith('data:') || raw.startsWith('/api/')) return raw
  if (raw.startsWith('/')) return raw
  return `/api/v1/files/view/${raw}`
}

/** Demo / seed avatars shipped in frontend public/avatars. */
const SEED_AVATAR_RE = /^avatars\/(avatar0[1-5]|default-avatar)(?:\.(?:png|jpg|jpeg|webp|svg))?$/i

export function resolveAvatarUrl(raw) {
  if (!raw || typeof raw !== 'string') return ''
  const path = raw.replace(/^\/+/, '')
  const seed = path.match(SEED_AVATAR_RE)
  if (seed) {
    const name = seed[1].toLowerCase()
    return `/avatars/${name}.svg`
  }
  return resolveMediaUrl(raw)
}

export function avatarLetter(name) {
  const s = String(name || '?').trim()
  return (s[0] || '?').toUpperCase()
}

/** First product cover from coverImage / imageUrl / images[] / productImage. */
export function productCoverSrc(product) {
  if (!product) return ''
  if (product.coverImage) return resolveMediaUrl(product.coverImage)
  if (product.imageUrl) return resolveMediaUrl(product.imageUrl)
  if (product.productImage) return resolveMediaUrl(product.productImage)
  let images = product.images
  if (typeof images === 'string') {
    try {
      images = JSON.parse(images)
    } catch {
      return resolveMediaUrl(images)
    }
  }
  if (Array.isArray(images) && images.length) {
    const first = images[0]
    return resolveMediaUrl(typeof first === 'string' ? first : first?.url || first?.path || '')
  }
  return ''
}

export function fmtDateTime(t) {
  return t ? new Date(t).toLocaleString('zh-CN') : ''
}
