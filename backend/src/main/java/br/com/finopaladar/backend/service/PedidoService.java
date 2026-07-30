package br.com.finopaladar.backend.service;

import br.com.finopaladar.backend.dto.HistoricoStatusResponse;
import br.com.finopaladar.backend.dto.PedidoEnderecoRequest;
import br.com.finopaladar.backend.dto.PedidoItemRequest;
import br.com.finopaladar.backend.dto.PedidoRequest;
import br.com.finopaladar.backend.dto.PedidoResponse;
import br.com.finopaladar.backend.dto.PedidoStatusRequest;
import br.com.finopaladar.backend.dto.VendaManualRequest;
import br.com.finopaladar.backend.entity.CanalVenda;
import br.com.finopaladar.backend.entity.DisponibilidadeProduto;
import br.com.finopaladar.backend.entity.HistoricoStatus;
import br.com.finopaladar.backend.entity.ItemPedido;
import br.com.finopaladar.backend.entity.Pedido;
import br.com.finopaladar.backend.entity.Produto;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.entity.TipoRecebimento;
import br.com.finopaladar.backend.exception.BusinessException;
import br.com.finopaladar.backend.exception.ResourceNotFoundException;
import br.com.finopaladar.backend.mapper.PedidoMapper;
import br.com.finopaladar.backend.repository.DisponibilidadeProdutoRepository;
import br.com.finopaladar.backend.repository.HistoricoStatusRepository;
import br.com.finopaladar.backend.repository.PedidoRepository;
import br.com.finopaladar.backend.repository.ProdutoRepository;
import br.com.finopaladar.backend.util.PedidoCodigoGenerator;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    private static final String TIPO_ENDERECO_CONDOMINIO = "CONDOMINIO";
    private static final int TENTATIVAS_GERAR_CODIGO = 5;

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;
    private final DisponibilidadeProdutoRepository disponibilidadeProdutoRepository;
    private final HistoricoStatusRepository historicoStatusRepository;
    private final PedidoMapper pedidoMapper;
    private final PedidoCodigoGenerator pedidoCodigoGenerator;
    private final WhatsAppMessageService whatsAppMessageService;
    private final BigDecimal taxaEntregaConfigurada;

    public PedidoService(
            PedidoRepository pedidoRepository,
            ProdutoRepository produtoRepository,
            DisponibilidadeProdutoRepository disponibilidadeProdutoRepository,
            HistoricoStatusRepository historicoStatusRepository,
            PedidoMapper pedidoMapper,
            PedidoCodigoGenerator pedidoCodigoGenerator,
            WhatsAppMessageService whatsAppMessageService,
            @Value("${pedido.taxa-entrega:0.00}") BigDecimal taxaEntregaConfigurada
    ) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
        this.disponibilidadeProdutoRepository = disponibilidadeProdutoRepository;
        this.historicoStatusRepository = historicoStatusRepository;
        this.pedidoMapper = pedidoMapper;
        this.pedidoCodigoGenerator = pedidoCodigoGenerator;
        this.whatsAppMessageService = whatsAppMessageService;
        this.taxaEntregaConfigurada = taxaEntregaConfigurada == null ? BigDecimal.ZERO : taxaEntregaConfigurada;
    }

    @Transactional
    public PedidoResponse criar(PedidoRequest request) {
        return criarPedido(request, StatusPedido.AGUARDANDO_CONFIRMACAO, null, true);
    }

    @Transactional
    public PedidoResponse registrarVendaManual(VendaManualRequest request) {
        validarVendaManual(request);
        return criarPedido(request.toPedidoRequest(), StatusPedido.ENTREGUE, request.canalVenda(), false);
    }

    private PedidoResponse criarPedido(
            PedidoRequest request,
            StatusPedido statusInicial,
            CanalVenda canalVenda,
            boolean gerarWhatsApp
    ) {
        validarItens(request.itens());
        validarEnderecoEntrega(request);

        LocalDate dataPedido = LocalDate.now();
        Pedido pedido = pedidoMapper.toEntity(request);
        pedido.setCodigo(gerarCodigoUnico());
        pedido.setStatus(statusInicial);
        pedido.setCanalVenda(canalVenda);
        pedido.setData(dataPedido);

        BigDecimal valorProdutos = BigDecimal.ZERO;
        for (PedidoItemRequest itemRequest : request.itens()) {
            Produto produto = buscarProdutoValido(itemRequest.produtoId());
            DisponibilidadeProduto disponibilidadeProduto = buscarDisponibilidadeVigente(produto.getId(), dataPedido);
            validarQuantidadeDisponivel(disponibilidadeProduto, itemRequest.quantidade(), produto);
            reduzirDisponibilidade(disponibilidadeProduto, itemRequest.quantidade());

            ItemPedido itemPedido = criarItemPedido(produto, itemRequest.quantidade());
            pedido.adicionarItem(itemPedido);
            valorProdutos = valorProdutos.add(itemPedido.getSubtotal());
        }

        BigDecimal taxaEntrega = calcularTaxaEntrega(request.tipoRecebimento());
        pedido.setValorProdutos(valorProdutos);
        pedido.setTaxaEntrega(taxaEntrega);
        pedido.setValorTotal(valorProdutos.add(taxaEntrega));
        adicionarEnderecoSeNecessario(request, pedido);
        registrarHistoricoStatus(pedido, statusInicial);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);
        if (!gerarWhatsApp) {
            return pedidoMapper.toResponse(pedidoSalvo);
        }

        WhatsAppMessageService.WhatsAppPedido whatsAppPedido = whatsAppMessageService.gerarParaPedido(pedidoSalvo);
        return pedidoMapper.toResponse(pedidoSalvo, whatsAppPedido.mensagem(), whatsAppPedido.linkWhatsApp());
    }

    @Transactional(readOnly = true)
    public PedidoResponse buscarPorCodigo(String codigo) {
        Pedido pedido = pedidoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado"));
        return pedidoMapper.toResponse(pedido);
    }

    @Transactional
    public void alterarStatus(Long id, PedidoStatusRequest request) {
        if (request == null || request.status() == null) {
            throw new BusinessException("Status deve ser informado");
        }

        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido nao encontrado"));

        StatusPedido statusAtual = pedido.getStatus();
        StatusPedido novoStatus = request.status();
        validarTransicaoStatus(statusAtual, novoStatus);

        if (novoStatus == StatusPedido.CANCELADO) {
            devolverItensAoEstoque(pedido);
        }

        pedido.setStatus(novoStatus);
        registrarHistoricoStatus(pedido, novoStatus);
        pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public List<HistoricoStatusResponse> listarHistoricoPorCodigo(String codigo) {
        if (!pedidoRepository.existsByCodigo(codigo)) {
            throw new ResourceNotFoundException("Pedido nao encontrado");
        }

        return historicoStatusRepository.findByPedidoCodigoOrderByDataHoraAsc(codigo)
                .stream()
                .map(pedidoMapper::toHistoricoStatusResponse)
                .toList();
    }

    private void validarItens(List<PedidoItemRequest> itens) {
        if (itens == null || itens.isEmpty()) {
            throw new BusinessException("Pedido deve possuir ao menos um item");
        }

        Set<Long> produtos = new HashSet<>();
        for (PedidoItemRequest item : itens) {
            if (item.produtoId() == null) {
                throw new BusinessException("Produto deve ser informado");
            }
            if (item.quantidade() == null || item.quantidade() <= 0) {
                throw new BusinessException("Quantidade deve ser maior que zero");
            }
            if (!produtos.add(item.produtoId())) {
                throw new BusinessException("Produto duplicado no pedido");
            }
        }
    }

    private void validarVendaManual(VendaManualRequest request) {
        if (request == null) {
            throw new BusinessException("Venda manual deve ser informada");
        }
        if (request.canalVenda() == null) {
            throw new BusinessException("Canal da venda manual deve ser informado");
        }
    }

    private void validarEnderecoEntrega(PedidoRequest request) {
        if (request.tipoRecebimento() != TipoRecebimento.ENTREGA) {
            return;
        }

        PedidoEnderecoRequest endereco = request.enderecoEntrega();
        if (endereco == null) {
            throw new BusinessException("Endereco de entrega deve ser informado");
        }
        if (endereco.cidade() == null) {
            throw new BusinessException("Cidade deve ser informada para entrega");
        }
        if (isBlank(endereco.tipoEndereco())) {
            throw new BusinessException("Tipo de endereco deve ser informado para entrega");
        }

        if (isEnderecoCondominio(endereco)) {
            validarCamposCondominio(endereco);
            return;
        }

        validarCamposEnderecoComum(endereco);
    }

    private void validarCamposCondominio(PedidoEnderecoRequest endereco) {
        if (isBlank(endereco.condominio()) || isBlank(endereco.quadra()) || isBlank(endereco.lote())) {
            throw new BusinessException("Condominio, quadra e lote devem ser informados");
        }
    }

    private void validarCamposEnderecoComum(PedidoEnderecoRequest endereco) {
        if (isBlank(endereco.bairro()) || isBlank(endereco.rua()) || isBlank(endereco.numero())) {
            throw new BusinessException("Bairro, rua e numero devem ser informados");
        }
    }

    private Produto buscarProdutoValido(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto nao encontrado"));

        if (!Boolean.TRUE.equals(produto.getAtivo())) {
            throw new BusinessException("Produto inativo nao pode ser comprado");
        }

        return produto;
    }

    private DisponibilidadeProduto buscarDisponibilidadeVigente(Long produtoId, LocalDate dataPedido) {
        return disponibilidadeProdutoRepository.findDisponibilidadeVigenteComLock(produtoId, dataPedido)
                .orElseThrow(() -> new BusinessException("Produto indisponivel"));
    }

    private void validarQuantidadeDisponivel(
            DisponibilidadeProduto disponibilidadeProduto,
            Integer quantidadeSolicitada,
            Produto produto
    ) {
        if (disponibilidadeProduto.getQuantidadeDisponivel() < quantidadeSolicitada) {
            throw new BusinessException("Quantidade indisponivel para o produto " + produto.getNome());
        }
    }

    private void reduzirDisponibilidade(DisponibilidadeProduto disponibilidadeProduto, Integer quantidadeSolicitada) {
        disponibilidadeProduto.setQuantidadeDisponivel(
                disponibilidadeProduto.getQuantidadeDisponivel() - quantidadeSolicitada
        );
    }

    private ItemPedido criarItemPedido(Produto produto, Integer quantidade) {
        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setProduto(produto);
        itemPedido.setNomeProduto(produto.getNome());
        itemPedido.setPrecoUnitario(produto.getPreco());
        itemPedido.setPesoMedioGramas(produto.getPesoMedioGramas());
        itemPedido.setQuantidade(quantidade);
        itemPedido.setSubtotal(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));
        return itemPedido;
    }

    private BigDecimal calcularTaxaEntrega(TipoRecebimento tipoRecebimento) {
        if (tipoRecebimento == TipoRecebimento.ENTREGA) {
            return taxaEntregaConfigurada;
        }

        return BigDecimal.ZERO;
    }

    private void validarTransicaoStatus(StatusPedido statusAtual, StatusPedido novoStatus) {
        if (statusAtual == novoStatus) {
            throw new BusinessException("Novo status deve ser diferente do status atual");
        }

        if (!isTransicaoStatusPermitida(statusAtual, novoStatus)) {
            throw new BusinessException("Transicao de status invalida");
        }
    }

    private boolean isTransicaoStatusPermitida(StatusPedido statusAtual, StatusPedido novoStatus) {
        return switch (statusAtual) {
            case AGUARDANDO_CONFIRMACAO -> novoStatus == StatusPedido.ACEITO
                    || novoStatus == StatusPedido.CANCELADO;
            case ACEITO -> novoStatus == StatusPedido.EM_PREPARACAO
                    || novoStatus == StatusPedido.CANCELADO;
            case EM_PREPARACAO -> novoStatus == StatusPedido.PRONTO_PARA_RETIRADA
                    || novoStatus == StatusPedido.SAIU_PARA_ENTREGA;
            case PRONTO_PARA_RETIRADA, SAIU_PARA_ENTREGA -> novoStatus == StatusPedido.ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }

    private void devolverItensAoEstoque(Pedido pedido) {
        for (ItemPedido itemPedido : pedido.getItens()) {
            DisponibilidadeProduto disponibilidadeProduto = disponibilidadeProdutoRepository
                    .findByProdutoIdComLock(itemPedido.getProduto().getId())
                    .orElseThrow(() -> new BusinessException("Disponibilidade do produto nao encontrada"));
            disponibilidadeProduto.setQuantidadeDisponivel(
                    disponibilidadeProduto.getQuantidadeDisponivel() + itemPedido.getQuantidade()
            );
        }
    }

    private void adicionarEnderecoSeNecessario(PedidoRequest request, Pedido pedido) {
        if (request.tipoRecebimento() == TipoRecebimento.ENTREGA) {
            pedido.setEnderecoEntrega(pedidoMapper.toEnderecoEntrega(request.enderecoEntrega()));
        }
    }

    private void registrarHistoricoStatus(Pedido pedido, StatusPedido status) {
        HistoricoStatus historicoStatus = new HistoricoStatus();
        historicoStatus.setStatus(status);
        historicoStatus.setDataHora(Instant.now());
        pedido.adicionarHistoricoStatus(historicoStatus);
    }

    private String gerarCodigoUnico() {
        for (int tentativa = 0; tentativa < TENTATIVAS_GERAR_CODIGO; tentativa++) {
            String codigo = pedidoCodigoGenerator.gerar();
            if (!pedidoRepository.existsByCodigo(codigo)) {
                return codigo;
            }
        }

        throw new BusinessException("Nao foi possivel gerar codigo do pedido");
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.isBlank();
    }

    private boolean isEnderecoCondominio(PedidoEnderecoRequest endereco) {
        return TIPO_ENDERECO_CONDOMINIO.equalsIgnoreCase(endereco.tipoEndereco().trim());
    }
}
