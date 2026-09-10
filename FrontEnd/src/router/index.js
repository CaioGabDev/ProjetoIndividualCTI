import { createRouter, createWebHistory } from 'vue-router'
import { estaLogado } from '../services/auth.js'

// Layout do painel interno (sidebar + topbar)
import AppLayout from '../layouts/AppLayout.vue'

// Páginas públicas
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'

/*
  As páginas internas usam import dinâmico: só são baixadas quando
  o usuário entra no painel, deixando o carregamento inicial mais leve.
*/
const routes = [
  // ---------- Público ----------
  { path: '/', name: 'Home', component: Home, meta: { titulo: 'Âncora — Insights Comerciais' } },
  { path: '/login', name: 'Login', component: Login, meta: { titulo: 'Entrar · Âncora', somenteVisitante: true } },

  // ---------- Painel interno ----------
  {
    path: '/app',
    component: AppLayout,
    redirect: { name: 'Dashboard' },
    meta: { exigeLogin: true },
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { titulo: 'Dashboard · Âncora' } },
      { path: 'clientes', name: 'Clientes', component: () => import('../views/Clientes.vue'), meta: { titulo: 'Clientes · Âncora' } },
      { path: 'upload', name: 'Upload', component: () => import('../views/Upload.vue'), meta: { titulo: 'Enviar planilha · Âncora' } },
      { path: 'configuracoes', name: 'Configuracoes', component: () => import('../views/Configuracoes.vue'), meta: { titulo: 'Configurações · Âncora' } },
    ],
  },

  // ---------- Rota não encontrada ----------
  { path: '/:pathMatch(.*)*', name: 'NaoEncontrado', component: () => import('../views/NaoEncontrado.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: (to, from, posicaoSalva) => posicaoSalva || { top: 0 },
})

/*
  Proteção das rotas:
  - páginas com exigeLogin mandam pro /login guardando o destino em ?redirect
  - quem já está logado não precisa ver o /login de novo
*/
router.beforeEach((to) => {
  if (to.matched.some(r => r.meta.exigeLogin) && !estaLogado()) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  if (to.meta.somenteVisitante && estaLogado()) {
    return { name: 'Dashboard' }
  }

  return true
})

router.afterEach((to) => {
  document.title = to.meta.titulo || 'Âncora'
})

export default router
