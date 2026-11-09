/*
  Números ILUSTRATIVOS da página inicial (Home.vue), que é pública: quem ainda
  não entrou não pode ver a carteira de verdade, então a vitrine usa este
  exemplo fixo. Nenhuma tela do painel lê daqui — Dashboard, Clientes e
  Envio buscam tudo na API (services/api.js).
*/

/*
  `serie` são os últimos 6 meses do indicador, usados na sparkline do cartão.
  Servem só para desenhar a tendência — o número grande continua sendo `valor`.
*/
export const indicadores = [
  {
    rotulo: 'Faturamento total', valor: 'R$ 4,82M', variacao: '+12,4%', contexto: 'vs. mês anterior', positiva: true,
    serie: [3.61, 3.84, 3.72, 4.15, 4.29, 4.82],
  },
  {
    rotulo: 'Clientes ativos', valor: '312', variacao: '+8', contexto: 'novos neste mês', positiva: true,
    serie: [281, 288, 292, 297, 304, 312],
  },
  {
    rotulo: 'Ticket médio', valor: 'R$ 15,4k', variacao: '-2,1%', contexto: 'vs. mês anterior', positiva: false,
    serie: [16.2, 16.4, 15.9, 16.1, 15.7, 15.4],
  },
  {
    rotulo: 'Nível A da carteira', valor: '28%', variacao: '+3 p.p.', contexto: 'no trimestre', positiva: true,
    serie: [23, 24, 25, 25, 27, 28],
  },
]

/*
  Faturamento por segmento — barras verticais do dashboard.
  A cor é uma única escala do verde da marca: quanto maior o valor, mais forte
  a barra. Cor aqui é hierarquia, não decoração.
*/
export const faturamentoPorSegmento = [
  { segmento: 'Indústria', valor: 1_480_000, rotulo: 'R$ 1,48M', cor: '#2BB98B' },
  { segmento: 'Varejo',    valor: 1_040_000, rotulo: 'R$ 1,04M', cor: '#1D9E75' },
  { segmento: 'Serviços',  valor: 860_000,   rotulo: 'R$ 860k',  cor: '#198A66' },
  { segmento: 'Saúde',     valor: 620_000,   rotulo: 'R$ 620k',  cor: '#147356' },
  { segmento: 'Agro',      valor: 480_000,   rotulo: 'R$ 480k',  cor: '#105C45' },
  { segmento: 'Educação',  valor: 340_000,   rotulo: 'R$ 340k',  cor: '#0C4534' },
]
