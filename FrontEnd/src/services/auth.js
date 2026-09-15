import { reactive, computed } from 'vue'

const CHAVE = 'ancora:sessao'

function carregarSessao() {
  try {
    return JSON.parse(localStorage.getItem(CHAVE)) || null
  } catch {
    return null
  }
}

const estado = reactive({
  sessao: carregarSessao(),
})

/*
  Sessão do usuário logado.
  Hoje é só localStorage; quando o back tiver /api/auth/login,
  basta trocar o corpo de entrar() por uma chamada à API e guardar o token.
*/
export const usuarioAtual = reactive({
  logado: computed(() => estado.sessao !== null),
  nome:   computed(() => estado.sessao?.nome  || 'Caio Gabriel'),
  email:  computed(() => estado.sessao?.email || ''),
  cargo:  computed(() => estado.sessao?.cargo || 'Gestor comercial'),
  iniciais: computed(() => {
    const nome = estado.sessao?.nome || 'Caio Gabriel'
    return nome.split(' ').filter(Boolean).slice(0, 2).map(p => p[0]).join('').toUpperCase()
  }),

  entrar(dados) {
    estado.sessao = {
      nome: dados.nome || 'Caio Gabriel',
      email: dados.email,
      cargo: dados.cargo || 'Gestor comercial',
      token: dados.token || 'sessao-local',
    }
    localStorage.setItem(CHAVE, JSON.stringify(estado.sessao))
  },

  sair() {
    estado.sessao = null
    localStorage.removeItem(CHAVE)
  },
})

export function estaLogado() {
  return estado.sessao !== null
}

/*
  Carrega o perfil da conta.

  Hoje devolve o que está na sessão local; quando o back expuser
  GET /api/usuarios/me, é só trocar o corpo. O atraso existe pelo mesmo motivo
  das outras telas: manter o estado de carregamento vivo e testado.
*/
export function carregarPerfil() {
  return new Promise(resolve => {
    setTimeout(() => resolve({
      nome: usuarioAtual.nome,
      email: usuarioAtual.email,
      cargo: usuarioAtual.cargo,
    }), 400)
  })
}
