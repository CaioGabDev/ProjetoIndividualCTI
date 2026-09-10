<script setup>
import { ref, computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import AppIcon from '../components/AppIcon.vue'
import { clientes, totalClientes, estiloNivel, formatarReal } from '../data/mock.js'

const route = useRoute()

const lista = ref([...clientes])
const filtroNivel = ref('todos')          // 'todos' | 'A' | 'B' | 'C'
const filtroSegmento = ref('')            // vazio = todos os segmentos
const busca = ref(route.query.q || '')    // vem da busca da topbar
const pagina = ref(1)
const porPagina = 5

const niveis = ['A', 'B', 'C']

/* A busca da topbar altera ?q= — refletir aqui */
watch(() => route.query.q, valor => {
  busca.value = valor || ''
  pagina.value = 1
})

const segmentos = computed(() => [...new Set(clientes.map(c => c.segmento))].sort())

const filtrados = computed(() => {
  const termo = busca.value.trim().toLowerCase()

  return lista.value.filter(cliente => {
    const passaNivel = filtroNivel.value === 'todos' || cliente.nivel === filtroNivel.value
    const passaSegmento = !filtroSegmento.value || cliente.segmento === filtroSegmento.value
    const passaBusca = !termo
      || cliente.nome.toLowerCase().includes(termo)
      || cliente.segmento.toLowerCase().includes(termo)
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

const contagemPorNivel = computed(() => ({
  todos: lista.value.length,
  A: lista.value.filter(c => c.nivel === 'A').length,
  B: lista.value.filter(c => c.nivel === 'B').length,
  C: lista.value.filter(c => c.nivel === 'C').length,
}))

function remover(cliente) {
  if (!confirm(`Remover ${cliente.nome} da carteira?`)) return
  lista.value = lista.value.filter(c => c.id !== cliente.id)
  if (pagina.value > totalPaginas.value) pagina.value = totalPaginas.value
}
</script>

<template>
  <div class="space-y-5">

    <!-- ============ CABEÇALHO ============ -->
    <div class="flex items-start justify-between gap-4">
      <div>
        <h1 class="text-xl font-medium text-zinc-100">Clientes</h1>
        <p class="text-xs text-zinc-500 mt-1">{{ totalClientes }} clientes na carteira</p>
      </div>

      <button class="flex items-center gap-2 rounded-lg bg-emerald-500 px-4 py-2 text-xs font-medium text-emerald-950 hover:bg-emerald-400 transition">
        <AppIcon name="plus" class="w-3.5 h-3.5" />
        Novo cliente
      </button>
    </div>

    <!-- ============ FILTROS ============ -->
    <div class="flex flex-wrap items-center gap-2">
      <button
        class="rounded-full px-3.5 py-1.5 text-[11px] border transition"
        :class="filtroNivel === 'todos'
          ? 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400'
          : 'border-zinc-800 text-zinc-500 hover:text-zinc-300'"
        @click="filtroNivel = 'todos'">
        Todos ({{ contagemPorNivel.todos }})
      </button>

      <button
        v-for="nivel in niveis"
        :key="nivel"
        class="rounded-full px-3.5 py-1.5 text-[11px] border transition"
        :class="filtroNivel === nivel
          ? 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400'
          : 'border-zinc-800 text-zinc-500 hover:text-zinc-300'"
        @click="filtroNivel = nivel">
        Nível {{ nivel }}
      </button>

      <div class="relative">
        <AppIcon name="filter" class="w-3 h-3 absolute left-3 top-1/2 -translate-y-1/2 text-zinc-600 pointer-events-none" />
        <select
          v-model="filtroSegmento"
          class="appearance-none rounded-full border border-zinc-800 bg-transparent py-1.5 pl-8 pr-7 text-[11px] text-zinc-400 outline-none hover:text-zinc-200 focus:border-emerald-500/40 transition">
          <option value="" class="bg-zinc-900">Segmento</option>
          <option v-for="seg in segmentos" :key="seg" :value="seg" class="bg-zinc-900">{{ seg }}</option>
        </select>
      </div>

      <!-- Deixa visível que há uma busca da topbar ativa -->
      <span v-if="busca" class="ml-1 text-[11px] text-zinc-500">
        Buscando por "<span class="text-zinc-300">{{ busca }}</span>"
        <button class="ml-1 text-emerald-400 hover:text-emerald-300" @click="busca = ''">limpar</button>
      </span>
    </div>

    <!-- ============ TABELA ============ -->
    <div class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-xs min-w-[860px]">
          <thead>
            <tr class="text-[10px] font-mono uppercase tracking-wider text-zinc-600 border-b border-zinc-800/60">
              <th class="text-left font-normal px-5 py-3.5">Cliente</th>
              <th class="text-left font-normal px-5 py-3.5">Segmento</th>
              <th class="text-left font-normal px-5 py-3.5">Nível</th>
              <th class="text-left font-normal px-5 py-3.5">Serviço principal</th>
              <th class="text-left font-normal px-5 py-3.5">Faturamento</th>
              <th class="text-right font-normal px-5 py-3.5">Ações</th>
            </tr>
          </thead>

          <tbody>
            <tr
              v-for="cliente in visiveis"
              :key="cliente.id"
              class="border-b border-zinc-800/40 last:border-0 hover:bg-zinc-800/20 transition">

              <td class="px-5 py-3.5">
                <div class="flex items-center gap-3">
                  <div class="w-7 h-7 shrink-0 rounded-md bg-emerald-500/15 flex items-center justify-center text-[10px] font-medium text-emerald-400">
                    {{ cliente.nome.slice(0, 2).toUpperCase() }}
                  </div>
                  <div class="leading-tight">
                    <p class="text-zinc-200">{{ cliente.nome }}</p>
                    <p class="text-[10px] font-mono text-zinc-600">CNPJ {{ cliente.cnpj }}</p>
                  </div>
                </div>
              </td>

              <td class="px-5 py-3.5 text-zinc-400">{{ cliente.segmento }}</td>

              <td class="px-5 py-3.5">
                <span class="rounded-md px-2 py-1 text-[10px] font-mono" :class="estiloNivel[cliente.nivel]">
                  Nível {{ cliente.nivel }}
                </span>
              </td>

              <td class="px-5 py-3.5 text-zinc-400">{{ cliente.servico }}</td>

              <td class="px-5 py-3.5 font-mono text-zinc-200">{{ formatarReal(cliente.faturamento) }}</td>

              <td class="px-5 py-3.5">
                <div class="flex items-center justify-end gap-1.5">
                  <button class="w-7 h-7 rounded-md bg-zinc-800/60 text-zinc-500 hover:text-emerald-400 flex items-center justify-center transition" title="Editar">
                    <AppIcon name="edit" class="w-3.5 h-3.5" />
                  </button>
                  <button class="w-7 h-7 rounded-md bg-zinc-800/60 text-zinc-500 hover:text-violet-400 flex items-center justify-center transition" title="Enviar contato">
                    <AppIcon name="send" class="w-3.5 h-3.5" />
                  </button>
                  <button
                    class="w-7 h-7 rounded-md bg-zinc-800/60 text-zinc-500 hover:text-red-400 flex items-center justify-center transition"
                    title="Remover"
                    @click="remover(cliente)">
                    <AppIcon name="trash" class="w-3.5 h-3.5" />
                  </button>
                </div>
              </td>
            </tr>

            <!-- Nenhum resultado -->
            <tr v-if="!visiveis.length">
              <td colspan="6" class="px-5 py-10 text-center text-zinc-600">
                Nenhum cliente encontrado com esses filtros.
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- ============ PAGINAÇÃO ============ -->
      <div class="flex items-center justify-between gap-4 px-5 py-3.5 border-t border-zinc-800/60">
        <p class="text-[11px] text-zinc-600">
          Exibindo {{ visiveis.length }} de {{ filtrados.length }} clientes
        </p>

        <div class="flex items-center gap-1">
          <button
            class="w-7 h-7 rounded-md text-zinc-500 hover:text-zinc-200 disabled:opacity-30 flex items-center justify-center transition"
            :disabled="pagina === 1"
            @click="pagina--">
            <AppIcon name="chevronLeft" class="w-3.5 h-3.5" />
          </button>

          <button
            v-for="n in totalPaginas"
            :key="n"
            class="w-7 h-7 rounded-md text-[11px] font-mono transition"
            :class="n === pagina
              ? 'bg-emerald-500 text-emerald-950'
              : 'text-zinc-500 hover:text-zinc-200 hover:bg-zinc-800/60'"
            @click="pagina = n">
            {{ n }}
          </button>

          <button
            class="w-7 h-7 rounded-md text-zinc-500 hover:text-zinc-200 disabled:opacity-30 flex items-center justify-center transition"
            :disabled="pagina === totalPaginas"
            @click="pagina++">
            <AppIcon name="chevronRight" class="w-3.5 h-3.5" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
