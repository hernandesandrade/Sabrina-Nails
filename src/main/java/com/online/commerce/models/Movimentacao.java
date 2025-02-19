package com.online.commerce.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "movimentacoes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime data = LocalDateTime.now();

    private int quantidade;
    private boolean movimento;
    private boolean status;

    private String motivo;

    public Movimentacao(Produto produto, int quantidade, boolean movimento){
        this.produto = produto;
        this.quantidade = quantidade;
        this.movimento = movimento;
    }

}
