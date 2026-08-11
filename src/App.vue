<template>
  <div class="min-h-screen bg-zinc-950 text-zinc-100">
    <header class="flex items-center justify-between px-8 py-4 border-b border-zinc-800/60">
      <div class="flex items-center gap-2.5">
        <div class="w-7 h-7 rounded-md bg-emerald-500 flex items-center justify-center text-xs font-semibold text-emerald-950">
          A
        </div>
        <div class="leading-tight">
          <p class="text-sm font-medium text-zinc-100">Âncora</p>
          <p class="text-[11px] text-zinc-500">Insights Comerciais</p>
        </div>
      </div>

      <nav class="flex items-center gap-8 text-sm">
        <router-link
          to="/"
          class="text-zinc-100 border-b-2 border-emerald-500 pb-1"
        >
          Início
        </router-link>
        <router-link to="/upload" class="text-zinc-500 hover:text-zinc-200 transition">
          Enviar planilha
        </router-link>
        <router-link to="/dashboard" class="text-zinc-500 hover:text-zinc-200 transition">
          Dashboard
        </router-link>
        <router-link to="/clientes" class="text-zinc-500 hover:text-zinc-200 transition">
          Clientes
        </router-link>
      </nav>
    </header>

    <section class="px-8 pt-10">
      <div
        class="relative overflow-hidden rounded-2xl border border-zinc-800/60 bg-gradient-to-b from-emerald-950/30 via-zinc-900 to-zinc-950 px-8 py-20 text-center"
      >
        <div
          class="pointer-events-none absolute inset-0 bg-[radial-gradient(ellipse_at_center,_rgba(29,158,117,0.12),_transparent_60%)]"
        />

        <h1 class="relative text-3xl md:text-4xl font-medium leading-tight">
          Transforme planilhas em
          <span class="text-emerald-400">decisões estratégicas</span>
        </h1>

        <p class="relative mt-4 max-w-2xl mx-auto text-sm text-zinc-400 leading-relaxed">
          A plataforma que organiza a carteira de clientes da CTI. Envie sua planilha, deixe o
          tratamento automático dos dados por conta do sistema e visualize insights por segmento,
          nível e faturamento em segundos.
        </p>

        <div class="relative mt-8 flex items-center justify-center gap-3">
          <button
            class="rounded-lg bg-emerald-500 px-5 py-2.5 text-sm font-medium text-emerald-950 hover:bg-emerald-400 transition"
            @click="$router.push('/dashboard')"
          >
            Entrar
          </button>
          <button
            class="rounded-lg border border-zinc-700 px-5 py-2.5 text-sm text-zinc-300 hover:bg-zinc-800/60 transition"
          >
            Como funciona
          </button>
        </div>
      </div>
    </section>

    <!-- Feature cards -->
    <section class="px-8 py-10">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <article
          v-for="feature in features"
          :key="feature.title"
          class="rounded-xl border border-zinc-800/60 bg-zinc-900/60 p-5"
        >
          <div
            class="w-9 h-9 rounded-lg flex items-center justify-center mb-4"
            :class="feature.iconBg"
          >
            <component :is="feature.icon" class="w-4 h-4" :class="feature.iconColor" />
          </div>
          <h3 class="text-sm font-medium text-zinc-100 mb-2">{{ feature.title }}</h3>
          <p class="text-xs text-zinc-500 leading-relaxed">{{ feature.description }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup>
import { h } from 'vue'

const ChartIcon = () =>
  h(
    'svg',
    { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8' },
    [h('path', { d: 'M4 20V10M12 20V4M20 20v-7' })]
  )

const InsightIcon = () =>
  h(
    'svg',
    { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8' },
    [h('path', { d: 'M3 17l5-5 4 4 8-9' })]
  )

const CloudIcon = () =>
  h(
    'svg',
    { viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', 'stroke-width': '1.8' },
    [h('path', { d: 'M6 18a4 4 0 010-8 5 5 0 019.6-1.5A4.5 4.5 0 0118 18H6z' })]
  )

const features = [
  {
    title: 'Tratamento automático',
    description:
      'Padronize planilhas preenchidas de forma inconsistente. O sistema limpa, organiza e unifica os dados da sua carteira automaticamente.',
    icon: ChartIcon,
    iconBg: 'bg-purple-500/15',
    iconColor: 'text-purple-400',
  },
  {
    title: 'Insights por segmento',
    description:
      'Descubra quais segmentos, níveis (A/B/C) e serviços concentram mais valor na sua carteira, com gráficos gerados a partir dos seus dados reais.',
    icon: InsightIcon,
    iconBg: 'bg-emerald-500/15',
    iconColor: 'text-emerald-400',
  },
  {
    title: 'Dados seguros na nuvem',
    description:
      'Suas informações ficam armazenadas com segurança em banco de dados na nuvem, com acesso restrito e conforme princípios de privacidade (LGPD).',
    icon: CloudIcon,
    iconBg: 'bg-purple-500/15',
    iconColor: 'text-purple-400',
  },
]
</script>