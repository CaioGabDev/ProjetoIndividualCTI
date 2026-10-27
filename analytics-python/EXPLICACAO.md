# Explicação do analytics-python (Ciência de Dados com Python)

Neste documento eu explico a parte de Python do projeto, arquivo por arquivo.
Eu escrevi o código com o que vimos nas aulas de Ciência de Dados: **listas, dicionários (dict),
`for`, `if`, `range`, `len`, `append`, numpy, scipy, statistics, pandas e matplotlib**.
O que não é da aula (API e banco) está separado e marcado.

**Resumo do que o Python faz (ETL):**

- **E (Extrair):** o backend (Spring) manda as linhas da planilha em JSON. Quem recebe é o `api.py`.
- **T (Transformar):** eu limpo e padronizo (`limpeza.py`), calculo as estatísticas (`analise.py`) e escrevo as frases (`insights.py`).
- **L (Carregar):** eu gravo tudo no PostgreSQL do Azure (`banco.py`).

Para estudar sem banco e sem API, existe o `estudo.py`: ele lê a planilha de exemplo
(`amostras/clientes.csv`), mostra os números no terminal e desenha os gráficos.

---

## 0. De qual aula veio cada coisa

| O que o código faz | Onde está | De qual aula |
|---|---|---|
| Guardar a tabela como **dicionário de listas** (`{"coluna": [valores]}`) | `limpeza.py` (`tabela`) | Aula 2 – Estruturas de Dados (dict) / `exemplo_dict.py` |
| Percorrer as linhas com `for i in range(len(...))` e separar "dado OK" de "dado ruim" | `limpeza.py` (`tratar`) | `exemplo_dict.py` (tabela_limpa e vetor_outliers) |
| Ler a planilha `.csv` com `pd.read_csv` e virar dict com `to_dict` | `estudo.py` | Aula 2 – Estruturas de Dados (Arquivos) |
| Média, mediana, moda | `analise.py` (`medidas_resumo`, `moda`) | Aula 1 – Estatística (Medidas Resumo) / `ic_pilha.py` |
| Desvio padrão (dividindo por n−1) | `analise.py` (`medidas_resumo`) | Aula 1 – Estatística (fórmula da variância) |
| Quartis Q1, Q2, Q3 com `np.percentile` | `analise.py` (`medidas_resumo`, `boxplot`) | `boxplot_cidades.py` |
| Limites do boxplot e **outliers** (DQ, LI, LS) | `analise.py` (`boxplot`) | `boxplot_cidades.py` / `exemplo_dict.py` |
| **Histograma** com a fórmula de **Sturges** e `np.histogram` | `analise.py` (`histograma`) | Aula 2 – Estatística (Histograma) / `aula_freq2.py` |
| **Intervalo de confiança de 95%** com Z-score | `analise.py` (`intervalo_confianca`) | `ic_pilha.py` / `alturas.py` |
| Gráficos `plt.boxplot`, `plt.hist` | `estudo.py` | `boxplot_cidades.py`, `aula_freq2.py` |
| Correlação de Pearson (`sp.stats.pearsonr`) | `analise.py` (`correlacao`) | Ementa: "associações e correlações" (ainda vamos ver em aula) |
| API (FastAPI) e banco (SQLAlchemy) | `api.py`, `banco.py`, `config.py` | **Não é da aula**: é só para o Spring conversar com o Python e salvar o resultado |

---

## 1. requirements.txt

É a lista dos pacotes que o projeto precisa (parecido com o `pom.xml` do Java). Eu instalo tudo com `pip install -r requirements.txt`.

- `numpy` → contas: média, mediana, desvio padrão, quartis, histograma. **(aula)**
- `scipy` → estatística: Z-score do intervalo de confiança e correlação. **(aula)**
- `pandas` → só para ler o `.csv` no `estudo.py`. **(aula)**
- `matplotlib` → gráficos do `estudo.py`. **(aula)**
- `SQLAlchemy`, `psycopg[binary]` → conversar com o PostgreSQL.
- `python-dotenv` → ler o arquivo `.env` (senha do banco).
- `fastapi`, `uvicorn` → criar a API e deixar ela ligada.

O `statistics`, o `datetime`, o `json` e o `traceback` não aparecem aí porque já vêm com o Python.

---

## 2. limpeza.py (T – limpar e padronizar)

Recebe as linhas da planilha (uma **lista de dicionários**, cada dicionário é uma linha) e devolve um **dict** com:

- `"tratado"` → a tabela limpa, no formato **dicionário de listas**, igual ao `tabela_seguros` da aula:
  ```python
  {"codigo_cliente": ["CTI001", "CTI002"], "faturamento_anual": [1850000.0, 420000.0], ...}
  ```
  Para pegar o faturamento do 2º cliente: `tabela["faturamento_anual"][1]`.
- `"rejeitadas"` → lista das linhas recusadas, com o motivo.
- `"padronizacoes"`, `"segmentos_originais"`, `"avisos"` → relatório de qualidade.

### Carregamento das bibliotecas

- `import numpy as np` → só uso o `np.isnan` para saber se uma célula está vazia (o pandas chama célula vazia de `NaN`, "not a number").
- `from datetime import date` → o tipo "data" do Python.

### Entrada de dados (tabelas fixas)

- `COLUNAS` → lista com as 10 colunas da planilha.
- `OBRIGATORIAS = COLUNAS[0:8]` → as 8 primeiras são obrigatórias (`cidade` e `uf` não).
- `SEGMENTOS` → dicionário que traduz o jeito que a pessoa escreveu para o nome oficial. Ex.: `"ind"`, `"industria"` e `"industrial"` viram `"Indústria"`.
- `SERVICOS` → a mesma ideia para os serviços.
- `NIVEIS = ["A", "B", "C"]` → níveis aceitos.
- `PARTICULAS` → palavras que ficam minúsculas no meio do nome ("Maria **da** Silva").
- `ACENTOS` → dicionário letra acentuada → letra sem acento (`"ç": "c"`, `"ã": "a"`...).

### Funções de apoio

- **`vazio(valor)`** → devolve `True` se o valor é `None`, `NaN` ou um texto só com espaços. O `isinstance(valor, float)` pergunta "esse valor é do tipo float?".
- **`limpar_texto(valor)`** → `str(valor).split()` quebra o texto em palavras (o `split()` sem nada separa em qualquer quantidade de espaço) e `" ".join(...)` junta de novo com um espaço só. Então `"  ana   souza "` vira `"ana souza"`.
- **`simplificar(texto)`** → deixa o texto pronto para procurar nos dicionários:
  1. `texto.lower()` → tudo minúsculo;
  2. `for letra in ACENTOS:` → troco cada letra acentuada pela letra sem acento;
  3. `for letra in texto:` → se a letra for letra ou número (`isalnum`), eu mantenho; se for pontuação, troco por espaço.
  Assim `"IND."` vira `"ind"` e `"Comércio"` vira `"comercio"`.
- **`titulo(texto)`** → `"CARLOS DA SILVA"` vira `"Carlos da Silva"`. Percorro as palavras com `for i in range(len(palavras))`; se não é a primeira (`i > 0`) e é uma partícula, fica minúscula; senão uso `capitalize()` (primeira letra maiúscula).
- **`padronizar_servicos(texto)`** → separa o texto pelo `;` com `split(";")`, limpa cada pedaço, procura o nome oficial no dicionário `SERVICOS` e só faz `append` se o serviço ainda não está na lista (não repete).
- **`nome_da_coluna(nome)`** → o JSON do backend vem em camelCase (`codigoCliente`). Percorro letra por letra: se a letra é maiúscula (`isupper`), troco por `"_"` + a letra minúscula. Então `codigoCliente` vira `codigo_cliente`.
- **`converter_numero(valor)`** → transforma texto em número:
  - tiro o `"R$"` e os espaços com `replace`;
  - se sobrou alguma letra, não é número → `None`;
  - se tem ponto **e** vírgula, o que aparece por último é o decimal (`rfind` acha a posição da última ocorrência): `1.850.000,50` (Brasil) ou `1,850,000.50` (EUA);
  - se só tem vírgula: uma vírgula é decimal (`1850,5`), várias são de milhar;
  - se só tem ponto e os grupos depois do ponto têm 3 dígitos (`940.000`, `1.850.000`), o ponto é de milhar;
  - no fim, `float(texto)` dentro de um `try`; se der erro (`except ValueError`), devolvo `None`.
- **`converter_data(valor)`** → aceita `"2025-01-15"` e `"15/01/2025"`. Uso `split("-")` ou `split("/")` para separar dia, mês e ano, e monto com `date(ano, mes, dia)`. Data que não existe (`31/02/2025`) dá `ValueError` e vira `None`.

### Função principal `tratar(linhas)`

1. Crio a **tabela DICT zerada** (cada coluna é uma lista vazia), igual o `tabela_limpa` do `exemplo_dict.py`, e as listas `rejeitadas`, `segmentos_originais` e `codigos_vistos`.
2. `for i in range(len(linhas)):` → olho linha por linha.
3. **Passo 1:** converto os nomes das colunas para snake_case.
4. **Passo 2 – limpeza:** `limpar_texto` em cada coluna de texto.
5. **Passo 3 – padronização:** código e nível em maiúsculas (`upper`), consultor em formato título, UF só se tiver 2 letras, segmento pelo dicionário `SEGMENTOS`, serviços viram lista.
6. **Passo 4 – conversão de tipos:** faturamento vira número e a data vira data.
7. Conto quantos valores a padronização mudou (comparo o valor de antes com o de depois).
8. **Passo 5 – validação:** monto a lista `motivos`:
   - coluna obrigatória vazia → `"coluna: vazio ou inválido"`;
   - segmento que não está no dicionário → `"segmento: 'Agro' não reconhecido"`;
   - nível fora de A/B/C;
   - faturamento negativo;
   - código que já apareceu antes (`if codigo in codigos_vistos`) → duplicado.
9. **Passo 6:** se a lista de motivos não está vazia, **"aqui temos uma linha recusada !!!"** e ela vai para `rejeitadas`. Senão, **"aqui temos uma linha OK !!!"** e eu faço `append` em cada lista da tabela (exatamente como no `exemplo_dict.py`). Também guardo o mês com `str(data)[0:7]` (`"2025-01-15"` vira `"2025-01"`).
10. Ordeno `segmentos_originais` com `sort(key=ordem_segmento)`. O `key` diz **por qual critério** ordenar: a função `ordem_segmento` devolve (nome do segmento, −ocorrências), então ordena por nome e, no empate, quem tem mais ocorrências vem primeiro (o `-` inverte).
11. Devolvo tudo num dict.

---

## 3. analise.py (T – calcular as estatísticas)

Pega a tabela limpa e calcula os números do dashboard. Devolve um **dict** que vira JSON e é gravado no banco.

### Carregamento das bibliotecas

- `import numpy as np` → média, mediana, desvio, quartis, histograma.
- `import scipy as sp` → Z-score (`sp.stats.norm.ppf`) e correlação.
- `import statistics as stat` → moda.
- `from datetime import datetime, timezone` → hora em que o resultado foi gerado.

### Funções de apoio

- **`arredondar(valor, casas=2)`** → `round(float(valor), casas)`. O `float(...)` é importante: o numpy devolve números do tipo dele (`np.float64`) e o JSON não aceita.
- **`porcentagem(parte, todo)`** → regra de três com 1 casa. Se o todo for 0, devolve 0 (não dá para dividir por zero).
- **`moda(lista)`** → o valor que mais aparece. Uso `stat.multimode`, que devolve **todas** as modas quando há empate, e o `min` pega a primeira em ordem alfabética.
- **`medidas_resumo(valores)`** → as medidas da Aula 1 para uma lista de números:
  - `np.mean` → **média**;
  - `np.median` → **mediana**;
  - `np.std(valores, ddof=1)` → **desvio padrão**. O `ddof=1` faz dividir por **n−1**, igual à fórmula da variância do slide. (O `np.std` sozinho, como no `ic_pilha.py`, divide por n.) Com 1 valor só não existe desvio, então devolvo `None`;
  - `np.percentile(valores, 25)` e `np.percentile(valores, 75)` → **Q1** e **Q3**;
  - `min`, `max`, `sum` → menor, maior e soma.

### Blocos do dashboard

- **`boxplot(tabela)`** → é a mesma conta do `boxplot_cidades.py`:
  ```python
  Q1 = np.percentile(fat, 25)
  Q3 = np.percentile(fat, 75)
  DQ = Q3 - Q1                     # intervalo interquartil
  LS = min(max(fat), Q3 + 1.5*DQ)  # limite superior
  LI = max(min(fat), Q1 - 1.5*DQ)  # limite inferior
  ```
  Depois, igual ao `exemplo_dict.py`, eu olho cada valor: se for maior que `LS` ou menor que `LI`, **é outlier** e vai para a lista `outliers` (com código, nome e faturamento).
  Na amostra, o "Data Center Norte" (R$ 15 milhões) é outlier.

- **`histograma(tabela)`** → igual ao `aula_freq2.py`:
  ```python
  k = 1 + 3.32 * np.log10(N)      # Fórmula de Sturges
  k = int(np.round(k))            # arredonda para inteiro
  freq, classes = np.histogram(fat, k)
  ```
  `freq` é quantos clientes caem em cada classe e `classes` são os limites. Eu monto uma lista de faixas `{"de", "ate", "clientes", "percentual"}`. Com 30 clientes, Sturges dá 6 classes.

- **`intervalo_confianca(tabela)`** → igual ao `ic_pilha.py`:
  ```python
  alpha = 0.95
  cauda = (1.0 - alpha)/2.0
  Z = sp.stats.norm.ppf(1.0 - cauda)   # 1,96 para 95%
  IC = Z*sigma/np.sqrt(n)
  ```
  O intervalo é `[media - IC, media + IC]`: com 95% de confiança a média verdadeira está aí dentro.

- **`agrupar(tabela, coluna, nome)`** → medidas separadas por segmento, nível ou consultor:
  1. faço a lista das categorias sem repetir (`if valor not in categorias: append`);
  2. para cada categoria, pego só os faturamentos dela com `for i in range(...)` + `if`;
  3. chamo `medidas_resumo` nessa lista e calculo as porcentagens;
  4. ordeno com `sort(key=criterio)`: mais clientes primeiro e, no empate, ordem alfabética.

- **`por_mes(tabela)`** → contratações por mês. Primeiro monto a lista de **todos** os meses entre a primeira e a última data com um `while` (somo 1 no mês; quando passa de 12, volta para 1 e soma 1 no ano). O `str(mes).zfill(2)` coloca zero na frente (`1` vira `"01"`). Depois, para cada mês, conto os clientes e somo o faturamento. Mês sem contratação fica com 0, para o gráfico não "pular" meses. O `acumulado` é a soma acumulada.

- **`por_servico(tabela)`** → faço a lista de serviços sem repetir e, para cada um, junto o faturamento de quem contratou (`if servico in tabela["servicos"][i]`). Calculo quantos contratos, a porcentagem e o faturamento médio.

- **`correlacao(tabela)`** → quem tem mais serviços também fatura mais? `x` = quantidade de serviços, `y` = faturamento.
  - Precisa de pelo menos 3 clientes e de valores diferentes (`len(np.unique(x)) < 2` quer dizer "todo mundo tem o mesmo valor").
  - `sp.stats.pearsonr(x, y)` → o `r` vai de −1 a 1 (perto de 1: sobem juntos; perto de −1: um sobe e o outro desce; perto de 0: sem relação). O `p` diz se o resultado pode ter sido sorte.
  - `sp.stats.spearmanr(x, y)` → mesma ideia, mas usando a ordem (ranking) dos valores.
  - `forte` se |r| ≥ 0,7, `moderada` se ≥ 0,4, senão `fraca`.

- **`maiores_clientes(tabela, 5)`** → monto a lista de clientes, ordeno pelo faturamento com `reverse=True` (do maior para o menor) e pego `[0:5]`.

### Função principal `calcular(res, linhas_recebidas)`

Segue a mesma ordem dos programas da aula:

- **Entrada de dados:** pego a tabela; se não tem nenhum cliente, gero um erro.
- **Processamento dos dados:** chamo todas as funções acima, junto todos os serviços numa lista só (para a moda) e calculo o **coeficiente de variação** = desvio ÷ média.
- **Apresentação dos resultados:** devolvo o dict com `resumo`, `porSegmento`, `porNivel`, `porConsultor`, `porServico`, `porMes`, `correlacao`, `boxplot`, `histograma`, `intervaloConfianca`, `topClientes`, `qualidade` e `insights`.

---

## 4. insights.py (T – números viram frases)

Só Python puro: listas, `for`, `if` e juntar textos com `+`.

- **`brl(valor)`** → formata em reais. `f"{valor:,.2f}"` escreve no jeito americano (`1,135,000.50`); depois troco a vírgula por `X`, o ponto por vírgula e o `X` por ponto → `1.135.000,50`.
- **`virgula(numero)`** → `25.0` vira `"25,0"`.
- **`gerar_insights(...)`** → monta a lista `frases`:
  1. segmento com mais clientes (o primeiro da lista, que já vem ordenada);
  2. **média × mediana**: se a média é mais de 10% maior que a mediana, poucos clientes grandes estão puxando a média para cima (como no exemplo da renda da Aula 1, com a pessoa que ganha R$ 15.000);
  3. **outliers do boxplot**, com os limites e os nomes;
  4. **intervalo de confiança de 95%** da média;
  5. quanto os clientes nível A representam do faturamento;
  6. consultor com maior faturamento (percorro a lista com `for` guardando o maior);
  7. serviço mais contratado;
  8. mês com mais contratações (acho o maior número com `for` e depois os meses que têm esse número);
  9. a frase da correlação.

---

## 5. estudo.py (para eu rodar e ver os gráficos)

É um programa no mesmo formato dos exercícios da aula. Eu rodo com `python estudo.py` dentro da pasta `analytics-python`.

- **Carregamento das bibliotecas:** numpy, pandas, matplotlib e as funções `tratar` e `calcular`.
- **Entrada de dados:**
  - `pd.read_csv("amostras/clientes.csv", dtype=str)` lê a planilha (o `dtype=str` deixa tudo como texto, quem converte é o `limpeza.py`);
  - `.fillna("")` troca as células vazias (`NaN`) por texto vazio;
  - `.to_dict("records")` transforma em lista de dicionários, que é o mesmo formato que o backend manda.
- **Processamento dos dados:** `tratar` e `calcular`.
- **Apresentação dos resultados:** `print` das linhas recusadas, das medidas resumo, do boxplot, do histograma, do intervalo de confiança, da correlação e das frases. Depois 3 gráficos: `plt.boxplot`, `plt.hist` (com a quantidade de classes de Sturges) e `plt.scatter` (dispersão, para ver a correlação). O faturamento é dividido por 1000 para o eixo ficar em R$ mil.

A planilha `amostras/clientes.csv` tem 33 clientes fictícios, alguns escritos de propósito "errado" (`IND.`, `comercio`, `carlos lima`, `R$ 2.300.000,00`, data `15/02/2025`), para testar a limpeza. As 3 últimas linhas são recusadas: segmento "Agro" desconhecido, código repetido e uma linha com vários erros.

---

## 6. main.py (junta as etapas)

- `processar_lote(engine, lote_id, linhas, recebidas)` → a função que a API chama:
  1. `banco.reservar_lote` marca o lote como PROCESSANDO (se ele não estava esperando, para);
  2. `tratar(linhas)` → limpeza;
  3. `calcular(res, recebidas)` → estatísticas;
  4. `banco.gravar_resultado(...)` → grava no Azure;
  5. se acontecer qualquer erro (`except Exception`), o `traceback.print_exc()` mostra o erro completo no terminal e o lote é marcado como ERRO.

---

## 7. Partes que não são da aula (mas precisam existir)

Essas partes só existem para o sistema funcionar de verdade (o Spring conversar com o Python e o resultado ficar salvo). A ciência de dados está toda nos arquivos 2 a 5.

### api.py

- `FastAPI` cria a API. Ligo com `uvicorn api:app --port 8000`.
- `class Pedido(BaseModel)` → descreve o JSON que o Spring manda: `loteId`, `recebidas` e `linhas`.
- `pegar_conexao()` → abre a conexão com o Azure só uma vez (`global conexao` usa a variável de fora da função).
- `GET /saude` → responde `{"status": "ok"}` para saber se a API está ligada.
- `POST /etl` → confere a chave `X-ETL-Key` (se tiver uma no `.env`), chama `processar_lote` e responde 200 (deu certo), 401 (chave errada), 422 (sem linhas) ou 500 (erro no ETL).
- `POST /testar` → recebe a lista de clientes (como o `amostras/clientes.json`), calcula e devolve o resultado **sem gravar no banco**.

### banco.py

- `criar_engine()` → abre a conexão com o PostgreSQL.
- `reservar_lote` → `UPDATE` que muda o status para PROCESSANDO só se estava AGUARDANDO_ETL (evita processar o mesmo lote duas vezes).
- `marcar_erro` → grava ERRO e a mensagem no lote.
- `gravar_resultado` → numa transação só (ou grava tudo, ou nada):
  1. apaga o resultado antigo do lote (se estiver reprocessando);
  2. `for i in range(qtd_clientes)` → insere cada cliente, lendo da tabela dict de listas (`tabela["codigo_cliente"][i]`...);
  3. para cada serviço do cliente, insere um contrato;
  4. `id_de` acha (ou cria) o consultor/serviço e guarda o id num dict, para não perguntar ao banco toda hora;
  5. grava o JSON das estatísticas (`json.dumps`) e marca o lote como PROCESSADO.

### config.py

- Lê o `.env` com `load_dotenv` e monta o endereço do banco com `URL.create` (usuário, senha, servidor, porta, nome do banco e `sslmode=require`, que o Azure exige).

### testar_conexao.py

- Roda `SELECT version()` para ver se conectou no Azure e conta quantos lotes existem.

### .env.example e .gitignore

- `.env.example` é o modelo do `.env` (o `.env` de verdade tem a senha e **não vai para o Git**).
- `.gitignore` ignora o `.env`, o `.venv/` e os arquivos `__pycache__`.

---

## 8. Como rodar

```bash
python -m venv .venv                 # cria o ambiente virtual (só na 1a vez)
.venv\Scripts\activate               # ativa (Windows)
pip install -r requirements.txt      # instala as bibliotecas

python estudo.py                     # estudo: números no terminal + gráficos
uvicorn api:app --port 8000          # API p/ o Spring (precisa do .env c/ o banco)
```
