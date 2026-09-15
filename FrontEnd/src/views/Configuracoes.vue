<script setup>
import { onMounted, ref } from 'vue'
import Esqueleto from '../components/Esqueleto.vue'
import { carregarPerfil, usuarioAtual } from '../services/auth.js'
import { preferencias } from '../services/preferencias.js'

/*
  Página simples de configurações — o item "Configurações" da sidebar
  precisava de um destino. Amplie conforme o back-end for crescendo.
*/
/*
  O perfil virá de GET /api/usuarios/me (o back já tem o UsuarioController).
  Por isso a tela já carrega de forma assíncrona, com esqueleto nos campos.
*/
const nome = ref('')
const email = ref('')
const cargo = ref('')
const carregando = ref(true)

onMounted(async () => {
  const perfil = await carregarPerfil()
  nome.value = perfil.nome
  email.value = perfil.email
  cargo.value = perfil.cargo
  carregando.value = false
})

const notificacoes = ref([
  { chave: 'email', titulo: 'Avisos por e-mail', texto: 'Um resumo quando a planilha terminar de processar.', ativo: true },
  { chave: 'erros', titulo: 'Alertas de erro', texto: 'Aviso quando um envio falhar por formato inválido.', ativo: true },
  { chave: 'resumo', titulo: 'Resumo semanal', texto: 'Panorama da carteira toda segunda de manhã.', ativo: false },
])

/*
  A escolha de movimento vale na hora e fica salva no navegador — não depende
  do botão "Salvar alterações", que é do bloco de perfil.
*/
const modosAnimacao = [
  { valor: 'sistema',   rotulo: 'Seguir o sistema', texto: 'Usa a configuração de animação do computador.' },
  { valor: 'ligada',    rotulo: 'Sempre ligada',    texto: 'Anima mesmo em máquina com os efeitos desativados.' },
  { valor: 'desligada', rotulo: 'Desligada',        texto: 'Interface sem nenhum movimento.' },
]

const salvo = ref(false)

function salvar() {
  usuarioAtual.entrar({ nome: nome.value, email: email.value, cargo: cargo.value })
  salvo.value = true
  setTimeout(() => { salvo.value = false }, 2500)
}
</script>

<template>
  <div class="space-y-5 max-w-2xl">

    <div>
      <h1 class="text-xl font-medium text-zinc-100">Configurações</h1>
      <p class="mt-1 text-xs text-zinc-500">Dados da conta e preferências de notificação</p>
    </div>

    <!-- ============ PERFIL ============ -->
    <section class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
      <h2 class="text-sm font-medium text-zinc-100">Perfil</h2>

      <div v-if="carregando" class="mt-5 grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div v-for="n in 3" :key="n" :class="n === 3 ? 'sm:col-span-2' : ''">
          <Esqueleto class="h-2.5 w-16" />
          <Esqueleto class="mt-2 h-10 w-full" arredondado="rounded-lg" />
        </div>
      </div>

      <div v-else class="mt-5 grid grid-cols-1 sm:grid-cols-2 gap-4">
        <div>
          <label for="nome" class="block text-[11px] text-zinc-400 mb-1.5">Nome</label>
          <input
            id="nome"
            v-model="nome"
            type="text"
            class="w-full rounded-lg border border-zinc-800 bg-zinc-950/60 px-3 py-2.5 text-sm text-zinc-200 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
        </div>

        <div>
          <label for="cargo" class="block text-[11px] text-zinc-400 mb-1.5">Cargo</label>
          <input
            id="cargo"
            v-model="cargo"
            type="text"
            class="w-full rounded-lg border border-zinc-800 bg-zinc-950/60 px-3 py-2.5 text-sm text-zinc-200 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
        </div>

        <div class="sm:col-span-2">
          <label for="email-conta" class="block text-[11px] text-zinc-400 mb-1.5">E-mail</label>
          <input
            id="email-conta"
            v-model="email"
            type="email"
            class="w-full rounded-lg border border-zinc-800 bg-zinc-950/60 px-3 py-2.5 text-sm text-zinc-200 outline-none transition-colors hover:border-zinc-700 focus:border-emerald-500/50" />
        </div>
      </div>
    </section>

    <!-- ============ NOTIFICAÇÕES ============ -->
    <section class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
      <h2 class="text-sm font-medium text-zinc-100">Notificações</h2>

      <ul class="mt-4 divide-y divide-zinc-800">
        <li v-for="item in notificacoes" :key="item.chave" class="flex items-center gap-4 py-3.5">
          <div class="min-w-0 flex-1 leading-tight">
            <p class="text-xs text-zinc-200">{{ item.titulo }}</p>
            <p class="text-[10px] text-zinc-500 mt-1">{{ item.texto }}</p>
          </div>

          <button
            type="button"
            role="switch"
            class="relative h-5 w-9 shrink-0 rounded-full transition-colors"
            :class="item.ativo ? 'bg-emerald-500' : 'bg-zinc-700'"
            :aria-checked="item.ativo"
            :aria-label="item.titulo"
            @click="item.ativo = !item.ativo">
            <span
              class="absolute top-0.5 left-0.5 h-4 w-4 rounded-full bg-white transition-transform"
              :class="item.ativo ? 'translate-x-4' : 'translate-x-0'"></span>
          </button>
        </li>
      </ul>
    </section>

    <!-- ============ MOVIMENTO ============ -->
    <section class="rounded-xl border border-zinc-800 bg-zinc-900/40 p-5">
      <h2 class="text-sm font-medium text-zinc-100">Movimento</h2>
      <p class="mt-0.5 text-[11px] text-zinc-500">
        Animações de entrada, gráficos e transições entre telas
      </p>

      <div class="mt-4 flex flex-wrap gap-2" role="radiogroup" aria-label="Preferência de movimento">
        <button
          v-for="modo in modosAnimacao"
          :key="modo.valor"
          type="button"
          role="radio"
          :aria-checked="preferencias.animacao === modo.valor"
          class="rounded-full border px-3.5 py-1.5 text-[11px] transition-colors"
          :class="preferencias.animacao === modo.valor
            ? 'border-emerald-500/40 bg-emerald-500/10 text-emerald-400'
            : 'border-zinc-800 text-zinc-400 hover:border-zinc-700 hover:text-zinc-100'"
          @click="preferencias.animacao = modo.valor">
          {{ modo.rotulo }}
        </button>
      </div>

      <p class="mt-3 text-[11px] leading-relaxed text-zinc-500">
        {{ modosAnimacao.find(m => m.valor === preferencias.animacao)?.texto }}
      </p>
    </section>

    <div class="flex items-center gap-3">
      <button
        class="rounded-lg bg-emerald-500 px-5 py-2.5 text-xs font-medium text-emerald-950 transition-colors hover:bg-emerald-400 disabled:cursor-not-allowed disabled:opacity-50"
        :disabled="carregando"
        @click="salvar">
        Salvar alterações
      </button>
      <span v-if="salvo" role="status" class="text-[11px] text-emerald-400">Alterações salvas.</span>
    </div>
  </div>
</template>
