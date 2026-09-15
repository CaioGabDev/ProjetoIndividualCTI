<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '../components/Sidebar.vue'
import Topbar from '../components/Topbar.vue'

const route = useRoute()

/*
  Onde o navegador suporta View Transitions, quem anima a troca de tela é ele
  (ver router/index.js) e o <transition> do Vue sai da frente por completo.

  Não basta deixar o <transition> sem CSS: com mode="out-in" ele desmonta a tela
  antiga num tick e monta a nova no seguinte, e a View Transition precisa do DOM
  novo pronto quando tira a segunda foto. Duas trocas em ticks diferentes fariam
  o navegador fotografar a tela no meio do caminho.
*/
const usaViewTransition = 'startViewTransition' in document

/*
  No desktop a sidebar é fixa e este estado não faz diferença.
  No celular ela vira uma gaveta que desliza por cima do conteúdo.
*/
const menuAberto = ref(false)

/* Navegou para outra tela: a gaveta fecha sozinha. */
watch(() => route.fullPath, () => { menuAberto.value = false })

/* Esc fecha — o mesmo que o usuário espera de qualquer painel lateral. */
function aoTeclar(evento) {
  if (evento.key === 'Escape') menuAberto.value = false
}
onMounted(() => window.addEventListener('keydown', aoTeclar))
onBeforeUnmount(() => window.removeEventListener('keydown', aoTeclar))
</script>

<template>
  <!-- Casca do painel: no lg vira duas colunas; abaixo disso, uma só -->
  <div class="min-h-screen bg-zinc-950 text-zinc-100 lg:flex">
    <Sidebar :aberto="menuAberto" @fechar="menuAberto = false" />

    <!-- Véu que escurece o conteúdo e fecha a gaveta ao toque -->
    <div
      v-if="menuAberto"
      class="fixed inset-0 z-30 bg-zinc-950/70 lg:hidden"
      aria-hidden="true"
      @click="menuAberto = false"></div>

    <div class="flex min-w-0 flex-1 flex-col">
      <Topbar @abrir-menu="menuAberto = true" />

      <main class="flex-1 overflow-y-auto px-4 py-5 sm:px-6 sm:py-6">
        <!-- Aqui entram Dashboard, Clientes e Enviar planilha -->
        <router-view v-slot="{ Component }">
          <component :is="Component" v-if="usaViewTransition" />
          <transition v-else name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<style scoped>
/*
  Entrada e saída com tempos diferentes (mode="out-in" roda uma depois da outra):
  a tela antiga some rápido, a nova entra devagar. Tempo igual nos dois lados dá
  aquela sensação de "esperar o fade" no meio da navegação.
*/
.fade-leave-active {
  transition: opacity 120ms ease-in;
}

.fade-enter-active {
  transition: opacity 280ms var(--ease-saida), transform 280ms var(--ease-saida);
}

.fade-leave-to {
  opacity: 0;
}

.fade-enter-from {
  opacity: 0;
  transform: translateY(6px);
}
</style>
