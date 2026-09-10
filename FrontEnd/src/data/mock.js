/*
  Dados de exemplo usados enquanto o back-end não expõe os endpoints.
  Mesma estrutura que a API deve devolver — quando trocar por api.js,
  os componentes não precisam mudar.
*/

export const indicadores = [
  { rotulo: 'Faturamento total', valor: 'R$ 4,82M', variacao: '+12,4% vs mês anterior', positiva: true },
  { rotulo: 'Clientes ativos',   valor: '312',      variacao: '+8 novos este mês',       positiva: true },
  { rotulo: 'Ticket médio',      valor: 'R$ 15,4k', variacao: '-2,1% vs mês anterior',   positiva: false },
  { rotulo: 'Nível A da carteira', valor: '28%',    variacao: '+3pp no trimestre',       positiva: true },
]

/* Faturamento por segmento — barras verticais do dashboard */
export const faturamentoPorSegmento = [
  { segmento: 'Indústria', valor: 1_480_000, rotulo: 'R$ 1,48M', cor: '#1D9E75' },
  { segmento: 'Varejo',    valor: 1_040_000, rotulo: 'R$ 1,04M', cor: '#8B5CF6' },
  { segmento: 'Serviços',  valor: 860_000,   rotulo: 'R$ 860k',  cor: '#F97362' },
  { segmento: 'Saúde',     valor: 620_000,   rotulo: 'R$ 620k',  cor: '#34D399' },
  { segmento: 'Agro',      valor: 480_000,   rotulo: 'R$ 480k',  cor: '#A78BFA' },
  { segmento: 'Educação',  valor: 340_000,   rotulo: 'R$ 340k',  cor: '#FB923C' },
]

/* Distribuição por nível — rosca do dashboard */
export const distribuicaoPorNivel = [
  { nivel: 'Nível A', percentual: 28, cor: '#1D9E75' },
  { nivel: 'Nível B', percentual: 45, cor: '#8B5CF6' },
  { nivel: 'Nível C', percentual: 27, cor: '#F97362' },
]

/* Top serviços — barras horizontais do dashboard */
export const topServicos = [
  { servico: 'Consultoria estratégica',    percentual: 32, cor: '#1D9E75' },
  { servico: 'Implementação de sistemas',  percentual: 24, cor: '#8B5CF6' },
  { servico: 'Suporte e manutenção',       percentual: 19, cor: '#F97362' },
  { servico: 'Treinamento de equipe',      percentual: 13, cor: '#34D399' },
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
  C: 'bg-orange-500/15 text-orange-400',
}

/* Formata número em real brasileiro */
export function formatarReal(valor) {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 })
}
