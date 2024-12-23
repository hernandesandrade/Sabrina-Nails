package com.online.commerce.controllers;

import com.online.commerce.auth.models.Produto;
import com.online.commerce.auth.repositories.ProdutoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class GeralController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @GetMapping("")
    public String home(Model model, HttpServletRequest request) {
        return "inicio";
    }

    @GetMapping("/produtos")
    public String listarProdutos(Model model, HttpServletRequest request){
        List<Produto> produtos = produtoRepository.findAll();
        model.addAttribute("produtos", produtos);
        return "produtos"; // Nome do arquivo HTML Thymeleaf
    }

}
