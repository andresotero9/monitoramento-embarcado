import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Em desenvolvimento, o proxy evita problemas de CORS ao chamar a API do backend
// diretamente de "localhost:5173". Em produção (Docker), o Nginx do container
// do frontend assume esse papel (ver docker/nginx.conf, Etapa 10).
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: process.env.VITE_API_PROXY_TARGET || 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
