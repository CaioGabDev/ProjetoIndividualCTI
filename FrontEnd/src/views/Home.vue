<script setup>
import { h, computed } from 'vue'
import MarcaAncora from '../components/MarcaAncora.vue'
import { indicadores, faturamentoPorSegmento } from '../data/mock.js'

/* ---------------- Ícones dos cartões ---------------- */
const criarIcone = (d) => () => h(
  'svg',
  { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8', 'stroke-linecap': 'round', 'stroke-linejoin': 'round' },
  [h('path', { d })],
)

const IconePlanilha = criarIcone('M4 6h16M4 12h16M4 18h16M9 4v16')
const IconeInsight = criarIcone('M3 17l5-5 4 4 8-9')
const IconeNuvem = criarIcone('M6 18a4 4 0 010-8 5 5 0 019.6-1.5A4.5 4.5 0 0118 18H6z')

/*
  A prévia do topo mostra os mesmos números do dashboard: um só lugar define
  os dados, então a página inicial nunca fica desencontrada do produto.
*/
const previaIndicadores = computed(() => indicadores.slice(0, 3))
const maiorSegmento = Math.max(...faturamentoPorSegmento.map(s => s.valor))
const previaBarras = faturamentoPorSegmento.map(item => ({
  ...item,
  altura: `${Math.round((item.valor / maiorSegmento) * 100)}%`,
}))

/* ---------------- Conteúdo ---------------- */
const recursos = [
  {
    titulo: 'Padronização na importação',
    descricao: 'Nomes em caixas diferentes, valores com e sem "R$", datas em três formatos. A leitura do arquivo resolve isso antes do dado entrar na base.',
    icone: IconePlanilha, corIcone: 'text-violet-400', fundoIcone: 'bg-violet-500/10',
  },
  {
    titulo: 'Leitura por segmento e nível',
    descricao: 'Faturamento por segmento, participação de cada serviço na receita e divisão da carteira entre os níveis A, B e C.',
    icone: IconeInsight, corIcone: 'text-emerald-400', fundoIcone: 'bg-emerald-500/10',
  },
  {
    titulo: 'Base única na nuvem',
    descricao: 'Cada envio atualiza a mesma base, com acesso restrito à equipe comercial e registro de quem enviou o quê.',
    icone: IconeNuvem, corIcone: 'text-orange-400', fundoIcone: 'bg-orange-400/10',
  },
]

const passos = [
  { numero: '01', titulo: 'Envie o arquivo', descricao: 'Arraste a planilha da carteira em .xlsx, .xls ou .csv, do jeito que ela está hoje.' },
  { numero: '02', titulo: 'Confira a prévia', descricao: 'O sistema aponta as linhas com problema e mostra os dados já padronizados antes de gravar.' },
  { numero: '03', titulo: 'Acompanhe no painel', descricao: 'Faturamento, segmentos e níveis atualizados, com a carteira completa na aba Clientes.' },
]

const numeros = [
  { valor: 'A · B · C', texto: 'Níveis atribuídos a cada cliente da carteira' },
  { valor: '6', texto: 'Segmentos acompanhados, da indústria à educação' },
  { valor: 'xlsx · csv', texto: 'Formatos aceitos no envio, até 10 MB por arquivo' },
]
</script>

<template>
  <div class="min-h-screen bg-zinc-950 text-zinc-100 overflow-x-hidden">

    <!-- ==================== HEADER ==================== -->
    <header class="sticky top-0 z-30 flex items-center justify-between border-b border-zinc-800 bg-zinc-950/90 px-8 py-4 backdrop-blur-sm">
      <MarcaAncora />

      <nav class="hidden md:flex items-center gap-8 text-sm">
        <a href="#como-funciona" class="text-zinc-400 transition-colors hover:text-zinc-100">Como funciona</a>
        <a href="#recursos" class="text-zinc-400 transition-colors hover:text-zinc-100">Recursos</a>
      </nav>

      <button
        class="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-emerald-950 transition-colors hover:bg-emerald-400"
        @click="$router.push('/login')">
        Entrar
      </button>
    </header>

    <!-- ==================== HERO ==================== -->
    <section class="px-8 pt-16">
      <div class="relative overflow-hidden rounded-2xl border border-zinc-800 bg-zinc-900/40 px-8 pt-20 pb-16 text-center">

        <!-- Fundo: malha fina e um brilho curto atrás do título -->
        <div class="pointer-events-none absolute inset-0 grid-texture"></div>
        <div
          class="pointer-events-none absolute inset-x-0 top-0 h-80"
          style="background: radial-gradient(ellipse 50% 100% at 50% 0%, rgba(29,158,117,0.16), transparent 70%)"></div>

        <!--
          Cascata do hero: mesma animação (animate-surgir) em todos, mudando só
          o animation-delay. O olho lê na ordem em que os elementos aparecem.
        -->
        <span
          class="relative inline-flex items-center gap-2 rounded-full border border-zinc-700 bg-zinc-900 px-3 py-1 text-[11px] text-zinc-400 mb-6 animate-surgir">
          <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
          Uso interno · Equipe comercial da CTI
        </span>

        <h1
          class="relative mx-auto max-w-3xl text-3xl md:text-5xl font-medium leading-[1.15] animate-surgir"
          style="animation-delay: 40ms">
          Transforme planilhas em
          <span class="text-emerald-400">decisões estratégicas</span>
        </h1>

        <p
          class="relative mt-5 mx-auto max-w-xl text-sm md:text-base leading-relaxed text-zinc-400 animate-surgir"
          style="animation-delay: 90ms">
          Envie a planilha da carteira como ela está. A Âncora padroniza os dados na importação
          e devolve faturamento, segmentos e níveis prontos para a reunião comercial.
        </p>

        <div class="relative mt-8 flex flex-wrap items-center justify-center gap-3 animate-surgir" style="animation-delay: 140ms">
          <button
            class="rounded-lg bg-emerald-500 px-5 py-2.5 text-sm font-medium text-emerald-950 transition-colors hover:bg-emerald-400"
            @click="$router.push('/login')">
            Entrar no painel
          </button>
          <a href="#como-funciona" class="rounded-lg border border-zinc-700 px-5 py-2.5 text-sm text-zinc-300 transition-colors hover:border-zinc-600 hover:bg-zinc-800/60">
            Como funciona
          </a>
        </div>

        <!-- Prévia do dashboard -->
        <div class="relative mt-14 mx-auto max-w-3xl animate-surgir" style="animation-delay: 190ms">
          <div class="flex items-center gap-2 rounded-t-xl border border-b-0 border-zinc-800 bg-zinc-900 px-5 py-3 text-left">
            <span class="w-2.5 h-2.5 rounded-full bg-zinc-700"></span>
            <span class="w-2.5 h-2.5 rounded-full bg-zinc-700"></span>
            <span class="w-2.5 h-2.5 rounded-full bg-zinc-700"></span>
            <span class="ml-3 font-mono text-[11px] text-zinc-500">ancora.cti.com.br/app/dashboard</span>
          </div>

          <div class="rounded-b-xl border border-zinc-800 bg-zinc-900/60 px-6 py-6 text-left">
            <div class="grid grid-cols-3 gap-4 mb-6">
              <div v-for="item in previaIndicadores" :key="item.rotulo" class="rounded-lg border border-zinc-800 bg-zinc-950/60 p-3">
                <p class="font-mono text-[10px] uppercase tracking-wider text-zinc-500">{{ item.rotulo }}</p>
                <p class="mt-1 font-mono text-lg text-zinc-100">{{ item.valor }}</p>
              </div>
            </div>

            <div class="flex items-end gap-3 h-24">
              <div
                v-for="barra in previaBarras"
                :key="barra.segmento"
                class="flex-1 rounded-t-md"
                :style="{ height: barra.altura, backgroundColor: barra.cor }">
              </div>
            </div>
            <div class="mt-2 flex gap-3">
              <span
                v-for="barra in previaBarras"
                :key="barra.segmento + '-rotulo'"
                class="flex-1 truncate text-center font-mono text-[9px] text-zinc-600">
                {{ barra.segmento }}
              </span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== COMO FUNCIONA ==================== -->
    <section id="como-funciona" class="px-8 py-24" aria-labelledby="titulo-como-funciona">
      <div class="max-w-4xl mx-auto">
        <p v-revelar class="mb-3 text-center font-mono text-[11px] uppercase tracking-wider text-emerald-400">Como funciona</p>
        <h2 v-revelar="60" id="titulo-como-funciona" class="mb-16 text-center text-2xl md:text-3xl font-medium">
          Três passos entre a planilha e a reunião
        </h2>

        <!-- v-revelar recebe o atraso em ms: 0, 100, 200... -->
        <ol class="grid grid-cols-1 md:grid-cols-3 gap-10">
          <li v-for="(passo, i) in passos" :key="passo.numero" v-revelar="i * 100" class="text-center md:text-left">
            <div class="mx-auto md:mx-0 mb-5 flex h-12 w-12 items-center justify-center rounded-full border border-emerald-500/40 bg-emerald-500/10 font-mono text-sm text-emerald-400">
              {{ passo.numero }}
            </div>
            <h3 class="mb-2 text-sm font-medium text-zinc-100">{{ passo.titulo }}</h3>
            <p class="text-xs leading-relaxed text-zinc-400">{{ passo.descricao }}</p>
          </li>
        </ol>
      </div>
    </section>

    <!-- ==================== RECURSOS ==================== -->
    <section id="recursos" class="px-8 py-10" aria-label="Recursos">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <article
          v-for="(recurso, i) in recursos"
          :key="recurso.titulo"
          v-revelar="i * 100"
          class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5 transition-colors hover:border-zinc-700">
          <div class="mb-4 flex h-9 w-9 items-center justify-center rounded-lg" :class="recurso.fundoIcone">
            <component :is="recurso.icone" class="h-4 w-4" :class="recurso.corIcone" />
          </div>
          <h3 class="mb-2 text-sm font-medium text-zinc-100">{{ recurso.titulo }}</h3>
          <p class="text-xs leading-relaxed text-zinc-400">{{ recurso.descricao }}</p>
        </article>
      </div>
    </section>

    <!-- ==================== NÚMEROS ==================== -->
    <section class="px-8 py-16">
      <div v-revelar class="rounded-2xl border border-zinc-800 bg-zinc-900/40 px-8 py-10">
        <dl class="grid grid-cols-1 md:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-zinc-800 text-center">
          <div
            v-for="(numero, i) in numeros"
            :key="numero.valor"
            :class="['px-4', i === 0 ? 'pb-6 md:pb-0' : i === numeros.length - 1 ? 'pt-6 md:pt-0' : 'py-6 md:py-0']">
            <dt class="font-mono text-3xl text-emerald-400">{{ numero.valor }}</dt>
            <dd class="mt-2 text-xs text-zinc-400">{{ numero.texto }}</dd>
          </div>
        </dl>
      </div>
    </section>

    <!-- ==================== CHAMADA FINAL ==================== -->
    <section class="px-8 pb-20">
      <div v-revelar class="rounded-2xl border border-zinc-800 bg-zinc-900 px-8 py-16 text-center">
        <h2 class="mb-3 text-2xl md:text-3xl font-medium">Comece pela última planilha que você fechou</h2>
        <p class="mx-auto mb-8 max-w-md text-sm text-zinc-400">
          Suba o arquivo da carteira e confira a prévia dos dados tratados antes de gravar qualquer coisa na base.
        </p>
        <button
          class="rounded-lg bg-emerald-500 px-6 py-3 text-sm font-medium text-emerald-950 transition-colors hover:bg-emerald-400"
          @click="$router.push('/login')">
          Entrar e enviar planilha
        </button>
      </div>
    </section>

    <!-- ==================== RODAPÉ ==================== -->
    <footer class="flex flex-wrap items-center justify-between gap-4 border-t border-zinc-800 px-8 py-8 text-xs text-zinc-500">
      <MarcaAncora :subtitulo="false" />
      <span>© {{ new Date().getFullYear() }} CTI · Painel interno da equipe comercial</span>
    </footer>

  </div>
</template>
