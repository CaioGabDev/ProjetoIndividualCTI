<script setup>
/*
  Ícones em SVG inline (sem dependência externa).
  Uso: <AppIcon name="dashboard" class="w-4 h-4" />
*/
defineProps({
  name: { type: String, required: true },
})

const paths = {
  dashboard: 'M4 20V10M12 20V4M20 20v-7',
  clientes: 'M16 19v-1.5a3.5 3.5 0 00-3.5-3.5h-5A3.5 3.5 0 004 17.5V19M10 11a3.5 3.5 0 100-7 3.5 3.5 0 000 7M20 19v-1.5a3.5 3.5 0 00-2.6-3.4M15.5 4.2a3.5 3.5 0 010 6.6',
  upload: 'M12 16V4m0 0L8 8m4-4l4 4M4 17v1.5A2.5 2.5 0 006.5 21h11a2.5 2.5 0 002.5-2.5V17',
  config: 'M12 15a3 3 0 100-6 3 3 0 000 6zM19.4 15a1.6 1.6 0 00.3 1.8l.1.1a2 2 0 11-2.8 2.8l-.1-.1a1.6 1.6 0 00-2.7 1.1v.2a2 2 0 11-4 0v-.1a1.6 1.6 0 00-2.7-1.2l-.1.1a2 2 0 11-2.8-2.8l.1-.1A1.6 1.6 0 004 15a2 2 0 01-2-2 2 2 0 012-2 1.6 1.6 0 001.1-2.7l-.1-.1a2 2 0 112.8-2.8l.1.1A1.6 1.6 0 009 4.6V4a2 2 0 114 0v.1a1.6 1.6 0 002.7 1.2l.1-.1a2 2 0 112.8 2.8l-.1.1A1.6 1.6 0 0020 11h.2a2 2 0 110 4H20a1.6 1.6 0 00-.6.1z',
  search: 'M11 18a7 7 0 100-14 7 7 0 000 14zM20 20l-4-4',
  bell: 'M18 8a6 6 0 10-12 0c0 6-2 7-2 7h16s-2-1-2-7M13.7 20a1.9 1.9 0 01-3.4 0',
  logout: 'M15 17l5-5-5-5M20 12H9M12 4H6a2 2 0 00-2 2v12a2 2 0 002 2h6',
  edit: 'M4 20h4l10-10a2.1 2.1 0 00-3-3L5 17v3z',
  send: 'M21 3L10.5 13.5M21 3l-6.5 18-4-8-8-4L21 3z',
  trash: 'M4 7h16M9 7V5a1 1 0 011-1h4a1 1 0 011 1v2M6 7l1 13a1 1 0 001 1h8a1 1 0 001-1l1-13',
  plus: 'M12 5v14M5 12h14',
  check: 'M4 12.5l5 5L20 7',
  file: 'M14 3H7a2 2 0 00-2 2v14a2 2 0 002 2h10a2 2 0 002-2V8l-5-5zM14 3v5h5',
  filter: 'M3 5h18l-7 8v5l-4 2v-7L3 5z',
  clock: 'M12 21a9 9 0 100-18 9 9 0 000 18zM12 7v5l3 2',
  shield: 'M12 3l8 3v5c0 5-3.4 8.7-8 10-4.6-1.3-8-5-8-10V6l8-3z',
  alert: 'M12 8v5M12 17h.01M10.3 3.9L2.4 17a2 2 0 001.7 3h15.8a2 2 0 001.7-3L13.7 3.9a2 2 0 00-3.4 0z',
  download: 'M12 4v10m0 0l-3.5-3.5M12 14l3.5-3.5M5 18h14',
  menu: 'M4 7h16M4 12h16M4 17h16',
  fechar: 'M6 6l12 12M18 6L6 18',
  chevronLeft: 'M14 7l-5 5 5 5',
  chevronRight: 'M10 7l5 5-5 5',
}
</script>

<template>
  <svg
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.7"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true">
    <path :d="paths[name]" />
  </svg>
</template>
