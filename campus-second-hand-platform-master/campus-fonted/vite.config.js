import { fileURLToPath, URL } from 'node:url'

import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '')
  const apiBase = env.VITE_API_BASE || '/api/v1'

  return {
    plugins: [vue()],
    define: {
      global: 'globalThis',
    },
    server: {
      proxy: {
        '/ws': {
          target: 'http://localhost:8081',
          ws: true,
          changeOrigin: true,
        },
        // Proxy API prefix (covers /api/v1 via /api)
        [apiBase.startsWith('/api') ? '/api' : apiBase]: {
          target: 'http://localhost:8081',
          changeOrigin: true,
        },
      },
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
  }
})
