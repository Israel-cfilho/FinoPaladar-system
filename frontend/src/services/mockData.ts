import type { ProdutoPublico } from '@/types';

export const produtosMock: ProdutoPublico[] = [
  {
    id: 1,
    nome: 'Bolo de Rolo Clássico',
    preco: 45.0,
    pesoMedioGramas: 450,
    imagem: '/images/bolo-classico.jpg',
    quantidadeDisponivel: 12,
  },
  {
    id: 2,
    nome: 'Bolo de Rolo de Goiabada',
    preco: 48.0,
    pesoMedioGramas: 450,
    imagem: '/images/bolo-goiabada.jpg',
    quantidadeDisponivel: 8,
  },
  {
    id: 3,
    nome: 'Bolo de Rolo de Chocolate',
    preco: 52.0,
    pesoMedioGramas: 480,
    imagem: '/images/bolo-chocolate.jpg',
    quantidadeDisponivel: 5,
  },
  {
    id: 4,
    nome: 'Bolo de Rolo de Doce de Leite',
    preco: 50.0,
    pesoMedioGramas: 460,
    imagem: '/images/bolo-doce-de-leite.jpg',
    quantidadeDisponivel: 0,
  },
];

export const WHATSAPP_NUMBER = '5581999999999';

export const LOJA_INFO = {
  nome: 'Fino Paladar',
  slogan: 'Bolos de rolo artesanais, feitos com carinho de família.',
  cidade: 'Recife',
  estado: 'PE',
};
