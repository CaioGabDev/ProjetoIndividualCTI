/*
  Camada única de acesso ao back-end (Spring Boot).
  Enquanto os endpoints não existem, as telas usam os dados de src/data/mock.js.
  Quando o back estiver pronto, é só chamar estas funções no lugar do mock.
*/
const BASE_URL = import.meta.env?.VITE_API_URL || 'http://localhost:8080/api'

async function request(caminho, opcoes = {}) {
  const resposta = await fetch(`${BASE_URL}${caminho}`, {
    headers: { 'Content-Type': 'application/json', ...(opcoes.headers || {}) },
    ...opcoes,
  })

  if (!resposta.ok) {
    throw new Error(`Erro ${resposta.status} ao chamar ${caminho}`)
  }

  return resposta.status === 204 ? null : resposta.json()
}

export const api = {
  // --- Usuários (endpoints que já existem no UsuarioController) ---
  listarUsuarios: () => request('/usuarios'),
  criarUsuario: (dto) => request('/usuarios', { method: 'POST', body: JSON.stringify(dto) }),
  atualizarUsuario: (id, dto) => request(`/usuarios/${id}`, { method: 'PUT', body: JSON.stringify(dto) }),
  removerUsuario: (id) => request(`/usuarios/${id}`, { method: 'DELETE' }),

  // --- Clientes / carteira (ainda a implementar no back) ---
  listarClientes: () => request('/clientes'),
  criarCliente: (dto) => request('/clientes', { method: 'POST', body: JSON.stringify(dto) }),
  removerCliente: (id) => request(`/clientes/${id}`, { method: 'DELETE' }),

  // --- Indicadores do dashboard ---
  buscarIndicadores: () => request('/dashboard'),

  // --- Envio de planilha (multipart, sem Content-Type manual) ---
  enviarPlanilha(arquivo) {
    const form = new FormData()
    form.append('arquivo', arquivo)
    return fetch(`${BASE_URL}/planilhas`, { method: 'POST', body: form })
      .then(r => { if (!r.ok) throw new Error('Falha no envio da planilha'); return r.json() })
  },
  listarEnvios: () => request('/planilhas'),
}
