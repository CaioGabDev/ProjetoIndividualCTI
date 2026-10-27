# ============================================================================
# insights.py — transforma os números da análise em frases em português.
# Só usa Python puro: listas, dicionários, for e if.
# ============================================================================


def brl(valor):
    # Formata como dinheiro: 1135000.5 -> "R$ 1.135.000,50"
    if valor is None:
        return "—"
    texto = f"{valor:,.2f}"                 # jeito americano: 1,135,000.50
    texto = texto.replace(",", "X").replace(".", ",").replace("X", ".")   # troca , por . e . por ,
    return "R$ " + texto


def virgula(numero):
    # 25.0 -> "25,0"
    return str(numero).replace(".", ",")


def gerar_insights(resumo, por_segmento, por_nivel, por_consultor, por_servico,
                   por_mes, correlacao, boxplot, ic):
    frases = []                             # lista das frases

    # 1) Segmento c/ mais clientes (a lista já vem ordenada, então é o primeiro)
    top = por_segmento[0]
    frases.append("O segmento " + top["segmento"] + " concentra a maior carteira: " +
                  str(top["clientes"]) + " clientes (" + virgula(top["percentual"]) + "%).")

    # 2) Média x mediana (Aula 1: a média é puxada por valores muito grandes)
    media = resumo["media"]
    mediana = resumo["mediana"]
    if media > mediana * 1.1:
        frases.append("A média de faturamento (" + brl(media) + ") é maior que a mediana (" +
                      brl(mediana) + "): poucos clientes muito grandes puxam a média para cima, "
                      "então a mediana descreve melhor o cliente típico.")
    else:
        frases.append("Média (" + brl(media) + ") e mediana (" + brl(mediana) +
                      ") são próximas: a carteira é equilibrada.")

    # 3) Outliers do boxplot
    outliers = boxplot["outliers"]
    if len(outliers) > 0:
        nomes = []
        for cliente in outliers:
            nomes.append(cliente["nome"])
        frases.append("Pelo boxplot, " + str(len(outliers)) + " cliente(s) ficaram fora dos limites (" +
                      brl(boxplot["limiteInferior"]) + " a " + brl(boxplot["limiteSuperior"]) +
                      ") e são outliers: " + ", ".join(nomes) + ".")
    else:
        frases.append("Pelo boxplot, nenhum cliente ficou fora dos limites: não há outliers de faturamento.")

    # 4) Intervalo de confiança de 95%
    if ic["de"] is not None:
        frases.append("Com 95% de confiança, a média de faturamento dos clientes fica entre " +
                      brl(ic["de"]) + " e " + brl(ic["ate"]) + ".")

    # 5) Clientes nível A
    for nivel in por_nivel:
        if nivel["nivel"] == "A":
            frases.append("Os " + str(nivel["clientes"]) + " clientes nível A representam " +
                          virgula(nivel["percentualFaturamento"]) + "% do faturamento total, com apenas " +
                          virgula(nivel["percentual"]) + "% dos clientes.")

    # 6) Consultor c/ maior faturamento
    maior = por_consultor[0]
    for consultor in por_consultor:
        if consultor["faturamentoTotal"] > maior["faturamentoTotal"]:
            maior = consultor
    frases.append(maior["consultor"] + " é o consultor com maior faturamento sob gestão (" +
                  brl(maior["faturamentoTotal"]) + ", " + str(maior["clientes"]) + " clientes).")

    # 7) Serviço mais contratado
    if len(por_servico) > 0:
        s = por_servico[0]
        frases.append(s["servico"] + " é o serviço mais contratado: presente em " +
                      str(s["contratos"]) + " clientes (" + virgula(s["percentual"]) + "%).")

    # 8) Mês c/ mais contratações
    if len(por_mes) > 0:
        pico = 0
        for m in por_mes:
            if m["clientes"] > pico:
                pico = m["clientes"]
        meses_pico = []
        for m in por_mes:
            if m["clientes"] == pico:
                meses_pico.append(m["mes"][5:7] + "/" + m["mes"][0:4])   # "2025-03" -> "03/2025"
        if len(meses_pico) <= 3:
            frases.append("O pico de contratações foi em " + ", ".join(meses_pico) +
                          " (" + str(pico) + " por mês).")
        else:
            frases.append("As contratações ficaram distribuídas ao longo do tempo: no máximo " +
                          str(pico) + " por mês, repetido em " + str(len(meses_pico)) + " meses.")

    # 9) Correlação
    if correlacao["pearson"] is not None:
        frases.append(correlacao["interpretacao"].rstrip(".") +
                      " (r de Pearson = " + virgula(correlacao["pearson"]) + ").")

    return frases
