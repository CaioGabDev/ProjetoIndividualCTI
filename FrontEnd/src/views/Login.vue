<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import MarcaAncora from '../components/MarcaAncora.vue'
import { usuarioAtual } from '../services/auth.js'

const router = useRouter()
const route = useRoute()

const email = ref('')
const senha = ref('')
const mostrarSenha = ref(false)
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
</script>

<template>
  <div class="min-h-screen bg-zinc-950 text-zinc-100 grid grid-cols-1 lg:grid-cols-2">

    <!-- ============ LADO ESQUERDO: marca ============ -->
    <section class="relative hidden lg:flex flex-col justify-between overflow-hidden border-r border-zinc-800 bg-zinc-900/40 p-10">
      <!-- Mesma malha do site, com um brilho curto vindo da esquerda -->
      <div class="pointer-events-none absolute inset-0 grid-texture"></div>
      <div
        class="pointer-events-none absolute inset-0"
        style="background: radial-gradient(ellipse 60% 70% at 15% 40%, rgba(29,158,117,0.14), transparent 70%)"></div>

      <router-link :to="{ name: 'Home' }" class="relative w-fit" aria-label="Âncora — ir para o início">
        <MarcaAncora />
      </router-link>

      <div class="relative max-w-md animate-surgir" style="animation-delay: 120ms">
        <p class="mb-6 font-mono text-[11px] uppercase tracking-[0.2em] text-zinc-500">
          Painel interno · Equipe comercial
        </p>
        <p class="text-2xl font-light leading-relaxed text-zinc-200">
          Uma base só para a carteira, com o mesmo número na frente de todo mundo —
          em vez de três versões da planilha circulando por e-mail.
        </p>
      </div>

      <div class="relative grid grid-cols-2 gap-8 max-w-sm animate-surgir" style="animation-delay: 240ms">
        <div>
          <p class="font-mono text-2xl text-zinc-100">312</p>
          <p class="mt-1 text-[11px] text-zinc-500">Clientes na carteira</p>
        </div>
        <div>
          <p class="font-mono text-2xl text-zinc-100">6</p>
          <p class="mt-1 text-[11px] text-zinc-500">Segmentos acompanhados</p>
        </div>
      </div>
    </section>

    <!-- ============ LADO DIREITO: formulário ============ -->
    <section class="flex items-center justify-center px-6 py-16">
      <div class="w-full max-w-sm animate-surgir">

        <!-- Marca só no mobile (no desktop ela aparece no painel da esquerda) -->
        <router-link :to="{ name: 'Home' }" class="lg:hidden mb-10 block w-fit" aria-label="Âncora — ir para o início">
          <MarcaAncora :subtitulo="false" />
        </router-link>

        <h1 class="text-xl font-medium text-zinc-100">Entrar na Âncora</h1>
        <p class="mt-1.5 text-xs text-zinc-500">Use o e-mail corporativo da CTI para acessar o painel.</p>

        <form class="mt-8 space-y-4" novalidate @submit.prevent="entrar">
          <div>
            <label for="email" class="mb-1.5 block text-[11px] text-zinc-400">E-mail</label>
            <input
              id="email"
              v-model="email"
              type="email"
              autocomplete="email"
              placeholder="seu.nome@cti.com.br"
              :aria-invalid="erro ? 'true' : undefined"
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 px-3 py-2.5 text-sm text-zinc-200 placeholder-zinc-600 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
          </div>

          <div>
            <label for="senha" class="mb-1.5 block text-[11px] text-zinc-400">Senha</label>
            <div class="relative">
              <input
                id="senha"
                v-model="senha"
                :type="mostrarSenha ? 'text' : 'password'"
                autocomplete="current-password"
                placeholder="••••••••"
                :aria-invalid="erro ? 'true' : undefined"
                class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 py-2.5 pl-3 pr-16 text-sm text-zinc-200 placeholder-zinc-600 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
              <button
                type="button"
                class="absolute right-2.5 top-1/2 -translate-y-1/2 rounded px-1 text-[11px] text-zinc-500 transition-colors hover:text-zinc-200"
                @click="mostrarSenha = !mostrarSenha">
                {{ mostrarSenha ? 'ocultar' : 'mostrar' }}
              </button>
            </div>
          </div>

          <div class="flex items-center justify-between text-[11px]">
            <label class="flex cursor-pointer items-center gap-2 text-zinc-400">
              <input v-model="manterConectado" type="checkbox" class="accent-emerald-500" />
              Manter conectado
            </label>
            <a href="#" class="text-emerald-400 transition-colors hover:text-emerald-300">Esqueci minha senha</a>
          </div>

          <p v-if="erro" role="alert" class="rounded-lg border border-red-500/30 bg-red-500/10 px-3 py-2 text-[11px] text-red-400">
            {{ erro }}
          </p>

          <button
            type="submit"
            :disabled="carregando"
            class="w-full rounded-lg bg-emerald-500 px-4 py-2.5 text-sm font-medium text-emerald-950 transition-colors hover:bg-emerald-400 disabled:cursor-not-allowed disabled:opacity-60">
            {{ carregando ? 'Entrando...' : 'Entrar' }}
          </button>
        </form>

        <!-- Divisor -->
        <div class="my-6 flex items-center gap-3">
          <span class="h-px flex-1 bg-zinc-800"></span>
          <span class="text-[11px] text-zinc-600">ou</span>
          <span class="h-px flex-1 bg-zinc-800"></span>
        </div>

        <button
          type="button"
          class="w-full rounded-lg border border-zinc-800 px-4 py-2.5 text-sm text-zinc-300 transition-colors hover:border-zinc-700 hover:bg-zinc-800/50">
          Entrar com SSO corporativo
        </button>

        <p class="mt-8 text-center text-[11px] text-zinc-600">
          Sem acesso ao painel?
          <a href="#" class="text-zinc-300 transition-colors hover:text-emerald-400">Fale com o administrador</a>
        </p>
      </div>
    </section>
  </div>
</template>
