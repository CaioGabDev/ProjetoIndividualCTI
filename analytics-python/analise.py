# ============================================================================
# analise.py — etapa de ANÁLISE: calcula as estatísticas do dashboard.
# Entrada: o DICT que o tratar() do limpeza.py devolve.
# Saída: um DICT com os resultados (vira JSON e é gravado no banco).
#
# Conteúdo das aulas usado aqui:
#   - média, mediana, moda, desvio padrão, quartis    (Aula 1 - Estatística)
#   - limites do boxplot e outliers                   (boxplot_cidades.py / exemplo_dict.py)
#   - histograma c/ a fórmula de Sturges              (Aula 2 - aula_freq2.py)
#   - intervalo de confiança de 95% (Z-score)         (ic_pilha.py / alturas.py)
#   - correlação (Pearson)                            (ementa: "associações e correlações")
# ============================================================================

# Carregamento das bibliotecas
import numpy as np                      # Numérica (média, mediana, desvio, quartis...)
import scipy as sp                      # Estatística (Z-score e correlação)
import statistics as stat               # Moda
from datetime import datetime, timezone # Data e hora de quando o resultado foi gerado

from insights import gerar_insights     # Monta as frases (insights.py)


# ---------------------------------------------------------------- funções de apoio
def arredondar(valor, casas=2):
    # Arredonda e transforma em float comum do Python (o JSON não aceita o tipo do numpy)
    if valor is None:
        return None
    return round(float(valor), casas)


def porcentagem(parte, todo):
    # Regra de três c/ 1 casa. Se o todo for 0, devolve 0 (não dá p/ dividir por zero)
    if todo == 0:
        return 0.0
    return round(100 * parte / todo, 1)


def moda(lista):
    # Moda = valor que mais aparece. multimode devolve TODAS as modas (se empatar)
    # e o min pega a primeira em ordem alfabética
    if len(lista) == 0:
        return None
    return min(stat.multimode(lista))


def medidas_resumo(valores):
    # Medidas resumo de uma lista de números (Aula 1)
    n = len(valores)
    if n > 1:
        desvio = np.std(valores, ddof=1)    # ddof=1 divide por (n-1), igual à fórmula da aula
    else:
        desvio = None                       # c/ 1 valor só não existe desvio
    return {"clientes": n,
            "total": arredondar(sum(valores)),
            "media": arredondar(np.mean(valores)),              # média
            "mediana": arredondar(np.median(valores)),          # mediana (valor do meio)
            "desvioPadrao": arredondar(desvio),                 # desvio padrão
            "minimo": arredondar(min(valores)),
            "q1": arredondar(np.percentile(valores, 25)),       # 1o quartil (25%)
            "q3": arredondar(np.percentile(valores, 75)),       # 3o quartil (75%)
            "maximo": arredondar(max(valores))}


# ---------------------------------------------------------------- blocos do dashboard
def boxplot(tabela):
    # Limites do boxplot e outliers (mesma conta do boxplot_cidades.py e do exemplo_dict.py)
    fat = tabela["faturamento_anual"]
    Q1 = np.percentile(fat, 25)             # 1o quartil (25%)
    Q2 = np.percentile(fat, 50)             # 2o quartil (50%) ou mediana
    Q3 = np.percentile(fat, 75)             # 3o quartil (75%)
    DQ = Q3 - Q1                            # intervalo interquartil
    LS = min(max(fat), Q3 + 1.5*DQ)         # lim. superior
    LI = max(min(fat), Q1 - 1.5*DQ)         # lim. inferior

    outliers = []                           # Vetor vazio p/ guardar os outliers
    for i in range(len(fat)):
        valor_checado_atual = fat[i]
        if (valor_checado_atual > LS) or (valor_checado_atual < LI):
            # Aqui temos um outlier !!!
            outliers.append({"codigo": tabela["codigo_cliente"][i],
                             "nome": tabela["nome_cliente"][i],
                             "faturamento": arredondar(valor_checado_atual)})

    return {"q1": arredondar(Q1), "mediana": arredondar(Q2), "q3": arredondar(Q3),
            "intervaloInterquartil": arredondar(DQ),
            "limiteInferior": arredondar(LI), "limiteSuperior": arredondar(LS),
            "outliers": outliers}


def histograma(tabela):
    # Tabela de distribuição de frequências do faturamento (igual aula_freq2.py)
    fat = tabela["faturamento_anual"]
    N = len(fat)                            # Acha o numero de dados
    k = 1 + 3.32 * np.log10(N)              # Fórmula de Sturges p/ a qtd de classes
    k = int(np.round(k))                    # Arredonda p/ um inteiro
    freq, classes = np.histogram(fat, k)    # Cria a tabela de frequência

    faixas = []
    for i in range(len(freq)):
        faixas.append({"de": arredondar(classes[i]),        # início da classe
                       "ate": arredondar(classes[i + 1]),   # fim da classe
                       "clientes": int(freq[i]),            # frequência observada
                       "percentual": porcentagem(freq[i], N)})  # frequência relativa
    return {"regra": "Sturges", "classes": k, "faixas": faixas}


def intervalo_confianca(tabela):
    # Intervalo de confiança de 95% p/ a média do faturamento (igual ic_pilha.py)
    fat = tabela["faturamento_anual"]
    n = len(fat)
    alpha = 0.95                            # Confiabilidade desejada (95%)
    if n < 2:
        return {"confianca": alpha, "z": None, "margem": None, "de": None, "ate": None}

    media = np.mean(fat)
    sigma = np.std(fat, ddof=1)             # desvio da amostra (n-1)

    # Determinação do Z-score
    cauda = (1.0 - alpha)/2.0
    Z = sp.stats.norm.ppf(1.0 - cauda)      # p/ 95% dá 1.96

    # Calculo do intervalo de confiança final
    IC = Z*sigma/np.sqrt(n)
    return {"confianca": alpha, "z": arredondar(Z, 3), "margem": arredondar(IC),
            "de": arredondar(media - IC), "ate": arredondar(media + IC)}


def agrupar(tabela, coluna, nome):
    # Medidas resumo separadas por categoria (segmento, nível ou consultor)
    fat = tabela["faturamento_anual"]
    total_clientes = len(fat)
    total_fat = sum(fat)

    categorias = []                         # lista das categorias sem repetir
    for valor in tabela[coluna]:
        if valor not in categorias:
            categorias.append(valor)

    saida = []
    for categoria in categorias:
        valores = []                        # faturamentos só dessa categoria
        for i in range(total_clientes):
            if tabela[coluna][i] == categoria:
                valores.append(fat[i])
        r = medidas_resumo(valores)
        saida.append({nome: categoria,
                      "clientes": r["clientes"],
                      "percentual": porcentagem(len(valores), total_clientes),
                      "percentualFaturamento": porcentagem(sum(valores), total_fat),
                      "faturamentoTotal": r["total"], "total": r["total"],
                      "media": r["media"], "mediana": r["mediana"],
                      "desvioPadrao": r["desvioPadrao"],
                      "minimo": r["minimo"], "maximo": r["maximo"]})

    def criterio(item):
        # Mais clientes primeiro (o "-" inverte); no empate, ordem alfabética
        return (-item["clientes"], item[nome])

    saida.sort(key=criterio)
    return saida


def por_mes(tabela):
    # Contratações por mês, incluindo os meses sem nenhuma contratação
    datas = tabela["data_contratacao"]
    primeira = min(datas)
    ultima = max(datas)

    # Monta a lista de TODOS os meses entre a primeira e a última data
    meses = []
    ano = primeira.year
    mes = primeira.month
    while (ano < ultima.year) or (ano == ultima.year and mes <= ultima.month):
        meses.append(str(ano) + "-" + str(mes).zfill(2))    # zfill(2): 1 -> "01"
        mes = mes + 1
        if mes == 13:                       # passou de dezembro: vira o ano
            mes = 1
            ano = ano + 1

    saida = []
    acumulado = 0
    for m in meses:
        clientes = 0
        faturamento = 0
        for i in range(len(tabela["mes"])):
            if tabela["mes"][i] == m:
                clientes = clientes + 1
                faturamento = faturamento + tabela["faturamento_anual"][i]
        acumulado = acumulado + clientes    # soma acumulada
        saida.append({"mes": m, "clientes": clientes,
                      "faturamento": arredondar(faturamento), "acumulado": acumulado})
    return saida


def por_servico(tabela):
    # Quantos clientes contrataram cada serviço
    fat = tabela["faturamento_anual"]
    total = len(fat)

    servicos = []                           # todos os serviços, sem repetir
    for lista in tabela["servicos"]:
        for servico in lista:
            if servico not in servicos:
                servicos.append(servico)

    saida = []
    for servico in servicos:
        valores = []                        # faturamento de quem contratou esse serviço
        for i in range(total):
            if servico in tabela["servicos"][i]:
                valores.append(fat[i])
        saida.append({"servico": servico,
                      "contratos": len(valores),
                      "percentual": porcentagem(len(valores), total),
                      "faturamentoMedio": arredondar(np.mean(valores))})

    def criterio(item):
        # Mais contratado primeiro; no empate, ordem alfabética
        return (-item["contratos"], item["servico"])

    saida.sort(key=criterio)
    return saida


def correlacao(tabela):
    # Quem tem mais serviços também fatura mais?  x = qtd de serviços | y = faturamento
    x = tabela["qtd_servicos"]
    y = tabela["faturamento_anual"]
    resultado = {"pearson": None, "p": None, "n": len(x),
                 "spearman": None, "spearmanP": None, "interpretacao": None}

    # Precisa de pelo menos 3 clientes e de valores diferentes em x e em y
    if len(x) < 3 or len(np.unique(x)) < 2 or len(np.unique(y)) < 2:
        return resultado

    r, p = sp.stats.pearsonr(x, y)          # Pearson: r vai de -1 a 1
    rho, p_rho = sp.stats.spearmanr(x, y)   # Spearman: mesma ideia, usando a ordem (ranking)

    if abs(r) >= 0.7:
        forca = "forte"
    elif abs(r) >= 0.4:
        forca = "moderada"
    else:
        forca = "fraca"
    if r > 0:
        sentido = "positiva"
    else:
        sentido = "negativa"

    resultado["pearson"] = arredondar(r, 3)
    resultado["p"] = arredondar(p, 6)
    resultado["spearman"] = arredondar(rho, 3)
    resultado["spearmanP"] = arredondar(p_rho, 6)
    resultado["interpretacao"] = ("Correlação " + forca + " " + sentido +
                                  " entre a quantidade de serviços e o faturamento.")
    return resultado


def maiores_clientes(tabela, quantidade):
    # Os N clientes c/ maior faturamento
    clientes = []
    for i in range(len(tabela["codigo_cliente"])):
        clientes.append({"codigo": tabela["codigo_cliente"][i],
                         "nome": tabela["nome_cliente"][i],
                         "segmento": tabela["segmento"][i],
                         "nivel": tabela["nivel_cliente"][i],
                         "faturamento": arredondar(tabela["faturamento_anual"][i])})

    def criterio(item):
        return item["faturamento"]

    clientes.sort(key=criterio, reverse=True)   # reverse=True: do maior p/ o menor
    return clientes[0:quantidade]


# ---------------------------------------------------------------- função principal
def calcular(res, linhas_recebidas):
    # Entrada de dados
    tabela = res["tratado"]
    if len(tabela["codigo_cliente"]) == 0:
        raise ValueError("Não há clientes válidos para calcular estatísticas.")
    fat = tabela["faturamento_anual"]

    # Processamento dos dados
    r = medidas_resumo(fat)
    blocos_segmento = agrupar(tabela, "segmento", "segmento")
    blocos_nivel = agrupar(tabela, "nivel_cliente", "nivel")
    blocos_nivel.sort(key=pelo_nivel)       # A -> B -> C
    blocos_consultor = agrupar(tabela, "consultor", "consultor")
    blocos_servico = por_servico(tabela)
    blocos_mes = por_mes(tabela)
    corr = correlacao(tabela)
    caixa = boxplot(tabela)
    hist = histograma(tabela)
    ic = intervalo_confianca(tabela)

    todos_servicos = []                     # junta as listas de serviços numa lista só
    for lista in tabela["servicos"]:
        for servico in lista:
            todos_servicos.append(servico)

    if len(fat) > 1 and np.mean(fat) != 0:
        cv = arredondar(np.std(fat, ddof=1) / np.mean(fat), 3)  # coef. de variação = desvio / média
    else:
        cv = None

    resumo = {"totalClientes": r["clientes"], "faturamentoTotal": r["total"],
              "media": r["media"], "mediana": r["mediana"],
              "desvioPadrao": r["desvioPadrao"], "minimo": r["minimo"], "maximo": r["maximo"],
              "q1": r["q1"], "q3": r["q3"],
              "segmentoMaisFrequente": moda(tabela["segmento"]),
              "nivelMaisFrequente": moda(tabela["nivel_cliente"]),
              "servicoMaisContratado": moda(todos_servicos),
              "mediaServicosPorCliente": arredondar(np.mean(tabela["qtd_servicos"])),
              "coeficienteVariacao": cv,
              "consultores": len(np.unique(tabela["consultor"]))}

    qualidade = {"linhasRecebidas": int(linhas_recebidas),
                 "linhasValidas": len(fat),
                 "linhasInvalidas": int(linhas_recebidas) - len(fat),
                 "correcoesSegmento": res["padronizacoes"]["segmento"],
                 "correcoesConsultor": res["padronizacoes"]["consultor"],
                 "correcoesNivel": res["padronizacoes"]["nivel_cliente"],
                 "segmentosOriginais": res["segmentos_originais"],
                 "avisos": res["avisos"]}

    frases = gerar_insights(resumo, blocos_segmento, blocos_nivel, blocos_consultor,
                            blocos_servico, blocos_mes, corr, caixa, ic)

    # Apresentação dos resultados (DICT que vira JSON no banco)
    return {"geradoEm": datetime.now(timezone.utc).isoformat(timespec="seconds"),
            "resumo": resumo,
            "porSegmento": blocos_segmento,
            "porNivel": blocos_nivel,
            "porConsultor": blocos_consultor,
            "porServico": blocos_servico,
            "porMes": blocos_mes,
            "correlacao": corr,
            "boxplot": caixa,
            "histograma": hist,
            "intervaloConfianca": ic,
            "topClientes": maiores_clientes(tabela, 5),
            "qualidade": qualidade,
            "insights": frases}


def pelo_nivel(item):
    # Critério de ordenação do nível: A, B, C
    return item["nivel"]
