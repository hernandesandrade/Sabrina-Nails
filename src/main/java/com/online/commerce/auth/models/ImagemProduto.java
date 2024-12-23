package com.online.commerce.auth.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "imagens_produto")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImagemProduto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(nullable = false)
    private String nomeArquivo;

    @Column(nullable = false)
    private String tipoArquivo;

    public String getUrl() {
        return "/upload/" + id + "_" + nomeArquivo + "." + tipoArquivo;
    }
}
