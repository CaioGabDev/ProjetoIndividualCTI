import { defineStore } from 'pinia'
import * as XLSX from 'xlsx'
import { api } from '../services/api.js'

const EXTENSOES = ['.xlsx', '.xls', '.csv']
const TAMANHO_MAXIMO = 10 * 1024 * 1024 // 10MB

/*
  A planilha de cada consultor vem com o cabeçalho escrito de um jeito.
  Aqui cada campo do sistema lista os nomes de coluna que aceita, para que
  "Nome do Cliente", "nome_cliente" e "cliente" caiam todos em `nome`.
*/
const COLUNAS = {
  nome:        ['nome', 'cliente', 'nomecliente', 'nomedocliente', 'razaosocial'],
  cnpj:        ['cnpj', 'documento', 'cnpjcpf'],
  segmento:    ['segmento', 'setor', 'ramo'],
  nivel:       ['nivel', 'nivelcliente', 'niveldocliente', 'classificacao', 'classe'],
  servico:     ['servico', 'servicoprestado', 'produto'],
  faturamento: ['faturamento', 'valor', 'valorfaturamento', 'receita', 'faturamentoanual'],
  consultor:   ['consultor', 'responsavel', 'vendedor'],
}

/* Variações de segmento que a planilha traz e o nome único que o sistema usa. */
const SEGMENTOS = {
  IND: 'Indústria',        INDUSTRIA: 'Indústria',
  COM: 'Comércio',         COMERCIO: 'Comércio',
  SERV: 'Serviços',        SERVICOS: 'Serviços',
  VAREJO: 'Varejo',        SAUDE: 'Saúde',
  AGRO: 'Agro',            AGRONEGOCIO: 'Agro',
  EDUCACAO: 'Educação',    ENSINO: 'Educação',
}

/* Tira acentos, espaços e pontuação: "Nível do Cliente" -> "niveldocliente" */
function normalizarChave(texto) {
  return String(texto)
    .normalize('NFD').replace(/\p{Diacritic}/gu, '')
    .toLowerCase().replace(/[^a-z0-9]/g, '')
}

/* Procura na linha da planilha o primeiro cabeçalho aceito para o campo. */
function buscarCampo(linha, aceitos) {
  for (const [cabecalho, valor] of Object.entries(linha)) {
    if (aceitos.includes(normalizarChave(cabecalho))) return valor
  }
  return ''
}

/* "R$ 284.000,50" e 284000.5 viram o número 284000.5 */
function converterValor(bruto) {
  if (typeof bruto === 'number') return bruto

  const limpo = String(bruto || '').replace(/[^\d,.-]/g, '')
  if (!limpo) return 0

  // formato brasileiro: ponto separa milhar, vírgula separa decimal
  const numero = Number(limpo.replace(/\./g, '').replace(',', '.'))
  return Number.isFinite(numero) ? numero : 0
}

export const useUploadStore = defineStore('upload', {
  state: () => ({
    arquivo: null,          // File escolhido no input ou arrastado
    dadosOriginais: [],     // linhas cruas vindas da planilha
    dadosTratados: [],      // linhas já padronizadas
    erros: [],              // { linha, mensagem } encontrados na validação
    carregando: false,      // controla o "Lendo arquivo..."
  }),

  getters: {
    totalClientes:  (state) => state.dadosTratados.length,
    totalErros:     (state) => state.erros.length,
    temDados:       (state) => state.dadosTratados.length > 0,
    clientesNivelA: (state) => state.dadosTratados.filter(c => c.nivel === 'A').length,

    /* Quantos clientes por segmento — alimenta os cards da prévia. */
    resumoPorSegmento: (state) => {
      const resumo = {}

      for (const cliente of state.dadosTratados) {
        const chave = cliente.segmento || 'Sem segmento'
        resumo[chave] = (resumo[chave] || 0) + 1
      }

      return Object.entries(resumo)
        .map(([segmento, quantidade]) => ({ segmento, quantidade }))
        .sort((a, b) => b.quantidade - a.quantidade)
    },

    faturamentoTotal: (state) =>
      state.dadosTratados.reduce((soma, cliente) => soma + cliente.faturamento, 0),
  },

  actions: {
    selecionarArquivo(file) {
      this.arquivo = file
      this.erros = []
      this.dadosOriginais = []
      this.dadosTratados = []
    },

    /* Confere se dá para ler o arquivo antes de tentar abrir. */
    validarArquivo() {
      if (!this.arquivo) {
        this.erros.push({ linha: null, mensagem: 'Selecione uma planilha.' })
        return false
      }

      const nome = this.arquivo.name.toLowerCase()
      if (!EXTENSOES.some(ext => nome.endsWith(ext))) {
        this.erros.push({ linha: null, mensagem: 'Formato não suportado. Envie .xlsx, .xls ou .csv.' })
        return false
      }

      if (this.arquivo.size > TAMANHO_MAXIMO) {
        this.erros.push({ linha: null, mensagem: 'Arquivo acima de 10MB. Divida a planilha e envie em partes.' })
        return false
      }

      return true
    },

    /* Lê a planilha no navegador e guarda as versões bruta e tratada. */
    async processarPlanilha() {
      this.erros = []
      if (!this.validarArquivo()) return false

      this.carregando = true

      try {
        const buffer = await this.arquivo.arrayBuffer()
        const workbook = XLSX.read(buffer)
        const primeiraAba = workbook.SheetNames[0]

        if (!primeiraAba) {
          this.erros.push({ linha: null, mensagem: 'A planilha não tem nenhuma aba.' })
          return false
        }

        const linhas = XLSX.utils.sheet_to_json(workbook.Sheets[primeiraAba], { defval: '' })

        /*
          Numera antes de filtrar para que o número mostrado no erro seja
          mesmo o da linha no Excel: +2 porque a linha 1 é o cabeçalho.
          Linhas em branco no meio ou no fim da planilha não contam como erro.
        */
        const numeradas = linhas
          .map((linha, i) => ({ linha, numeroLinha: i + 2 }))
          .filter(({ linha }) => Object.values(linha).some(valor => String(valor).trim() !== ''))

        if (!numeradas.length) {
          this.erros.push({ linha: null, mensagem: 'A primeira aba está vazia.' })
          return false
        }

        this.dadosOriginais = linhas
        this.dadosTratados = numeradas.map(({ linha, numeroLinha }) => this.tratarLinha(linha, numeroLinha))
        return true
      } catch (e) {
        this.erros.push({ linha: null, mensagem: `Não foi possível ler o arquivo: ${e.message}` })
        return false
      } finally {
        this.carregando = false
      }
    },

    /*
      Padroniza uma linha e registra em `erros` o que estiver fora do esperado.
      Devolve sempre um objeto no formato que as outras telas já usam.
    */
    tratarLinha(linha, numeroLinha) {
      const nome = String(buscarCampo(linha, COLUNAS.nome) || '').trim()
      const segmentoBruto = String(buscarCampo(linha, COLUNAS.segmento) || '').trim()
      const nivelBruto = String(buscarCampo(linha, COLUNAS.nivel) || '').trim().toUpperCase()

      const chaveSegmento = normalizarChave(segmentoBruto).toUpperCase()
      const segmento = SEGMENTOS[chaveSegmento] || segmentoBruto

      /*
        O nível pode vir como "Nível A", "Classe B", "a" ou "A" — fica só a letra.
        A letra precisa estar isolada, senão o C de "CLASSE" viraria o nível.
      */
      const nivel = (nivelBruto.match(/\b([ABC])\b/) || ['', ''])[1]

      const identificacao = nome || `linha ${numeroLinha}`

      if (!nome)     this.erros.push({ linha: numeroLinha, mensagem: 'Cliente sem nome.' })
      if (!segmento) this.erros.push({ linha: numeroLinha, mensagem: `"${identificacao}" está sem segmento.` })
      if (!nivel)    this.erros.push({ linha: numeroLinha, mensagem: `"${identificacao}" está sem nível A, B ou C.` })

      return {
        id: numeroLinha,
        nome,
        cnpj: String(buscarCampo(linha, COLUNAS.cnpj) || '').trim(),
        segmento,
        nivel,
        servico: String(buscarCampo(linha, COLUNAS.servico) || '').trim(),
        faturamento: converterValor(buscarCampo(linha, COLUNAS.faturamento)),
        consultor: String(buscarCampo(linha, COLUNAS.consultor) || '').trim(),
      }
    },

    limpar() {
      this.arquivo = null
      this.dadosOriginais = []
      this.dadosTratados = []
      this.erros = []
      this.carregando = false
    },

    /*
      Próxima etapa do projeto: gravar no banco pelo Spring Boot.
      Ainda não há botão chamando esta action porque o endpoint
      POST /api/planilhas não existe (o back só expõe /api/usuarios).
    */
    async enviarParaBackend() {
      if (!this.arquivo) return null
      return api.enviarPlanilha(this.arquivo)
    },
  },
})
