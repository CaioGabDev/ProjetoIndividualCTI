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
  email:       ['email', 'emailcliente', 'emailcontato', 'contato'],
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

/*
  Rótulos das validações: a chave é o `tipo` gravado em cada erro e o valor é
  o texto da coluna "Validação realizada" do relatório. Concentrar aqui evita
  que a tela precise inventar nome quando surgir um tipo novo.
*/
const VALIDACOES = {
  obrigatorio:  'Campos obrigatórios vazios',
  espacos:      'Espaços em branco desnecessários',
  duplicado:    'Registros duplicados',
  email:        'E-mails inválidos',
  formato:      'Dados fora do padrão',
  padronizacao: 'Textos que precisam ser padronizados',
}

/* Nome de cada campo como ele aparece para o usuário no relatório. */
const ROTULOS = {
  nome:        'Cliente',
  cnpj:        'CNPJ',
  email:       'E-mail',
  segmento:    'Segmento',
  nivel:       'Nível',
  servico:     'Serviço',
  faturamento: 'Faturamento',
  consultor:   'Consultor',
}

/* Sem estes três a linha não vira cliente no sistema. */
const CAMPOS_OBRIGATORIOS = ['nome', 'segmento', 'nivel']

/*
  Teto de erros guardados em detalhe. Uma planilha de 3.000 linhas toda errada
  passaria de 15 mil objetos, e montar essa tabela travaria a aba. Batendo no
  teto paramos de guardar o detalhe — os contadores de `analise` continuam
  somando tudo, então os totais do relatório seguem corretos.
*/
const LIMITE_ERROS = 5000

/* Checagem de e-mail suficiente para planilha, sem tentar cobrir a RFC. */
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[a-z]{2,}$/i

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

/* Sobra de espaço na borda ("  Silva ") ou entre palavras ("Silva   ME"). */
function temEspacoSobrando(bruto) {
  const texto = String(bruto ?? '')
  return texto !== texto.trim() || /\s{2,}/.test(texto.trim())
}

/* Chave para achar repetição: CNPJ quando existe, senão o nome normalizado. */
function chaveDuplicidade(cliente) {
  const cnpj = cliente.cnpj.replace(/\D/g, '')
  return cnpj || normalizarChave(cliente.nome)
}

/*
  Quais campos do sistema realmente existem no cabeçalho do arquivo.
  Serve para não acusar "e-mail inválido" em 300 linhas de uma planilha que
  simplesmente não tem coluna de e-mail.
*/
function detectarColunas(primeiraLinha) {
  const cabecalhos = Object.keys(primeiraLinha || {}).map(normalizarChave)

  return Object.entries(COLUNAS)
    .filter(([, aceitos]) => aceitos.some(aceito => cabecalhos.includes(aceito)))
    .map(([campo]) => campo)
}

export const useUploadStore = defineStore('upload', {
  state: () => ({
    arquivo: null,          // File escolhido no input ou arrastado
    nomeArquivo: '',        // copiado na leitura: o relatório não depende do File
    tamanhoArquivo: 0,
    validadoEm: null,       // Date do fim da análise; libera a tela de relatório
    dadosOriginais: [],     // linhas cruas vindas da planilha
    dadosTratados: [],      // linhas já padronizadas
    colunasDetectadas: [],  // campos do sistema presentes no cabeçalho
    erros: [],              // { linha, campo, tipo, valor, mensagem }
    analise: {},            // contagem por tipo de erro — soma tudo, sem teto
    errosTruncados: false,  // true quando a lista de detalhe bateu no LIMITE_ERROS
    carregando: false,      // controla o "Lendo arquivo..."
    etapa: '',              // 'lendo' | 'tratando' | 'validando'
    progresso: { atual: 0, total: 0 },   // linhas já processadas / total
  }),

  getters: {
    /* 0 a 100 para a barra. Sem total conhecido ainda, fica em 0. */
    percentualProgresso: (state) => state.progresso.total
      ? Math.round((state.progresso.atual / state.progresso.total) * 100)
      : 0,

    totalClientes:  (state) => state.dadosTratados.length,
    /* Soma de `analise`, não de `erros`: segue certo mesmo com a lista truncada. */
    totalErros:     (state) => Object.values(state.analise).reduce((soma, n) => soma + n, 0),
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

    /* ==================== RELATÓRIO DE VALIDAÇÃO ====================
       Tudo aqui é derivado de `dadosTratados` + `erros` + `analise`.
       A tela de relatório só lê destes getters — não recalcula nada. */

    /* Linhas da planilha que entraram na análise (as em branco são descartadas). */
    totalRegistros: (state) => state.dadosTratados.length,

    /* Números de linha distintos com pelo menos um problema. */
    linhasComErro: (state) => new Set(
      state.erros.filter(erro => erro.linha != null).map(erro => erro.linha),
    ),

    /* Uma linha com 3 problemas conta como 1 registro com erro, não 3. */
    registrosComErro() {
      return this.linhasComErro.size
    },

    registrosValidos() {
      return Math.max(this.totalRegistros - this.registrosComErro, 0)
    },

    percentualValidos() {
      return this.totalRegistros
        ? Math.round((this.registrosValidos / this.totalRegistros) * 100)
        : 0
    },

    /* A tabela "Validação realizada / Quantidade" do relatório. */
    errosPorTipo: (state) => Object.entries(VALIDACOES)
      .map(([tipo, rotulo]) => ({ tipo, rotulo, quantidade: state.analise[tipo] || 0 }))
      .sort((a, b) => b.quantidade - a.quantidade),

    /* Qual coluna concentra os problemas — aponta a origem da bagunça. */
    errosPorCampo: (state) => {
      const resumo = {}

      for (const erro of state.erros) {
        if (!erro.campo) continue
        resumo[erro.campo] = (resumo[erro.campo] || 0) + 1
      }

      return Object.entries(resumo)
        .map(([campo, quantidade]) => ({ campo, quantidade }))
        .sort((a, b) => b.quantidade - a.quantidade)
    },


    /* A tela de relatório só tem o que mostrar depois de uma análise concluída. */
    temRelatorio: (state) => Boolean(state.validadoEm),
  },

  actions: {
    selecionarArquivo(file) {
      this.arquivo = file
      this.zerarAnalise()
    },

    /* Apaga o resultado da análise anterior sem soltar o arquivo escolhido. */
    zerarAnalise() {
      this.nomeArquivo = ''
      this.tamanhoArquivo = 0
      this.validadoEm = null
      this.dadosOriginais = []
      this.dadosTratados = []
      this.colunasDetectadas = []
      this.erros = []
      this.analise = {}
      this.errosTruncados = false
      this.progresso = { atual: 0, total: 0 }
    },

    /*
      Porta única de entrada dos erros. Sempre conta em `analise`; só guarda o
      detalhe enquanto a lista couber no LIMITE_ERROS.
    */
    registrarErro({ linha = null, campo = '', tipo, valor = '', mensagem }) {
      this.analise[tipo] = (this.analise[tipo] || 0) + 1

      if (this.erros.length >= LIMITE_ERROS) {
        this.errosTruncados = true
        return
      }

      this.erros.push({ linha, campo, tipo, valor: String(valor ?? ''), mensagem })
    },

    /* Confere se dá para ler o arquivo antes de tentar abrir. */
    validarArquivo() {
      if (!this.arquivo) {
        this.registrarErro({ tipo: 'arquivo', mensagem: 'Selecione uma planilha.' })
        return false
      }

      const nome = this.arquivo.name.toLowerCase()
      if (!EXTENSOES.some(ext => nome.endsWith(ext))) {
        this.registrarErro({ tipo: 'arquivo', valor: this.arquivo.name, mensagem: 'Formato não suportado. Envie .xlsx, .xls ou .csv.' })
        return false
      }

      if (this.arquivo.size > TAMANHO_MAXIMO) {
        this.registrarErro({ tipo: 'arquivo', mensagem: 'Arquivo acima de 10MB. Divida a planilha e envie em partes.' })
        return false
      }

      return true
    },

    /*
      Fluxo completo da atividade: lê a planilha no navegador, padroniza as
      linhas e valida o resultado. No fim, `validadoEm` libera o relatório.
    */
    async processarPlanilha() {
      this.zerarAnalise()
      if (!this.validarArquivo()) return false

      this.carregando = true
      this.etapa = 'lendo'
      this.nomeArquivo = this.arquivo.name
      this.tamanhoArquivo = this.arquivo.size

      try {
        const buffer = await this.arquivo.arrayBuffer()
        const workbook = XLSX.read(buffer)
        const primeiraAba = workbook.SheetNames[0]

        if (!primeiraAba) {
          this.registrarErro({ tipo: 'arquivo', mensagem: 'A planilha não tem nenhuma aba.' })
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
          this.registrarErro({ tipo: 'arquivo', mensagem: 'A primeira aba está vazia.' })
          return false
        }

        this.dadosOriginais = linhas
        this.colunasDetectadas = detectarColunas(linhas[0])
        this.dadosTratados = await this.tratarEmLotes(numeradas)

        await this.validarRegistros()
        this.validadoEm = new Date()
        return true
      } catch (e) {
        this.registrarErro({ tipo: 'arquivo', mensagem: `Não foi possível ler o arquivo: ${e.message}` })
        return false
      } finally {
        this.carregando = false
        this.etapa = ''
        this.progresso = { atual: 0, total: 0 }
      }
    },

    /*
      Trata as linhas em lotes, devolvendo o controle ao navegador entre um e
      outro.

      Um `.map()` em 3.000 linhas roda de uma vez só e trava a aba inteira: nada
      é repintado no meio, então a barra de progresso ficaria parada em 0% e
      pularia direto para 100% — além de a página não responder a cliques.
      O `setTimeout(0)` encerra a tarefa atual e agenda a próxima, dando ao
      navegador a brecha para pintar o quadro com o progresso novo.
    */
    async tratarEmLotes(numeradas, tamanhoLote = 400) {
      const tratadas = []
      this.etapa = 'tratando'
      this.progresso = { atual: 0, total: numeradas.length }

      for (let inicio = 0; inicio < numeradas.length; inicio += tamanhoLote) {
        const lote = numeradas.slice(inicio, inicio + tamanhoLote)

        for (const { linha, numeroLinha } of lote) {
          tratadas.push(this.tratarLinha(linha, numeroLinha))
        }

        this.progresso.atual = tratadas.length

        /* Planilha pequena termina em um lote só e nem chega a piscar a barra. */
        if (inicio + tamanhoLote < numeradas.length) {
          await new Promise(resolve => setTimeout(resolve, 0))
        }
      }

      return tratadas
    },

    /*
      Padroniza uma linha e devolve o objeto no formato que as outras telas já
      usam. Não acusa nada: quem aponta problema é `validarRegistros()`, que
      precisa da planilha inteira na mão para enxergar duplicados.

      `_brutos` guarda o valor como veio da célula, para a validação comparar o
      antes com o depois (espaço sobrando, texto fora do padrão).
    */
    tratarLinha(linha, numeroLinha) {
      const nomeBruto        = buscarCampo(linha, COLUNAS.nome)
      const cnpjBruto        = buscarCampo(linha, COLUNAS.cnpj)
      const emailBruto       = buscarCampo(linha, COLUNAS.email)
      const segmentoBruto    = buscarCampo(linha, COLUNAS.segmento)
      const nivelBruto       = buscarCampo(linha, COLUNAS.nivel)
      const servicoBruto     = buscarCampo(linha, COLUNAS.servico)
      const faturamentoBruto = buscarCampo(linha, COLUNAS.faturamento)
      const consultorBruto   = buscarCampo(linha, COLUNAS.consultor)

      const segmentoLimpo = String(segmentoBruto || '').trim()
      const chaveSegmento = normalizarChave(segmentoLimpo).toUpperCase()
      const segmento = SEGMENTOS[chaveSegmento] || segmentoLimpo

      /*
        O nível pode vir como "Nível A", "Classe B", "a" ou "A" — fica só a letra.
        A letra precisa estar isolada, senão o C de "CLASSE" viraria o nível.
      */
      const nivel = (String(nivelBruto || '').trim().toUpperCase().match(/\b([ABC])\b/) || ['', ''])[1]

      /* Aqui a padronização acontece de fato: borda e espaço duplo somem. */
      const arrumarTexto = (bruto) => String(bruto || '').trim().replace(/\s{2,}/g, ' ')

      return {
        id: numeroLinha,
        nome: arrumarTexto(nomeBruto),
        cnpj: arrumarTexto(cnpjBruto),
        email: arrumarTexto(emailBruto).toLowerCase(),
        segmento,
        nivel,
        servico: arrumarTexto(servicoBruto),
        faturamento: converterValor(faturamentoBruto),
        consultor: arrumarTexto(consultorBruto),

        _brutos: {
          nome: nomeBruto,
          cnpj: cnpjBruto,
          email: emailBruto,
          segmento: segmentoBruto,
          nivel: nivelBruto,
          servico: servicoBruto,
          faturamento: faturamentoBruto,
          consultor: consultorBruto,
        },
      }
    },

    /*
      Percorre os registros já padronizados aplicando as validações da atividade.
      Roda em lotes pelo mesmo motivo do tratamento: um laço corrido em milhares
      de linhas segura a thread e congela a aba.
    */
    async validarRegistros(tamanhoLote = 400) {
      const total = this.dadosTratados.length

      this.etapa = 'validando'
      this.progresso = { atual: 0, total }

      /* chave do registro -> primeira linha em que ela apareceu (dedup em O(n)) */
      const vistos = new Map()

      for (let inicio = 0; inicio < total; inicio += tamanhoLote) {
        for (const registro of this.dadosTratados.slice(inicio, inicio + tamanhoLote)) {
          this.validarRegistro(registro, vistos)
        }

        this.progresso.atual = Math.min(inicio + tamanhoLote, total)

        if (inicio + tamanhoLote < total) {
          await new Promise(resolve => setTimeout(resolve, 0))
        }
      }
    },

    /* As regras propriamente ditas, uma linha por vez. */
    validarRegistro(registro, vistos) {
      const linha = registro.id
      const brutos = registro._brutos
      const temColuna = (campo) => this.colunasDetectadas.includes(campo)

      /* ---- 1. Campos obrigatórios vazios ---- */
      for (const campo of CAMPOS_OBRIGATORIOS) {
        if (registro[campo]) continue

        this.registrarErro({
          linha,
          campo: ROTULOS[campo],
          tipo: 'obrigatorio',
          mensagem: `${ROTULOS[campo]} não preenchido.`,
        })
      }

      /* ---- 2. Espaços em branco desnecessários ---- */
      for (const [campo, bruto] of Object.entries(brutos)) {
        if (!temEspacoSobrando(bruto)) continue

        this.registrarErro({
          linha,
          campo: ROTULOS[campo],
          tipo: 'espacos',
          valor: bruto,
          mensagem: 'Espaço sobrando no início, no fim ou entre as palavras — corrigido na importação.',
        })
      }

      /* ---- 3. E-mails inválidos (só se a planilha tiver a coluna) ---- */
      if (temColuna('email') && registro.email && !EMAIL_REGEX.test(registro.email)) {
        this.registrarErro({
          linha,
          campo: ROTULOS.email,
          tipo: 'email',
          valor: registro.email,
          mensagem: `"${registro.email}" não é um endereço de e-mail válido.`,
        })
      }

      /* ---- 4. Dados fora do padrão ---- */
      const digitosCnpj = registro.cnpj.replace(/\D/g, '')
      if (registro.cnpj && digitosCnpj.length !== 14) {
        this.registrarErro({
          linha,
          campo: ROTULOS.cnpj,
          tipo: 'formato',
          valor: registro.cnpj,
          mensagem: `CNPJ com ${digitosCnpj.length} dígitos — o padrão tem 14.`,
        })
      }

      /* Veio alguma coisa na coluna de nível, mas não era A, B nem C. */
      if (String(brutos.nivel || '').trim() && !registro.nivel) {
        this.registrarErro({
          linha,
          campo: ROTULOS.nivel,
          tipo: 'formato',
          valor: brutos.nivel,
          mensagem: `"${String(brutos.nivel).trim()}" não corresponde a nível A, B ou C.`,
        })
      }

      if (temColuna('faturamento')) {
        const faturamentoTexto = String(brutos.faturamento ?? '').trim()

        if (faturamentoTexto && !/\d/.test(faturamentoTexto)) {
          this.registrarErro({
            linha,
            campo: ROTULOS.faturamento,
            tipo: 'formato',
            valor: faturamentoTexto,
            mensagem: `"${faturamentoTexto}" não é um valor numérico.`,
          })
        } else if (registro.faturamento < 0) {
          this.registrarErro({
            linha,
            campo: ROTULOS.faturamento,
            tipo: 'formato',
            valor: faturamentoTexto,
            mensagem: 'Faturamento negativo.',
          })
        }
      }

      /* ---- 5. Textos que precisam ser padronizados ---- */
      if (registro.segmento && !Object.values(SEGMENTOS).includes(registro.segmento)) {
        this.registrarErro({
          linha,
          campo: ROTULOS.segmento,
          tipo: 'padronizacao',
          valor: registro.segmento,
          mensagem: `Segmento "${registro.segmento}" fora da lista padrão — revise a grafia.`,
        })
      }

      /* "METALURGICA SILVA" e "metalurgica silva" viram cadastro inconsistente. */
      const nome = registro.nome
      if (nome.length > 3 && (nome === nome.toUpperCase() || nome === nome.toLowerCase())) {
        this.registrarErro({
          linha,
          campo: ROTULOS.nome,
          tipo: 'padronizacao',
          valor: nome,
          mensagem: 'Nome todo em maiúsculas ou todo em minúsculas — fora do padrão de cadastro.',
        })
      }

      /* ---- 6. Registros duplicados ---- */
      const chave = chaveDuplicidade(registro)
      if (!chave) return

      const primeiraLinha = vistos.get(chave)

      if (primeiraLinha) {
        this.registrarErro({
          linha,
          campo: registro.cnpj ? ROTULOS.cnpj : ROTULOS.nome,
          tipo: 'duplicado',
          valor: registro.cnpj || registro.nome,
          mensagem: `Registro repetido — já aparece na linha ${primeiraLinha}.`,
        })
      } else {
        vistos.set(chave, linha)
      }
    },

    limpar() {
      this.arquivo = null
      this.carregando = false
      this.etapa = ''
      this.zerarAnalise()
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
