<script setup>
import Sidebar from '../components/Sidebar.vue'
import Topbar from '../components/Topbar.vue'
</script>

<template>
  <!-- Casca do painel interno: sidebar fixa à esquerda, topbar e conteúdo à direita -->
  <div class="min-h-screen bg-zinc-950 text-zinc-100 flex">
    <Sidebar />

    <div class="flex-1 flex flex-col min-w-0">
      <Topbar />

      <main class="flex-1 overflow-y-auto px-6 py-6">
        <!-- Aqui entram Dashboard, Clientes e Enviar planilha -->
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active { transition: opacity .15s ease; }
.fade-enter-from,
.fade-leave-to { opacity: 0; }
</style>
