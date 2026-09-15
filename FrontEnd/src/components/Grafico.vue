<script setup>
import { onMounted, onBeforeUnmount, ref, watch } from 'vue'
import {
  Chart,
  BarController, BarElement,
  DoughnutController, ArcElement,
  CategoryScale, LinearScale,
  Tooltip,
} from 'chart.js'
import { deveAnimar } from '../services/preferencias.js'

/*
  Casca do Chart.js para o projeto.

  O import é peça por peça de propósito: o Chart.js só entra no bundle com o
  que a gente registra aqui. Importar o pacote inteiro ("chart.js/auto")
  traria controllers de linha, radar, bolha e polar que este painel não usa.
*/
Chart.register(
  BarController, BarElement,
  DoughnutController, ArcElement,
  CategoryScale, LinearScale,
  Tooltip,
)

/*
  Plugin: escreve o valor de cada barra no fim da animação.

  Fica desligado por padrão e liga por gráfico, passando a função de formatação:
    plugins: { rotulosDeValor: { formatar: (v) => `R$ ${v}` } }

  Sem isso, ler o valor exato só passando o mouse — o que não existe em celular
  e não aparece em print de apresentação.
*/
const rotulosDeValor = {
  id: 'rotulosDeValor',

  afterDatasetsDraw(chart, args, opcoes) {
    /* Enquanto as barras crescem, o rótulo ficaria pulando junto: espera acabar. */
    if (!opcoes?.formatar || chart.$animando) return

    const { ctx } = chart
    const raiz = getComputedStyle(document.documentElement)
    const deitado = chart.options.indexAxis === 'y'

    ctx.save()
    ctx.fillStyle = raiz.getPropertyValue('--color-zinc-400').trim()
    ctx.font = `10px ${raiz.getPropertyValue('--font-mono').trim()}`
    ctx.textAlign = deitado ? 'left' : 'center'
    ctx.textBaseline = deitado ? 'middle' : 'bottom'

    chart.getDatasetMeta(0).data.forEach((barra, i) => {
      const texto = opcoes.formatar(chart.data.datasets[0].data[i])
      /* Barra em pé: rótulo acima do topo. Barra deitada: à direita da ponta. */
      if (deitado) ctx.fillText(texto, barra.x + 6, barra.y)
      else ctx.fillText(texto, barra.x, barra.y - 6)
    })

    ctx.restore()
  },
}

Chart.register(rotulosDeValor)

const props = defineProps({
  tipo: { type: String, required: true },      // 'bar' | 'doughnut'
  dados: { type: Object, required: true },     // { labels, datasets }
  opcoes: { type: Object, default: () => ({}) },
  altura: { type: String, default: '12rem' },
  /* Atraso em ms entre um item e o outro na entrada (0 = todos juntos) */
  atraso: { type: Number, default: 70 },
})

const canvas = ref(null)
let grafico = null

/*
  Lê uma variável do design system em tempo de execução.
  Assim o gráfico usa exatamente a mesma cor do resto da interface: mudou o
  token no style.css, mudou o gráfico — sem hexadecimal repetido no JS.
*/
function token(nome) {
  return getComputedStyle(document.documentElement).getPropertyValue(nome).trim()
}

/*
  Entrada em cascata: cada barra/fatia espera um pouco mais que a anterior.
  O Chart.js aceita `delay` como função, chamada para cada elemento.

  Os três guardas importam:
  - type === 'data'     ignora a animação dos eixos e da grade
  - mode === 'default'  só na entrada; ao filtrar ou passar o mouse, sem atraso
  - !ctx.dropped        a função é chamada a cada quadro; sem essa marca, o
                        atraso seria recalculado e a barra nunca terminaria
*/
function animacao() {
  /* Movimento desligado (pelo sistema ou pelo usuário): gráfico aparece pronto. */
  if (!deveAnimar()) return false

  const atraso = props.atraso

  return {
    duration: 700,
    easing: 'easeOutQuart',
    delay: (ctx) => {
      if (ctx.type !== 'data' || ctx.mode !== 'default' || ctx.dropped) return 0
      ctx.dropped = true
      return ctx.dataIndex * atraso
    },
  }
}

function opcoesBase() {
  const tinta = token('--color-zinc-500')
  const grade = token('--color-zinc-800')

  return {
    responsive: true,
    maintainAspectRatio: false,      // quem manda na altura é o contêiner
    animation: animacao(),

    /*
      Só vira cursor de mão em cima de uma barra/fatia clicável — e só quando a
      tela passou um onClick. Sem isso o gráfico inteiro finge ser clicável.
    */
    onHover: (evento, elementos) => {
      const alvo = evento?.native?.target
      if (!props.opcoes.onClick || !alvo) return
      alvo.style.cursor = elementos.length ? 'pointer' : 'default'
    },
    plugins: {
      legend: { display: false },    // as legendas do painel são feitas em HTML, para serem legíveis
      tooltip: {
        backgroundColor: token('--color-zinc-900'),
        borderColor: grade,
        borderWidth: 1,
        titleColor: token('--color-zinc-100'),
        bodyColor: token('--color-zinc-300'),
        padding: 10,
        displayColors: false,
        titleFont: { family: token('--font-sans'), size: 12 },
        bodyFont: { family: token('--font-mono'), size: 11 },
      },
    },
    scales: props.tipo === 'doughnut' ? undefined : {
      x: {
        border: { display: false },
        grid: { display: false },
        ticks: { color: tinta, font: { family: token('--font-sans'), size: 10 } },
      },
      y: {
        border: { display: false },
        grid: { color: grade, drawTicks: false },
        ticks: { color: tinta, font: { family: token('--font-mono'), size: 10 }, padding: 8 },
      },
    },
  }
}

/* Junta as opções base com as que a tela mandou (as da tela têm prioridade). */
function mesclar(base, extra) {
  const saida = { ...base }
  for (const [chave, valor] of Object.entries(extra)) {
    saida[chave] = valor && typeof valor === 'object' && !Array.isArray(valor)
      ? mesclar(base[chave] || {}, valor)
      : valor
  }
  return saida
}

function criar() {
  const opcoes = mesclar(opcoesBase(), props.opcoes)

  /*
    O plugin de rótulos espera a animação terminar. Quem avisa é o onComplete —
    e, se não há animação, já nasce liberado.
  */
  const anima = Boolean(opcoes.animation)
  if (anima) {
    opcoes.animation.onComplete = () => {
      if (!grafico?.$animando) return
      grafico.$animando = false
      grafico.draw()          // um último desenho, agora com os rótulos
    }
  }

  grafico = new Chart(canvas.value, {
    type: props.tipo,
    data: props.dados,
    options: opcoes,
  })

  grafico.$animando = anima
}

onMounted(criar)

/* Dados novos (outra planilha, outro filtro): atualiza sem recriar o gráfico. */
watch(() => props.dados, (novos) => {
  if (!grafico) return
  grafico.data = novos
  grafico.update()
}, { deep: true })

/* Chart.js guarda listeners no canvas — sem destroy, vaza a cada troca de rota. */
onBeforeUnmount(() => grafico?.destroy())
</script>

<template>
  <div :style="{ height: altura }">
    <canvas ref="canvas"></canvas>
  </div>
</template>
