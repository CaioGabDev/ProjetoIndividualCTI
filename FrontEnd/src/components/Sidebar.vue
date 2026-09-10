<script setup>
import { useRouter } from 'vue-router'
import AppIcon from './AppIcon.vue'
import { usuarioAtual } from '../services/auth.js'

const router = useRouter()

/* Os "name" precisam bater com os nomes das rotas em src/router/index.js */
const links = [
  { name: 'Dashboard', label: 'Dashboard', icon: 'dashboard' },
  { name: 'Clientes',  label: 'Clientes',  icon: 'clientes' },
  { name: 'Upload',    label: 'Enviar planilha', icon: 'upload' },
]

function sair() {
  usuarioAtual.sair()
  router.push({ name: 'Login' })
}
</script>

<template>
  <aside class="w-56 shrink-0 border-r border-zinc-800/60 bg-zinc-900/30 flex flex-col">

    <!-- Logo -->
    <router-link :to="{ name: 'Home' }" class="flex items-center gap-2.5 px-4 h-16 shrink-0">
      <div class="w-7 h-7 rounded-md bg-emerald-500 flex items-center justify-center text-xs font-semibold text-emerald-950">A</div>
      <div class="leading-tight">
        <p class="text-sm font-medium text-zinc-100">Âncora</p>
        <p class="text-[10px] text-zinc-500">Insights Comerciais</p>
      </div>
    </router-link>

    <!-- Navegação principal -->
    <nav class="flex-1 px-3 space-y-1">
      <router-link
        v-for="link in links"
        :key="link.name"
        :to="{ name: link.name }"
        class="group flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition"
        active-class="bg-emerald-500/10 text-emerald-400"
        exact-active-class="bg-emerald-500/10 text-emerald-400"
        :class="'text-zinc-500 hover:text-zinc-200 hover:bg-zinc-800/50'">
        <AppIcon :name="link.icon" class="w-4 h-4" />
        {{ link.label }}
      </router-link>
    </nav>

    <!-- Rodapé: configurações + usuário -->
    <div class="px-3 pb-4 space-y-1">
      <router-link
        :to="{ name: 'Configuracoes' }"
        class="flex items-center gap-3 rounded-lg px-3 py-2 text-sm text-zinc-500 hover:text-zinc-200 hover:bg-zinc-800/50 transition"
        active-class="bg-emerald-500/10 text-emerald-400">
        <AppIcon name="config" class="w-4 h-4" />
        Configurações
      </router-link>

      <div class="mt-2 flex items-center gap-2.5 rounded-lg bg-zinc-800/40 px-3 py-2.5">
        <div class="w-7 h-7 shrink-0 rounded-full bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center text-[10px] font-medium text-emerald-400">
          {{ usuarioAtual.iniciais }}
        </div>
        <div class="leading-tight min-w-0 flex-1">
          <p class="text-xs text-zinc-200 truncate">{{ usuarioAtual.nome }}</p>
          <p class="text-[10px] text-zinc-500 truncate">{{ usuarioAtual.cargo }}</p>
        </div>
        <button
          class="text-zinc-600 hover:text-red-400 transition"
          title="Sair da conta"
          @click="sair">
          <AppIcon name="logout" class="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  </aside>
</template>
