<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppIcon from '../components/AppIcon.vue'
import Esqueleto from '../components/Esqueleto.vue'
import { carregarClientes, estiloNivel, formatarReal } from '../data/mock.js'

const route = useRoute()

const niveis = ['A', 'B', 'C']

/*
  Os filtros nascem da URL. É isso que faz o clique no gráfico do dashboard
  funcionar: ele navega para /app/clientes?segmento=Indústria (ou ?nivel=B) e a
  tela já abre filtrada — e o link fica compartilhável.
*/
function nivelDaUrl(valor) {
  return niveis.includes(valor) ? valor : 'todos'
}

/* A carteira chega por uma função assíncrona; até lá, a tabela mostra o esqueleto. */
const lista = ref([])
const carregando = ref(true)

onMounted(async () => {
  const dados = await carregarClientes()
  lista.value = [...dados.clientes]
  carregando.value = false
})
const filtroNivel = ref(nivelDaUrl(route.query.nivel))    // 'todos' | 'A' | 'B' | 'C'
const filtroSegmento = ref(route.query.segmento || '')    // vazio = todos os segmentos
const busca = ref(route.query.q || '')                    // vem da busca da topbar
const pagina = ref(1)
const confirmandoRemocao = ref(null)      // id do cliente aguardando confirmação
const porPagina = 5

/*
  A URL muda por fora da tela em dois casos: a busca da topbar (?q=) e o clique
  num gráfico do dashboard (?nivel= / ?segmento=). Um watcher só na query dá
  conta dos dois.
*/
watch(() => route.query, (query) => {
  busca.value = query.q || ''
  filtroNivel.value = nivelDaUrl(query.nivel)
  filtroSegmento.value = query.segmento || ''
  pagina.value = 1
})

const segmentos = computed(() => [...new Set(lista.value.map(c => c.segmento))].sort())

const filtrados = computed(() => {
  const termo = busca.value.trim().toLowerCase()

  return lista.value.filter(cliente => {
    const passaNivel = filtroNivel.value === 'todos' || cliente.nivel === filtroNivel.value
    const passaSegmento = !filtroSegmento.value || cliente.segmento === filtroSegmento.value
    const passaBusca = !termo
      || cliente.nome.toLowerCase().includes(termo)
      || cliente.segmento.toLowerCase().includes(termo)
      || cliente.servico.toLowerCase().includes(termo)
      || cliente.cnpj.includes(termo)
    return passaNivel && passaSegmento && passaBusca
  })
})

const totalPaginas = computed(() => Math.max(1, Math.ceil(filtrados.value.length / porPagina)))

const visiveis = computed(() => {
  const inicio = (pagina.value - 1) * porPagina
  return filtrados.value.slice(inicio, inicio + porPagina)
})

/* Se um filtro reduzir a lista, volta pra primeira página */
watch([filtroNivel, filtroSegmento], () => { pagina.value = 1 })

/* Trocar de página ou filtrar cancela qualquer remoção pendente */
watch([pagina, filtrados], () => { confirmandoRemocao.value = null })

const contagemPorNivel = computed(() => ({
  todos: lista.value.length,
  A: lista.value.filter(c => c.nivel === 'A').length,
  B: lista.value.filter(c => c.nivel === 'B').length,
  C: lista.value.filter(c => c.nivel === 'C').length,
}))

const somaFiltrada = computed(() => filtrados.value.reduce((total, c) => total + c.faturamento, 0))

/* Iniciais pelo começo de cada palavra: "TechData Serviços" vira "TS" */
function iniciais(nome) {
  const partes = nome.split(' ').filter(parte => parte.length > 2)
  if (!partes.length) return nome.slice(0, 2).toUpperCase()
  return partes.slice(0, 2).map(parte => parte[0]).join('').toUpperCase()
}

function remover(cliente) {
  lista.value = lista.value.filter(c => c.id !== cliente.id)
  confirmandoRemocao.value = null
  if (pagina.value > totalPaginas.value) pagina.value = totalPaginas.value
}

const exportando = ref(false)

/*
  Exporta exatamente o que está filtrado na tela, não a carteira inteira:
  "me manda os clientes nível A de indústria" vira dois cliques.

  O import é dinâmico de propósito. O SheetJS tem ~480kB e esta tela não precisa
  dele para nada além do botão de exportar — com import estático, todo mundo que
  abrisse a lista de clientes baixaria a biblioteca inteira à toa. Assim ela só
  é buscada no primeiro clique.
*/
async function exportar() {
  if (!filtrados.value.length || exportando.value) return

  exportando.value = true
  try {
    const { exportarClientes } = await import('../services/exportar.js')
    exportarClientes(filtrados.value)
  } finally {
    exportando.value = false
  }
}

function limparFiltros() {
  filtroNivel.value = 'todos'
  filtroSegmento.value = ''
  busca.value = ''
}
</script>

<template>
  <div class="space-y-5">

    <!-- ============ CABEÇALHO ============ -->
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h1 class="text-xl font-medium text-zinc-100">Clientes</h1>
        <Esqueleto v-if="carregando" class="mt-2 h-3 w-72" />
        <p v-else class="mt-1 text-xs text-zinc-500">
          {{ lista.length }} clientes na carteira ·
          <span class="font-mono text-zinc-400">{{ formatarReal(somaFiltrada) }}</span> em faturamento no filtro atual
        </p>
      </div>

      <div class="flex items-center gap-2">
        <button
          class="flex items-center gap-2 rounded-lg border border-zinc-800 px-3.5 py-2 text-xs text-zinc-300 transition-colors hover:border-zinc-700 hover:bg-zinc-800/50 disabled:cursor-not-allowed disabled:opacity-40"
          :disabled="!filtrados.length || exportando"
          :title="`Baixar ${filtrados.length} cliente(s) em .xlsx`"
          @click="exportar">
          <AppIcon name="download" class="h-3.5 w-3.5" />
          {{ exportando ? 'Gerando...' : 'Exportar' }}
          <span class="font-mono text-zinc-500">{{ filtrados.length }}</span>
        </button>

        <button class="flex items-center gap-2 rounded-lg bg-emerald-500 px-4 py-2 text-xs font-medium text-emerald-950 transition-colors hover:bg-emerald-400">
          <AppIcon name="plus" class="w-3.5 h-3.5" />
          Novo cliente
        </button>
      </div>
    </div>

    <!-- ============ FILTROS ============ -->
    <div class="flex flex-wrap items-center gap-2">
      <button
        class="rounded-full border px-3.5 py-1.5 text-[11px] transition-colors"
        :class="filtroNivel === 'todos'
          ? 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400'
          : 'border-zinc-800 text-zinc-400 hover:border-zinc-700 hover:text-zinc-100'"
        @click="filtroNivel = 'todos'">
        Todos ({{ contagemPorNivel.todos }})
      </button>

      <button
        v-for="nivel in niveis"
        :key="nivel"
        class="rounded-full border px-3.5 py-1.5 text-[11px] transition-colors"
        :class="filtroNivel === nivel
          ? 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400'
          : 'border-zinc-800 text-zinc-400 hover:border-zinc-700 hover:text-zinc-100'"
        @click="filtroNivel = nivel">
        Nível {{ nivel }} ({{ contagemPorNivel[nivel] }})
      </button>

      <div class="relative">
        <AppIcon name="filter" class="pointer-events-none absolute left-3 top-1/2 h-3 w-3 -translate-y-1/2 text-zinc-600" />
        <select
          v-model="filtroSegmento"
          aria-label="Filtrar por segmento"
          class="appearance-none rounded-full border border-zinc-800 bg-transparent py-1.5 pl-8 pr-7 text-[11px] text-zinc-400 outline-none transition-colors hover:border-zinc-700 hover:text-zinc-100 focus:border-emerald-500/40">
          <option value="">Segmento</option>
          <option v-for="seg in segmentos" :key="seg" :value="seg">{{ seg }}</option>
        </select>
      </div>

      <!-- Deixa visível que há uma busca da topbar ativa -->
      <span v-if="busca" class="ml-1 text-[11px] text-zinc-500">
        Buscando por "<span class="text-zinc-300">{{ busca }}</span>"
        <button class="ml-1 text-emerald-400 transition-colors hover:text-emerald-300" @click="busca = ''">limpar</button>
      </span>
    </div>

    <!-- ============ TABELA ============ -->
    <div class="overflow-hidden rounded-xl border border-zinc-800 bg-zinc-900/40">
      <div class="overflow-x-auto">
        <table class="w-full min-w-[860px] text-xs">
          <thead>
            <tr class="border-b border-zinc-800 font-mono text-[10px] uppercase tracking-wider text-zinc-600">
              <th scope="col" class="px-5 py-3.5 text-left font-normal">Cliente</th>
              <th scope="col" class="px-5 py-3.5 text-left font-normal">Segmento</th>
              <th scope="col" class="px-5 py-3.5 text-left font-normal">Nível</th>
              <th scope="col" class="px-5 py-3.5 text-left font-normal">Serviço principal</th>
              <th scope="col" class="px-5 py-3.5 text-right font-normal">Faturamento</th>
              <th scope="col" class="px-5 py-3.5 text-right font-normal">Ações</th>
            </tr>
          </thead>

          <!-- Carregando: cinco linhas com a mesma altura das de verdade -->
          <tbody v-if="carregando">
            <tr v-for="n in 5" :key="n" class="border-b border-zinc-800/60 last:border-0">
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <Esqueleto class="h-7 w-7" />
                  <div class="flex-1 space-y-1.5">
                    <Esqueleto class="h-3 w-40" />
                    <Esqueleto class="h-2 w-28" />
                  </div>
                </div>
              </td>
              <td class="px-5 py-3.5"><Esqueleto class="h-3 w-20" /></td>
              <td class="px-5 py-3.5"><Esqueleto class="h-5 w-16" /></td>
              <td class="px-5 py-3.5"><Esqueleto class="h-3 w-32" /></td>
              <td class="px-5 py-3.5"><Esqueleto class="ml-auto h-3 w-24" /></td>
              <td class="px-5 py-3.5"><Esqueleto class="ml-auto h-7 w-24" /></td>
            </tr>
          </tbody>

          <!--
            TransitionGroup é o <tbody> de verdade (tag="tbody") e anima os
            filhos que entram, saem ou mudam de lugar na lista. O :key é o que
            deixa o Vue saber qual linha é qual — sem ele, nada anima.
            O prefixo "linha" liga nas classes .linha-enter-from etc. do <style>.
          -->
          <TransitionGroup v-else tag="tbody" name="linha">
            <tr
              v-for="cliente in visiveis"
              :key="cliente.id"
              class="border-b border-zinc-800/60 transition-colors last:border-0 hover:bg-zinc-800/20">

              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="flex h-7 w-7 shrink-0 items-center justify-center rounded-md bg-emerald-500/15 text-[10px] font-medium text-emerald-400">
                    {{ iniciais(cliente.nome) }}
                  </div>
                  <div class="leading-tight">
                    <p class="text-zinc-200">{{ cliente.nome }}</p>
                    <p class="font-mono text-[10px] text-zinc-500">CNPJ {{ cliente.cnpj }}</p>
                  </div>
                </div>
              </td>

              <td class="px-5 py-3.5 text-zinc-400">{{ cliente.segmento }}</td>

              <td class="px-5 py-3.5">
                <span class="rounded-md px-2 py-1 font-mono text-[10px]" :class="estiloNivel[cliente.nivel]">
                  Nível {{ cliente.nivel }}
                </span>
              </td>

              <td class="px-5 py-3.5 text-zinc-400">{{ cliente.servico }}</td>

              <td class="px-5 py-3.5 text-right font-mono text-zinc-200">{{ formatarReal(cliente.faturamento) }}</td>

              <td class="px-5 py-3.5">
                <!-- Confirmação no lugar do alerta do navegador -->
                <div v-if="confirmandoRemocao === cliente.id" class="flex items-center justify-end gap-2 text-[11px]">
                  <span class="text-zinc-400">Remover da carteira?</span>
                  <button
                    class="rounded-md bg-red-500/15 px-2 py-1 text-red-400 transition-colors hover:bg-red-500/25"
                    @click="remover(cliente)">
                    Remover
                  </button>
                  <button
                    class="rounded-md px-2 py-1 text-zinc-500 transition-colors hover:text-zinc-200"
                    @click="confirmandoRemocao = null">
                    Cancelar
                  </button>
                </div>

                <div v-else class="flex items-center justify-end gap-1.5">
                  <button
                    class="flex h-7 w-7 items-center justify-center rounded-md bg-zinc-800/60 text-zinc-500 transition-colors hover:text-emerald-400"
                    :title="`Editar ${cliente.nome}`">
                    <AppIcon name="edit" class="w-3.5 h-3.5" />
                  </button>
                  <button
                    class="flex h-7 w-7 items-center justify-center rounded-md bg-zinc-800/60 text-zinc-500 transition-colors hover:text-violet-400"
                    :title="`Registrar contato com ${cliente.nome}`">
                    <AppIcon name="send" class="w-3.5 h-3.5" />
                  </button>
                  <button
                    class="flex h-7 w-7 items-center justify-center rounded-md bg-zinc-800/60 text-zinc-500 transition-colors hover:text-red-400"
                    :title="`Remover ${cliente.nome}`"
                    @click="confirmandoRemocao = cliente.id">
                    <AppIcon name="trash" class="w-3.5 h-3.5" />
                  </button>
                </div>
              </td>
            </tr>

          </TransitionGroup>

          <!-- Estado vazio em tbody separado: a tabela pode ter mais de um -->
          <tbody v-if="!carregando && !visiveis.length">
            <tr>
              <td colspan="6" class="px-5 py-12 text-center">
                <p class="text-zinc-400">Nenhum cliente encontrado com esses filtros.</p>
                <button class="mt-2 text-[11px] text-emerald-400 transition-colors hover:text-emerald-300" @click="limparFiltros">
                  Limpar filtros
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ============ PAGINAÇÃO ============ -->
      <div class="flex items-center justify-between gap-4 border-t border-zinc-800 px-5 py-3.5">
        <Esqueleto v-if="carregando" class="h-3 w-44" />
        <p v-else class="text-[11px] text-zinc-500">
          Exibindo {{ visiveis.length }} de {{ filtrados.length }} clientes
        </p>

        <div class="flex items-center gap-1">
          <button
            class="flex h-7 w-7 items-center justify-center rounded-md text-zinc-500 transition-colors hover:text-zinc-100 disabled:opacity-30 disabled:hover:text-zinc-500"
            :disabled="pagina === 1"
            aria-label="Página anterior"
            @click="pagina--">
            <AppIcon name="chevronLeft" class="w-3.5 h-3.5" />
          </button>

          <button
            v-for="n in totalPaginas"
            :key="n"
            class="h-7 w-7 rounded-md font-mono text-[11px] transition-colors"
            :class="n === pagina
              ? 'bg-emerald-500 text-emerald-950'
              : 'text-zinc-500 hover:bg-zinc-800/60 hover:text-zinc-100'"
            :aria-current="n === pagina ? 'page' : undefined"
            @click="pagina = n">
            {{ n }}
          </button>

          <button
            class="flex h-7 w-7 items-center justify-center rounded-md text-zinc-500 transition-colors hover:text-zinc-100 disabled:opacity-30 disabled:hover:text-zinc-500"
            :disabled="pagina === totalPaginas"
            aria-label="Próxima página"
            @click="pagina++">
            <AppIcon name="chevronRight" class="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/*
  Nomes vêm do name="linha" do TransitionGroup:
  -enter-from  estado de onde a linha entra
  -leave-to    estado para onde a linha sai
  -enter-active / -leave-active  a transição em si
  -move        usada quando a linha só muda de posição na lista
*/
.linha-enter-from,
.linha-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

.linha-enter-active,
.linha-leave-active {
  transition: opacity 260ms var(--ease-saida), transform 260ms var(--ease-saida);
}

/* O deslize das linhas que sobem quando uma some é o que mais se nota: mais tempo. */
.linha-move {
  transition: transform 400ms var(--ease-saida);
}
</style>
