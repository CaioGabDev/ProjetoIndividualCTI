import { nextTick } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import { estaLogado } from '../services/auth.js'
import { deveAnimar } from '../services/preferencias.js'

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
      { path: 'relatorio', name: 'Relatorio', component: () => import('../views/Relatorio.vue'), meta: { titulo: 'Relatório de validação · Âncora' } },
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

/*
  ============ VIEW TRANSITIONS ============
  API nativa do navegador: ele fotografa a tela ANTES da troca, fotografa
  DEPOIS, e faz a transição entre as duas fotos — fora da thread principal.

  A dança abaixo existe porque o navegador precisa do DOM novo já pronto
  dentro do callback, e quem atualiza o DOM é o Vue, depois que a navegação
  termina. Então:

    1. beforeResolve segura a navegação devolvendo uma promessa;
    2. dentro de startViewTransition, liberamos a navegação (`seguir`);
    3. o callback devolve outra promessa, que só é resolvida no afterEach,
       depois do nextTick — ou seja, com a tela nova já renderizada;
    4. aí o navegador tira a segunda foto e anima.

  Sem suporte (Firefox, hoje) nada disso roda e o <transition> do AppLayout
  assume — a tela continua funcionando igual.
*/
let liberarTransicao = null

router.beforeResolve((to, from) => {
  if (!document.startViewTransition || !deveAnimar()) return
  if (!from.name) return              // primeira carga da página não tem "antes"

  return new Promise((seguir) => {
    document.startViewTransition(() => {
      seguir()
      return new Promise((resolver) => { liberarTransicao = resolver })
    })
  })
})

router.afterEach(async (to) => {
  document.title = to.meta.titulo || 'Âncora'

  if (!liberarTransicao) return
  await nextTick()                    // espera o Vue pintar a tela nova
  liberarTransicao()
  liberarTransicao = null
})

export default router
