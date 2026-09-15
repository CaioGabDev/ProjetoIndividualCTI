<script setup>
import { onMounted, ref } from 'vue'
import AppIcon from '../components/AppIcon.vue'
import Esqueleto from '../components/Esqueleto.vue'
import { carregarEnvios, estiloStatus, estiloNivel, formatarReal } from '../data/mock.js'
import { useUploadStore } from '../stores/uploadStore.js'
import { exportarDadosTratados } from '../services/exportar.js'

/*
  A tela cuida só da interface: quem valida, lê e padroniza a planilha
  é o store do Pinia (src/stores/uploadStore.js).
*/
const upload = useUploadStore()

/* O histórico vem do servidor; o resto da tela (dropzone, checklist) é estático. */
const historico = ref([])
const carregandoHistorico = ref(true)

onMounted(async () => {
  historico.value = await carregarEnvios()
  carregandoHistorico.value = false
})
const arrastando = ref(false)
const inputArquivo = ref(null)

const LIMITE_PREVIA = 50 // a tabela mostra só o começo; o total vem dos getters

const checklist = [
  { icone: 'check',  cor: 'text-emerald-400', titulo: 'Cabeçalho na primeira linha', texto: 'Precisa ter nome do cliente, segmento, serviço e faturamento — nessa ou em outra ordem.' },
  { icone: 'check',  cor: 'text-emerald-400', titulo: 'Um cliente por linha',        texto: 'Sem células mescladas e sem linha de total no meio da planilha.' },
  { icone: 'clock',  cor: 'text-violet-400',  titulo: 'O resto o sistema ajeita',    texto: 'Caixa do texto, "R$" no valor e formato de data são padronizados na leitura.' },
  { icone: 'shield', cor: 'text-orange-400',  titulo: 'Acesso restrito',             texto: 'A base fica na nuvem, visível só para a equipe comercial da CTI.' },
]

function selecionar(arquivos) {
  const escolhido = arquivos?.[0]
  if (!escolhido) return

  upload.selecionarArquivo(escolhido)
  upload.validarArquivo() // mostra formato/tamanho inválido na hora
}

function aoSoltar(evento) {
  arrastando.value = false
  selecionar(evento.dataTransfer.files)
}

function formatarTamanho(bytes) {
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)}KB`
  return `${(bytes / 1024 / 1024).toFixed(1)}MB`
}

function remover() {
  upload.limpar()
  if (inputArquivo.value) inputArquivo.value.value = ''
}

/*
  Lê a planilha no navegador e registra o resultado no histórico.
  O envio ao Spring Boot entra depois, em upload.enviarParaBackend().
*/
async function processar() {
  const nomeArquivo = upload.arquivo?.name
  const tamanho = upload.arquivo?.size

  const deuCerto = await upload.processarPlanilha()
  if (!deuCerto) return

  historico.value.unshift({
    id: Date.now(),
    arquivo: nomeArquivo,
    detalhe: `${upload.totalClientes} linhas · ${formatarTamanho(tamanho)}`,
    quando: 'agora',
    status: upload.totalErros > 0 ? 'Erro de formato' : 'Processado',
  })
}
</script>

<template>
  <div class="space-y-5">

    <!-- ============ CABEÇALHO ============ -->
    <div>
      <h1 class="text-xl font-medium text-zinc-100">Enviar planilha</h1>
      <p class="mt-1 text-xs text-zinc-500">A leitura acontece no navegador: você confere a prévia antes de gravar na base</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">

      <!-- ============ DROPZONE ============ -->
      <div class="lg:col-span-2 space-y-4">
        <div
          role="button"
          tabindex="0"
          aria-label="Selecionar planilha para enviar"
          class="cursor-pointer rounded-xl border-2 border-dashed px-6 py-14 text-center transition-colors"
          :class="arrastando
            ? 'border-emerald-500/60 bg-emerald-500/5'
            : 'border-zinc-800 bg-zinc-900/40 hover:border-zinc-700'"
          @click="inputArquivo.click()"
          @keydown.enter.prevent="inputArquivo.click()"
          @keydown.space.prevent="inputArquivo.click()"
          @dragover.prevent="arrastando = true"
          @dragleave.prevent="arrastando = false"
          @drop.prevent="aoSoltar">

          <div class="w-12 h-12 mx-auto rounded-full bg-emerald-500/15 flex items-center justify-center mb-5">
            <AppIcon name="upload" class="w-5 h-5 text-emerald-400" />
          </div>

          <p class="text-sm font-medium text-zinc-100">Arraste a planilha da carteira aqui</p>
          <p class="mx-auto mt-2 max-w-sm text-xs leading-relaxed text-zinc-400">
            Ou clique para escolher um arquivo do computador. Nada é gravado antes de você
            conferir a prévia dos dados tratados.
          </p>

          <input
            ref="inputArquivo"
            type="file"
            accept=".xlsx,.xls,.csv"
            class="hidden"
            @change="selecionar($event.target.files)" />

          <span
            class="mt-6 inline-block rounded-lg bg-emerald-500 px-5 py-2.5 text-xs font-medium text-emerald-950 transition-colors">
            Selecionar arquivo
          </span>

          <p class="mt-5 font-mono text-[10px] text-zinc-500">.XLSX · .XLS · .CSV · ATÉ 10MB</p>
        </div>

        <!-- Arquivo escolhido, pronto pra processar -->
        <div v-if="upload.arquivo" class="flex items-center gap-3 rounded-xl border border-zinc-800 bg-zinc-900/40 px-4 py-3.5 animate-surgir">
          <div class="w-8 h-8 shrink-0 rounded-lg bg-emerald-500/15 flex items-center justify-center">
            <AppIcon name="file" class="w-4 h-4 text-emerald-400" />
          </div>
          <div class="min-w-0 flex-1 leading-tight">
            <p class="text-xs text-zinc-200 truncate">{{ upload.arquivo.name }}</p>
            <p class="font-mono text-[10px] text-zinc-500">{{ formatarTamanho(upload.arquivo.size) }}</p>
          </div>
          <button class="text-[11px] text-zinc-500 transition-colors hover:text-zinc-100" @click="remover">
            Remover
          </button>
          <button
            :disabled="upload.carregando"
            class="rounded-lg bg-emerald-500 px-4 py-2 text-[11px] font-medium text-emerald-950 hover:bg-emerald-400 disabled:opacity-60 transition"
            @click="processar">
            {{ upload.carregando
              ? (upload.percentualProgresso ? `${upload.percentualProgresso}%` : 'Lendo arquivo...')
              : 'Processar planilha' }}
          </button>
        </div>

        <!-- ============ PROGRESSO DA LEITURA ============ -->
        <div v-if="upload.carregando && upload.progresso.total" class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-4">
          <div class="flex items-baseline justify-between text-[11px]">
            <span class="text-zinc-300">Padronizando as linhas da planilha</span>
            <span class="font-mono text-zinc-500">
              {{ upload.progresso.atual.toLocaleString('pt-BR') }} de
              {{ upload.progresso.total.toLocaleString('pt-BR') }}
            </span>
          </div>

          <div
            class="mt-2.5 h-1.5 overflow-hidden rounded-full bg-zinc-800"
            role="progressbar"
            :aria-valuenow="upload.percentualProgresso"
            aria-valuemin="0"
            aria-valuemax="100">
            <!-- A largura vem do store: é progresso real, não uma animação fingida -->
            <div
              class="h-full rounded-full bg-emerald-500 transition-[width] duration-150 ease-out"
              :style="{ width: upload.percentualProgresso + '%' }"></div>
          </div>
        </div>

        <!-- ============ ERROS ENCONTRADOS ============ -->
        <div v-if="upload.totalErros > 0" class="rounded-xl border border-red-500/30 bg-red-500/5 p-4 animate-surgir">
          <p class="flex items-center gap-2 text-[11px] font-medium text-red-400">
            <AppIcon name="alert" class="w-3.5 h-3.5 shrink-0" />
            {{ upload.totalErros }} {{ upload.totalErros === 1 ? 'inconsistência encontrada' : 'inconsistências encontradas' }}
          </p>

          <ul class="mt-3 space-y-1.5 max-h-40 overflow-y-auto">
            <li v-for="(item, i) in upload.erros" :key="i" class="text-[11px] text-red-300/80 leading-relaxed">
              <span v-if="item.linha" class="font-mono text-red-400/60">L{{ item.linha }}</span>
              {{ item.mensagem }}
            </li>
          </ul>
        </div>

        <!-- ============ PRÉVIA DOS DADOS TRATADOS ============ -->
        <div v-if="upload.temDados" class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5 animate-surgir">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 class="text-sm font-medium text-zinc-100">Prévia dos dados tratados</h2>
              <p class="mt-0.5 font-mono text-[10px] text-zinc-500">
                {{ upload.totalClientes }} {{ upload.totalClientes === 1 ? 'linha' : 'linhas' }}
              </p>
            </div>

            <!-- Fecha o ciclo: entrou planilha bagunçada, sai planilha padronizada -->
            <button
              class="flex items-center gap-2 rounded-lg border border-zinc-800 px-3.5 py-2 text-xs text-zinc-300 transition-colors hover:border-zinc-700 hover:bg-zinc-800/50"
              @click="exportarDadosTratados(upload.dadosTratados)">
              <AppIcon name="download" class="h-3.5 w-3.5" />
              Baixar planilha tratada
            </button>
          </div>

          <!-- Indicadores vindos dos getters do Pinia -->
          <div class="mt-4 grid grid-cols-2 sm:grid-cols-4 gap-2.5">
            <div class="rounded-lg border border-zinc-800 bg-zinc-950/40 px-3 py-2.5">
              <p class="text-[10px] text-zinc-500">Clientes</p>
              <p class="text-sm text-zinc-100 mt-0.5">{{ upload.totalClientes }}</p>
            </div>
            <div class="rounded-lg border border-zinc-800 bg-zinc-950/40 px-3 py-2.5">
              <p class="text-[10px] text-zinc-500">Nível A</p>
              <p class="text-sm text-emerald-400 mt-0.5">{{ upload.clientesNivelA }}</p>
            </div>
            <div class="rounded-lg border border-zinc-800 bg-zinc-950/40 px-3 py-2.5">
              <p class="text-[10px] text-zinc-500">Segmentos</p>
              <p class="text-sm text-zinc-100 mt-0.5">{{ upload.resumoPorSegmento.length }}</p>
            </div>
            <div class="rounded-lg border border-zinc-800 bg-zinc-950/40 px-3 py-2.5">
              <p class="text-[10px] text-zinc-500">Faturamento</p>
              <p class="text-sm text-zinc-100 mt-0.5">{{ formatarReal(upload.faturamentoTotal) }}</p>
            </div>
          </div>

          <!-- Tabela de conferência -->
          <div class="mt-4 overflow-x-auto">
            <table class="w-full text-left">
              <thead>
                <tr class="text-[10px] uppercase tracking-wide text-zinc-600">
                  <th class="font-normal pb-2 pr-3">Cliente</th>
                  <th class="font-normal pb-2 pr-3">Segmento</th>
                  <th class="font-normal pb-2 pr-3">Nível</th>
                  <th class="font-normal pb-2 text-right">Faturamento</th>
                </tr>
              </thead>
              <tbody>
                <tr
                  v-for="cliente in upload.dadosTratados.slice(0, LIMITE_PREVIA)"
                  :key="cliente.id"
                  class="border-t border-zinc-800">
                  <td class="py-2 pr-3 text-xs text-zinc-200">
                    {{ cliente.nome || '—' }}
                  </td>
                  <td class="py-2 pr-3 text-xs text-zinc-400">
                    {{ cliente.segmento || '—' }}
                  </td>
                  <td class="py-2 pr-3">
                    <span
                      v-if="cliente.nivel"
                      class="rounded-md px-1.5 py-0.5 text-[10px]"
                      :class="estiloNivel[cliente.nivel]">
                      {{ cliente.nivel }}
                    </span>
                    <span v-else class="text-xs text-zinc-600">—</span>
                  </td>
                  <td class="py-2 text-xs text-zinc-300 text-right font-mono">
                    {{ formatarReal(cliente.faturamento) }}
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          <p v-if="upload.totalClientes > LIMITE_PREVIA" class="mt-3 text-[10px] text-zinc-600">
            Mostrando as primeiras {{ LIMITE_PREVIA }} linhas de {{ upload.totalClientes }}.
          </p>
        </div>

        <!-- ============ HISTÓRICO ============ -->
        <div class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Histórico de envios</h2>

          <ul v-if="carregandoHistorico" class="mt-4 space-y-2.5">
            <li
              v-for="n in 3"
              :key="n"
              class="flex items-center gap-3 rounded-lg border border-zinc-800 bg-zinc-950/40 px-3.5 py-3">
              <Esqueleto class="h-8 w-8" arredondado="rounded-lg" />
              <div class="flex-1 space-y-1.5">
                <Esqueleto class="h-3 w-44" />
                <Esqueleto class="h-2 w-32" />
              </div>
              <Esqueleto class="h-5 w-20" />
            </li>
          </ul>

          <ul v-else class="mt-4 space-y-2.5">
            <li
              v-for="item in historico"
              :key="item.id"
              class="flex items-center gap-3 rounded-lg border border-zinc-800 bg-zinc-950/40 px-3.5 py-3">
              <div class="w-8 h-8 shrink-0 rounded-lg bg-zinc-800/60 flex items-center justify-center">
                <AppIcon name="file" class="w-4 h-4 text-zinc-500" />
              </div>

              <div class="min-w-0 flex-1 leading-tight">
                <p class="text-xs text-zinc-200 truncate">{{ item.arquivo }}</p>
                <p class="text-[10px] text-zinc-500">{{ item.detalhe }} · {{ item.quando }}</p>
              </div>

              <span class="rounded-md px-2 py-1 text-[10px] shrink-0" :class="estiloStatus[item.status]">
                {{ item.status }}
              </span>
            </li>
          </ul>
        </div>
      </div>

      <!-- ============ ANTES DE ENVIAR ============ -->
      <aside class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5 h-fit">
        <h2 class="text-sm font-medium text-zinc-100">Antes de enviar</h2>

        <ul class="mt-5 space-y-5">
          <li v-for="item in checklist" :key="item.titulo" class="flex gap-3">
            <div class="w-7 h-7 shrink-0 rounded-lg bg-zinc-800/60 flex items-center justify-center">
              <AppIcon :name="item.icone" class="w-3.5 h-3.5" :class="item.cor" />
            </div>
            <div class="leading-tight">
              <p class="text-xs text-zinc-200">{{ item.titulo }}</p>
              <p class="text-[10px] text-zinc-500 mt-1 leading-relaxed">{{ item.texto }}</p>
            </div>
          </li>
        </ul>

        <!-- Resumo por segmento aparece junto com a prévia -->
        <div v-if="upload.temDados" class="mt-6 border-t border-zinc-800 pt-5">
          <h3 class="text-xs font-medium text-zinc-300">Clientes por segmento</h3>

          <ul class="mt-3 space-y-2">
            <li
              v-for="item in upload.resumoPorSegmento"
              :key="item.segmento"
              class="flex items-center justify-between text-[11px]">
              <span class="text-zinc-400 truncate">{{ item.segmento }}</span>
              <span class="font-mono text-zinc-300 shrink-0 ml-2">{{ item.quantidade }}</span>
            </li>
          </ul>
        </div>
      </aside>
    </div>
  </div>
</template>
