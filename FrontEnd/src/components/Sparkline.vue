<script setup>
import { computed } from 'vue'

/*
  Mini linha de tendência dos cartões de indicador.

    <Sparkline :serie="[3.6, 3.8, 3.7, 4.1, 4.3, 4.8]" :positiva="true" />

  É SVG puro de propósito. O Chart.js desenha os gráficos do painel, mas aqui
  são 28px de altura, sem eixo, sem legenda, sem tooltip — instanciar um motor
  de gráficos quatro vezes para desenhar quatro polilinhas custaria mais do que
  entrega. A regra: gráfico que o usuário LÊ é Chart.js; gráfico que ele só
  SENTE (a forma da curva) é SVG.
*/
const props = defineProps({
  serie: { type: Array, required: true },
  positiva: { type: Boolean, default: true },
})

/* A caixa de desenho é fixa; a série é reescalada para caber nela. */
const LARGURA = 100
const ALTURA = 28

/*
  Cada sparkline precisa de um id próprio para o gradiente: dois <defs> com o
  mesmo id fariam todas herdarem a cor da primeira. O contador é do módulo,
  então incrementa a cada instância criada.
*/
let contador = 0
const idDegrade = `sparkline-degrade-${contador++}`

const pontos = computed(() => {
  const valores = props.serie
  const menor = Math.min(...valores)
  const maior = Math.max(...valores)
  const faixa = maior - menor || 1        // série toda igual: evita divisão por zero

  return valores.map((valor, i) => {
    const x = (i / (valores.length - 1)) * LARGURA
    /* O y do SVG cresce para baixo, então o maior valor precisa do menor y. */
    const y = ALTURA - ((valor - menor) / faixa) * ALTURA
    return { x, y }
  })
})

const linha = computed(() => pontos.value.map(p => `${p.x},${p.y}`).join(' '))

/* A mesma linha, fechada até a base, para o preenchimento suave por baixo. */
const area = computed(() => `0,${ALTURA} ${linha.value} ${LARGURA},${ALTURA}`)

const ultimo = computed(() => pontos.value[pontos.value.length - 1])

/* Verde quando o indicador sobe, terracota quando cai — a mesma leitura do texto de variação. */
const cor = computed(() => props.positiva ? 'var(--color-emerald-400)' : 'var(--color-orange-400)')
</script>

<template>
  <!-- preserveAspectRatio="none" deixa a curva esticar na largura do cartão -->
  <svg
    :viewBox="`0 0 ${LARGURA} ${ALTURA}`"
    class="w-full"
    :style="{ height: `${ALTURA}px` }"
    preserveAspectRatio="none"
    aria-hidden="true">

    <defs>
      <linearGradient :id="idDegrade" x1="0" y1="0" x2="0" y2="1">
        <stop offset="0%" :stop-color="cor" stop-opacity="0.22" />
        <stop offset="100%" :stop-color="cor" stop-opacity="0" />
      </linearGradient>
    </defs>

    <polygon :points="area" :fill="`url(#${idDegrade})`" />

    <!-- non-scaling-stroke: sem isso o esticão horizontal engrossaria a linha -->
    <polyline
      :points="linha"
      fill="none"
      :stroke="cor"
      stroke-width="1.5"
      stroke-linecap="round"
      stroke-linejoin="round"
      vector-effect="non-scaling-stroke" />

    <circle :cx="ultimo.x" :cy="ultimo.y" r="2" :fill="cor" vector-effect="non-scaling-stroke" />
  </svg>
</template>
