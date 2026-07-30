package br.com.finopaladar.backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "pedidos",
        uniqueConstraints = @UniqueConstraint(name = "uk_pedidos_codigo", columnNames = "codigo")
)
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 40)
    @Column(nullable = false, length = 40)
    private String codigo;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String cliente;

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, length = 30)
    private String telefone;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusPedido status = StatusPedido.AGUARDANDO_CONFIRMACAO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 20)
    private FormaPagamento formaPagamento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_recebimento", nullable = false, length = 20)
    private TipoRecebimento tipoRecebimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "canal_venda", length = 20)
    private CanalVenda canalVenda;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_produtos", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorProdutos;

    @NotNull
    @PositiveOrZero
    @Column(name = "taxa_entrega", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxaEntrega = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(columnDefinition = "text")
    private String observacao;

    @NotNull
    @Column(nullable = false)
    private LocalDate data;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private EnderecoEntrega enderecoEntrega;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataHora ASC")
    private List<HistoricoStatus> historicoStatus = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public TipoRecebimento getTipoRecebimento() {
        return tipoRecebimento;
    }

    public void setTipoRecebimento(TipoRecebimento tipoRecebimento) {
        this.tipoRecebimento = tipoRecebimento;
    }

    public CanalVenda getCanalVenda() {
        return canalVenda;
    }

    public void setCanalVenda(CanalVenda canalVenda) {
        this.canalVenda = canalVenda;
    }

    public BigDecimal getValorProdutos() {
        return valorProdutos;
    }

    public void setValorProdutos(BigDecimal valorProdutos) {
        this.valorProdutos = valorProdutos;
    }

    public BigDecimal getTaxaEntrega() {
        return taxaEntrega;
    }

    public void setTaxaEntrega(BigDecimal taxaEntrega) {
        this.taxaEntrega = taxaEntrega;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }

    public void setItens(List<ItemPedido> itens) {
        this.itens = itens;
    }

    public void adicionarItem(ItemPedido itemPedido) {
        itemPedido.setPedido(this);
        itens.add(itemPedido);
    }

    public EnderecoEntrega getEnderecoEntrega() {
        return enderecoEntrega;
    }

    public void setEnderecoEntrega(EnderecoEntrega enderecoEntrega) {
        this.enderecoEntrega = enderecoEntrega;
        if (enderecoEntrega != null) {
            enderecoEntrega.setPedido(this);
        }
    }

    public List<HistoricoStatus> getHistoricoStatus() {
        return historicoStatus;
    }

    public void setHistoricoStatus(List<HistoricoStatus> historicoStatus) {
        this.historicoStatus = historicoStatus;
    }

    public void adicionarHistoricoStatus(HistoricoStatus historicoStatus) {
        historicoStatus.setPedido(this);
        this.historicoStatus.add(historicoStatus);
    }
}
