package com.online.commerce.controllers;

import com.online.commerce.models.Produto;
import com.online.commerce.services.CarrinhoService;
import com.online.commerce.services.ProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private CarrinhoService carrinhoService;

    @GetMapping("/produto/{id}")
    public String produto(@PathVariable("id") long id, Model model){
        Produto p = produtoService.getProduto(id);
        assert p != null;
        //redirecionar pra pagina de erro depois
        model.addAttribute("produto", p);
        model.addAttribute("descricao", p.getDescricao());
        return "produto";
    }

    @PostMapping("/produto/adicionar")
    public String adicionarItem(@RequestParam Long produtoId, @RequestParam int quantidade, Model model) {
        carrinhoService.adicionarItem(produtoId, quantidade);
        return "redirect:/carrinho"; // Redireciona para a página do carrinho
    }


}
