# ============================================================================
# api.py — a "porta de entrada" do Python: transforma o ETL numa API (FastAPI).
# O Spring chama POST /etl mandando o JSON do lote; o Python limpa, calcula e grava no Azure.
# Como ligar:  uvicorn api:app --port 8000   (documentação automática em http://localhost:8000/docs)
#
# Obs: FastAPI e Pydantic não são das aulas de Ciência de Dados. Eles só existem aqui
# p/ o Spring conseguir conversar com o Python pela internet.
# ============================================================================

# Carregamento das bibliotecas
import os                                           # lê a variável ETL_API_KEY

from fastapi import FastAPI, Header, HTTPException  # cria a API e devolve erros HTTP
from pydantic import BaseModel                      # descreve o formato do JSON que chega

import config                                       # carrega o .env (senha do banco, chave)
import banco                                        # conexão com o Azure
from limpeza import tratar
from analise import calcular
from main import processar_lote

app = FastAPI(title="CTI Insights — ETL em Python")
conexao = None                                      # conexão com o Azure (criada no 1o uso)


class Pedido(BaseModel):
    # Formato do JSON que o Spring manda
    loteId: int                                     # id do lote no banco
    recebidas: int                                  # total de linhas que chegaram ao backend
    linhas: list[dict]                              # as linhas válidas (lista de dicionários)


def pegar_conexao():
    # Abre a conexão com o Azure só uma vez e reaproveita depois
    global conexao                                  # usa a variável de fora da função
    if conexao is None:
        conexao = banco.criar_engine()
    return conexao


@app.get("/saude")                                  # GET /saude: confere se a API está ligada
def saude():
    return {"status": "ok"}


@app.post("/etl")                                   # POST /etl: processa o lote e grava no Azure
def etl(pedido: Pedido, x_etl_key: str | None = Header(default=None)):
    chave = os.getenv("ETL_API_KEY", "").strip()    # chave combinada com o Spring
    if chave != "" and x_etl_key != chave:
        raise HTTPException(status_code=401, detail="Chave do ETL inválida.")
    if len(pedido.linhas) == 0:
        raise HTTPException(status_code=422, detail="Nenhuma linha válida foi enviada.")
    ok = processar_lote(pegar_conexao(), pedido.loteId, pedido.linhas, pedido.recebidas)
    if not ok:
        raise HTTPException(status_code=500, detail="Falha no ETL (veja o terminal do Python).")
    return {"ok": True, "loteId": pedido.loteId}


@app.post("/testar")                                # POST /testar: calcula sem gravar nada no banco
def testar(linhas: list[dict]):
    res = tratar(linhas)
    return calcular(res, len(linhas))
