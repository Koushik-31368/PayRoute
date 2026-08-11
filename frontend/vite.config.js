import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],

  // sockjs-client uses Node's `global` which doesn't exist in browsers.
  // This tells Vite to replace every reference to `global` with `globalThis`,
  // which is the browser-native equivalent.
  define: {
    global: 'globalThis',
  },

  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      '/ws': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        ws: true,
      },
    },
  },
})

