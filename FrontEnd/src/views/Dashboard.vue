<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../components/AppIcon.vue'
import {
  indicadores,
  faturamentoPorSegmento,
  distribuicaoPorNivel,
  topServicos,
  envios,
  estiloStatus,
} from '../data/mock.js'

const router = useRouter()

/* Altura de cada barra em % do maior valor */
const maiorFaturamento = computed(() => Math.max(...faturamentoPorSegmento.map(s => s.valor)))
function alturaBarra(valor) {
  return `${Math.round((valor / maiorFaturamento.value) * 100)}%`
}

/*
  Rosca em SVG: cada fatia é um círculo com stroke-dasharray.
  circunferência = 2 * PI * raio (raio 42) ≈ 263.9
*/
const CIRCUNFERENCIA = 2 * Math.PI * 42
const fatias = computed(() => {
  let acumulado = 0
  return distribuicaoPorNivel.map(item => {
    const fatia = {
      ...item,
      dash: `${(item.percentual / 100) * CIRCUNFERENCIA} ${CIRCUNFERENCIA}`,
      offset: -(acumulado / 100) * CIRCUNFERENCIA,
    }
    acumulado += item.percentual
    return fatia
  })
})
</script>

<template>
  <div class="space-y-5">

    <!-- ============ CABEÇALHO ============ -->
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-xl font-medium text-zinc-100">Dashboard</h1>
        <p class="text-xs text-zinc-500 mt-1">Visão geral da carteira de clientes da CTI</p>
      </div>

      <button
        class="flex items-center gap-2 rounded-lg bg-emerald-500 px-4 py-2 text-xs font-medium text-emerald-950 hover:bg-emerald-400 transition"
        @click="router.push({ name: 'Upload' })">
        <AppIcon name="plus" class="w-3.5 h-3.5" />
        Enviar planilha
      </button>
    </div>

    <!-- ============ INDICADORES ============ -->
    <section class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
      <article
        v-for="item in indicadores"
        :key="item.rotulo"
        class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-4">
        <p class="text-[10px] font-mono uppercase tracking-wider text-zinc-500">{{ item.rotulo }}</p>
        <p class="text-2xl font-mono text-zinc-100 mt-2">{{ item.valor }}</p>
        <p class="text-[11px] mt-1.5" :class="item.positiva ? 'text-emerald-400' : 'text-orange-400'">
          {{ item.variacao }}
        </p>
      </article>
    </section>

    <!-- ============ FATURAMENTO POR SEGMENTO + DISTRIBUIÇÃO ============ -->
    <section class="grid grid-cols-1 lg:grid-cols-3 gap-4">

      <!-- Barras verticais -->
      <div class="lg:col-span-2 rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-5">
        <h2 class="text-sm font-medium text-zinc-100">Faturamento por segmento</h2>
        <p class="text-[11px] text-zinc-500 mt-0.5">Últimos 12 meses</p>

        <div class="mt-8 flex items-end gap-4 h-48">
          <div v-for="item in faturamentoPorSegmento" :key="item.segmento" class="flex-1 flex flex-col items-center justify-end h-full">
            <span class="text-[10px] font-mono text-zinc-400 mb-2">{{ item.rotulo }}</span>
            <div
              class="w-full rounded-t-md transition-all"
              :style="{ height: alturaBarra(item.valor), backgroundColor: item.cor }">
            </div>
          </div>
        </div>

        <div class="flex gap-4 mt-3">
          <span
            v-for="item in faturamentoPorSegmento"
            :key="item.segmento + '-rotulo'"
            class="flex-1 text-center text-[10px] text-zinc-500 truncate">
            {{ item.segmento }}
          </span>
        </div>
      </div>

      <!-- Rosca -->
      <div class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-5">
        <h2 class="text-sm font-medium text-zinc-100">Distribuição por nível</h2>
        <p class="text-[11px] text-zinc-500 mt-0.5">Carteira classificada</p>

        <div class="mt-6 flex items-center gap-6">
          <svg viewBox="0 0 100 100" class="w-32 h-32 shrink-0 -rotate-90">
            <circle
              v-for="fatia in fatias"
              :key="fatia.nivel"
              cx="50" cy="50" r="42"
              fill="none"
              :stroke="fatia.cor"
              stroke-width="13"
              :stroke-dasharray="fatia.dash"
              :stroke-dashoffset="fatia.offset" />
          </svg>

          <ul class="space-y-2.5 text-xs">
            <li v-for="item in distribuicaoPorNivel" :key="item.nivel" class="flex items-center gap-2.5">
              <span class="w-2 h-2 rounded-full shrink-0" :style="{ backgroundColor: item.cor }"></span>
              <span class="text-zinc-400">{{ item.nivel }}</span>
              <span class="ml-auto font-mono text-zinc-200">{{ item.percentual }}%</span>
            </li>
          </ul>
        </div>
      </div>
    </section>

    <!-- ============ TOP SERVIÇOS + UPLOADS RECENTES ============ -->
    <section class="grid grid-cols-1 lg:grid-cols-2 gap-4">

      <!-- Barras horizontais -->
      <div class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-5">
        <h2 class="text-sm font-medium text-zinc-100">Top serviços por faturamento</h2>
        <p class="text-[11px] text-zinc-500 mt-0.5">Participação na receita total</p>

        <ul class="mt-5 space-y-4">
          <li v-for="item in topServicos" :key="item.servico">
            <div class="flex items-center justify-between text-xs mb-1.5">
              <span class="text-zinc-300">{{ item.servico }}</span>
              <span class="font-mono text-zinc-500">{{ item.percentual }}%</span>
            </div>
            <div class="h-1.5 rounded-full bg-zinc-800 overflow-hidden">
              <div
                class="h-full rounded-full transition-all"
                :style="{ width: item.percentual + '%', backgroundColor: item.cor }">
              </div>
            </div>
          </li>
        </ul>
      </div>

      <!-- Uploads recentes -->
      <div class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-5">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-sm font-medium text-zinc-100">Uploads recentes</h2>
            <p class="text-[11px] text-zinc-500 mt-0.5">Últimas planilhas processadas</p>
          </div>
          <router-link :to="{ name: 'Upload' }" class="text-[11px] text-emerald-400 hover:text-emerald-300 transition">
            Ver todos
          </router-link>
        </div>

        <table class="w-full mt-5 text-xs">
          <thead>
            <tr class="text-[10px] font-mono uppercase tracking-wider text-zinc-600">
              <th class="text-left font-normal pb-3">Arquivo</th>
              <th class="text-right font-normal pb-3">Status</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="envio in envios" :key="envio.id" class="border-t border-zinc-800/60">
              <td class="py-3">
                <div class="flex items-center gap-2.5">
                  <AppIcon name="file" class="w-3.5 h-3.5 text-zinc-600 shrink-0" />
                  <span class="text-zinc-300 truncate">{{ envio.arquivo }}</span>
                </div>
              </td>
              <td class="py-3 text-right">
                <span class="rounded-md px-2 py-1 text-[10px]" :class="estiloStatus[envio.status]">
                  {{ envio.status }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </div>
</template>
