import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true, // Docker için şart
    port: 5173,
    watch: {
      usePolling: true, // Windows dosya sistemi için gerekli
    },
  },
  optimizeDeps: {
    include: ['react', 'react-dom', 'react-is', 'recharts', 'react-credit-cards-2'],
  },
})

