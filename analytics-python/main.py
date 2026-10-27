# ============================================================================
# main.py — junta as etapas na ordem: limpeza -> análise -> grava no banco.
# Quem chama a função processar_lote é a API (api.py), quando o Spring manda um lote.
# ============================================================================

# Carregamento das bibliotecas
import traceback                    # mostra o erro completo no terminal

from limpeza import tratar          # limpa e padroniza (limpeza.py)
from analise import calcular        # calcula as estatísticas (analise.py)
import banco                        # grava no PostgreSQL do Azure (banco.py)


def processar_lote(engine, lote_id, linhas, recebidas):
    # Marca o lote como PROCESSANDO (só se ele ainda estava esperando o ETL)
    if not banco.reservar_lote(engine, lote_id):
        print("Lote", lote_id, "não está disponível para processamento.")
        return False

    try:
        # Entrada de dados
        if len(linhas) == 0:
            raise ValueError("o backend não enviou linhas válidas")

        # Processamento dos dados
        res = tratar(linhas)                        # limpeza
        resultado = calcular(res, recebidas)        # estatísticas e frases

        # Apresentação dos resultados (grava no banco)
        banco.gravar_resultado(engine, lote_id, res["tratado"], resultado, len(res["rejeitadas"]))
        print("Lote", lote_id, "processado.")
        return True
    except Exception as erro:                       # qualquer erro: avisa e marca ERRO no lote
        traceback.print_exc()
        banco.marcar_erro(engine, lote_id, "Falha no ETL: " + str(erro))
        return False
