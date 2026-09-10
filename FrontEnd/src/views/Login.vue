<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usuarioAtual } from '../services/auth.js'

const router = useRouter()
const route = useRoute()

const email = ref('')
const senha = ref('')
const manterConectado = ref(false)
const erro = ref('')
const carregando = ref(false)

async function entrar() {
  erro.value = ''

  if (!email.value || !senha.value) {
    erro.value = 'Preencha e-mail e senha para continuar.'
    return
  }

  carregando.value = true
  try {
    /* Quando o back tiver /api/auth/login, troque este bloco pela chamada real. */
    usuarioAtual.entrar({ email: email.value, nome: 'Caio Gabriel', cargo: 'Gestor comercial' })

    /* Volta pra página que o usuário tentou abrir antes de logar, ou vai pro dashboard */
    const destino = route.query.redirect || { name: 'Dashboard' }
    router.push(destino)
  } catch {
    erro.value = 'Não foi possível entrar. Verifique suas credenciais.'
  } finally {
    carregando.value = false
  }
}

const heroGlow = { background: 'radial-gradient(ellipse at 30% 50%, rgba(29,158,117,0.16), transparent 65%)' }
</script>

<template>
  <div class="min-h-screen bg-zinc-950 text-zinc-100 grid grid-cols-1 lg:grid-cols-2">

    <!-- ============ LADO ESQUERDO: marca ============ -->
    <section class="relative hidden lg:flex flex-col justify-between overflow-hidden border-r border-zinc-800/60 bg-gradient-to-br from-emerald-950/40 via-zinc-900 to-zinc-950 p-10">
      <div class="pointer-events-none absolute inset-0" :style="heroGlow"></div>

      <router-link :to="{ name: 'Home' }" class="relative flex items-center gap-2.5 w-fit">
        <div class="w-7 h-7 rounded-md bg-emerald-500 flex items-center justify-center text-xs font-semibold text-emerald-950">A</div>
        <div class="leading-tight">
          <p class="text-sm font-medium text-zinc-100">Âncora</p>
          <p class="text-[10px] text-zinc-500">Insights Comerciais</p>
        </div>
      </router-link>

      <div class="relative max-w-md">
        <p class="text-[11px] font-mono uppercase tracking-[0.2em] text-zinc-400 mb-6">
          Feito para a carteira da CTI
        </p>
        <blockquote class="text-2xl font-light leading-relaxed text-zinc-200">
          “Cada planilha carrega uma decisão. A Âncora só ajuda você a enxergar mais rápido.”
        </blockquote>
      </div>

      <div class="relative grid grid-cols-2 gap-8 max-w-sm">
        <div>
          <p class="text-2xl font-mono text-zinc-100">A · B · C</p>
          <p class="text-[11px] text-zinc-500 mt-1">Classificação por nível</p>
        </div>
        <div>
          <p class="text-2xl font-mono text-zinc-100">100%</p>
          <p class="text-[11px] text-zinc-500 mt-1">Tratamento automático</p>
        </div>
      </div>
    </section>

    <!-- ============ LADO DIREITO: formulário ============ -->
    <section class="flex items-center justify-center px-6 py-16">
      <div class="w-full max-w-sm">

        <!-- Logo só no mobile (no desktop ela aparece no painel da esquerda) -->
        <router-link :to="{ name: 'Home' }" class="lg:hidden flex items-center gap-2.5 mb-10">
          <div class="w-7 h-7 rounded-md bg-emerald-500 flex items-center justify-center text-xs font-semibold text-emerald-950">A</div>
          <p class="text-sm font-medium text-zinc-100">Âncora</p>
        </router-link>

        <h1 class="text-xl font-medium text-zinc-100">Bem-vindo de volta</h1>
        <p class="text-xs text-zinc-500 mt-1.5">Entre com sua conta para acessar o painel de insights.</p>

        <form class="mt-8 space-y-4" @submit.prevent="entrar">
          <div>
            <label for="email" class="block text-[11px] text-zinc-400 mb-1.5">E-mail</label>
            <input
              id="email"
              v-model="email"
              type="email"
              autocomplete="email"
              placeholder="seu.nome@cti.com.br"
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 px-3 py-2.5 text-sm text-zinc-200 placeholder-zinc-600 outline-none focus:border-emerald-500/50 transition" />
          </div>

          <div>
            <label for="senha" class="block text-[11px] text-zinc-400 mb-1.5">Senha</label>
            <input
              id="senha"
              v-model="senha"
              type="password"
              autocomplete="current-password"
              placeholder="••••••••"
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 px-3 py-2.5 text-sm text-zinc-200 placeholder-zinc-600 outline-none focus:border-emerald-500/50 transition" />
          </div>

          <div class="flex items-center justify-between text-[11px]">
            <label class="flex items-center gap-2 text-zinc-400 cursor-pointer">
              <input v-model="manterConectado" type="checkbox" class="accent-emerald-500" />
              Manter conectado
            </label>
            <a href="#" class="text-emerald-400 hover:text-emerald-300 transition">Esqueci minha senha</a>
          </div>

          <p v-if="erro" class="rounded-lg bg-red-500/10 border border-red-500/30 px-3 py-2 text-[11px] text-red-400">
            {{ erro }}
          </p>

          <button
            type="submit"
            :disabled="carregando"
            class="w-full rounded-lg bg-emerald-500 px-4 py-2.5 text-sm font-medium text-emerald-950 hover:bg-emerald-400 disabled:opacity-60 transition">
            {{ carregando ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <!-- Divisor -->
        <div class="flex items-center gap-3 my-6">
          <span class="h-px flex-1 bg-zinc-800"></span>
          <span class="text-[11px] text-zinc-600">Ou</span>
          <span class="h-px flex-1 bg-zinc-800"></span>
        </div>

        <button
          type="button"
          class="w-full rounded-lg border border-zinc-800 px-4 py-2.5 text-sm text-zinc-300 hover:bg-zinc-800/50 transition">
          Entrar com SSO corporativo
        </button>

        <p class="mt-8 text-center text-[11px] text-zinc-600">
          Não tem acesso?
          <a href="#" class="text-zinc-300 hover:text-emerald-400 transition">Fale com o administrador</a>
        </p>
      </div>
    </section>
  </div>
</template>
