<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { deveAnimar } from '../services/preferencias.js'

/*
  Conta de zero até o valor final, mantendo prefixo e sufixo.
  Aceita o texto já formatado que vem do mock: 'R$ 4,82M', '312', '28%'.

    <NumeroAnimado texto="R$ 4,82M" />
*/
const props = defineProps({
  texto: { type: String, required: true },
  duracao: { type: Number, default: 700 },
})

/*
  Formatadores guardados fora do componente e reaproveitados.
  Intl.NumberFormat é caro de construir — e "valor.toLocaleString()" constrói um
  novo a CADA chamada. Como a contagem chama isso 60 vezes por segundo em 4
  cartões ao mesmo tempo, era o suficiente para engasgar a animação inteira.
  Aqui cada quantidade de casas decimais é construída uma vez só, e só.
*/
const formatadores = new Map()
function formatador(casas) {
  if (!formatadores.has(casas)) {
    formatadores.set(casas, new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: casas,
      maximumFractionDigits: casas,
    }))
  }
  return formatadores.get(casas)
}

const exibido = ref(props.texto)
let quadro = null   // id do requestAnimationFrame em andamento

/*
  Quebra 'R$ 4,82M' em três pedaços: prefixo 'R$ ', número '4,82' e sufixo 'M'.
  \D* pega tudo que não é dígito no começo, [\d.,]+ pega o número e o resto é sufixo.
*/
function separar(texto) {
  const partes = texto.match(/^(\D*)([\d.,]+)(.*)$/)
  if (!partes) return null

  const [, prefixo, numero, sufixo] = partes
  const casas = numero.includes(',') ? numero.split(',')[1].length : 0     // quantas casas decimais manter
  const valor = Number(numero.replace(/\./g, '').replace(',', '.'))        // '4,82' -> 4.82

  return Number.isNaN(valor) ? null : { prefixo, sufixo, casas, valor }
}

function contar() {
  const alvo = separar(props.texto)
  /* Texto sem número reconhecível, ou movimento desligado: mostra pronto. */
  if (!alvo || !deveAnimar()) {
    exibido.value = props.texto
    return
  }

  const formatar = formatador(alvo.casas)
  const inicio = performance.now()
  let ultimo = null   // último texto escrito, para não mexer no DOM à toa

  /* requestAnimationFrame roda a cada quadro, acompanhando a taxa da tela. */
  const passo = (agora) => {
    const decorrido = Math.min((agora - inicio) / props.duracao, 1)   // 0 -> 1
    const suave = 1 - Math.pow(1 - decorrido, 5)                      // easeOutQuint: mesma curva do CSS

    const texto = alvo.prefixo + formatar.format(alvo.valor * suave) + alvo.sufixo

    /*
      Em números inteiros ('312') vários quadros seguidos dão o mesmo texto.
      Escrever o mesmo valor no DOM obriga o Vue a revisitar o nó sem necessidade.
    */
    if (texto !== ultimo) {
      exibido.value = texto
      ultimo = texto
    }

    if (decorrido < 1) quadro = requestAnimationFrame(passo)
    else exibido.value = props.texto   // no fim, usa o texto original (sem risco de arredondar diferente)
  }

  quadro = requestAnimationFrame(passo)
}

onMounted(contar)
watch(() => props.texto, contar)   // se o valor mudar depois (dados novos), conta de novo

/* Sair da tela no meio da contagem não pode deixar o loop rodando. */
onBeforeUnmount(() => { if (quadro) cancelAnimationFrame(quadro) })
</script>

<template>
  <span>{{ exibido }}</span>
</template>
