package br.com.finopaladar.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "enderecos_entrega",
        uniqueConstraints = @UniqueConstraint(name = "uk_enderecos_entrega_pedido", columnNames = "pedido_id")
)
public class EnderecoEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "pedido_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_enderecos_entrega_pedido")
    )
    private Pedido pedido;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Cidade cidade;

    @NotBlank
    @Size(max = 40)
    @Column(name = "tipo_endereco", nullable = false, length = 40)
    private String tipoEndereco;

    @Size(max = 120)
    @Column(length = 120)
    private String condominio;

    @Size(max = 50)
    @Column(length = 50)
    private String quadra;

    @Size(max = 50)
    @Column(length = 50)
    private String lote;

    @Size(max = 120)
    @Column(length = 120)
    private String bairro;

    @Size(max = 150)
    @Column(length = 150)
    private String rua;

    @Size(max = 30)
    @Column(length = 30)
    private String numero;

    @Size(max = 150)
    @Column(length = 150)
    private String complemento;

    @Size(max = 150)
    @Column(name = "ponto_referencia", length = 150)
    private String pontoReferencia;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public void setPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    public String getTipoEndereco() {
        return tipoEndereco;
    }

    public void setTipoEndereco(String tipoEndereco) {
        this.tipoEndereco = tipoEndereco;
    }

    public String getCondominio() {
        return condominio;
    }

    public void setCondominio(String condominio) {
        this.condominio = condominio;
    }

    public String getQuadra() {
        return quadra;
    }

    public void setQuadra(String quadra) {
        this.quadra = quadra;
    }

    public String getLote() {
        return lote;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getPontoReferencia() {
        return pontoReferencia;
    }

    public void setPontoReferencia(String pontoReferencia) {
        this.pontoReferencia = pontoReferencia;
    }
}
