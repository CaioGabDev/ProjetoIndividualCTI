import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'   // src/router/index.js
import { revelar } from './directives/revelar.js'
import './services/preferencias.js'   // aplica o modo de animação no <html> antes da primeira pintura
import './style.css'

createApp(App)
  .use(createPinia())
  .use(router)
  /* Deixa v-revelar disponível em qualquer componente, sem precisar importar */
  .directive('revelar', revelar)
  .mount('#app')
