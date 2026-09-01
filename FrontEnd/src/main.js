import { createApp } from 'vue'
import App from './App.vue'
import router from './router' // Importa as rotas de src/router/index.js
import './style.css'
import App from './pages/Home.vue'
app.use(router)

createApp(App).mount('#app')
 
