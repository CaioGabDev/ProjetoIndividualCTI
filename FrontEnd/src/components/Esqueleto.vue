<script setup>
/*
  Bloco cinza pulsando que ocupa o lugar do conteúdo enquanto ele carrega.

    <Esqueleto class="h-4 w-32" />
    <Esqueleto arredondado="rounded-xl" class="h-40" />

  Por que não um "Carregando...": o esqueleto mantém a altura que o conteúdo
  vai ocupar, então a tela não pula quando os dados chegam. O pulso segue a
  preferência de movimento — com animação desligada, fica um bloco estático.
*/
defineProps({
  arredondado: { type: String, default: 'rounded-md' },
})
</script>

<template>
  <div class="animate-pulse bg-zinc-800/60" :class="arredondado" aria-hidden="true"></div>
</template>
