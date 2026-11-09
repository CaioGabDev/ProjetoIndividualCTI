/*
  Formatação e estilos compartilhados pelas telas do painel.
  (Antes moravam no data/mock.js; saíram de lá quando as telas passaram a
  ler os dados de verdade da API.)
*/

/* 1850000 -> "R$ 1.850.000" */
export function formatarReal(valor) {
  return Number(valor || 0).toLocaleString('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 })
}

/* 1850000 -> "R$ 1,85M" · 420000 -> "R$ 420k" — para cartões e eixos de gráfico */
export function realCurto(valor) {
  const numero = Number(valor || 0)
  if (Math.abs(numero) >= 1_000_000) {
    return `R$ ${(numero / 1_000_000).toLocaleString('pt-BR', { maximumFractionDigits: 2 })}M`
  }
  if (Math.abs(numero) >= 1_000) return `R$ ${Math.round(numero / 1000)}k`
  return `R$ ${Math.round(numero)}`
}

/* 30.7 -> "30,7%" */
export function percentual(valor, casas = 1) {
  return `${Number(valor || 0).toLocaleString('pt-BR', { minimumFractionDigits: casas, maximumFractionDigits: casas })}%`
}

/* "2025-03" -> "mar/25" */
export function mesCurto(anoMes) {
  const [ano, mes] = String(anoMes).split('-').map(Number)
  if (!ano || !mes) return anoMes
  const nome = new Date(ano, mes - 1, 1).toLocaleDateString('pt-BR', { month: 'short' }).replace('.', '')
  return `${nome}/${String(ano).slice(-2)}`
}

/*
  "há 5 min", "há 2 h", "há 3 dias" a partir da data que a API manda.
  O Spring devolve LocalDateTime sem fuso (horário do servidor); tratamos
  como UTC, que é o fuso dos servidores da Azure.
*/
export function tempoRelativo(dataIso) {
  if (!dataIso) return ''
  const texto = /[zZ]|[+-]\d\d:?\d\d$/.test(dataIso) ? dataIso : `${dataIso}Z`
  const data = new Date(texto)
  if (Number.isNaN(data.getTime())) return ''

  const segundos = Math.max(0, (Date.now() - data.getTime()) / 1000)
  if (segundos < 60) return 'agora'
  if (segundos < 3600) return `há ${Math.floor(segundos / 60)} min`
  if (segundos < 86400) return `há ${Math.floor(segundos / 3600)} h`
  const dias = Math.floor(segundos / 86400)
  if (dias < 30) return `há ${dias} ${dias === 1 ? 'dia' : 'dias'}`
  return data.toLocaleDateString('pt-BR')
}

/* Status do lote (upload_lote.status) -> texto da tela */
export const rotuloStatus = {
  AGUARDANDO_ETL: 'Na fila',
  PROCESSANDO: 'Processando',
  PROCESSADO: 'Processado',
  ERRO: 'Erro',
}

/* Cores dos selos de status e de nível — usadas em várias telas */
export const estiloStatus = {
  'Na fila':     'bg-zinc-500/15 text-zinc-300',
  'Processando': 'bg-amber-500/15 text-amber-400',
  'Processado':  'bg-emerald-500/15 text-emerald-400',
  'Erro':        'bg-red-500/15 text-red-400',
}

export const estiloNivel = {
  A: 'bg-emerald-500/15 text-emerald-400',
  B: 'bg-violet-500/15 text-violet-400',
  C: 'bg-orange-400/15 text-orange-400',
}

/* Lote como vem do GET /api/upload -> item do histórico das telas */
export function loteParaHistorico(lote) {
  const status = rotuloStatus[lote.status] || lote.status
  const linhas = `${lote.linhasValidas} de ${lote.linhasRecebidas} linhas enviadas`
  return {
    id: lote.id,
    arquivo: lote.arquivo,
    detalhe: lote.mensagem ? `${linhas} · ${lote.mensagem}` : linhas,
    quando: tempoRelativo(lote.enviadoEm),
    status,
  }
}
