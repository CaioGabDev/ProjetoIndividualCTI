<script setup>
import { computed, ref, watch } from 'vue'
import AppIcon from '../components/AppIcon.vue'
import { useUploadStore } from '../stores/uploadStore.js'
import { exportarDadosTratados } from '../services/exportar.js'

/*
  Tela de relatório da validação.

  Ela não lê arquivo nem valida nada: tudo que aparece aqui vem pronto dos
  getters do Pinia, preenchidos pela tela de envio. É o último passo do fluxo
  Planilha → Upload → Pinia → Relatório.
*/
const upload = useUploadStore()

const LOTE_DETALHE = 100   // a lista cresce de 100 em 100 para não montar 5 mil <tr> de uma vez

const tipoSelecionado = ref('todos')
const quantosMostrar = ref(LOTE_DETALHE)

/* Trocar de filtro sempre recomeça a lista do topo. */
watch(tipoSelecionado, () => { quantosMostrar.value = LOTE_DETALHE })

/*
  Cor por tipo de problema. Cinza é proposital: são os dois casos que o
  sistema já corrigiu sozinho na importação — o relatório só avisa que mexeu.
*/
const estiloTipo = {
  obrigatorio:  'bg-red-500/15 text-red-400',
  formato:      'bg-orange-400/15 text-orange-400',
  email:        'bg-violet-500/15 text-violet-400',
  duplicado:    'bg-amber-500/15 text-amber-400',
  espacos:      'bg-zinc-800 text-zinc-400',
  padronizacao: 'bg-zinc-800 text-zinc-400',
  arquivo:      'bg-red-500/15 text-red-400',
}

/* tipo -> rótulo legível, montado a partir do próprio getter do store. */
const rotuloTipo = computed(() => {
  const mapa = { arquivo: 'Problema no arquivo' }
  for (const item of upload.errosPorTipo) mapa[item.tipo] = item.rotulo
  return mapa
})

/* Só os tipos que realmente apareceram viram filtro — nada de chip zerado. */
const tiposComOcorrencia = computed(() =>
  upload.errosPorTipo.filter(item => item.quantidade > 0),
)

const errosFiltrados = computed(() => tipoSelecionado.value === 'todos'
  ? upload.erros
  : upload.erros.filter(erro => erro.tipo === tipoSelecionado.value),
)

const errosVisiveis = computed(() => errosFiltrados.value.slice(0, quantosMostrar.value))

/* Os 4 cartões do topo. `tom` só decide a cor do número. */
const cartoes = computed(() => [
  { rotulo: 'Total de registros',  valor: upload.totalRegistros,    tom: 'text-zinc-100' },
  { rotulo: 'Registros válidos',   valor: upload.registrosValidos,  tom: 'text-emerald-400' },
  { rotulo: 'Registros com erro',  valor: upload.registrosComErro,  tom: 'text-red-400' },
  { rotulo: 'Aproveitamento',      valor: `${upload.percentualValidos}%`, tom: 'text-zinc-100' },
])

function formatarTamanho(bytes) {
  if (!bytes) return '—'
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)}KB`
  return `${(bytes / 1024 / 1024).toFixed(1)}MB`
}

function formatarMomento(data) {
  return data?.toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) || '—'
}
</script>

<template>
  <div class="space-y-5">

    <!-- ============ CABEÇALHO ============ -->
    <div class="flex flex-wrap items-start justify-between gap-3">
      <div>
        <h1 class="text-xl font-medium text-zinc-100">Relatório de validação</h1>
        <p class="mt-1 text-xs text-zinc-500">
          O que o sistema encontrou na última planilha analisada, linha a linha
        </p>
      </div>

      <div v-if="upload.temRelatorio" class="flex items-center gap-2">
        <router-link
          :to="{ name: 'Upload' }"
          class="flex items-center gap-2 rounded-lg border border-zinc-800 px-3.5 py-2 text-xs text-zinc-300 transition-colors hover:border-zinc-700 hover:bg-zinc-800/50">
          <AppIcon name="upload" class="h-3.5 w-3.5" />
          Analisar outra
        </router-link>

        <button
          v-if="upload.temDados"
          class="flex items-center gap-2 rounded-lg border border-zinc-800 px-3.5 py-2 text-xs text-zinc-300 transition-colors hover:border-zinc-700 hover:bg-zinc-800/50"
          @click="exportarDadosTratados(upload.dadosTratados)">
          <AppIcon name="download" class="h-3.5 w-3.5" />
          Baixar planilha tratada
        </button>
      </div>
    </div>

    <!-- ============ SEM ANÁLISE AINDA ============ -->
    <div
      v-if="!upload.temRelatorio"
      class="rounded-xl border border-zinc-800 bg-zinc-900/40 px-6 py-16 text-center">

      <div class="mx-auto mb-5 flex h-12 w-12 items-center justify-center rounded-full bg-zinc-800/60">
        <AppIcon name="file" class="h-5 w-5 text-zinc-500" />
      </div>

      <p class="text-sm font-medium text-zinc-100">Nenhuma planilha analisada ainda</p>
      <p class="mx-auto mt-2 max-w-sm text-xs leading-relaxed text-zinc-400">
        Os dados da validação ficam guardados enquanto a aba estiver aberta. Envie
        uma planilha para o relatório aparecer aqui.
      </p>

      <router-link
        :to="{ name: 'Upload' }"
        class="mt-6 inline-block rounded-lg bg-emerald-500 px-5 py-2.5 text-xs font-medium text-emerald-950 transition-colors hover:bg-emerald-400">
        Enviar planilha
      </router-link>
    </div>

    <!-- ============ RELATÓRIO ============ -->
    <template v-else>

      <!-- Arquivo analisado -->
      <div class="flex items-center gap-3 rounded-xl border border-zinc-800 bg-zinc-900/40 px-4 py-3.5 animate-surgir">
        <div class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-emerald-500/15">
          <AppIcon name="file" class="h-4 w-4 text-emerald-400" />
        </div>

        <div class="min-w-0 flex-1 leading-tight">
          <p class="truncate text-xs text-zinc-200">{{ upload.nomeArquivo }}</p>
          <p class="font-mono text-[10px] text-zinc-500">
            {{ formatarTamanho(upload.tamanhoArquivo) }} · analisado em {{ formatarMomento(upload.validadoEm) }}
          </p>
        </div>

        <span
          class="shrink-0 rounded-md px-2 py-1 text-[10px]"
          :class="upload.totalErros ? 'bg-red-500/15 text-red-400' : 'bg-emerald-500/15 text-emerald-400'">
          {{ upload.totalErros ? `${upload.totalErros} ocorrências` : 'Sem problemas' }}
        </span>
      </div>


      <!-- ============ NÚMEROS GERAIS ============ -->
      <div class="grid grid-cols-2 gap-2.5 lg:grid-cols-4">
        <div
          v-for="(cartao, i) in cartoes"
          :key="cartao.rotulo"
          class="rounded-xl border border-zinc-800 bg-zinc-900/40 px-4 py-3.5 animate-surgir"
          :style="{ animationDelay: `${i * 60}ms` }">
          <p class="text-[10px] text-zinc-500">{{ cartao.rotulo }}</p>
          <p class="mt-1 font-mono text-xl" :class="cartao.tom">{{ cartao.valor }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 gap-4 lg:grid-cols-3">

        <!-- ============ VALIDAÇÕES REALIZADAS ============ -->
        <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Validações realizadas</h2>
          <p class="mt-0.5 text-[10px] text-zinc-500">Quantas ocorrências de cada tipo</p>

          <table class="mt-4 w-full text-left">
            <thead>
              <tr class="text-[10px] uppercase tracking-wide text-zinc-600">
                <th class="pb-2 font-normal">Validação</th>
                <th class="pb-2 text-right font-normal">Qtd.</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in upload.errosPorTipo" :key="item.tipo" class="border-t border-zinc-800">
                <td class="py-2 text-xs text-zinc-300">{{ item.rotulo }}</td>
                <td class="py-2 text-right font-mono text-xs" :class="item.quantidade ? 'text-zinc-100' : 'text-zinc-600'">
                  {{ item.quantidade }}
                </td>
              </tr>

              <!-- Fecha a conta, como no exemplo da atividade -->
              <tr class="border-t border-zinc-700">
                <td class="py-2 text-xs text-emerald-400">Registros válidos</td>
                <td class="py-2 text-right font-mono text-xs text-emerald-400">{{ upload.registrosValidos }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- ============ CAMPOS MAIS PROBLEMÁTICOS ============ -->
        <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Campos com mais problemas</h2>
          <p class="mt-0.5 text-[10px] text-zinc-500">Onde a planilha costuma errar</p>

          <p v-if="!upload.errosPorCampo.length" class="mt-4 text-xs text-zinc-500">
            Nenhum campo apresentou problema.
          </p>

          <ul v-else class="mt-4 space-y-2.5">
            <li v-for="item in upload.errosPorCampo" :key="item.campo">
              <div class="flex items-center justify-between text-[11px]">
                <span class="truncate text-zinc-300">{{ item.campo }}</span>
                <span class="ml-2 shrink-0 font-mono text-zinc-400">{{ item.quantidade }}</span>
              </div>

              <!-- Barra proporcional ao campo campeão de erros -->
              <div class="mt-1.5 h-1 overflow-hidden rounded-full bg-zinc-800">
                <div
                  class="h-full rounded-full bg-orange-400"
                  :style="{ width: `${(item.quantidade / upload.errosPorCampo[0].quantidade) * 100}%` }"></div>
              </div>
            </li>
          </ul>
        </div>

        <!-- ============ APROVEITAMENTO ============ -->
        <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Aproveitamento da planilha</h2>
          <p class="mt-0.5 text-[10px] text-zinc-500">Quanto entraria na base sem revisão</p>

          <p class="mt-5 font-mono text-4xl text-emerald-400">{{ upload.percentualValidos }}%</p>

          <div class="mt-4 h-2 overflow-hidden rounded-full bg-zinc-800">
            <div
              class="h-full rounded-full bg-emerald-500 transition-[width] duration-500 ease-saida"
              :style="{ width: `${upload.percentualValidos}%` }"></div>
          </div>

          <p class="mt-3 text-[11px] leading-relaxed text-zinc-400">
            {{ upload.registrosValidos }} de {{ upload.totalRegistros }}
            {{ upload.totalRegistros === 1 ? 'registro' : 'registros' }} passaram em todas as validações.
          </p>
        </div>
      </div>

      <!-- ============ DETALHE LINHA A LINHA ============ -->
      <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
        <div class="flex flex-wrap items-baseline justify-between gap-3">
          <div>
            <h2 class="text-sm font-medium text-zinc-100">Problemas encontrados</h2>
            <p class="mt-0.5 font-mono text-[10px] text-zinc-500">
              {{ errosFiltrados.length }} {{ errosFiltrados.length === 1 ? 'ocorrência' : 'ocorrências' }}
            </p>
          </div>
        </div>

        <!-- Filtro por tipo -->
        <div v-if="tiposComOcorrencia.length" class="mt-4 flex flex-wrap gap-1.5">
          <button
            class="rounded-lg px-2.5 py-1.5 text-[11px] transition-colors"
            :class="tipoSelecionado === 'todos'
              ? 'bg-emerald-500/15 text-emerald-400'
              : 'border border-zinc-800 text-zinc-400 hover:border-zinc-700 hover:text-zinc-200'"
            @click="tipoSelecionado = 'todos'">
            Todos ({{ upload.erros.length }})
          </button>

          <button
            v-for="item in tiposComOcorrencia"
            :key="item.tipo"
            class="rounded-lg px-2.5 py-1.5 text-[11px] transition-colors"
            :class="tipoSelecionado === item.tipo
              ? 'bg-emerald-500/15 text-emerald-400'
              : 'border border-zinc-800 text-zinc-400 hover:border-zinc-700 hover:text-zinc-200'"
            @click="tipoSelecionado = item.tipo">
            {{ item.rotulo }} ({{ item.quantidade }})
          </button>
        </div>

        <p v-if="!errosFiltrados.length" class="mt-5 text-xs text-zinc-500">
          Nenhum problema encontrado nesta planilha. Todos os registros estão prontos para importar.
        </p>

        <!-- Tabela: linha, campo, tipo, valor e descrição -->
        <div v-else class="mt-4 overflow-x-auto">
          <table class="w-full text-left">
            <thead>
              <tr class="text-[10px] uppercase tracking-wide text-zinc-600">
                <th class="pb-2 pr-3 font-normal">Linha</th>
                <th class="pb-2 pr-3 font-normal">Campo</th>
                <th class="pb-2 pr-3 font-normal">Validação</th>
                <th class="pb-2 pr-3 font-normal">Valor na planilha</th>
                <th class="pb-2 font-normal">Descrição do erro</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(erro, i) in errosVisiveis"
                :key="`${erro.linha}-${erro.tipo}-${erro.campo}-${i}`"
                class="border-t border-zinc-800">
                <td class="py-2 pr-3 font-mono text-xs text-zinc-500">
                  {{ erro.linha ?? '—' }}
                </td>
                <td class="py-2 pr-3 text-xs text-zinc-300">
                  {{ erro.campo || '—' }}
                </td>
                <td class="py-2 pr-3">
                  <span class="rounded-md px-1.5 py-0.5 text-[10px]" :class="estiloTipo[erro.tipo]">
                    {{ rotuloTipo[erro.tipo] || erro.tipo }}
                  </span>
                </td>
                <td class="max-w-[14rem] truncate py-2 pr-3 font-mono text-[11px] text-zinc-500" :title="erro.valor">
                  {{ erro.valor || '—' }}
                </td>
                <td class="py-2 text-xs leading-relaxed text-zinc-400">
                  {{ erro.mensagem }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <div v-if="errosVisiveis.length < errosFiltrados.length" class="mt-4 flex items-center justify-between gap-3">
          <p class="text-[10px] text-zinc-600">
            Mostrando {{ errosVisiveis.length }} de {{ errosFiltrados.length }}.
          </p>
          <button
            class="rounded-lg border border-zinc-800 px-3.5 py-2 text-xs text-zinc-300 transition-colors hover:border-zinc-700 hover:bg-zinc-800/50"
            @click="quantosMostrar += LOTE_DETALHE">
            Mostrar mais {{ LOTE_DETALHE }}
          </button>
        </div>

        <!-- Só aparece em planilha muito grande e muito errada -->
        <p v-if="upload.errosTruncados" class="mt-4 rounded-lg bg-amber-500/10 px-3 py-2 text-[11px] text-amber-400">
          A planilha gerou ocorrências demais para listar. Os totais acima estão completos,
          mas o detalhe foi cortado nas primeiras {{ upload.erros.length }} linhas de erro.
        </p>
      </div>
    </template>
  </div>
</template>
