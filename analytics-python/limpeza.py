# ============================================================================
# limpeza.py — etapa de TRANSFORMAÇÃO: limpa e padroniza as linhas da planilha.
# Entrada: lista de dicionários (cada dicionário é uma linha da planilha).
# Saída: um DICT com a tabela limpa no formato "dicionário de listas"
#        (igual ao tabela_seguros do exemplo_dict.py da aula).
# ============================================================================

# Carregamento das bibliotecas
import numpy as np                  # Numérica (uso o np.isnan p/ achar célula vazia)
from datetime import date           # date: tipo "data" do Python

# Entrada de dados (tabelas fixas usadas na padronização)
COLUNAS = ["codigo_cliente", "nome_cliente", "consultor", "segmento",
           "nivel_cliente", "faturamento_anual", "servicos_contratados",
           "data_contratacao", "cidade", "uf"]      # as 10 colunas da planilha
OBRIGATORIAS = COLUNAS[0:8]         # as 8 primeiras são obrigatórias (cidade e uf não)

# Dicionário: jeito que a pessoa escreveu (simplificado) -> nome oficial
SEGMENTOS = {"ind": "Indústria", "industria": "Indústria", "industrial": "Indústria",
             "com": "Comércio", "comercio": "Comércio",
             "serv": "Serviços", "servico": "Serviços", "servicos": "Serviços",
             "saude": "Saúde",
             "edu": "Educação", "educacao": "Educação",
             "tec": "Tecnologia", "ti": "Tecnologia", "tecnologia": "Tecnologia"}

SERVICOS = {"internet dedicada": "Internet Dedicada",
            "link empresarial": "Link Empresarial",
            "firewall": "Firewall",
            "backup": "Backup",
            "mpls": "MPLS",
            "cloud": "Cloud"}

NIVEIS = ["A", "B", "C"]                            # níveis aceitos
PARTICULAS = ["de", "da", "do", "das", "dos", "e"]  # ficam minúsculas no nome ("Maria da Silva")
ACENTOS = {"á": "a", "à": "a", "ã": "a", "â": "a", "é": "e", "ê": "e",
           "í": "i", "ó": "o", "ô": "o", "õ": "o", "ú": "u", "ç": "c"}   # letra c/ acento -> sem acento


# ---------------------------------------------------------------- funções de apoio
def vazio(valor):
    # Diz se a célula está vazia: None, NaN (vazio do pandas) ou texto só c/ espaços
    if valor is None:
        return True
    if isinstance(valor, float) and np.isnan(valor):   # isinstance = "é do tipo float?"
        return True
    if isinstance(valor, str) and valor.strip() == "":
        return True
    return False


def limpar_texto(valor):
    # "  ana   souza " -> "ana souza" (tira espaços das pontas e repetidos)
    if vazio(valor):
        return None
    palavras = str(valor).split()   # split() sem nada separa em qualquer espaço
    return " ".join(palavras)       # junta de novo com 1 espaço só


def simplificar(texto):
    # "IND." -> "ind" | "Comércio" -> "comercio" (p/ procurar nos dicionários)
    texto = texto.lower()                   # tudo minúsculo
    for letra in ACENTOS:                   # troca cada letra acentuada
        texto = texto.replace(letra, ACENTOS[letra])
    novo = ""
    for letra in texto:                     # pontuação vira espaço
        if letra.isalnum():                 # isalnum = é letra ou número?
            novo = novo + letra
        else:
            novo = novo + " "
    return " ".join(novo.split())           # tira espaços sobrando


def titulo(texto):
    # "CARLOS DA SILVA" -> "Carlos da Silva"
    if texto is None:
        return None
    palavras = texto.lower().split(" ")
    resultado = []
    for i in range(len(palavras)):
        palavra = palavras[i]
        if i > 0 and palavra in PARTICULAS:     # "da", "de"... no meio ficam minúsculas
            resultado.append(palavra)
        else:
            resultado.append(palavra.capitalize())  # 1a letra maiúscula
    return " ".join(resultado)


def padronizar_servicos(texto):
    # "backup; Firewall;;backup" -> ["Backup", "Firewall"]
    if texto is None:
        return []
    lista = []
    for parte in texto.split(";"):          # separa pelo ;
        parte = limpar_texto(parte)
        if parte is None:                   # pedaço vazio: pula
            continue
        chave = simplificar(parte)
        if chave in SERVICOS:
            nome = SERVICOS[chave]          # nome oficial
        else:
            nome = titulo(parte)            # não conheço: só arrumo as maiúsculas
        if nome not in lista:               # não deixa repetir
            lista.append(nome)
    return lista


def nome_da_coluna(nome):
    # O JSON do backend vem em camelCase: "codigoCliente" -> "codigo_cliente"
    novo = ""
    for letra in str(nome):
        if letra.isupper():                 # letra maiúscula vira "_" + minúscula
            novo = novo + "_" + letra.lower()
        else:
            novo = novo + letra
    return novo


def converter_numero(valor):
    # Aceita 1850000 | "1850000.50" | "940.000" | "1.850.000,50" | "R$ 1.850.000,50"
    if vazio(valor):
        return None
    if isinstance(valor, (int, float)):     # já é número
        return float(valor)
    texto = str(valor).replace("R$", "").replace(" ", "")
    for letra in texto:                     # só pode ter dígito, ponto, vírgula e "-"
        if letra not in "0123456789.,-":
            return None
    if ("." in texto) and ("," in texto):
        # Tem os dois: o que vem por último é o separador decimal
        if texto.rfind(",") > texto.rfind("."):     # rfind = posição da última ocorrência
            texto = texto.replace(".", "").replace(",", ".")    # 1.850.000,50 (Brasil)
        else:
            texto = texto.replace(",", "")                      # 1,850,000.50 (EUA)
    elif "," in texto:
        if texto.count(",") > 1:
            texto = texto.replace(",", "")      # 1,850,000 -> vírgulas de milhar
        else:
            texto = texto.replace(",", ".")     # 1850,50 -> vírgula decimal
    elif "." in texto:
        # "940.000" e "1.850.000" são milhar (grupos de 3 dígitos depois do ponto)
        partes = texto.replace("-", "").split(".")
        milhar = 1 <= len(partes[0]) <= 3
        for parte in partes[1:]:
            if len(parte) != 3:
                milhar = False
        if milhar:
            texto = texto.replace(".", "")
    try:
        return float(texto)
    except ValueError:                      # não deu p/ converter
        return None


def converter_data(valor):
    # Aceita "2025-01-15" (pode ter hora depois) e "15/01/2025". Data inválida -> None
    if vazio(valor):
        return None
    texto = str(valor).strip()
    try:
        if "-" in texto:
            partes = texto[0:10].split("-")         # aaaa-mm-dd
            ano, mes, dia = partes[0], partes[1], partes[2]
        else:
            partes = texto.split("/")               # dd/mm/aaaa
            dia, mes, ano = partes[0], partes[1], partes[2]
        if len(partes) != 3:
            return None
        return date(int(ano), int(mes), int(dia))   # 31/02 dá ValueError
    except (ValueError, IndexError):
        return None


# ---------------------------------------------------------------- função principal
def tratar(linhas):
    # Cria a tabela DICT zerada p/ guardar os dados tratados (igual tabela_limpa da aula)
    tabela = {"linha": [], "codigo_cliente": [], "nome_cliente": [], "consultor": [],
              "segmento": [], "segmento_original": [], "nivel_cliente": [],
              "faturamento_anual": [], "servicos": [], "qtd_servicos": [],
              "data_contratacao": [], "mes": [], "cidade": [], "uf": []}
    rejeitadas = []                         # linhas recusadas (c/ o motivo)
    padronizacoes = {"segmento": 0, "consultor": 0, "nivel_cliente": 0}   # contadores
    segmentos_originais = []                # "como estava escrito" -> "como ficou"
    codigos_vistos = []                     # p/ achar código repetido

    # Processamento dos dados: olha linha por linha
    for i in range(len(linhas)):
        # Passo 1: nomes das colunas em snake_case
        bruto = {}
        for chave in linhas[i]:
            bruto[nome_da_coluna(chave)] = linhas[i][chave]
        numero_linha = bruto.get("linha", i + 2)    # linha 1 da planilha = cabeçalho

        # Passo 2: limpeza (tira espaços)
        codigo = limpar_texto(bruto.get("codigo_cliente"))
        nome = limpar_texto(bruto.get("nome_cliente"))
        consultor = limpar_texto(bruto.get("consultor"))
        segmento_original = limpar_texto(bruto.get("segmento"))
        nivel = limpar_texto(bruto.get("nivel_cliente"))
        cidade = limpar_texto(bruto.get("cidade"))
        uf = limpar_texto(bruto.get("uf"))

        # Passo 3: padronização
        if codigo is not None:
            codigo = codigo.upper()
        consultor = titulo(consultor)
        if nivel is not None:
            nivel = nivel.upper()
        if uf is not None:
            if len(uf) == 2 and uf.isalpha():
                uf = uf.upper()
            else:
                uf = None                   # UF inválida (é opcional, não recusa a linha)

        segmento = None
        segmento_reconhecido = False
        if segmento_original is not None:
            chave = simplificar(segmento_original)
            if chave in SEGMENTOS:
                segmento = SEGMENTOS[chave]
                segmento_reconhecido = True
            else:
                segmento = titulo(segmento_original)

        servicos = padronizar_servicos(limpar_texto(bruto.get("servicos_contratados")))

        # Passo 4: conversão de tipos
        faturamento = converter_numero(bruto.get("faturamento_anual"))
        data = converter_data(bruto.get("data_contratacao"))

        # Conta quantos valores a padronização mudou (relatório de qualidade)
        pares = [["segmento", bruto.get("segmento"), segmento],
                 ["consultor", bruto.get("consultor"), consultor],
                 ["nivel_cliente", bruto.get("nivel_cliente"), nivel]]
        for coluna, antes, depois in pares:
            if depois is None:
                depois = ""
            if not vazio(antes) and str(antes) != str(depois):
                padronizacoes[coluna] = padronizacoes[coluna] + 1

        # Passo 5: validação (anota os motivos de recusa)
        valores = {"codigo_cliente": codigo, "nome_cliente": nome, "consultor": consultor,
                   "segmento": segmento, "nivel_cliente": nivel,
                   "faturamento_anual": faturamento, "servicos_contratados": servicos,
                   "data_contratacao": data}
        motivos = []
        for coluna in OBRIGATORIAS:
            if valores[coluna] is None or valores[coluna] == []:
                motivos.append(coluna + ": vazio ou inválido")
        if segmento_original is not None and not segmento_reconhecido:
            motivos.append("segmento: '" + segmento_original + "' não reconhecido")
        if nivel is not None and nivel not in NIVEIS:
            motivos.append("nivel_cliente: '" + nivel + "' fora de A/B/C")
        if faturamento is not None and faturamento < 0:
            motivos.append("faturamento_anual: negativo")
        if codigo is not None:
            if codigo in codigos_vistos:
                motivos.append("codigo_cliente: duplicado")
            else:
                codigos_vistos.append(codigo)

        # Passo 6: separa a linha boa da linha recusada
        if len(motivos) > 0:
            # Aqui temos uma linha recusada !!!
            rejeitadas.append({"linha": numero_linha, "codigo_cliente": codigo,
                               "nome_cliente": nome, "motivo": " | ".join(motivos)})
        else:
            # Aqui temos uma linha OK !!! Adiciona no DICT
            tabela["linha"].append(numero_linha)
            tabela["codigo_cliente"].append(codigo)
            tabela["nome_cliente"].append(nome)
            tabela["consultor"].append(consultor)
            tabela["segmento"].append(segmento)
            tabela["segmento_original"].append(segmento_original)
            tabela["nivel_cliente"].append(nivel)
            tabela["faturamento_anual"].append(faturamento)
            tabela["servicos"].append(servicos)
            tabela["qtd_servicos"].append(len(servicos))
            tabela["data_contratacao"].append(data)
            tabela["mes"].append(str(data)[0:7])    # "2025-01-15" -> "2025-01"
            tabela["cidade"].append(cidade)
            tabela["uf"].append(uf)

            # Conta "como estava escrito" -> "como ficou"
            achou = False
            for item in segmentos_originais:
                if item["original"] == segmento_original and item["padronizado"] == segmento:
                    item["ocorrencias"] = item["ocorrencias"] + 1
                    achou = True
            if not achou:
                segmentos_originais.append({"original": segmento_original,
                                            "padronizado": segmento, "ocorrencias": 1})

    segmentos_originais.sort(key=ordem_segmento)    # ordena por segmento e depois + ocorrências

    # Apresentação dos resultados (devolve tudo num DICT)
    return {"tratado": tabela,
            "rejeitadas": rejeitadas,
            "padronizacoes": padronizacoes,
            "segmentos_originais": segmentos_originais,
            "avisos": []}


def ordem_segmento(item):
    # Critério de ordenação: nome do segmento (A-Z) e, no empate, mais ocorrências primeiro
    return (item["padronizado"], -item["ocorrencias"])
