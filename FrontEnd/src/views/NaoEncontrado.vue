<script setup>
import { useRouter } from 'vue-router'
const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-zinc-950 text-zinc-100 flex flex-col items-center justify-center px-6 text-center">
    <p class="text-5xl font-mono text-emerald-400">404</p>
    <h1 class="mt-4 text-xl font-medium">Página não encontrada</h1>
    <p class="mt-2 text-xs text-zinc-500 max-w-xs">
      O endereço que você tentou abrir não existe ou foi movido.
    </p>

    <div class="mt-8 flex items-center gap-3">
      <button
        class="rounded-lg bg-emerald-500 px-5 py-2.5 text-xs font-medium text-emerald-950 hover:bg-emerald-400 transition"
        @click="router.push({ name: 'Dashboard' })">
        Ir para o painel
      </button>
      <button
        class="rounded-lg border border-zinc-800 px-5 py-2.5 text-xs text-zinc-300 hover:bg-zinc-800/50 transition"
        @click="router.push({ name: 'Home' })">
        Voltar ao início
      </button>
    </div>
  </div>
</template>
