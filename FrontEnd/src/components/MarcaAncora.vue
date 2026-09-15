<script setup>
/*
  Marca da Âncora — o mesmo desenho de public/ancora-logo.svg, inline para
  acompanhar a cor do tema e ficar nítido em qualquer tamanho.

  <MarcaAncora />                  símbolo + nome
  <MarcaAncora :subtitulo="false" />  só o nome
  <MarcaAncora tamanho="lg" />     versão do cabeçalho
*/
defineProps({
  tamanho: { type: String, default: 'md' },   // 'md' | 'lg'
  subtitulo: { type: Boolean, default: true },
  /* Quando o fundo já é escuro, o badge some e sobra só o símbolo. */
  badge: { type: Boolean, default: false },
})
</script>

<template>
  <span class="flex items-center gap-2.5">
    <svg
      viewBox="0 0 512 512"
      class="shrink-0"
      :class="tamanho === 'lg' ? 'w-9 h-9' : 'w-7 h-7'"
      aria-hidden="true">
      <rect v-if="badge" width="512" height="512" rx="128" class="fill-zinc-900" />
      <circle cx="256" cy="280" r="112" fill="none" class="stroke-emerald-900" stroke-width="32" />
      <line x1="256" y1="192" x2="256" y2="392" class="stroke-emerald-500" stroke-width="32" stroke-linecap="round" />
      <path d="M 159 224 A 112 112 0 0 0 353 224" fill="none" class="stroke-emerald-500" stroke-width="32" stroke-linecap="round" />
      <circle cx="256" cy="148" r="28" class="fill-zinc-950 stroke-emerald-500" stroke-width="32" />
    </svg>

    <span class="leading-tight">
      <span class="block font-medium text-zinc-100" :class="tamanho === 'lg' ? 'text-base' : 'text-sm'">Âncora</span>
      <span v-if="subtitulo" class="block text-[10px] text-zinc-500">Insights Comerciais</span>
    </span>
  </span>
</template>
