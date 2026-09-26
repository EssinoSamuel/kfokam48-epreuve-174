import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Evite les appels cross-origin en developpement : le frontend
    // appelle /api/... et Vite relaie vers le backend Spring Boot.
    // La regle de source de verite reste le backend (contrainte F3).
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
