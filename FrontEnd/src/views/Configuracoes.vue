<script setup>
import { ref } from 'vue'
import { usuarioAtual } from '../services/auth.js'

/*
  Página simples de configurações — o item "Configurações" da sidebar
  precisava de um destino. Amplie conforme o back-end for crescendo.
*/
const nome = ref(usuarioAtual.nome)
const email = ref(usuarioAtual.email)
const cargo = ref(usuarioAtual.cargo)

const preferencias = ref([
  { chave: 'email', titulo: 'Avisos por e-mail', texto: 'Receber um resumo quando uma planilha terminar de processar.', ativo: true },
  { chave: 'erros', titulo: 'Alertas de erro', texto: 'Ser avisado quando um envio falhar por formato inválido.', ativo: true },
  { chave: 'resumo', titulo: 'Resumo semanal', texto: 'Panorama da carteira toda segunda-feira pela manhã.', ativo: false },
])

const salvo = ref(false)

function salvar() {
  usuarioAtual.entrar({ nome: nome.value, email: email.value, cargo: cargo.value })
  salvo.value = true
  setTimeout(() => { salvo.value = false }, 2500)
}
</script>

<template>
  <div class="space-y-5 max-w-2xl">

    <div>
      <h1 class="text-xl font-medium text-zinc-100">Configurações</h1>
      <p class="text-xs text-zinc-500 mt-1">Dados da conta e preferências de notificação</p>
    </div>

    <!-- ============ PERFIL ============ -->
    <section class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-5">
      <h2 class="text-sm font-medium text-zinc-100">Perfil</h2>

      <div class="mt-5 grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label for="nome" class="block text-[11px] text-zinc-400 mb-1.5">Nome</label>
          <input
            id="nome"
            v-model="nome"
            type="text"
            class="w-full rounded-lg border border-zinc-800 bg-zinc-950/60 px-3 py-2.5 text-sm text-zinc-200 outline-none focus:border-emerald-500/50 transition" />
        </div>

        <div>
          <label for="cargo" class="block text-[11px] text-zinc-400 mb-1.5">Cargo</label>
          <input
            id="cargo"
            v-model="cargo"
            type="text"
            class="w-full rounded-lg border border-zinc-800 bg-zinc-950/60 px-3 py-2.5 text-sm text-zinc-200 outline-none focus:border-emerald-500/50 transition" />
        </div>

        <div class="sm:col-span-2">
          <label for="email-conta" class="block text-[11px] text-zinc-400 mb-1.5">E-mail</label>
          <input
            id="email-conta"
            v-model="email"
            type="email"
            class="w-full rounded-lg border border-zinc-800 bg-zinc-950/60 px-3 py-2.5 text-sm text-zinc-200 outline-none focus:border-emerald-500/50 transition" />
        </div>
      </div>
    </section>

    <!-- ============ NOTIFICAÇÕES ============ -->
    <section class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-5">
      <h2 class="text-sm font-medium text-zinc-100">Notificações</h2>

      <ul class="mt-4 divide-y divide-zinc-800/60">
        <li v-for="item in preferencias" :key="item.chave" class="flex items-center gap-4 py-3.5">
          <div class="min-w-0 flex-1 leading-tight">
            <p class="text-xs text-zinc-200">{{ item.titulo }}</p>
            <p class="text-[10px] text-zinc-500 mt-1">{{ item.texto }}</p>
          </div>

          <button
            class="relative w-9 h-5 shrink-0 rounded-full transition"
            :class="item.ativo ? 'bg-emerald-500' : 'bg-zinc-700'"
            :aria-pressed="item.ativo"
            @click="item.ativo = !item.ativo">
            <span
              class="absolute top-0.5 w-4 h-4 rounded-full bg-white transition-all"
              :class="item.ativo ? 'left-4.5' : 'left-0.5'"></span>
          </button>
        </li>
      </ul>
    </section>

    <div class="flex items-center gap-3">
      <button
        class="rounded-lg bg-emerald-500 px-5 py-2.5 text-xs font-medium text-emerald-950 hover:bg-emerald-400 transition"
        @click="salvar">
        Salvar alterações
      </button>
      <span v-if="salvo" class="text-[11px] text-emerald-400">Alterações salvas.</span>
    </div>
  </div>
</template>
