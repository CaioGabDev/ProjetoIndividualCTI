<script setup>
import { ref } from 'vue'
import AppIcon from '../components/AppIcon.vue'
import { envios, estiloStatus } from '../data/mock.js'

const historico = ref([...envios])
const arrastando = ref(false)
const arquivo = ref(null)
const erro = ref('')
const enviando = ref(false)
const inputArquivo = ref(null)

const EXTENSOES = ['.xlsx', '.xls', '.csv']
const TAMANHO_MAXIMO = 10 * 1024 * 1024 // 10MB

const checklist = [
  { icone: 'check',  cor: 'text-emerald-400', titulo: 'Cabeçalho na primeira linha', texto: 'Nome do cliente, segmento, serviço e valor de faturamento identificados.' },
  { icone: 'check',  cor: 'text-emerald-400', titulo: 'Um cliente por linha',        texto: 'Evite linhas mescladas ou totalizadores no meio da planilha.' },
  { icone: 'clock',  cor: 'text-violet-400',  titulo: 'Inconsistências são tratadas', texto: 'Formatos de texto, moeda e datas são padronizados automaticamente.' },
  { icone: 'shield', cor: 'text-orange-400',  titulo: 'Armazenamento seguro',        texto: 'Os dados enviados ficam armazenados com acesso restrito (LGPD).' },
]

function extensaoValida(nome) {
  return EXTENSOES.some(ext => nome.toLowerCase().endsWith(ext))
}

function selecionar(arquivos) {
  erro.value = ''
  const escolhido = arquivos?.[0]
  if (!escolhido) return

  if (!extensaoValida(escolhido.name)) {
    erro.value = 'Formato não suportado. Envie um arquivo .xlsx, .xls ou .csv.'
    return
  }
  if (escolhido.size > TAMANHO_MAXIMO) {
    erro.value = 'Arquivo acima de 10MB. Divida a planilha e envie em partes.'
    return
  }

  arquivo.value = escolhido
}

function aoSoltar(evento) {
  arrastando.value = false
  selecionar(evento.dataTransfer.files)
}

function formatarTamanho(bytes) {
  if (bytes < 1024 * 1024) return `${Math.round(bytes / 1024)}KB`
  return `${(bytes / 1024 / 1024).toFixed(1)}MB`
}

/*
  Envio de verdade acontece em api.enviarPlanilha(arquivo) quando o
  endpoint POST /api/planilhas existir. Por enquanto só registra no histórico.
*/
async function enviar() {
  if (!arquivo.value) return
  enviando.value = true

  await new Promise(resolve => setTimeout(resolve, 900))

  historico.value.unshift({
    id: Date.now(),
    arquivo: arquivo.value.name,
    detalhe: `${formatarTamanho(arquivo.value.size)} · enviado agora`,
    quando: 'agora',
    status: 'Processando',
  })

  arquivo.value = null
  if (inputArquivo.value) inputArquivo.value.value = ''
  enviando.value = false
}
</script>

<template>
  <div class="space-y-5">

    <!-- ============ CABEÇALHO ============ -->
    <div>
      <h1 class="text-xl font-medium text-zinc-100">Enviar planilha</h1>
      <p class="text-xs text-zinc-500 mt-1">Envie a base da carteira para tratamento automático</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-4">

      <!-- ============ DROPZONE ============ -->
      <div class="lg:col-span-2 space-y-4">
        <div
          class="rounded-xl border-2 border-dashed px-6 py-14 text-center transition"
          :class="arrastando
            ? 'border-emerald-500/60 bg-emerald-500/5'
            : 'border-zinc-800 bg-zinc-900/40'"
          @dragover.prevent="arrastando = true"
          @dragleave.prevent="arrastando = false"
          @drop.prevent="aoSoltar">

          <div class="w-12 h-12 mx-auto rounded-full bg-emerald-500/15 flex items-center justify-center mb-5">
            <AppIcon name="upload" class="w-5 h-5 text-emerald-400" />
          </div>

          <p class="text-sm font-medium text-zinc-100">Arraste sua planilha aqui</p>
          <p class="text-xs text-zinc-500 mt-2 max-w-sm mx-auto leading-relaxed">
            ou clique para selecionar um arquivo do seu computador.
            O tratamento e a padronização dos dados são feitos automaticamente.
          </p>

          <input
            ref="inputArquivo"
            type="file"
            accept=".xlsx,.xls,.csv"
            class="hidden"
            @change="selecionar($event.target.files)" />

          <button
            class="mt-6 rounded-lg bg-emerald-500 px-5 py-2.5 text-xs font-medium text-emerald-950 hover:bg-emerald-400 transition"
            @click="inputArquivo.click()">
            Selecionar arquivo
          </button>

          <p class="mt-5 text-[10px] font-mono text-zinc-600">.XLSX · .XLS · .CSV · ATÉ 10MB</p>
        </div>

        <!-- Erro de validação -->
        <p v-if="erro" class="flex items-center gap-2 rounded-lg bg-red-500/10 border border-red-500/30 px-3.5 py-2.5 text-[11px] text-red-400">
          <AppIcon name="alert" class="w-3.5 h-3.5 shrink-0" />
          {{ erro }}
        </p>

        <!-- Arquivo escolhido, pronto pra enviar -->
        <div v-if="arquivo" class="flex items-center gap-3 rounded-xl border border-zinc-800/60 bg-zinc-900/40 px-4 py-3.5">
          <div class="w-8 h-8 shrink-0 rounded-lg bg-emerald-500/15 flex items-center justify-center">
            <AppIcon name="file" class="w-4 h-4 text-emerald-400" />
          </div>
          <div class="min-w-0 flex-1 leading-tight">
            <p class="text-xs text-zinc-200 truncate">{{ arquivo.name }}</p>
            <p class="text-[10px] font-mono text-zinc-600">{{ formatarTamanho(arquivo.size) }}</p>
          </div>
          <button class="text-[11px] text-zinc-500 hover:text-zinc-300 transition" @click="arquivo = null">
            Remover
          </button>
          <button
            :disabled="enviando"
            class="rounded-lg bg-emerald-500 px-4 py-2 text-[11px] font-medium text-emerald-950 hover:bg-emerald-400 disabled:opacity-60 transition"
            @click="enviar">
            {{ enviando ? 'Enviando...' : 'Enviar' }}
          </button>
        </div>

        <!-- ============ HISTÓRICO ============ -->
        <div class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-5">
          <h2 class="text-sm font-medium text-zinc-100">Histórico de envios</h2>

          <ul class="mt-4 space-y-2.5">
            <li
              v-for="item in historico"
              :key="item.id"
              class="flex items-center gap-3 rounded-lg border border-zinc-800/50 bg-zinc-950/40 px-3.5 py-3">
              <div class="w-8 h-8 shrink-0 rounded-lg bg-zinc-800/60 flex items-center justify-center">
                <AppIcon name="file" class="w-4 h-4 text-zinc-500" />
              </div>

              <div class="min-w-0 flex-1 leading-tight">
                <p class="text-xs text-zinc-200 truncate">{{ item.arquivo }}</p>
                <p class="text-[10px] text-zinc-600">{{ item.detalhe }} · {{ item.quando }}</p>
              </div>

              <span class="rounded-md px-2 py-1 text-[10px] shrink-0" :class="estiloStatus[item.status]">
                {{ item.status }}
              </span>
            </li>
          </ul>
        </div>
      </div>

      <!-- ============ ANTES DE ENVIAR ============ -->
      <aside class="rounded-xl border border-zinc-800/60 bg-zinc-900/40 p-5 h-fit">
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
      </aside>
    </div>
  </div>
</template>
