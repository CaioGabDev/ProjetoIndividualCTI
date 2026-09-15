<div align="center">

  <img src="https://img.shields.io/badge/status-em%20desenvolvimento-1D9E75?style=for-the-badge" alt="status" />
  <img src="https://img.shields.io/badge/Vue-3-42b883?style=for-the-badge&logo=vue.js&logoColor=white" alt="vue" />
  <img src="https://img.shields.io/badge/Tailwind_CSS-38bdf8?style=for-the-badge&logo=tailwindcss&logoColor=white" alt="tailwind" />
  <img src="https://img.shields.io/badge/SENAI-Roberto%20Mange-E31937?style=for-the-badge" alt="senai" />

  <h1>⚓ Âncora — Insights Comerciais</h1>
  <p><b>Projeto Integrador · Sistema CTI Insights</b></p>
  <p>Transforme planilhas em decisões estratégicas.</p>

</div>

---

## 📖 Sobre o projeto

A **Âncora** é a interface do **Sistema CTI Insights**, desenvolvido como Projeto Integrador do curso técnico do **SENAI Roberto Mange**, sob orientação da professora **Ma. Tatiana Aparecida de Almeida**.

### O problema

A empresa parceira (CTI) gerencia sua carteira de clientes inteiramente em **planilhas de Excel**, preenchidas manualmente. Isso gera:

- Erros de digitação e inconsistências (ex.: `"IND."`, `"Industria"`, `"Indústria"` como se fossem coisas diferentes);
- Ausência de análises automáticas ou cruzamento de dados;
- Impossibilidade de responder rapidamente perguntas simples como *"qual segmento traz mais retorno para a empresa?"*.

### A solução

Um sistema web que:

- Recebe o upload da planilha da carteira de clientes;
- Trata e padroniza os dados automaticamente (espaços, capitalização, nomenclatura de segmento, nível A/B/C);
- Gera dashboards e insights estratégicos a partir dos dados tratados;
- Armazena tudo com segurança em nuvem, seguindo princípios de privacidade (LGPD).

---

## 🖥️ Telas do sistema

| Tela | Descrição |
|---|---|
| **Home** | Landing page institucional, apresentação do produto para quem ainda não está logado |
| **Login** | Autenticação do usuário (funcionário autorizado da CTI) |
| **Dashboard** | KPIs, gráficos de faturamento por segmento, distribuição por nível (A/B/C), ranking de serviços |
| **Clientes** | Listagem da carteira com filtros por nível/segmento e ações rápidas |
| **Enviar planilha** | Upload de `.xlsx` / `.xls` / `.csv`, com prévia dos dados tratados e histórico de envios |

---

## 🎨 Design system

### Paleta de cores

| Papel | Cor | Hex |
|---|---|---|
| Primary | 🟢 Verde | `#1D9E75` |
| Secondary | 🟣 Roxo | `#7F77D0` |
| Tertiary | 🟠 Terracota | `#E87A5F` |
| Neutral | ⚫ Grafite/Preto | `#0F1115` |

### Tipografia

| Uso | Fonte |
|---|---|
| Headline / Body | `Hanken Grotesk` |
| Labels / dados numéricos | `JetBrains Mono` |

### Componentes de UI

Botões (`Primary`, `Secondary`, `Inverted`, `Outlined`), badges de nível (A/B/C), chips de status de upload (`Processado`, `Processando`, `Erro`), cards de KPI, tabelas com paginação, barra de busca.

---

## 🛠️ Stack utilizada

### Front-end (este repositório)

| Camada | Tecnologia |
|---|---|
| Framework | [Vue 3](https://vuejs.org/) (Composition API + `<script setup>`) |
| Build tool | [Vite](https://vitejs.dev/) |
| Estilização | [Tailwind CSS](https://tailwindcss.com/) |
| Roteamento | [Vue Router](https://router.vuejs.org/) |
| Gerenciamento de estado | [Pinia](https://pinia.vuejs.org/) |
| Leitura de planilhas | [SheetJS (xlsx)](https://sheetjs.com/) |
| Gráficos | [Chart.js](https://www.chartjs.org/) |

### Ecossistema do Projeto Integrador (repositórios relacionados)

| Camada | Tecnologia |
|---|---|
| Back-end | Java + Spring Boot (API REST, autenticação, regras de negócio) |
| Ciência de dados | Python + Pandas (limpeza, tratamento e geração de insights) |
| Banco de dados | PostgreSQL (Neon / Azure Database for PostgreSQL) |

### Fluxo de dados

```
Planilha Excel → Vue 3 (Front-end) → Spring Boot (Java) → Python (Ciência de Dados) → PostgreSQL → Dashboards (Chart.js)
```

---

## 📁 Estrutura de pastas

```
src/
├── assets/              # imagens, ícones, fontes
├── components/          # componentes reutilizáveis (Grafico, MarcaAncora, AppIcon...)
├── directives/
│   └── revelar.js       # v-revelar: anima a entrada dos blocos ao rolar a página
├── router/
│   └── index.js         # definição das rotas
├── stores/
│   └── uploadStore.js   # estado global do upload (Pinia)
├── views/
│   ├── Home.vue
│   ├── Login.vue
│   ├── Dashboard.vue
│   ├── Clientes.vue
│   └── Upload.vue
├── services/
│   ├── api.js            # (futuro) cliente Axios para a API Spring Boot
│   ├── auth.js           # sessão do usuário (hoje em localStorage)
│   └── exportar.js       # geração dos arquivos .xlsx (SheetJS)
├── App.vue
├── main.js
└── style.css
```

---

## 🧭 Rotas

| Caminho | Componente | Acesso |
|---|---|---|
| `/` | `Home.vue` | Público |
| `/login` | `Login.vue` | Público |
| `/dashboard` | `Dashboard.vue` | Autenticado |
| `/clientes` | `Clientes.vue` | Autenticado |
| `/upload` | `Upload.vue` | Autenticado |

> As rotas autenticadas usam `meta: { requiresAuth: true }` e são protegidas por um *navigation guard* em `router/index.js`.

---

## 🚀 Como rodar o projeto

### Pré-requisitos

- [Node.js](https://nodejs.org/) 18+
- npm

### Instalação

```bash
# clonar o repositório
git clone https://github.com/CaioGabDev/ProjetoIndividualCTI.git
cd ProjetoIndividualCTI

# instalar dependências
npm install

# rodar em ambiente de desenvolvimento
npm run dev
```

O projeto abre por padrão em `http://localhost:5173`.

### Build de produção

```bash
npm run build
npm run preview
```

---

## 📦 Principais dependências

```json
{
  "dependencies": {
    "vue": "^3.5",
    "vue-router": "^4.6",
    "pinia": "^4.0",
    "xlsx": "0.20.2",
    "chart.js": "^4.5",
    "tailwindcss": "^4.3",
    "@fontsource-variable/hanken-grotesk": "^5.3",
    "@fontsource-variable/jetbrains-mono": "^5.3"
  },
  "devDependencies": {
    "vite": "^8.2",
    "@vitejs/plugin-vue": "^6.0"
  }
}
```

> As fontes são auto-hospedadas via `@fontsource`: o projeto não depende de CDN
> nem de internet para renderizar com a tipografia correta.

> Confira as versões exatas instaladas no `package.json` do repositório.

---

## ✅ Funcionalidades

- [x] Landing page institucional
- [x] Tela de login
- [x] Upload de planilha (`.xlsx`, `.xls`, `.csv`)
- [x] Tratamento e padronização de dados no front-end (Pinia + SheetJS)
- [x] Prévia dos dados tratados antes do envio
- [x] Dashboard com KPIs e gráficos
- [x] Listagem de clientes com filtros
- [x] Exportação da carteira em `.xlsx` (respeitando o filtro da tela)
- [x] Exportação da planilha tratada após o upload
- [x] Leitura da carteira: insights escritos a partir dos dados
- [x] Painel responsivo (sidebar vira gaveta no celular)
- [x] Gráficos clicáveis: a barra/fatia leva para a carteira já filtrada
- [x] Sparkline de tendência nos cartões de indicador
- [x] Esqueleto de carregamento e progresso real na leitura da planilha
- [x] Preferência de movimento (seguir o sistema / ligada / desligada)
- [ ] Integração com API Spring Boot
- [ ] Persistência em PostgreSQL
- [ ] Autenticação real (JWT / sessão)
- [ ] Geração de insights via Python

---

## 🗺️ Roadmap

1. **Front-end** (atual) — telas, tratamento local dos dados, protótipo visual.
2. **Back-end** — API REST em Spring Boot para autenticação e persistência.
3. **Ciência de dados** — scripts Python para tratamento avançado e geração de insights.
4. **Integração** — Axios conectando o front-end à API; deploy em nuvem (Azure).

---

## 👥 Autoria

| Papel | Nome |
|---|---|
| Desenvolvimento | Caio Gabriel ([@CaioGabDev](https://github.com/CaioGabDev)) |
| Orientação | Ma. Tatiana Aparecida de Almeida |
| Instituição | SENAI Roberto Mange |

---

## 📄 Licença

Projeto acadêmico desenvolvido para fins educacionais no âmbito do Projeto Integrador do SENAI Roberto Mange.
