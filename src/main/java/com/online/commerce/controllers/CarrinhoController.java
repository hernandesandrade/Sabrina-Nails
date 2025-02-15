package com.online.commerce.controllers;

import com.online.commerce.services.CarrinhoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/carrinho")
public class CarrinhoController {

    @Autowired
    private CarrinhoService carrinhoService;

    @GetMapping
    public String visualizarCarrinho(Model model) {
        model.addAttribute("carrinho", carrinhoService.obterCarrinhoDoCliente());
        return "carrinho"; // Retorna a página HTML chamada "carrinho.html"
    }

    @PostMapping("/adicionar")
    public String adicionarItem(@RequestParam Long produtoId, @RequestParam int quantidade, Model model) {
        carrinhoService.adicionarItem(produtoId, quantidade);
        return "redirect:/carrinho"; // Redireciona para a página do carrinho
    }

    @DeleteMapping("/remover")
    public String removerItem(@RequestParam Long produtoId) {
        carrinhoService.removerItem(produtoId);
        return "redirect:/carrinho";
    }

    @DeleteMapping("/limpar")
    public String limparCarrinho() {
        carrinhoService.limparCarrinho();
        return "redirect:/carrinho";
    }
}

