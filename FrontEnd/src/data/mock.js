/*
  Dados de exemplo usados enquanto o back-end não expõe os endpoints.
  Mesma estrutura que a API deve devolver — quando trocar por api.js,
  os componentes não precisam mudar.
*/

/*
  `serie` são os últimos 6 meses do indicador, usados na sparkline do cartão.
  Servem só para desenhar a tendência — o número grande continua sendo `valor`.
*/
export const indicadores = [
  {
    rotulo: 'Faturamento total', valor: 'R$ 4,82M', variacao: '+12,4%', contexto: 'vs. mês anterior', positiva: true,
    serie: [3.61, 3.84, 3.72, 4.15, 4.29, 4.82],
  },
  {
    rotulo: 'Clientes ativos', valor: '312', variacao: '+8', contexto: 'novos neste mês', positiva: true,
    serie: [281, 288, 292, 297, 304, 312],
  },
  {
    rotulo: 'Ticket médio', valor: 'R$ 15,4k', variacao: '-2,1%', contexto: 'vs. mês anterior', positiva: false,
    serie: [16.2, 16.4, 15.9, 16.1, 15.7, 15.4],
  },
  {
    rotulo: 'Nível A da carteira', valor: '28%', variacao: '+3 p.p.', contexto: 'no trimestre', positiva: true,
    serie: [23, 24, 25, 25, 27, 28],
  },
]

/* De quando é a base que o painel está mostrando */
export const baseAtual = {
  arquivo: 'carteira_agosto.xlsx',
  atualizadoEm: 'há 2 horas',
}

/*
  Faturamento por segmento — barras verticais do dashboard.
  A cor é uma única escala do verde da marca: quanto maior o valor, mais forte
  a barra. Cor aqui é hierarquia, não decoração.
*/
export const faturamentoPorSegmento = [
  { segmento: 'Indústria', valor: 1_480_000, rotulo: 'R$ 1,48M', cor: '#2BB98B' },
  { segmento: 'Varejo',    valor: 1_040_000, rotulo: 'R$ 1,04M', cor: '#1D9E75' },
  { segmento: 'Serviços',  valor: 860_000,   rotulo: 'R$ 860k',  cor: '#198A66' },
  { segmento: 'Saúde',     valor: 620_000,   rotulo: 'R$ 620k',  cor: '#147356' },
  { segmento: 'Agro',      valor: 480_000,   rotulo: 'R$ 480k',  cor: '#105C45' },
  { segmento: 'Educação',  valor: 340_000,   rotulo: 'R$ 340k',  cor: '#0C4534' },
]

/*
  Distribuição por nível — rosca do dashboard.
  Aqui a informação é categórica (A, B e C não têm ordem de grandeza visual),
  então cada nível usa uma cor do guia: primária, secundária e terciária.
*/
export const distribuicaoPorNivel = [
  { nivel: 'Nível A', percentual: 28, cor: '#1D9E75' },
  { nivel: 'Nível B', percentual: 45, cor: '#7F77D0' },
  { nivel: 'Nível C', percentual: 27, cor: '#E87A5F' },
]

/* Top serviços — barras horizontais do dashboard, na mesma escala de verde */
export const topServicos = [
  { servico: 'Consultoria estratégica',   percentual: 32, cor: '#2BB98B' },
  { servico: 'Implementação de sistemas', percentual: 24, cor: '#1D9E75' },
  { servico: 'Suporte e manutenção',      percentual: 19, cor: '#198A66' },
  { servico: 'Treinamento de equipe',     percentual: 13, cor: '#147356' },
]

/* Carteira de clientes */
export const clientes = [
  { id: 1, nome: 'Indústria Norte Ltda.',   cnpj: '12.345.678/0001-90', segmento: 'Indústria', nivel: 'A', servico: 'Consultoria estratégica',   faturamento: 284000 },
  { id: 2, nome: 'Varejo Sul S.A.',         cnpj: '98.765.432/0001-02', segmento: 'Varejo',    nivel: 'B', servico: 'Implementação de sistemas', faturamento: 132500 },
  { id: 3, nome: 'Serviços Brasil ME',      cnpj: '45.612.378/0001-55', segmento: 'Serviços',  nivel: 'C', servico: 'Suporte e manutenção',      faturamento: 41900 },
  { id: 4, nome: 'Agropecuária Bela Vista', cnpj: '77.324.220/0001-40', segmento: 'Agro',      nivel: 'A', servico: 'Consultoria estratégica',   faturamento: 198300 },
  { id: 5, nome: 'Educare Sistema de Ensino', cnpj: '32.889.887/0001-21', segmento: 'Educação', nivel: 'B', servico: 'Treinamento de equipe',   faturamento: 76400 },
  { id: 6, nome: 'Clínica Vida Plena',      cnpj: '21.554.901/0001-33', segmento: 'Saúde',     nivel: 'B', servico: 'Implementação de sistemas', faturamento: 118200 },
  { id: 7, nome: 'Metalúrgica Andrade',     cnpj: '10.223.884/0001-77', segmento: 'Indústria', nivel: 'A', servico: 'Consultoria estratégica',   faturamento: 312750 },
  { id: 8, nome: 'Mercado Central Ltda.',   cnpj: '55.109.633/0001-18', segmento: 'Varejo',    nivel: 'C', servico: 'Suporte e manutenção',      faturamento: 38600 },
  { id: 9, nome: 'TechData Serviços',       cnpj: '67.881.204/0001-64', segmento: 'Serviços',  nivel: 'B', servico: 'Implementação de sistemas', faturamento: 94100 },
  { id: 10, nome: 'Fazenda São Jorge',      cnpj: '89.410.775/0001-05', segmento: 'Agro',      nivel: 'C', servico: 'Treinamento de equipe',     faturamento: 52300 },
]

/* Total real da carteira (o mock traz só uma amostra) */
export const totalClientes = 312

/* Histórico de envios de planilha */
export const envios = [
  { id: 1, arquivo: 'carteira_agosto.xlsx', detalhe: '312 linhas · 2,4MB', quando: 'há 2h',    status: 'Processado' },
  { id: 2, arquivo: 'clientes_novos.csv',   detalhe: '48 linhas · 640KB',  quando: 'há 1 dia', status: 'Processando' },
  { id: 3, arquivo: 'base_industria.xlsx',  detalhe: 'Colunas fora do padrão', quando: 'há 3 dias', status: 'Erro de formato' },
]

/* Cores dos selos de status e de nível — usadas em várias telas */
export const estiloStatus = {
  'Processado':     'bg-emerald-500/15 text-emerald-400',
  'Processando':    'bg-amber-500/15 text-amber-400',
  'Erro de formato':'bg-red-500/15 text-red-400',
}

export const estiloNivel = {
  A: 'bg-emerald-500/15 text-emerald-400',
  B: 'bg-violet-500/15 text-violet-400',
  C: 'bg-orange-400/15 text-orange-400',
}

/* Formata número em real brasileiro */
export function formatarReal(valor) {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 })
}

/* ==================== LEITURA DOS DADOS ====================
   Todas as telas leem por estas funções, nunca importando as listas direto.
   Quando o Spring Boot subir, o corpo de cada uma vira uma chamada ao api.js e
   nenhuma tela precisa mudar — já estão todas preparadas para esperar.
*/

/*
  Atraso que simula a latência da rede. É ele que faz os esqueletos de
  carregamento realmente aparecerem, em vez de ficarem como código morto
  esperando o dia em que o back existir.

  Para uma apresentação com as telas instantâneas, é só pôr 0 aqui.
*/
const ATRASO_REDE = 400

function simularRede(dados, atraso = ATRASO_REDE) {
  return new Promise(resolve => setTimeout(() => resolve(dados), atraso))
}

/* Dashboard — futuramente GET /api/painel */
export function carregarPainel() {
  return simularRede({
    indicadores,
    baseAtual,
    faturamentoPorSegmento,
    distribuicaoPorNivel,
    topServicos,
    envios,
    totalClientes,
  })
}

/* Tela de Clientes — futuramente GET /api/clientes */
export function carregarClientes() {
  return simularRede({ clientes, totalClientes })
}

/* Histórico da tela de envio — futuramente GET /api/envios */
export function carregarEnvios() {
  return simularRede(envios)
}
