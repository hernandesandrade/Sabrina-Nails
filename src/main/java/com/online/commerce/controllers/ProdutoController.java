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
        assert p != null;
        //redirecionar pra pagina de erro depois
        String descricao = p.getDescricao().replace("\n", "<br>");
        model.addAttribute("produto", p);
        model.addAttribute("descricao", descricao);
        return "produto";
    }


}
