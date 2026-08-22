import { defineConfig } from 'vite';

export default defineConfig({
  base: '/Revamp-project/',

  server: {
    host: '0.0.0.0',
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});