# ============================================================================
# estudo.py — roda o ETL no meu computador, no mesmo estilo dos exercícios da aula.
# Não precisa de banco nem de API: lê a planilha de exemplo, calcula e mostra os gráficos.
# Como rodar (dentro da pasta analytics-python):  python estudo.py
# ============================================================================

# Carregamento das bibliotecas
import numpy as np
import pandas as pd                 # Lê o arquivo .csv (Aula 2 - Estruturas de Dados)
import matplotlib.pyplot as plt     # Gráficos

from limpeza import tratar
from analise import calcular

# Entrada de dados
# read_csv lê a planilha; dtype=str deixa tudo como texto (quem converte é o limpeza.py);
# fillna("") troca as células vazias (NaN) por texto vazio
dados = pd.read_csv("amostras/clientes.csv", dtype=str).fillna("")
linhas = dados.to_dict("records")   # vira uma lista de dicionários (uma linha = um dict)

# Processamento dos dados
res = tratar(linhas)                # limpeza
resultado = calcular(res, len(linhas))  # estatísticas
tabela = res["tratado"]             # DICT de listas c/ os clientes limpos
faturamento = np.array(tabela["faturamento_anual"]) / 1000   # em R$ mil (fica mais fácil de ler)

# Apresentação dos resultados
print("Linhas lidas: ", len(linhas))
print("Linhas válidas: ", len(tabela["codigo_cliente"]))
print("Linhas recusadas:")
for linha in res["rejeitadas"]:
    print("   linha", linha["linha"], "-", linha["motivo"])

resumo = resultado["resumo"]
print("\n--- Medidas resumo do faturamento ---")
print("Média = ", resumo["media"])
print("Mediana = ", resumo["mediana"])
print("Desvio padrão = ", resumo["desvioPadrao"])
print("Q1 = ", resumo["q1"], " - Q3 = ", resumo["q3"])
print("Segmento mais frequente (moda) = ", resumo["segmentoMaisFrequente"])

caixa = resultado["boxplot"]
print("\n--- Boxplot ---")
print("Limite inferior = ", caixa["limiteInferior"])
print("Limite superior = ", caixa["limiteSuperior"])
print("Outliers: ", caixa["outliers"])

hist = resultado["histograma"]
print("\n--- Histograma (Sturges) ---")
print("Qtd de classes: ", hist["classes"])
for faixa in hist["faixas"]:
    print("   ", faixa["de"], "a", faixa["ate"], "->", faixa["clientes"], "clientes")

ic = resultado["intervaloConfianca"]
print("\n--- Intervalo de confiança (95%) ---")
print("Z-score: ", ic["z"])
print("Intervalo: ", [ic["de"], ic["ate"]])

print("\n--- Correlação (qtd de serviços x faturamento) ---")
print("r de Pearson = ", resultado["correlacao"]["pearson"])

print("\n--- Insights ---")
for frase in resultado["insights"]:
    print("-", frase)

# Gráfico 1: boxplot do faturamento
plt.boxplot(faturamento)
plt.title("Boxplot do faturamento anual dos clientes")
plt.ylabel("Faturamento [R$ mil]")
plt.show()

# Gráfico 2: histograma c/ a qtd de classes de Sturges
plt.hist(faturamento, hist["classes"], color="#2a6fdb", edgecolor="white")
plt.title("Histograma do faturamento anual")
plt.xlabel("Faturamento [R$ mil]")
plt.ylabel("Freq")
plt.show()

# Gráfico 3: dispersão p/ ver a correlação
plt.scatter(tabela["qtd_servicos"], faturamento, color="#2a6fdb")
plt.title("Qtd de serviços x faturamento")
plt.xlabel("Qtd de serviços contratados")
plt.ylabel("Faturamento [R$ mil]")
plt.show()
