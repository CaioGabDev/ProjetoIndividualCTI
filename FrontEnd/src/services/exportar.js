import * as XLSX from 'xlsx'

/*
  Exportação de planilha.

  O SheetJS já era usado para LER o arquivo do usuário (uploadStore). Aqui usamos
  a outra metade da biblioteca, a de escrita, para fechar o ciclo do sistema:
  entra a planilha bagunçada do consultor, sai a planilha padronizada.
*/

/* "carteira" -> "carteira-2026-09-14.xlsx" */
function nomeComData(prefixo) {
  const hoje = new Date().toISOString().slice(0, 10)   // AAAA-MM-DD
  return `${prefixo}-${hoje}.xlsx`
}

/*
  Monta e baixa um .xlsx a partir de uma lista de objetos.
  As CHAVES de cada objeto viram o cabeçalho das colunas, na ordem em que
  aparecem — por isso as listas abaixo montam os objetos já com o nome final.
*/
function baixarPlanilha(linhas, { prefixo, aba, larguras }) {
  const planilha = XLSX.utils.json_to_sheet(linhas)

  /* Sem isso toda coluna sai com a largura padrão e o nome do cliente fica cortado. */
  if (larguras) planilha['!cols'] = larguras.map(wch => ({ wch }))

  const pasta = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(pasta, planilha, aba)

  /* writeFile monta o arquivo no navegador e dispara o download. */
  XLSX.writeFile(pasta, nomeComData(prefixo))
}

/* Carteira de clientes — usada na tela Clientes (respeita o filtro da tela). */
export function exportarClientes(clientes, { prefixo = 'carteira-ancora' } = {}) {
  const linhas = clientes.map(cliente => ({
    'Cliente': cliente.nome,
    'CNPJ': cliente.cnpj,
    'Segmento': cliente.segmento,
    'Nível': cliente.nivel,
    'Serviço principal': cliente.servico,
    'Faturamento (R$)': cliente.faturamento,
  }))

  baixarPlanilha(linhas, { prefixo, aba: 'Carteira', larguras: [34, 22, 16, 8, 28, 18] })
}

/* Resultado do tratamento — usada na tela de envio, depois de ler o arquivo. */
export function exportarDadosTratados(dados) {
  const linhas = dados.map(cliente => ({
    'Cliente': cliente.nome,
    'CNPJ': cliente.cnpj,
    'Segmento': cliente.segmento,
    'Nível': cliente.nivel,
    'Serviço': cliente.servico,
    'Consultor': cliente.consultor,
    'Faturamento (R$)': cliente.faturamento,
  }))

  baixarPlanilha(linhas, { prefixo: 'carteira-tratada', aba: 'Dados tratados', larguras: [34, 22, 16, 8, 28, 22, 18] })
}
