package com.online.commerce.services;

import com.online.commerce.auth.models.User;
import com.online.commerce.auth.repositories.UserRepository;
import com.online.commerce.models.Carrinho;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.CarrinhoRepository;
import com.online.commerce.repositories.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class CarrinhoService {
    @Autowired
    private CarrinhoRepository carrinhoRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UserRepository userRepository;

    public User getUsuarioLogado() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByName(username); // Ajuste conforme seu repositório
        }

        return null; // Caso o usuário não esteja autenticado
    }

    public Carrinho obterCarrinhoDoCliente() {
        User cliente = getUsuarioLogado();
        return carrinhoRepository.findByCliente(cliente)
                .orElseGet(() -> {
                    Carrinho novoCarrinho = new Carrinho();
                    novoCarrinho.setCliente(cliente);
                    return carrinhoRepository.save(novoCarrinho);
                });
    }

    public Carrinho adicionarItem(Long produtoId, int quantidade) {
        Carrinho carrinho = obterCarrinhoDoCliente();
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        carrinho.adicionarItem(produto, quantidade);
        return carrinhoRepository.save(carrinho);
    }

    public Carrinho removerItem(Long produtoId) {
        Carrinho carrinho = obterCarrinhoDoCliente();
        carrinho.removerItem(produtoId);
        return carrinhoRepository.save(carrinho);
    }

    public void limparCarrinho() {
        Carrinho carrinho = obterCarrinhoDoCliente();
        carrinho.getItens().clear();
        carrinhoRepository.save(carrinho);
    }
}

