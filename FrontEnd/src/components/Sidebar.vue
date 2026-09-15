<script setup>
import { useRoute, useRouter } from 'vue-router'
import AppIcon from './AppIcon.vue'
import MarcaAncora from './MarcaAncora.vue'
import { usuarioAtual } from '../services/auth.js'

/*
  `aberto` só tem efeito abaixo do breakpoint lg: é a gaveta do celular.
  No desktop a sidebar é sempre visível e o estado é ignorado.
*/
defineProps({ aberto: { type: Boolean, default: false } })
defineEmits(['fechar'])

const route = useRoute()
const router = useRouter()

/* Os "name" precisam bater com os nomes das rotas em src/router/index.js */
const links = [
  { name: 'Dashboard', label: 'Dashboard', icon: 'dashboard' },
  { name: 'Clientes',  label: 'Clientes',  icon: 'clientes' },
  { name: 'Upload',    label: 'Enviar planilha', icon: 'upload' },
]

const ativo = (nome) => route.name === nome

function sair() {
  usuarioAtual.sair()
  router.push({ name: 'Login' })
}
</script>

<template>
  <!--
    Celular: position fixed, deslizando de fora da tela (-translate-x-full).
    lg para cima: volta a ser uma coluna normal do flex (lg:static) e o
    translate é anulado, então o estado da gaveta deixa de importar.
  -->
  <aside
    class="fixed inset-y-0 left-0 z-40 flex w-56 shrink-0 flex-col border-r border-zinc-800 bg-zinc-950 transition-transform duration-300 ease-saida lg:static lg:translate-x-0 lg:bg-zinc-900/30"
    :class="aberto ? 'translate-x-0' : '-translate-x-full'">

    <!-- Marca -->
    <div class="flex h-16 shrink-0 items-center gap-2 px-4">
      <router-link :to="{ name: 'Home' }" class="min-w-0 flex-1" aria-label="Âncora — ir para o início">
        <MarcaAncora />
      </router-link>

      <!-- Fechar a gaveta: só existe no celular -->
      <button
        class="rounded-md p-1 text-zinc-500 transition-colors hover:text-zinc-100 lg:hidden"
        aria-label="Fechar menu"
        @click="$emit('fechar')">
        <AppIcon name="fechar" class="h-4 w-4" />
      </button>
    </div>

    <!-- Navegação principal -->
    <nav class="flex-1 px-3 space-y-0.5" aria-label="Navegação principal">
      <router-link
        v-for="link in links"
        :key="link.name"
        :to="{ name: link.name }"
        class="relative flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors"
        :class="ativo(link.name)
          ? 'bg-emerald-500/10 text-emerald-400'
          : 'text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50'"
        :aria-current="ativo(link.name) ? 'page' : undefined">
        <!-- Marcador da página atual -->
        <span
          v-if="ativo(link.name)"
          class="absolute left-0 top-1/2 h-4 w-0.5 -translate-y-1/2 rounded-full bg-emerald-400"></span>
        <AppIcon :name="link.icon" class="w-4 h-4 shrink-0" />
        {{ link.label }}
      </router-link>
    </nav>

    <!-- Rodapé: configurações + usuário -->
    <div class="px-3 pb-4 space-y-0.5">
      <router-link
        :to="{ name: 'Configuracoes' }"
        class="relative flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition-colors"
        :class="ativo('Configuracoes')
          ? 'bg-emerald-500/10 text-emerald-400'
          : 'text-zinc-400 hover:text-zinc-100 hover:bg-zinc-800/50'"
        :aria-current="ativo('Configuracoes') ? 'page' : undefined">
        <span
          v-if="ativo('Configuracoes')"
          class="absolute left-0 top-1/2 h-4 w-0.5 -translate-y-1/2 rounded-full bg-emerald-400"></span>
        <AppIcon name="config" class="w-4 h-4 shrink-0" />
        Configurações
      </router-link>

      <div class="mt-2 flex items-center gap-2.5 rounded-lg bg-zinc-800/40 px-3 py-2.5">
        <div class="w-7 h-7 shrink-0 rounded-full bg-emerald-500/15 border border-emerald-500/30 flex items-center justify-center text-[10px] font-medium text-emerald-400">
          {{ usuarioAtual.iniciais }}
        </div>
        <div class="leading-tight min-w-0 flex-1">
          <p class="text-xs text-zinc-200 truncate">{{ usuarioAtual.nome }}</p>
          <p class="text-[10px] text-zinc-500 truncate">{{ usuarioAtual.cargo }}</p>
        </div>
        <button
          class="rounded-md p-1 text-zinc-600 transition-colors hover:text-red-400"
          title="Sair da conta"
          aria-label="Sair da conta"
          @click="sair">
          <AppIcon name="logout" class="w-3.5 h-3.5" />
        </button>
      </div>
    </div>
  </aside>
</template>
