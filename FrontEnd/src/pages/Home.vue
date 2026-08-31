<template>
  <div class="min-h-screen bg-zinc-950 text-zinc-100 overflow-x-hidden">

    <!-- ==================== HEADER ==================== -->
    <header class="flex items-center justify-between px-8 py-4 border-b border-zinc-800/60 sticky top-0 z-30 bg-zinc-950/80 backdrop-blur-md">
      <div class="flex items-center gap-2.5">
        <div class="w-7 h-7 rounded-md bg-emerald-500 flex items-center justify-center text-xs font-semibold text-emerald-950">A</div>
        <div class="leading-tight">
          <p class="text-sm font-medium text-zinc-100">Âncora</p>
          <p class="text-[11px] text-zinc-500">Insights Comerciais</p>
        </div>
      </div>

      <nav class="hidden md:flex items-center gap-8 text-sm">
        <a href="#como-funciona" class="text-zinc-500 hover:text-zinc-200 transition">Como funciona</a>
        <a href="#recursos" class="text-zinc-500 hover:text-zinc-200 transition">Recursos</a>
      </nav>

      <button
        class="rounded-lg bg-emerald-500 px-4 py-2 text-sm font-medium text-emerald-950 hover:bg-emerald-400 transition"
        @click="$router.push('/login')">
        Entrar
      </button>
    </header>

    <!-- ==================== HERO ==================== -->
    <section class="px-8 pt-16">
      <div class="relative overflow-hidden rounded-2xl border border-zinc-800/60 bg-gradient-to-b from-emerald-950/30 via-zinc-900 to-zinc-950 px-8 pt-20 pb-16 text-center">

        <div class="pointer-events-none absolute inset-0" :style="heroGlow"></div>

        <span class="relative inline-flex items-center gap-2 rounded-full border border-zinc-700/80 bg-zinc-900/60 px-3 py-1 text-[11px] text-zinc-400 mb-6">
          <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
          Feito para a carteira comercial da CTI
        </span>

        <h1 class="relative text-3xl md:text-5xl font-medium leading-tight max-w-3xl mx-auto">
          Transforme planilhas em
          <span class="text-emerald-400">decisões estratégicas</span>
        </h1>

        <p class="relative mt-5 max-w-2xl mx-auto text-sm md:text-base text-zinc-400 leading-relaxed">
          A plataforma que organiza a carteira de clientes da CTI. Envie sua planilha, deixe o
          tratamento automático dos dados por conta do sistema e visualize insights por segmento,
          nível e faturamento em segundos.
        </p>

        <div class="relative mt-8 flex items-center justify-center gap-3">
          <button
            class="rounded-lg bg-emerald-500 px-5 py-2.5 text-sm font-medium text-emerald-950 hover:bg-emerald-400 transition"
            @click="$router.push('/login')">
            Entrar
          </button>
          <a href="#como-funciona" class="rounded-lg border border-zinc-700 px-5 py-2.5 text-sm text-zinc-300 hover:bg-zinc-800/60 transition">
            Como funciona
          </a>
        </div>

        <!-- Preview do dashboard -->
        <div class="relative mt-14 mx-auto max-w-3xl">
          <div class="rounded-t-xl border border-b-0 border-zinc-800 bg-zinc-900/90 px-5 py-3 flex items-center gap-2 text-left">
            <span class="w-2.5 h-2.5 rounded-full bg-zinc-700"></span>
            <span class="w-2.5 h-2.5 rounded-full bg-zinc-700"></span>
            <span class="w-2.5 h-2.5 rounded-full bg-zinc-700"></span>
            <span class="ml-3 text-[11px] font-mono text-zinc-500">ancora.app/dashboard</span>
          </div>

          <div class="rounded-b-xl border border-zinc-800 bg-zinc-900/60 px-6 py-6 text-left">
            <div class="grid grid-cols-3 gap-4 mb-6">
              <div class="rounded-lg border border-zinc-800/80 bg-zinc-950/60 p-3">
                <p class="text-[10px] text-zinc-500 font-mono uppercase">Faturamento</p>
                <p class="text-lg font-mono text-zinc-100 mt-1">R$ 4,82M</p>
              </div>
              <div class="rounded-lg border border-zinc-800/80 bg-zinc-950/60 p-3">
                <p class="text-[10px] text-zinc-500 font-mono uppercase">Clientes ativos</p>
                <p class="text-lg font-mono text-zinc-100 mt-1">312</p>
              </div>
              <div class="rounded-lg border border-zinc-800/80 bg-zinc-950/60 p-3">
                <p class="text-[10px] text-zinc-500 font-mono uppercase">Nível A</p>
                <p class="text-lg font-mono text-zinc-100 mt-1">28%</p>
              </div>
            </div>

            <div class="flex items-end gap-3 h-24">
              <div
                v-for="bar in previewBars"
                :key="bar.label"
                class="flex-1 rounded-t-md"
                :style="{ height: bar.height, backgroundColor: bar.color }">
              </div>
            </div>
            <div class="flex gap-3 mt-2">
              <span v-for="bar in previewBars" :key="bar.label + '-l'" class="flex-1 text-center text-[9px] text-zinc-600 font-mono">{{ bar.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== COMO FUNCIONA ==================== -->
    <section id="como-funciona" class="px-8 py-24">
      <div class="max-w-4xl mx-auto">
        <p class="text-center text-[11px] font-mono uppercase tracking-wider text-emerald-400 mb-3">Como funciona</p>
        <h2 class="text-center text-2xl md:text-3xl font-medium mb-16">Da planilha bruta ao insight, em três passos</h2>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-10">
          <div v-for="step in steps" :key="step.number" class="text-center md:text-left">
            <div class="w-12 h-12 rounded-full flex items-center justify-center mx-auto md:mx-0 mb-5 font-mono text-sm border-2 border-emerald-500/50 text-emerald-400 bg-emerald-500/10">
              {{ step.number }}
            </div>
            <h3 class="text-sm font-medium text-zinc-100 mb-2">{{ step.title }}</h3>
            <p class="text-xs text-zinc-500 leading-relaxed">{{ step.description }}</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== FEATURE CARDS ==================== -->
    <section id="recursos" class="px-8 py-10">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <article
          v-for="feature in features"
          :key="feature.title"
          class="rounded-xl border border-zinc-800/60 bg-zinc-900/60 p-5 transition hover:border-zinc-700">
          <div class="w-9 h-9 rounded-lg flex items-center justify-center mb-4" :class="feature.iconBg">
            <component :is="feature.icon" class="w-4 h-4" :class="feature.iconColor" />
          </div>
          <h3 class="text-sm font-medium text-zinc-100 mb-2">{{ feature.title }}</h3>
          <p class="text-xs text-zinc-500 leading-relaxed">{{ feature.description }}</p>
        </article>
      </div>
    </section>

    <!-- ==================== STATS ==================== -->
    <section class="px-8 py-16">
      <div class="rounded-2xl border border-zinc-800/60 bg-zinc-900/40 px-8 py-10">
        <div class="grid grid-cols-1 md:grid-cols-3 divide-y md:divide-y-0 md:divide-x divide-zinc-800/60 text-center">
          <div class="pb-6 md:pb-0">
            <p class="text-3xl font-mono text-emerald-400">A · B · C</p>
            <p class="text-xs text-zinc-500 mt-2">Classificação automática por nível</p>
          </div>
          <div class="py-6 md:py-0">
            <p class="text-3xl font-mono text-emerald-400">100%</p>
            <p class="text-xs text-zinc-500 mt-2">Dos dados tratados automaticamente</p>
          </div>
          <div class="pt-6 md:pt-0">
            <p class="text-3xl font-mono text-emerald-400">LGPD</p>
            <p class="text-xs text-zinc-500 mt-2">Armazenamento seguro e restrito</p>
          </div>
        </div>
      </div>
    </section>

    <!-- ==================== CTA FINAL ==================== -->
    <section class="px-8 pb-20">
      <div class="rounded-2xl border border-zinc-800/60 bg-zinc-900 px-8 py-16 text-center">
        <h2 class="text-2xl md:text-3xl font-medium mb-3">Pronto para organizar sua carteira?</h2>
        <p class="text-sm text-zinc-400 max-w-md mx-auto mb-8">
          Envie sua primeira planilha e veja os insights da sua carteira em minutos.
        </p>
        <button
          class="rounded-lg bg-emerald-500 px-6 py-3 text-sm font-medium text-emerald-950 hover:bg-emerald-400 transition"
          @click="$router.push('/login')">
          Começar agora
        </button>
      </div>
    </section>

    <!-- ==================== FOOTER ==================== -->
    <footer class="px-8 py-8 border-t border-zinc-800/60 flex items-center justify-between text-xs text-zinc-600">
      <div class="flex items-center gap-2.5">
        <div class="w-6 h-6 rounded-md bg-emerald-500 flex items-center justify-center text-[10px] font-semibold text-emerald-950">A</div>
        <span>Âncora — Insights Comerciais</span>
      </div>
      <span>© {{ new Date().getFullYear() }} CTI. Todos os direitos reservados.</span>
    </footer>

  </div>
</template>

<script setup>
import { h } from 'vue'

/* ---------------- Ícones ---------------- */
const ChartIcon = () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8' }, [h('path', { d: 'M4 20V10M12 20V4M20 20v-7' })])
const InsightIcon = () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8' }, [h('path', { d: 'M3 17l5-5 4 4 8-9' })])
const CloudIcon = () => h('svg', { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8' }, [h('path', { d: 'M6 18a4 4 0 010-8 5 5 0 019.6-1.5A4.5 4.5 0 0118 18H6z' })])

/* ---------------- Glow do hero ---------------- */
const heroGlow = { background: 'radial-gradient(ellipse at center, rgba(29,158,117,0.14), transparent 60%)' }

/* ---------------- Conteúdo ---------------- */
const features = [
  {
    title: 'Tratamento automático',
    description: 'Padronize planilhas preenchidas de forma inconsistente. O sistema limpa, organiza e unifica os dados da sua carteira automaticamente.',
    icon: ChartIcon, iconBg: 'bg-purple-500/15', iconColor: 'text-purple-400',
  },
  {
    title: 'Insights por segmento',
    description: 'Descubra quais segmentos, níveis (A/B/C) e serviços concentram mais valor na sua carteira, com gráficos gerados a partir dos seus dados reais.',
    icon: InsightIcon, iconBg: 'bg-emerald-500/15', iconColor: 'text-emerald-400',
  },
  {
    title: 'Dados seguros na nuvem',
    description: 'Suas informações ficam armazenadas com segurança em banco de dados na nuvem, com acesso restrito e conforme princípios de privacidade (LGPD).',
    icon: CloudIcon, iconBg: 'bg-orange-500/15', iconColor: 'text-orange-400',
  },
]

const steps = [
  { number: '01', title: 'Envie sua planilha', description: 'Arraste o arquivo .xlsx ou .csv da sua carteira de clientes, do jeito que ele estiver hoje.' },
  { number: '02', title: 'Tratamento automático', description: 'O sistema limpa inconsistências, padroniza formatos e organiza os dados sem trabalho manual.' },
  { number: '03', title: 'Veja os insights', description: 'Acesse o dashboard com faturamento, segmentos e níveis (A/B/C) prontos para decisão.' },
]

/* Gráfico do preview — uma única escala de verde, do mais forte pro mais claro */
const previewBars = [
  { label: 'Indústria', height: '100%', color: '#1D9E75' },
  { label: 'Varejo', height: '70%', color: '#1D9E75CC' },
  { label: 'Serviços', height: '61%', color: '#1D9E75AA' },
  { label: 'Saúde', height: '46%', color: '#1D9E7588' },
  { label: 'Agro', height: '38%', color: '#1D9E7566' },
  { label: 'Educação', height: '29%', color: '#1D9E754D' },
]
</script>