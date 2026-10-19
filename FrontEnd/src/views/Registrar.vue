<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import MarcaAncora from '../components/MarcaAncora.vue'
import { mensagemDeErro } from '../services/api.js'
import { useAuthStore } from '../stores/auth.js'

const router = useRouter()
const auth = useAuthStore()

const nome = ref('')
const email = ref('')
const cargo = ref('')
const senha = ref('')
const confirmar = ref('')
const mostrarSenha = ref(false)
const erro = ref('')
const carregando = ref(false)

/*
  Mesma regra do CadastroDTO no back: 8 a 72 caracteres, com letra e número.
  O limite de 72 vem do BCrypt, que ignora o que passa disso.

  Conferir aqui é só cortesia com o usuário — avisa sem ida ao servidor.
  Quem decide de verdade continua sendo o Spring: se alguém burlar o
  JavaScript, o @Pattern do DTO recusa com 400.
*/
const senhaForte = computed(() => /^(?=.*[A-Za-z])(?=.*\d).{8,72}$/.test(senha.value))
const senhasConferem = computed(() => senha.value === confirmar.value)

async function cadastrar() {
  erro.value = ''

  if (!nome.value || !email.value || !senha.value) {
    erro.value = 'Preencha nome, e-mail e senha para continuar.'
    return
  }
  if (!senhaForte.value) {
    erro.value = 'A senha deve ter de 8 a 72 caracteres, com letras e números.'
    return
  }
  if (!senhasConferem.value) {
    erro.value = 'As senhas não conferem.'
    return
  }

  carregando.value = true
  try {
    await auth.cadastrar({ nome: nome.value, email: email.value, cargo: cargo.value, senha: senha.value })
    router.push({ name: 'Dashboard' })
  } catch (e) {
    /* 409 = e-mail repetido, 400 = campo recusado pelo back. A frase certa
       vem da própria resposta; mensagemDeErro só escolhe qual delas mostrar. */
    erro.value = mensagemDeErro(e, 'Não foi possível criar a conta. Tente novamente.')
  } finally {
    carregando.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-zinc-950 text-zinc-100 grid grid-cols-1 lg:grid-cols-2">

    <!-- ============ LADO ESQUERDO: marca ============ -->
    <section class="relative hidden lg:flex flex-col justify-between overflow-hidden border-r border-zinc-800 bg-zinc-900/40 p-10">
      <div class="pointer-events-none absolute inset-0 grid-texture"></div>
      <div
        class="pointer-events-none absolute inset-0"
        style="background: radial-gradient(ellipse 60% 70% at 15% 40%, rgba(29,158,117,0.14), transparent 70%)"></div>

      <router-link :to="{ name: 'Home' }" class="relative w-fit" aria-label="Âncora — ir para o início">
        <MarcaAncora />
      </router-link>

      <div class="relative max-w-md animate-surgir" style="animation-delay: 120ms">
        <p class="mb-6 font-mono text-[11px] uppercase tracking-[0.2em] text-zinc-500">
          Primeiro acesso
        </p>
        <p class="text-2xl font-light leading-relaxed text-zinc-200">
          Sua senha é guardada embaralhada (hash BCrypt) e nunca fica no banco em
          texto puro — nem para quem administra o sistema.
        </p>
      </div>

      <div class="relative max-w-sm animate-surgir" style="animation-delay: 240ms">
        <p class="font-mono text-[11px] uppercase tracking-[0.2em] text-zinc-500">Requisitos da senha</p>
        <p class="mt-2 text-xs leading-relaxed text-zinc-400">
          De 8 a 72 caracteres, com pelo menos uma letra e um número.
        </p>
      </div>
    </section>

    <!-- ============ LADO DIREITO: formulário ============ -->
    <section class="flex items-center justify-center px-6 py-16">
      <div class="w-full max-w-sm animate-surgir">

        <router-link :to="{ name: 'Home' }" class="lg:hidden mb-10 block w-fit" aria-label="Âncora — ir para o início">
          <MarcaAncora :subtitulo="false" />
        </router-link>

        <h1 class="text-xl font-medium text-zinc-100">Criar conta na Âncora</h1>
        <p class="mt-1.5 text-xs text-zinc-500">Use o e-mail corporativo da CTI.</p>

        <form class="mt-8 space-y-4" novalidate @submit.prevent="cadastrar">
          <div>
            <label for="nome" class="mb-1.5 block text-[11px] text-zinc-400">Nome completo</label>
            <input
              id="nome"
              v-model="nome"
              type="text"
              autocomplete="name"
              maxlength="150"
              placeholder="Caio Gabriel"
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 px-3 py-2.5 text-sm text-zinc-200 placeholder-zinc-600 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
          </div>

          <div>
            <label for="email" class="mb-1.5 block text-[11px] text-zinc-400">E-mail</label>
            <input
              id="email"
              v-model="email"
              type="email"
              autocomplete="email"
              maxlength="255"
              placeholder="seu.nome@cti.com.br"
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 px-3 py-2.5 text-sm text-zinc-200 placeholder-zinc-600 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
          </div>

          <div>
            <label for="cargo" class="mb-1.5 block text-[11px] text-zinc-400">
              Cargo <span class="text-zinc-600">(opcional)</span>
            </label>
            <input
              id="cargo"
              v-model="cargo"
              type="text"
              maxlength="100"
              placeholder="Gestor comercial"
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 px-3 py-2.5 text-sm text-zinc-200 placeholder-zinc-600 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
          </div>

          <div>
            <label for="senha" class="mb-1.5 block text-[11px] text-zinc-400">Senha</label>
            <div class="relative">
              <input
                id="senha"
                v-model="senha"
                :type="mostrarSenha ? 'text' : 'password'"
                autocomplete="new-password"
                placeholder="••••••••"
                class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 py-2.5 pl-3 pr-16 text-sm text-zinc-200 placeholder-zinc-600 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
              <button
                type="button"
                class="absolute right-2.5 top-1/2 -translate-y-1/2 rounded px-1 text-[11px] text-zinc-500 transition-colors hover:text-zinc-200"
                @click="mostrarSenha = !mostrarSenha">
                {{ mostrarSenha ? 'ocultar' : 'mostrar' }}
              </button>
            </div>
            <p class="mt-1.5 text-[11px]" :class="senha && !senhaForte ? 'text-amber-400' : 'text-zinc-600'">
              De 8 a 72 caracteres, com letras e números.
            </p>
          </div>

          <div>
            <label for="confirmar" class="mb-1.5 block text-[11px] text-zinc-400">Confirmar senha</label>
            <input
              id="confirmar"
              v-model="confirmar"
              :type="mostrarSenha ? 'text' : 'password'"
              autocomplete="new-password"
              placeholder="••••••••"
              class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 px-3 py-2.5 text-sm text-zinc-200 placeholder-zinc-600 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
            <p v-if="confirmar && !senhasConferem" class="mt-1.5 text-[11px] text-amber-400">
              As senhas não conferem.
            </p>
          </div>

          <p v-if="erro" role="alert" class="rounded-lg border border-red-500/30 bg-red-500/10 px-3 py-2 text-[11px] text-red-400">
            {{ erro }}
          </p>

          <button
            type="submit"
            :disabled="carregando"
            class="w-full rounded-lg bg-emerald-500 px-4 py-2.5 text-sm font-medium text-emerald-950 transition-colors hover:bg-emerald-400 disabled:cursor-not-allowed disabled:opacity-60">
            {{ carregando ? 'Criando conta...' : 'Criar conta' }}
          </button>
        </form>

        <p class="mt-8 text-center text-[11px] text-zinc-600">
          Já tem acesso?
          <router-link :to="{ name: 'Login' }" class="text-emerald-400 transition-colors hover:text-emerald-300">
            Entrar
          </router-link>
        </p>
      </div>
    </section>
  </div>
</template>
