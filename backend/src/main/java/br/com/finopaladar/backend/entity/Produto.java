package br.com.finopaladar.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Entity
@Table(name = "produtos")
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @NotNull
    @Positive
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @NotNull
    @Positive
    @Column(name = "peso_medio_gramas", nullable = false)
    private Integer pesoMedioGramas;

    @NotBlank
    @Size(max = 500)
    @Column(nullable = false, length = 500)
    private String imagem;

    @NotNull
    @Column(nullable = false)
    private Boolean ativo = Boolean.TRUE;

    @OneToOne(mappedBy = "produto", fetch = FetchType.LAZY)
    private DisponibilidadeProduto disponibilidadeProduto;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getPesoMedioGramas() {
        return pesoMedioGramas;
    }

    public void setPesoMedioGramas(Integer pesoMedioGramas) {
        this.pesoMedioGramas = pesoMedioGramas;
    }

    public String getImagem() {
        return imagem;
    }

    public void setImagem(String imagem) {
        this.imagem = imagem;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public DisponibilidadeProduto getDisponibilidadeProduto() {
        return disponibilidadeProduto;
    }

    public void setDisponibilidadeProduto(DisponibilidadeProduto disponibilidadeProduto) {
        this.disponibilidadeProduto = disponibilidadeProduto;
    }
}
