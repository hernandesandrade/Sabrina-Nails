package com.online.commerce.services;

import com.online.commerce.auth.models.User;
import com.online.commerce.auth.repositories.UserRepository;
import com.online.commerce.auth.services.UserService;
import com.online.commerce.models.Movimentacao;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.MovimentacaoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovimentacaoService {

    @Autowired
    private UserService userService;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private UserRepository userRepository;

    public Movimentacao getMovimentacao(Long id){
        return movimentacaoRepository.findById(id).orElseThrow(() -> new RuntimeException("Movimentação nao encontrada."));
    }

    public void processar(Movimentacao movimentacao, HttpServletRequest request) {
        movimentacao.setUser(userService.getUser(request));
        Produto produto = produtoService.getProduto(movimentacao.getProduto().getId());
        if (produto == null) {
            movimentacao.setErro("Produto nao encontrado.");
        }else{
            if (movimentacao.isMovimento()) {
                produto.setEstoque(produto.getEstoque() + movimentacao.getQuantidade());
                produtoService.salvarProduto(produto);
                movimentacao.setConfirmacao(true);
            } else {
                if (movimentacao.getQuantidade() > produto.getEstoque()) {
                    movimentacao.setConfirmacao(false);
                    movimentacao.setErro("Nao é possivel remover mais do que existe no estoque.");
                } else {
                    produto.setEstoque(produto.getEstoque() - movimentacao.getQuantidade());
                    produtoService.salvarProduto(produto);
                    movimentacao.setConfirmacao(true);
                }
            }
        }
        movimentacaoRepository.save(movimentacao);
    }

    public List<Movimentacao> getMovimentacoes() {
        return movimentacaoRepository.findAll();
    }

    public Page<Movimentacao> listarMovimentacoes(int pagina, String pesquisa) {
        int paginaCorrigida = Math.max(pagina - 1, 0);
        Pageable pageable = PageRequest.of(paginaCorrigida, 5);
        return movimentacaoRepository.findAllByOrderByDataDesc(pageable);
    }

}
