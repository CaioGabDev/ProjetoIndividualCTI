<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import AppIcon from '../components/AppIcon.vue'
import Esqueleto from '../components/Esqueleto.vue'
import Grafico from '../components/Grafico.vue'
import NumeroAnimado from '../components/NumeroAnimado.vue'
import Sparkline from '../components/Sparkline.vue'
import { api, mensagemDeErro } from '../services/api.js'
import {
  estiloStatus, formatarReal, loteParaHistorico, mesCurto, percentual, realCurto, tempoRelativo,
} from '../services/formatar.js'

const router = useRouter()

/*
  De onde vem cada número desta tela:

    GET /api/dashboard -> { lote, resultado }
      lote       o último envio que o Python terminou de processar
      resultado  o JSON que o analytics-python calculou (analise.py):
                 resumo, porSegmento, porNivel, porServico, porMes,
                 boxplot, histograma, insights...
    GET /api/upload    -> histórico dos envios

  Nada é calculado aqui além de formatar e escolher cor: a estatística é do
  Python, o front só desenha.
*/
const painel = ref(null)
const lotes = ref([])
const carregando = ref(true)
const erro = ref('')

async function carregar() {
  carregando.value = true
  erro.value = ''

  /* allSettled: se só o histórico falhar, o painel ainda aparece. */
  const [respostaPainel, respostaLotes] = await Promise.allSettled([api.painel(), api.listarLotes()])

  if (respostaPainel.status === 'fulfilled') {
    painel.value = respostaPainel.value
  } else {
    erro.value = mensagemDeErro(respostaPainel.reason, 'Não foi possível carregar o painel.')
  }
  lotes.value = respostaLotes.status === 'fulfilled' ? respostaLotes.value : []
  carregando.value = false
}

onMounted(carregar)

/* Atalhos para não escrever painel.value?.resultado?.x em toda linha. */
const resultado = computed(() => painel.value?.resultado ?? null)
const lote = computed(() => painel.value?.lote ?? null)
const vazio = computed(() => !carregando.value && !erro.value && !resultado.value)
const pronto = computed(() => !carregando.value && !!resultado.value)

const resumo = computed(() => resultado.value?.resumo ?? {})
const porSegmento = computed(() =>
  [...(resultado.value?.porSegmento ?? [])].sort((a, b) => b.faturamentoTotal - a.faturamentoTotal))
const porNivel = computed(() => resultado.value?.porNivel ?? [])
const porServico = computed(() => (resultado.value?.porServico ?? []).slice(0, 6))
const porMes = computed(() => resultado.value?.porMes ?? [])
const histograma = computed(() => resultado.value?.histograma?.faixas ?? [])
const boxplot = computed(() => resultado.value?.boxplot ?? null)
const insights = computed(() => resultado.value?.insights ?? [])
const envios = computed(() => lotes.value.slice(0, 4).map(loteParaHistorico))

/* ==================== CORES ====================
   Segmentos e serviços: uma escala só do verde da marca, do mais forte para o
   mais fraco — cor aqui é hierarquia, não decoração. Níveis são categorias,
   então cada um tem a sua cor do guia de estilo. */
const ESCALA_VERDE = ['#2BB98B', '#1D9E75', '#198A66', '#147356', '#105C45', '#0C4534']
const corDaPosicao = (i) => ESCALA_VERDE[Math.min(i, ESCALA_VERDE.length - 1)]
const COR_NIVEL = { A: '#1D9E75', B: '#7F77D0', C: '#E87A5F' }
const COR_SECUNDARIA = '#7F77D0'

/* ==================== INDICADORES ==================== */
const indicadores = computed(() => {
  if (!resultado.value) return []

  const r = resumo.value
  const nivelA = porNivel.value.find(n => n.nivel === 'A')
  const outliers = boxplot.value?.outliers?.length ?? 0

  /* Faturamento acumulado mês a mês: a curva da sparkline do primeiro cartão. */
  let soma = 0
  const faturamentoAcumulado = porMes.value.map(m => (soma += m.faturamento || 0))

  return [
    {
      rotulo: 'Faturamento total', valor: realCurto(r.faturamentoTotal),
      destaque: realCurto(r.media), contexto: 'de média por cliente',
      serie: faturamentoAcumulado,
    },
    {
      rotulo: 'Clientes na base', valor: String(r.totalClientes ?? 0),
      destaque: String(r.consultores ?? 0), contexto: r.consultores === 1 ? 'consultor' : 'consultores',
      serie: porMes.value.map(m => m.acumulado),
    },
    {
      rotulo: 'Faturamento mediano', valor: realCurto(r.mediana),
      destaque: String(outliers), contexto: outliers === 1 ? 'outlier no boxplot' : 'outliers no boxplot',
      serie: porMes.value.map(m => m.faturamento),
    },
    {
      rotulo: 'Nível A da carteira', valor: percentual(nivelA?.percentual ?? 0, 0),
      destaque: percentual(nivelA?.percentualFaturamento ?? 0, 0), contexto: 'do faturamento',
      serie: [],
    },
  ]
})

/* ==================== GRÁFICOS ==================== */

/* 1. Faturamento por segmento — barras verticais */
const dadosSegmento = computed(() => ({
  labels: porSegmento.value.map(item => item.segmento),
  datasets: [{
    label: 'Faturamento',
    data: porSegmento.value.map(item => item.faturamentoTotal),
    backgroundColor: porSegmento.value.map((_, i) => corDaPosicao(i)),
    borderRadius: 6,
    borderSkipped: false,      // arredonda os dois cantos de cima
    maxBarThickness: 56,
  }],
}))

/* Rótulo em cima da barra, sem o "R$": 1480000 -> "1,48M" */
const abreviar = (valor) => realCurto(valor).replace('R$ ', '')

/*
  Clicar numa barra leva para a carteira daquele segmento.
  `elementos` vem vazio quando o clique cai fora de qualquer barra.
*/
function irParaSegmento(evento, elementos) {
  if (!elementos.length) return
  const segmento = porSegmento.value[elementos[0].index]?.segmento
  if (segmento) router.push({ name: 'Clientes', query: { segmento } })
}

function irParaNivel(evento, elementos) {
  if (!elementos.length) return
  const nivel = porNivel.value[elementos[0].index]?.nivel
  if (nivel) router.push({ name: 'Clientes', query: { nivel } })
}

function irParaServico(evento, elementos) {
  if (!elementos.length) return
  const servico = porServico.value[elementos[0].index]?.servico
  if (servico) router.push({ name: 'Clientes', query: { q: servico } })
}

const opcoesSegmento = {
  onClick: irParaSegmento,
  scales: {
    y: { beginAtZero: true, ticks: { callback: (valor) => realCurto(valor) } },
  },
  plugins: {
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const item = porSegmento.value[ctx.dataIndex]
          return [
            `${formatarReal(ctx.parsed.y)} (${percentual(item?.percentualFaturamento)} do total)`,
            `${item?.clientes ?? 0} clientes · clique para ver`,
          ]
        },
      },
    },
    rotulosDeValor: { formatar: abreviar },
  },
}

/* 2. Distribuição por nível — rosca */
const dadosNivel = computed(() => ({
  labels: porNivel.value.map(item => `Nível ${item.nivel}`),
  datasets: [{
    data: porNivel.value.map(item => item.percentual),
    backgroundColor: porNivel.value.map(item => COR_NIVEL[item.nivel] || COR_SECUNDARIA),
    borderWidth: 0,
    hoverOffset: 6,
  }],
}))

const opcoesNivel = {
  onClick: irParaNivel,
  cutout: '72%',                                   // espessura do anel
  plugins: {
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const item = porNivel.value[ctx.dataIndex]
          return `${item?.clientes ?? 0} clientes · ${percentual(ctx.parsed)} · clique para filtrar`
        },
      },
    },
  },
}

/* 3. Serviços mais contratados — barras horizontais (indexAxis: 'y' deita o gráfico) */
const dadosServicos = computed(() => ({
  labels: porServico.value.map(item => item.servico),
  datasets: [{
    data: porServico.value.map(item => item.percentual),
    backgroundColor: porServico.value.map((_, i) => corDaPosicao(i)),
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
      suggestedMax: 100,
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
      callbacks: {
        label: (ctx) => {
          const item = porServico.value[ctx.dataIndex]
          return `${item?.contratos ?? 0} clientes (${percentual(ctx.parsed.x)}) · clique para filtrar`
        },
      },
    },
    rotulosDeValor: { formatar: (valor) => percentual(valor) },
  },
}

/* 4. Contratações por mês — barras verticais na cor secundária */
const dadosMes = computed(() => ({
  labels: porMes.value.map(item => mesCurto(item.mes)),
  datasets: [{
    data: porMes.value.map(item => item.clientes),
    backgroundColor: COR_SECUNDARIA,
    borderRadius: 4,
    borderSkipped: false,
    maxBarThickness: 28,
  }],
}))

const opcoesMes = {
  scales: { y: { beginAtZero: true, ticks: { precision: 0 } } },
  plugins: {
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const item = porMes.value[ctx.dataIndex]
          return [
            `${item?.clientes ?? 0} contratações · ${formatarReal(item?.faturamento)}`,
            `${item?.acumulado ?? 0} clientes acumulados`,
          ]
        },
      },
    },
    rotulosDeValor: { formatar: (valor) => (valor ? String(valor) : '') },
  },
}

/* 5. Histograma do faturamento (regra de Sturges, calculado no Python) */
const dadosHistograma = computed(() => ({
  labels: histograma.value.map(f => `${realCurto(f.de)}–${realCurto(f.ate)}`),
  datasets: [{
    data: histograma.value.map(f => f.clientes),
    backgroundColor: '#1D9E75',
    borderRadius: 3,
    borderSkipped: false,
    categoryPercentage: 1,     // barras coladas: é histograma, não comparação de categorias
    barPercentage: 0.94,
  }],
}))

const opcoesHistograma = {
  scales: {
    x: { ticks: { maxRotation: 0, autoSkip: true, font: { size: 9 } } },
    y: { beginAtZero: true, ticks: { precision: 0 } },
  },
  plugins: {
    tooltip: {
      callbacks: {
        label: (ctx) => {
          const faixa = histograma.value[ctx.dataIndex]
          return `${faixa?.clientes ?? 0} clientes (${percentual(faixa?.percentual)})`
        },
      },
    },
    rotulosDeValor: { formatar: (valor) => (valor ? String(valor) : '') },
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
        <p v-else-if="lote" class="mt-1 text-xs text-zinc-500">
          Base <span class="font-mono text-zinc-400">{{ lote.arquivo }}</span> ·
          atualizada {{ tempoRelativo(lote.enviadoEm) }} ·
          {{ lote.linhasValidas }} de {{ lote.linhasRecebidas }} linhas enviadas
        </p>
      </div>

      <button
        class="flex items-center gap-2 rounded-lg bg-emerald-500 px-4 py-2 text-xs font-medium text-emerald-950 transition-colors hover:bg-emerald-400"
        @click="router.push({ name: 'Upload' })">
        <AppIcon name="plus" class="w-3.5 h-3.5" />
        Enviar planilha
      </button>
    </div>

    <!-- ============ ERRO AO CARREGAR ============ -->
    <div v-if="erro" class="rounded-xl border border-red-500/30 bg-red-500/5 p-5">
      <p class="flex items-center gap-2 text-sm text-red-400">
        <AppIcon name="alert" class="h-4 w-4 shrink-0" />
        {{ erro }}
      </p>
      <button class="mt-3 text-xs text-emerald-400 transition-colors hover:text-emerald-300" @click="carregar">
        Tentar de novo
      </button>
    </div>

    <!-- ============ BASE VAZIA ============ -->
    <div v-else-if="vazio" class="rounded-xl border border-dashed border-zinc-800 bg-zinc-900/40 px-6 py-14 text-center">
      <div class="mx-auto mb-4 flex h-12 w-12 items-center justify-center rounded-full bg-emerald-500/15">
        <AppIcon name="upload" class="h-5 w-5 text-emerald-400" />
      </div>
      <p class="text-sm font-medium text-zinc-100">Nenhuma planilha processada ainda</p>
      <p class="mx-auto mt-2 max-w-sm text-xs leading-relaxed text-zinc-400">
        Envie a planilha da carteira: o Python calcula as estatísticas e os gráficos aparecem aqui.
      </p>
      <button
        class="mt-5 rounded-lg bg-emerald-500 px-5 py-2.5 text-xs font-medium text-emerald-950 transition-colors hover:bg-emerald-400"
        @click="router.push({ name: 'Upload' })">
        Enviar a primeira planilha
      </button>
    </div>

    <template v-else>
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
              <span class="text-emerald-400">{{ item.destaque }}</span>
              <span class="text-zinc-600"> {{ item.contexto }}</span>
            </p>
            <!-- Evolução mês a mês das contratações: dá contexto ao número grande -->
            <Sparkline v-if="item.serie.length > 1" :serie="item.serie" :positiva="true" class="max-w-[5.5rem]" />
          </div>
        </article>
      </section>

      <!-- ============ LEITURA DA CARTEIRA (frases do Python) ============ -->
      <section class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div>
            <h2 class="text-sm font-medium text-zinc-100">Leitura da carteira</h2>
            <p class="mt-0.5 text-[11px] text-zinc-500">Insights gerados pela análise estatística da base atual</p>
          </div>
          <span v-if="pronto" class="rounded-md bg-emerald-500/10 px-2 py-1 font-mono text-[10px] text-emerald-400">
            {{ formatarReal(resumo.faturamentoTotal) }} na carteira
          </span>
        </div>

        <div v-if="carregando" class="mt-5 grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
          <Esqueleto v-for="n in 4" :key="n" class="h-8 w-full" />
        </div>

        <ul v-else class="mt-5 grid grid-cols-1 gap-x-8 gap-y-4 sm:grid-cols-2">
          <li v-for="frase in insights" :key="frase" class="flex gap-3">
            <span class="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-emerald-400"></span>
            <p class="text-xs leading-relaxed text-zinc-400">{{ frase }}</p>
          </li>
        </ul>
      </section>

      <!-- ============ FATURAMENTO POR SEGMENTO + DISTRIBUIÇÃO ============ -->
      <section class="grid grid-cols-1 lg:grid-cols-3 gap-4">

        <!-- Barras verticais -->
        <div class="lg:col-span-2 rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Faturamento por segmento</h2>
          <p class="mt-0.5 text-[11px] text-zinc-500">Faturamento anual somado · clique numa barra para ver os clientes</p>

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
                <span class="font-mono text-lg text-zinc-100">{{ resumo.totalClientes }}</span>
                <span class="text-[9px] text-zinc-500">clientes</span>
              </div>
            </div>

            <ul class="space-y-2.5 text-xs">
              <li v-for="item in porNivel" :key="item.nivel" class="flex items-center gap-2.5">
                <span class="h-2 w-2 shrink-0 rounded-full" :style="{ backgroundColor: COR_NIVEL[item.nivel] }"></span>
                <span class="text-zinc-400">Nível {{ item.nivel }}</span>
                <span class="ml-auto pl-3 font-mono text-zinc-200">{{ percentual(item.percentual, 0) }}</span>
              </li>
            </ul>
          </div>
        </div>
      </section>

      <!-- ============ CONTRATAÇÕES POR MÊS + HISTOGRAMA ============ -->
      <section class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Contratações por mês</h2>
          <p class="mt-0.5 text-[11px] text-zinc-500">Clientes novos pela data de contratação</p>

          <Esqueleto v-if="carregando" class="mt-5 h-48 w-full" arredondado="rounded-lg" />
          <Grafico v-else class="mt-5" tipo="bar" :dados="dadosMes" :opcoes="opcoesMes" altura="12rem" :atraso="40" />
        </div>

        <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Distribuição do faturamento</h2>
          <p class="mt-0.5 text-[11px] text-zinc-500">
            Histograma (regra de Sturges) · quantos clientes em cada faixa de faturamento anual
          </p>

          <Esqueleto v-if="carregando" class="mt-5 h-48 w-full" arredondado="rounded-lg" />
          <template v-else>
            <Grafico class="mt-5" tipo="bar" :dados="dadosHistograma" :opcoes="opcoesHistograma" altura="12rem" :atraso="40" />
            <!-- Resumo do boxplot: os mesmos quartis que definem os outliers -->
            <p v-if="boxplot" class="mt-3 font-mono text-[10px] text-zinc-500">
              Q1 {{ realCurto(boxplot.q1) }} · mediana {{ realCurto(boxplot.mediana) }} ·
              Q3 {{ realCurto(boxplot.q3) }} · {{ boxplot.outliers?.length ?? 0 }} outlier(s)
            </p>
          </template>
        </div>
      </section>

      <!-- ============ SERVIÇOS + ENVIOS RECENTES ============ -->
      <section class="grid grid-cols-1 lg:grid-cols-2 gap-4">

        <!-- Barras horizontais -->
        <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Serviços mais contratados</h2>
          <p class="mt-0.5 text-[11px] text-zinc-500">% dos clientes que têm o serviço · clique para filtrar</p>

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
              <p class="mt-0.5 text-[11px] text-zinc-500">Últimas planilhas enviadas</p>
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
                    <span class="shrink-0 text-[10px] text-zinc-600">{{ envio.quando }}</span>
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
    </template>
  </div>
</template>
