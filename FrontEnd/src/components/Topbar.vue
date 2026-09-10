<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from './AppIcon.vue'
import { usuarioAtual } from '../services/auth.js'

const router = useRouter()
const busca = ref('')

/* Enter na busca leva pra lista de clientes já filtrada (?q=...) */
function buscar() {
  if (!busca.value.trim()) return
  router.push({ name: 'Clientes', query: { q: busca.value.trim() } })
}
</script>

<template>
  <header class="h-16 shrink-0 border-b border-zinc-800/60 flex items-center gap-4 px-6">
    <!-- Busca -->
    <div class="relative w-full max-w-xs">
      <AppIcon name="search" class="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-zinc-600" />
      <input
        v-model="busca"
        type="search"
        placeholder="Buscar cliente, segmento..."
        class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 py-2 pl-9 pr-3 text-xs text-zinc-200 placeholder-zinc-600 outline-none focus:border-emerald-500/50 transition"
        @keyup.enter="buscar" />
    </div>

    <div class="ml-auto flex items-center gap-4">
      <button class="relative text-zinc-500 hover:text-zinc-200 transition" title="Notificações">
        <AppIcon name="bell" class="w-4 h-4" />
        <span class="absolute -top-0.5 -right-0.5 w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
      </button>

      <div class="w-7 h-7 rounded-full bg-emerald-500/20 border border-emerald-500/40 flex items-center justify-center text-[10px] font-medium text-emerald-400">
        {{ usuarioAtual.iniciais }}
      </div>
    </div>
  </header>
</template>
