package com.online.commerce.services;

import com.online.commerce.auth.models.User;
import com.online.commerce.auth.repositories.UserRepository;
import com.online.commerce.auth.services.UserService;
import com.online.commerce.models.Movimentacao;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.MovimentacaoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    public String processar(Movimentacao movimentacao, HttpServletRequest request){
        movimentacao.setUser(userService.getUser(request));
        Produto produto = produtoService.getProduto(movimentacao.getProduto().getId());
        if (produto == null){
            return "false%Produto inexistente.";
        }
        String retorno = "";
        if (movimentacao.isMovimento()){
            produto.setEstoque(produto.getEstoque() + movimentacao.getQuantidade());
            produtoService.salvarProduto(produto);
            movimentacao.setConfirmacao(true);
            retorno = "true%A entrada de estoque foi feita com sucesso.";
        }else {
            if (movimentacao.getQuantidade() > produto.getEstoque()){
                movimentacao.setConfirmacao(false);
                retorno = "false%Nao é possivel remover mais do que existe no estoque.";
            }else{
                produto.setEstoque(produto.getEstoque() - movimentacao.getQuantidade());
                produtoService.salvarProduto(produto);
                movimentacao.setConfirmacao(true);
                retorno = "true%A saida de estoque foi feita com sucesso.";
            }
        }
        movimentacaoRepository.save(movimentacao);
        return retorno;
    }

    public List<Movimentacao> getMovimentacoes(){
        return movimentacaoRepository.findAll();
    }

}
