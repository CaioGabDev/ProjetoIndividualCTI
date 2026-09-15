/*
  Diretiva v-revelar — o elemento aparece quando o scroll chega nele.

    <article v-revelar>        revela assim que entra na tela
    <article v-revelar="120">  espera 120ms antes de revelar (cascata)

  É o que bibliotecas como o AOS fazem, em ~40 linhas e sem dependência:
  o IntersectionObserver avisa quando o elemento entra na área visível e a
  transição de CSS faz o resto.
*/

import { deveAnimar } from '../services/preferencias.js'

/* Mesma curva do --ease-saida do style.css: arranca rápido, freia longo. */
const CURVA = 'cubic-bezier(0.22, 1, 0.36, 1)'
const DURACAO = 600

/*
  UM observador para a página inteira, criado na primeira vez que for preciso.
  Antes era um observador por elemento — dez elementos, dez observadores, dez
  callbacks separados competindo durante o scroll. Um só recebe todos os
  elementos de uma vez, em um callback, e o navegador agrupa o trabalho.
*/
let observador = null

function obterObservador() {
  if (observador) return observador

  observador = new IntersectionObserver((entradas) => {
    for (const entrada of entradas) {
      /* isIntersecting = o elemento cruzou a borda da tela. */
      if (!entrada.isIntersecting) continue

      const elemento = entrada.target

      /* Estado final. Como há transition, o navegador anima a diferença. */
      elemento.style.opacity = '1'
      elemento.style.transform = 'none'

      /* Revelou uma vez, não precisa mais ser observado. */
      observador.unobserve(elemento)

      /* willChange reserva memória na GPU — devolve quando a animação acaba. */
      elemento.addEventListener('transitionend', () => { elemento.style.willChange = '' }, { once: true })
    }
  }, {
    threshold: 0.12,                      // dispara com 12% do elemento visível
    rootMargin: '0px 0px -40px 0px',      // só quando sobe 40px da borda de baixo
  })

  return observador
}

export const revelar = {
  /* mounted roda uma vez, quando o elemento entra no DOM. */
  mounted(elemento, binding) {
    /* Sem suporte ou com movimento desligado: não mexe em nada e sai. */
    if (!deveAnimar() || !('IntersectionObserver' in window)) return

    const atraso = binding.value || 0

    /*
      Estado inicial: invisível e 14px abaixo do lugar definitivo.
      Só opacity e transform — as duas únicas propriedades que o navegador
      anima sem recalcular layout.
    */
    elemento.style.opacity = '0'
    elemento.style.transform = 'translateY(14px)'
    elemento.style.transition = `opacity ${DURACAO}ms ${CURVA} ${atraso}ms, transform ${DURACAO}ms ${CURVA} ${atraso}ms`
    elemento.style.willChange = 'opacity, transform'

    obterObservador().observe(elemento)
  },

  /* Se a rota mudar antes do elemento aparecer, ele sai da lista do observador. */
  unmounted(elemento) {
    observador?.unobserve(elemento)
  },
}
