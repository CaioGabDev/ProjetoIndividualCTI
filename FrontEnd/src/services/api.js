import axios from 'axios'

/*
  Camada única de acesso ao back-end (Spring Boot).

  Tudo passa por este cliente Axios, e é justamente isso que torna os dois
  "interceptors" abaixo possíveis: eles rodam em TODA chamada, sem nenhuma
  tela precisar lembrar de anexar o token ou de tratar o 401.
*/

/* Chaves do localStorage. Ficam aqui porque os interceptors precisam delas
   e a store (stores/auth.js) as reimporta — assim o nome existe num lugar só. */
export const CHAVE_TOKEN = 'ancora:token'
export const CHAVE_USUARIO = 'ancora:usuario'

/* Rotas públicas: um 401 vindo delas é "senha errada", não "sessão vencida". */
const ROTAS_PUBLICAS = ['/auth/login', '/auth/register']

export const http = axios.create({
  baseURL: import.meta.env?.VITE_API_URL || 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
})

/* ---------- INTERCEPTOR DE ENVIO: roda antes de todo pedido ---------- */
http.interceptors.request.use((config) => {
  const token = localStorage.getItem(CHAVE_TOKEN)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/* ---------- INTERCEPTOR DE RESPOSTA: roda depois de toda resposta ----------
   401 com token salvo = o token venceu, foi adulterado, ou o JWT_SECRET do
   back mudou. Em qualquer um dos casos a sessão não vale mais: limpamos e
   mandamos para o login.

   Usamos window.location em vez do router de propósito — o reload zera
   qualquer estado que tenha sobrado na memória do app. E a chamada de login
   fica de fora, senão errar a senha jogaria a pessoa para a própria tela de
   login num laço. */
http.interceptors.response.use(
  (resposta) => resposta,
  (erro) => {
    const url = erro.config?.url || ''
    const ehRotaPublica = ROTAS_PUBLICAS.some(rota => url.includes(rota))

    if (erro.response?.status === 401 && !ehRotaPublica && localStorage.getItem(CHAVE_TOKEN)) {
      localStorage.removeItem(CHAVE_TOKEN)
      localStorage.removeItem(CHAVE_USUARIO)
      window.location.assign('/login?expirada=1')
    }

    return Promise.reject(erro)
  },
)

/*
  Traduz o erro do Axios numa frase pronta para a tela.

  O GlobalExceptionHandler do Spring sempre devolve {"mensagem": ...} e, nos
  erros de validação (400), também {"campos": {campo: motivo}}. Preferimos a
  mensagem do campo porque ela diz o que corrigir; "Dados inválidos" não diz.
*/
export function mensagemDeErro(erro, padrao = 'Não foi possível concluir. Tente novamente.') {
  if (erro?.response) {
    const dados = erro.response.data
    if (dados?.campos) return Object.values(dados.campos).join(' ')
    if (dados?.mensagem) return dados.mensagem
    return padrao
  }
  /* Sem response = o pedido nem chegou: back desligado, URL errada ou CORS. */
  return `Não foi possível falar com o servidor (${http.defaults.baseURL}). Confira se a API está no ar.`
}

export const api = {
  // --- Autenticação (AuthController) ---
  login: (email, senha) => http.post('/auth/login', { email, senha }).then(r => r.data),
  registrar: (dto) => http.post('/auth/register', dto).then(r => r.data),
  eu: () => http.get('/auth/me').then(r => r.data),

  // --- Usuários (UsuarioController) ---
  listarUsuarios: () => http.get('/usuarios').then(r => r.data),
  criarUsuario: (dto) => http.post('/usuarios', dto).then(r => r.data),
  atualizarUsuario: (id, dto) => http.put(`/usuarios/${id}`, dto).then(r => r.data),
  removerUsuario: (id) => http.delete(`/usuarios/${id}`).then(() => null),

  // --- Clientes / carteira (ClienteController) ---
  // A base atual: clientes do último lote que o Python processou.
  listarClientes: () => http.get('/clientes').then(r => r.data),
  removerCliente: (id) => http.delete(`/clientes/${id}`).then(() => null),
  // Segunda validação da planilha (ClienteValidacaoController): não grava nada,
  // devolve a mesma lista padronizada ou 400 com { mensagem, erros: [{ indice, campo, erro }] }.
  validarClientes: (clientes) => http.post('/clientes/validar', clientes).then(r => r.data),

  // --- Dashboard (DashboardController) ---
  // { lote, resultado } — resultado é o JSON de estatísticas que o Python gravou.
  painel: () => http.get('/dashboard').then(r => r.data),

  // --- Envio de planilha (UploadController) ---
  // { arquivo, recebidas, clientes: [ClienteDTO] } -> o Java cria o lote e chama o Python.
  // Sem timeout no Axios: no plano grátis da Azure o Python pode demorar para acordar.
  enviarCarteira: (dto) => http.post('/upload', dto).then(r => r.data),
  listarLotes: () => http.get('/upload').then(r => r.data),
}
