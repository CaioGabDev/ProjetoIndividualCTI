import { reactive, watch } from 'vue'

/*
  Preferência de movimento do usuário.

  O padrão ('sistema') segue a configuração do computador — no Windows,
  Acessibilidade > Efeitos visuais > Efeitos de animação. Mas quem apresenta o
  sistema nem sempre está na própria máquina, então dá para forçar os dois lados:

    sistema    → obedece o sistema operacional (padrão, e o certo por acessibilidade)
    ligada     → anima mesmo com o sistema pedindo menos movimento
    desligada  → nunca anima, mesmo com o sistema permitindo
*/

const CHAVE = 'ancora:animacao'
const MODOS = ['sistema', 'ligada', 'desligada']

function carregar() {
  try {
    const salvo = localStorage.getItem(CHAVE)
    return MODOS.includes(salvo) ? salvo : 'sistema'
  } catch {
    return 'sistema'   // navegador com armazenamento bloqueado
  }
}

export const preferencias = reactive({
  animacao: carregar(),
})

/*
  O modo vira um atributo no <html>. É esse atributo que o CSS lê no style.css —
  assim a escolha vale para as animações declaradas em CSS sem uma linha de JS
  no meio do caminho.
*/
function aplicar(modo) {
  document.documentElement.dataset.animacao = modo
  try {
    localStorage.setItem(CHAVE, modo)
  } catch {
    /* sem armazenamento, a escolha vale só nesta sessão */
  }
}

aplicar(preferencias.animacao)
watch(() => preferencias.animacao, aplicar)

/*
  Fonte única da verdade para o JavaScript que anima por conta própria
  (Chart.js, o contador dos indicadores e a diretiva v-revelar).
  O CSS resolve o lado dele sozinho; aqui é o equivalente para o JS.
*/
export function deveAnimar() {
  if (preferencias.animacao === 'ligada') return true
  if (preferencias.animacao === 'desligada') return false
  return !window.matchMedia('(prefers-reduced-motion: reduce)').matches
}
