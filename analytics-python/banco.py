# ============================================================================
# banco.py — etapa de CARGA: grava no PostgreSQL (Azure) os clientes, os contratos
# e o JSON c/ as estatísticas.
#
# Obs: SQLAlchemy e SQL não são das aulas de Ciência de Dados. Esse arquivo só existe
# p/ guardar o resultado no banco que o resto do sistema usa.
# ============================================================================

# Carregamento das bibliotecas
import json                                         # transforma DICT em texto JSON
from datetime import datetime, timezone             # data e hora

from sqlalchemy import create_engine, text          # create_engine abre a conexão; text escreve SQL

from config import database_url                     # endereço do banco (vem do .env)


def criar_engine():
    # Abre a "porta" de conexão com o Azure. pool_pre_ping testa a conexão antes de usar
    return create_engine(database_url(), pool_pre_ping=True)


def agora_utc():
    # Hora atual em UTC sem fuso (o Spring Boot grava assim também)
    return datetime.now(timezone.utc).replace(tzinfo=None)


def reservar_lote(engine, lote_id):
    # Marca o lote como PROCESSANDO, só se ele estava AGUARDANDO_ETL (evita rodar 2 vezes)
    with engine.begin() as conn:                    # abre uma transação (confirma no fim do bloco)
        r = conn.execute(text("UPDATE upload_lote SET status = 'PROCESSANDO', "
                              "mensagem = 'ETL em andamento...' "
                              "WHERE id = :id AND status = 'AGUARDANDO_ETL'"),
                         {"id": lote_id})
        return r.rowcount == 1                      # True se mudou exatamente 1 lote


def marcar_erro(engine, lote_id, mensagem):
    # Grava ERRO no lote p/ a tela mostrar o que aconteceu
    with engine.begin() as conn:
        conn.execute(text("UPDATE upload_lote SET status = 'ERRO', mensagem = :m WHERE id = :id"),
                     {"id": lote_id, "m": mensagem[0:480]})     # a coluna aceita até 480 letras


def gravar_resultado(engine, lote_id, tabela, resultado, rejeitadas_etl):
    # Grava tudo numa transação só: ou grava o lote inteiro, ou nada muda
    with engine.begin() as conn:
        # Reprocessar o mesmo lote substitui o resultado anterior
        conn.execute(text("DELETE FROM cliente WHERE lote_id = :l"), {"l": lote_id})  # contratos saem junto
        conn.execute(text("DELETE FROM estatistica_lote WHERE lote_id = :l"), {"l": lote_id})

        consultores = {}                            # DICT: nome do consultor -> id no banco
        servicos = {}                               # DICT: nome do serviço -> id no banco

        def id_de(nome_tabela, nome, ids):
            # Acha (ou cria) o consultor/serviço e devolve o id dele
            if nome not in ids:
                conn.execute(text("INSERT INTO " + nome_tabela + " (nome) VALUES (:n) "
                                  "ON CONFLICT (nome) DO NOTHING"), {"n": nome})
                ids[nome] = conn.execute(text("SELECT id FROM " + nome_tabela + " WHERE nome = :n"),
                                         {"n": nome}).scalar_one()
            return ids[nome]

        qtd_clientes = len(tabela["codigo_cliente"])
        for i in range(qtd_clientes):               # um cliente por vez
            cliente_id = conn.execute(text("""
                INSERT INTO cliente (lote_id, codigo_cti, nome, consultor_id, segmento, nivel,
                                     faturamento_anual, cidade, uf)
                VALUES (:lote, :codigo, :nome, :consultor, :segmento, :nivel, :fat, :cidade, :uf)
                RETURNING id
            """), {"lote": lote_id,
                   "codigo": tabela["codigo_cliente"][i][0:60],         # corta no tamanho da coluna
                   "nome": tabela["nome_cliente"][i][0:255],
                   "consultor": id_de("consultor", tabela["consultor"][i][0:120], consultores),
                   "segmento": tabela["segmento"][i][0:40],
                   "nivel": tabela["nivel_cliente"][i],
                   "fat": round(tabela["faturamento_anual"][i], 2),
                   "cidade": tabela["cidade"][i][0:120] if tabela["cidade"][i] is not None else None,
                   "uf": tabela["uf"][i]}).scalar_one()

            for nome_servico in tabela["servicos"][i]:      # um contrato p/ cada serviço
                conn.execute(text("INSERT INTO contrato (cliente_id, servico_id, data_contratacao) "
                                  "VALUES (:c, :s, :d)"),
                             {"c": cliente_id,
                              "s": id_de("servico", nome_servico[0:120], servicos),
                              "d": tabela["data_contratacao"][i]})

        # Grava o JSON c/ média, mediana, boxplot, histograma...
        conn.execute(text("INSERT INTO estatistica_lote (lote_id, gerado_em, resultado) "
                          "VALUES (:l, :g, :r)"),
                     {"l": lote_id, "g": agora_utc(), "r": json.dumps(resultado, ensure_ascii=False)})

        mensagem = "ETL concluído: " + str(qtd_clientes) + " clientes tratados"
        if rejeitadas_etl > 0:
            mensagem = mensagem + ", " + str(rejeitadas_etl) + " linhas recusadas na limpeza"
        conn.execute(text("UPDATE upload_lote SET status = 'PROCESSADO', mensagem = :m WHERE id = :id"),
                     {"id": lote_id, "m": mensagem + "."})
    print("Lote", lote_id, "gravado:", qtd_clientes, "clientes,",
          len(consultores), "consultores,", len(servicos), "serviços")
