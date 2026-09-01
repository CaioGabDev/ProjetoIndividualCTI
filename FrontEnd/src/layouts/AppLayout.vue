<script setup>
// Importação do componente Header e Sidebar
import Header from '../components/Header.vue'
import Sidebar from '../components/Sidebar.vue'
</script>
<template>
 <div class="min-h-screen bg-slate-50 text-slate-900 flex flex-col">
 <!-- 1. O HEADER FICA AQUI (FIXO NO TOPO) -->
 <Header />
 <div class="flex flex-1 pt-14">
 <!-- 2. A SIDEBAR FICA NA LATERAL -->
 <Sidebar />
 <!-- 3. AQUI ABREM AS PÁGINAS (Upload, Relatórios, Gráficos) -->
 <main class="flex-1 p-6 overflow-y-auto">
 <router-view />
 </main>
 </div>
 </div>
</template>