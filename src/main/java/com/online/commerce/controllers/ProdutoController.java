package com.online.commerce.controllers;

import com.online.commerce.models.Produto;
import com.online.commerce.repositories.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProdutoController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("/produto/{id}")
    public String produto(@PathVariable("id") long id, Model model){
        Produto p = produtoRepository.findById(id).orElse(null);
        model.addAttribute("produto", p);
        return "produto";
    }


}
