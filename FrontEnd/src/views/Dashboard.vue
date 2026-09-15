<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../components/AppIcon.vue'
import Esqueleto from '../components/Esqueleto.vue'
import Grafico from '../components/Grafico.vue'
import NumeroAnimado from '../components/NumeroAnimado.vue'
import Sparkline from '../components/Sparkline.vue'
import { carregarPainel, estiloStatus, formatarReal } from '../data/mock.js'

const router = useRouter()

/*
  Os dados chegam por uma função assíncrona — hoje do mock, amanhã da API.
  Enquanto `painel` for null a tela mostra o esqueleto, então o estado de
  carregamento já está testado antes do back existir.
*/
const painel = ref(null)
const carregando = computed(() => painel.value === null)

onMounted(async () => { painel.value = await carregarPainel() })

/* Atalhos para não escrever painel.value?.x em toda linha do template. */
const indicadores = computed(() => painel.value?.indicadores ?? [])
const baseAtual = computed(() => painel.value?.baseAtual ?? {})
const faturamentoPorSegmento = computed(() => painel.value?.faturamentoPorSegmento ?? [])
const distribuicaoPorNivel = computed(() => painel.value?.distribuicaoPorNivel ?? [])
const topServicos = computed(() => painel.value?.topServicos ?? [])
const envios = computed(() => painel.value?.envios ?? [])
const totalClientes = computed(() => painel.value?.totalClientes ?? 0)

/* Formata percentual no padrão brasileiro: 30.7 -> "30,7%" */
function percentual(valor, casas = 1) {
  return `${valor.toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas })}%`
}

/* ==================== LEITURA DA CARTEIRA ====================
   O README define que o sistema existe para responder perguntas como
   "qual segmento traz mais retorno?". O gráfico mostra o dado; estas frases
   respondem a pergunta. Tudo calculado da mesma base que alimenta os gráficos.
*/
const faturamentoTotal = computed(() => faturamentoPorSegmento.value.reduce((soma, s) => soma + s.valor, 0))

const insights = computed(() => {
  if (!painel.value) return []

  const ordenados = [...faturamentoPorSegmento.value].sort((a, b) => b.valor - a.valor)
  const [primeiro, segundo] = ordenados
  const total = faturamentoTotal.value

  const parteLider = (primeiro.valor / total) * 100
  const parteTopDois = ((primeiro.valor + segundo.valor) / total) * 100
  const nivelA = distribuicaoPorNivel.value.find(n => n.nivel.endsWith('A'))
  const nivelB = distribuicaoPorNivel.value.find(n => n.nivel.endsWith('B'))
  const servicoLider = [...topServicos.value].sort((a, b) => b.percentual - a.percentual)[0]

  return [
    {
      destaque: percentual(parteLider),
      frase: `do faturamento vem de ${primeiro.segmento} — o segmento de maior peso entre os ${faturamentoPorSegmento.value.length} da carteira.`,
    },
    {
      destaque: percentual(parteTopDois),
      frase: `é o quanto ${primeiro.segmento} e ${segundo.segmento} somam juntos. Metade da receita em dois segmentos é concentração que pede plano B.`,
    },
    {
      destaque: percentual(nivelB.percentual, 0),
      frase: `da carteira está no nível ${nivelB.nivel.slice(-1)}, contra ${percentual(nivelA.percentual, 0)} no nível ${nivelA.nivel.slice(-1)}: é nesse grupo que está o espaço de crescimento.`,
    },
    {
      destaque: percentual(servicoLider.percentual, 0),
      frase: `da receita sai de ${servicoLider.servico.toLowerCase()}, o serviço mais relevante do portfólio hoje.`,
    },
  ]
})

/* ==================== GRÁFICOS ==================== */

/* Faturamento por segmento — barras verticais */
const dadosSegmento = computed(() => ({
  labels: faturamentoPorSegmento.value.map(item => item.segmento),
  datasets: [{
    label: 'Faturamento',
    data: faturamentoPorSegmento.value.map(item => item.valor),
    backgroundColor: faturamentoPorSegmento.value.map(item => item.cor),
    borderRadius: 6,
    borderSkipped: false,      // arredonda os dois cantos de cima
    maxBarThickness: 56,
  }],
}))

/* Formata para os rótulos em cima da barra: 1480000 -> "1,48M" */
function abreviar(valor) {
  return valor >= 1_000_000
    ? `${(valor / 1_000_000).toLocaleString('pt-BR', { maximumFractionDigits: 2 })}M`
    : `${Math.round(valor / 1000)}k`
}

/*
  Clicar numa barra leva para a carteira daquele segmento.
  `elementos` vem vazio quando o clique cai fora de qualquer barra.
*/
function irParaSegmento(evento, elementos) {
  if (!elementos.length) return
  const segmento = faturamentoPorSegmento.value[elementos[0].index]?.segmento
  if (segmento) router.push({ name: 'Clientes', query: { segmento } })
}

function irParaNivel(evento, elementos) {
  if (!elementos.length) return
  const nivel = distribuicaoPorNivel.value[elementos[0].index]?.nivel
  /* "Nível B" -> "B", que é o formato que a tela de Clientes filtra. */
  if (nivel) router.push({ name: 'Clientes', query: { nivel: nivel.slice(-1) } })
}

function irParaServico(evento, elementos) {
  if (!elementos.length) return
  const servico = topServicos.value[elementos[0].index]?.servico
  if (servico) router.push({ name: 'Clientes', query: { q: servico } })
}

const opcoesSegmento = {
  onClick: irParaSegmento,
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        /* Eixo em escala curta: "R$ 1,5M" em vez de "1480000" */
        callback: (valor) => valor >= 1_000_000
          ? `R$ ${(valor / 1_000_000).toLocaleString('pt-BR', { maximumFractionDigits: 1 })}M`
          : `R$ ${Math.round(valor / 1000)}k`,
      },
    },
  },
  plugins: {
    tooltip: {
      callbacks: { label: (ctx) => `${formatarReal(ctx.parsed.y)} · clique para ver os clientes` },
    },
    rotulosDeValor: { formatar: abreviar },
  },
}

/* Distribuição por nível — rosca */
const dadosNivel = computed(() => ({
  labels: distribuicaoPorNivel.value.map(item => item.nivel),
  datasets: [{
    data: distribuicaoPorNivel.value.map(item => item.percentual),
    backgroundColor: distribuicaoPorNivel.value.map(item => item.cor),
    borderWidth: 0,
    hoverOffset: 6,
  }],
}))

const opcoesNivel = {
  onClick: irParaNivel,
  cutout: '72%',                                   // espessura do anel
  plugins: {
    tooltip: {
      callbacks: { label: (ctx) => `${ctx.parsed}% da carteira · clique para filtrar` },
    },
  },
}

/* Top serviços — barras horizontais (indexAxis: 'y' deita o gráfico) */
const dadosServicos = computed(() => ({
  labels: topServicos.value.map(item => item.servico),
  datasets: [{
    data: topServicos.value.map(item => item.percentual),
    backgroundColor: topServicos.value.map(item => item.cor),
    borderRadius: 4,
    borderSkipped: false,
    maxBarThickness: 16,
  }],
}))

const opcoesServicos = {
  onClick: irParaServico,
  indexAxis: 'y',
  scales: {
    /* Deitado, os eixos trocam de papel: a grade vai para o x, os nomes para o y */
    x: {
      beginAtZero: true,
      grid: { display: true },
      ticks: { callback: (valor) => `${valor}%` },
    },
    y: {
      grid: { display: false },
      ticks: { font: { size: 11 } },   // nome de serviço pede a fonte de texto, não a mono
    },
  },
  plugins: {
    tooltip: {
      callbacks: { label: (ctx) => `${ctx.parsed.x}% da receita total · clique para filtrar` },
    },
    rotulosDeValor: { formatar: (valor) => `${valor}%` },
  },
}
</script>

<template>
  <div class="space-y-5">

    <!-- ============ CABEÇALHO ============ -->
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 class="text-xl font-medium text-zinc-100">Dashboard</h1>
        <!-- De onde vem o número que está na tela: evita a dúvida na reunião -->
        <Esqueleto v-if="carregando" class="mt-2 h-3 w-64" />
        <p v-else class="mt-1 text-xs text-zinc-500">
          Base <span class="font-mono text-zinc-400">{{ baseAtual.arquivo }}</span> ·
          atualizada {{ baseAtual.atualizadoEm }}
        </p>
      </div>

      <button
        class="flex items-center gap-2 rounded-lg bg-emerald-500 px-4 py-2 text-xs font-medium text-emerald-950 transition-colors hover:bg-emerald-400"
        @click="router.push({ name: 'Upload' })">
        <AppIcon name="plus" class="w-3.5 h-3.5" />
        Enviar planilha
      </button>
    </div>

    <!-- ============ INDICADORES ============ -->
    <section class="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4" aria-label="Indicadores da carteira">
      <!-- Enquanto carrega: quatro cartões com a mesma altura dos de verdade -->
      <template v-if="carregando">
        <div v-for="n in 4" :key="n" class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-4">
          <Esqueleto class="h-2.5 w-24" />
          <Esqueleto class="mt-3 h-7 w-32" />
          <Esqueleto class="mt-3 h-7 w-full" />
        </div>
      </template>

      <article
        v-for="item in indicadores"
        v-else
        :key="item.rotulo"
        class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-4">
        <p class="font-mono text-[10px] uppercase tracking-wider text-zinc-500">{{ item.rotulo }}</p>
        <p class="mt-2 font-mono text-2xl text-zinc-100">
          <NumeroAnimado :texto="item.valor" />
        </p>

        <div class="mt-2 flex items-end justify-between gap-3">
          <p class="text-[11px]">
            <span :class="item.positiva ? 'text-emerald-400' : 'text-orange-400'">{{ item.variacao }}</span>
            <span class="text-zinc-600"> {{ item.contexto }}</span>
          </p>
          <!-- Tendência dos últimos 6 meses: dá contexto ao número grande -->
          <Sparkline :serie="item.serie" :positiva="item.positiva" class="max-w-[5.5rem]" />
        </div>
      </article>
    </section>

    <!-- ============ LEITURA DA CARTEIRA ============ -->
    <section class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 class="text-sm font-medium text-zinc-100">Leitura da carteira</h2>
          <p class="mt-0.5 text-[11px] text-zinc-500">O que os números da base atual estão dizendo</p>
        </div>
        <span v-if="!carregando" class="rounded-md bg-emerald-500/10 px-2 py-1 font-mono text-[10px] text-emerald-400">
          {{ formatarReal(faturamentoTotal) }} no período
        </span>
      </div>

      <div v-if="carregando" class="mt-5 grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
        <Esqueleto v-for="n in 4" :key="n" class="h-8 w-full" />
      </div>

      <ul v-else class="mt-5 grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
        <li v-for="insight in insights" :key="insight.frase" class="flex gap-3">
          <span class="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-400"></span>
          <p class="text-xs leading-relaxed text-zinc-400">
            <span class="font-mono text-sm text-zinc-100">{{ insight.destaque }}</span>
            {{ insight.frase }}
          </p>
        </li>
      </ul>
    </section>

    <!-- ============ FATURAMENTO POR SEGMENTO + DISTRIBUIÇÃO ============ -->
    <section class="grid grid-cols-1 lg:grid-cols-3 gap-4">

      <!-- Barras verticais -->
      <div class="lg:col-span-2 rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
        <h2 class="text-sm font-medium text-zinc-100">Faturamento por segmento</h2>
        <p class="mt-0.5 text-[11px] text-zinc-500">Últimos 12 meses · clique numa barra para ver os clientes</p>

        <Esqueleto v-if="carregando" class="mt-6 h-60 w-full" arredondado="rounded-lg" />
        <Grafico
          v-else
          class="mt-6"
          tipo="bar"
          :dados="dadosSegmento"
          :opcoes="opcoesSegmento"
          altura="15rem" />
      </div>

      <!-- Rosca -->
      <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
        <h2 class="text-sm font-medium text-zinc-100">Distribuição por nível</h2>
        <p class="mt-0.5 text-[11px] text-zinc-500">Clique numa fatia para filtrar a carteira</p>

        <div v-if="carregando" class="mt-6 flex items-center gap-5">
          <Esqueleto class="h-32 w-32" arredondado="rounded-full" />
          <div class="flex-1 space-y-2.5">
            <Esqueleto v-for="n in 3" :key="n" class="h-3 w-full" />
          </div>
        </div>

        <div v-else class="mt-6 flex flex-wrap items-center gap-5">
          <div class="relative w-32 shrink-0">
            <Grafico tipo="doughnut" :dados="dadosNivel" :opcoes="opcoesNivel" altura="8rem" :atraso="140" />
            <!-- Total no centro: a rosca deixa de ser só desenho -->
            <div class="pointer-events-none absolute inset-0 flex flex-col items-center justify-center">
              <span class="font-mono text-lg text-zinc-100">{{ totalClientes }}</span>
              <span class="text-[9px] text-zinc-500">clientes</span>
            </div>
          </div>

          <ul class="space-y-2.5 text-xs">
            <li v-for="item in distribuicaoPorNivel" :key="item.nivel" class="flex items-center gap-2.5">
              <span class="h-2 w-2 shrink-0 rounded-full" :style="{ backgroundColor: item.cor }"></span>
              <span class="text-zinc-400">{{ item.nivel }}</span>
              <span class="ml-auto font-mono text-zinc-200">{{ item.percentual }}%</span>
            </li>
          </ul>
        </div>
      </div>
    </section>

    <!-- ============ TOP SERVIÇOS + ENVIOS RECENTES ============ -->
    <section class="grid grid-cols-1 lg:grid-cols-2 gap-4">

      <!-- Barras horizontais -->
      <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
        <h2 class="text-sm font-medium text-zinc-100">Top serviços por faturamento</h2>
        <p class="mt-0.5 text-[11px] text-zinc-500">Participação na receita total · clique para filtrar</p>

        <Esqueleto v-if="carregando" class="mt-5 h-44 w-full" arredondado="rounded-lg" />
        <Grafico
          v-else
          class="mt-5"
          tipo="bar"
          :dados="dadosServicos"
          :opcoes="opcoesServicos"
          altura="11rem"
          :atraso="90" />
      </div>

      <!-- Envios recentes -->
      <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
        <div class="flex items-start justify-between">
          <div>
            <h2 class="text-sm font-medium text-zinc-100">Envios recentes</h2>
            <p class="mt-0.5 text-[11px] text-zinc-500">Últimas planilhas processadas</p>
          </div>
          <router-link :to="{ name: 'Upload' }" class="text-[11px] text-emerald-400 transition-colors hover:text-emerald-300">
            Ver todos
          </router-link>
        </div>

        <table class="mt-5 w-full text-xs">
          <thead>
            <tr class="font-mono text-[10px] uppercase tracking-wider text-zinc-600">
              <th scope="col" class="pb-3 text-left font-normal">Arquivo</th>
              <th scope="col" class="pb-3 text-right font-normal">Status</th>
            </tr>
          </thead>
          <tbody v-if="carregando">
            <tr v-for="n in 3" :key="n" class="border-t border-zinc-800">
              <td class="py-3 pr-4"><Esqueleto class="h-3 w-40" /></td>
              <td class="py-3"><Esqueleto class="ml-auto h-5 w-20" /></td>
            </tr>
          </tbody>

          <tbody v-else>
            <tr v-for="envio in envios" :key="envio.id" class="border-t border-zinc-800">
              <td class="py-3">
                <div class="flex items-center gap-2.5">
                  <AppIcon name="file" class="w-3.5 h-3.5 shrink-0 text-zinc-600" />
                  <span class="truncate text-zinc-300">{{ envio.arquivo }}</span>
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
