<script setup>
import { onMounted, onBeforeUnmount, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from './AppIcon.vue'
import { usuarioAtual } from '../services/auth.js'

defineEmits(['abrir-menu'])

const router = useRouter()
const busca = ref('')
const campoBusca = ref(null)

/* Enter na busca leva pra lista de clientes já filtrada (?q=...) */
function buscar() {
  if (!busca.value.trim()) return
  router.push({ name: 'Clientes', query: { q: busca.value.trim() } })
}

/* Atalho "/" foca a busca, como nas ferramentas que o time já usa */
function atalho(evento) {
  if (evento.key !== '/' || evento.ctrlKey || evento.metaKey) return
  const alvo = evento.target
  if (alvo instanceof HTMLInputElement || alvo instanceof HTMLTextAreaElement || alvo instanceof HTMLSelectElement) return
  evento.preventDefault()
  campoBusca.value?.focus()
}

onMounted(() => window.addEventListener('keydown', atalho))
onBeforeUnmount(() => window.removeEventListener('keydown', atalho))
</script>

<template>
  <header class="flex h-16 shrink-0 items-center gap-3 border-b border-zinc-800 px-4 sm:gap-4 sm:px-6">
    <!-- Abre a gaveta da sidebar (só no celular) -->
    <button
      class="-ml-1 rounded-md p-1.5 text-zinc-400 transition-colors hover:text-zinc-100 lg:hidden"
      aria-label="Abrir menu"
      @click="$emit('abrir-menu')">
      <AppIcon name="menu" class="h-5 w-5" />
    </button>

    <!-- Busca -->
    <div class="relative w-full max-w-xs">
      <AppIcon name="search" class="w-3.5 h-3.5 absolute left-3 top-1/2 -translate-y-1/2 text-zinc-600 pointer-events-none" />
      <input
        ref="campoBusca"
        v-model="busca"
        type="search"
        aria-label="Buscar cliente ou segmento"
        placeholder="Buscar cliente, segmento..."
        class="w-full rounded-lg border border-zinc-800 bg-zinc-900/60 py-2 pl-9 pr-9 text-xs text-zinc-200 placeholder-zinc-600 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50"
        @keyup.enter="buscar" />
      <kbd
        class="pointer-events-none absolute right-2.5 top-1/2 hidden -translate-y-1/2 rounded border border-zinc-800 bg-zinc-900 px-1.5 py-0.5 font-mono text-[10px] text-zinc-600 sm:block">
        /
      </kbd>
    </div>

    <div class="ml-auto flex items-center gap-4">
      <button class="relative rounded-md p-1 text-zinc-500 transition-colors hover:text-zinc-100" title="Notificações">
        <AppIcon name="bell" class="w-4 h-4" />
        <span class="absolute top-0.5 right-0.5 w-1.5 h-1.5 rounded-full bg-emerald-400 ring-2 ring-zinc-950"></span>
        <span class="sr-only">Notificações não lidas</span>
      </button>

      <div
        class="w-7 h-7 rounded-full bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-[10px] font-medium text-emerald-400"
        :title="usuarioAtual.nome">
        {{ usuarioAtual.iniciais }}
      </div>
    </div>
  </header>
</template>
