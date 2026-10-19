import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { api, CHAVE_TOKEN, CHAVE_USUARIO } from '../services/api.js'

/*
  Memória compartilhada de quem está logado.

  O token fica no localStorage para a sessão sobreviver ao F5 — é o modelo
  simples, adequado ao projeto. A contrapartida está anotada no README: um XSS
  conseguiria ler o localStorage; em produção o caminho é cookie HttpOnly.

  A proteção de verdade não está aqui: apagar o token no navegador só esconde
  as telas. Quem recusa um pedido sem token válido é o Spring Security.
*/

function lerUsuarioSalvo() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE_USUARIO)) || null
  } catch {
    return null   // JSON corrompido: trata como "não logado"
  }
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem(CHAVE_TOKEN))
  const usuario = ref(lerUsuarioSalvo())

  const logado = computed(() => !!token.value)
  const nome = computed(() => usuario.value?.nome || '')
  const email = computed(() => usuario.value?.email || '')
  const cargo = computed(() => usuario.value?.cargo || '')
  const perfil = computed(() => usuario.value?.perfil || 'USER')
  const ehAdmin = computed(() => perfil.value === 'ADMIN')
  const id = computed(() => usuario.value?.id ?? null)

  /* Iniciais para o avatar da topbar/sidebar: "Caio Gabriel" -> "CG" */
  const iniciais = computed(() => {
    const partes = (usuario.value?.nome || '').split(' ').filter(Boolean)
    return partes.slice(0, 2).map(p => p[0]).join('').toUpperCase() || '?'
  })

  function guardar(novoToken, novoUsuario) {
    token.value = novoToken
    usuario.value = novoUsuario
    localStorage.setItem(CHAVE_TOKEN, novoToken)
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(novoUsuario))
  }

  /* POST /auth/login — a resposta já traz o usuário, então não é preciso
     uma segunda chamada a /auth/me logo depois de entrar. */
  async function entrar(emailDigitado, senha) {
    const dados = await api.login(emailDigitado.trim().toLowerCase(), senha)
    guardar(dados.token, dados.usuario)
    return dados.usuario
  }

  /* POST /auth/register — cadastra e já entra, para a pessoa não precisar
     digitar as mesmas credenciais duas vezes seguidas. */
  async function cadastrar({ nome: nomeCompleto, email: emailDigitado, cargo: cargoDigitado, senha }) {
    await api.registrar({
      nome: nomeCompleto.trim(),
      email: emailDigitado.trim().toLowerCase(),
      cargo: cargoDigitado?.trim() || undefined,
      senha,
    })
    return entrar(emailDigitado, senha)
  }

  /*
    GET /auth/me — confere com o servidor se a sessão ainda vale.

    Necessário porque o localStorage sobrevive a tudo: o token pode ter
    vencido, a conta pode ter sido excluída ou o perfil rebaixado enquanto a
    aba estava fechada. Se o token não valer mais, o interceptor do api.js
    recebe o 401, limpa a sessão e devolve ao login.
  */
  async function revalidar() {
    if (!token.value) return null
    const dados = await api.eu()
    usuario.value = dados
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(dados))
    return dados
  }

  /* PUT /usuarios/{id} — usado pela tela de Configurações. */
  async function atualizarPerfil({ nome: novoNome, email: novoEmail, cargo: novoCargo, senha }) {
    const dados = await api.atualizarUsuario(id.value, {
      nome: novoNome,
      email: novoEmail,
      cargo: novoCargo,
      senha: senha || '',   // vazio = mantém a senha atual (ver AtualizarUsuarioDTO)
    })
    usuario.value = dados
    localStorage.setItem(CHAVE_USUARIO, JSON.stringify(dados))
    return dados
  }

  function sair() {
    token.value = null
    usuario.value = null
    localStorage.removeItem(CHAVE_TOKEN)
    localStorage.removeItem(CHAVE_USUARIO)
  }

  return {
    token, usuario,
    logado, id, nome, email, cargo, perfil, ehAdmin, iniciais,
    entrar, cadastrar, revalidar, atualizarPerfil, sair,
  }
})
