package com.online.commerce.services;

import com.online.commerce.models.Movimentacao;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.MovimentacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MovimentacaoService {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    public String processar(Long produtoId, int quantidade, boolean movimento){

        System.out.println("id: " + produtoId);

        Produto produto = produtoService.getProduto(produtoId);
        if (produto == null){
            return "false%Produto inexistente.";
        }
        String retorno = "";
        if (movimento){
            produto.setEstoque(produto.getEstoque() + quantidade);
            produtoService.salvarProduto(produto);
            retorno = "true%A entrada de estoque foi feita com sucesso.";
        }else {
            if (quantidade > produto.getEstoque()){
                retorno = "false%Nao é possivel remover mais do que existe no estoque.";
            }else{
                produto.setEstoque(0);
                produtoService.salvarProduto(produto);
                retorno = "true%A saida de estoque foi feita com sucesso.";
            }
        }
        movimentacaoRepository.save(new Movimentacao(produto, quantidade, movimento));
        return retorno;
    }

}
