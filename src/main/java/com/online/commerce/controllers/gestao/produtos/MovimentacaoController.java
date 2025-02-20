package com.online.commerce.controllers.gestao.produtos;

import com.online.commerce.models.Movimentacao;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.MovimentacaoRepository;
import com.online.commerce.services.MovimentacaoService;
import com.online.commerce.services.ProdutoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/gestao-produtos/movimentacao")
@Controller
public class MovimentacaoController {

    @Autowired
    ProdutoService produtoService;

    @Autowired
    MovimentacaoService movimentacaoService;

    @Autowired
    MovimentacaoRepository movimentacaoRepository;

    @GetMapping
    public String view(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model){
        Page<Movimentacao> movimentacoes = movimentacaoRepository.findAll(PageRequest.of(page, size));

        // Adiciona a lista de movimentações ao modelo
        model.addAttribute("movimentacoes", movimentacoes);
        model.addAttribute("movimentacao", new Movimentacao());
        model.addAttribute("movimentacoes", movimentacaoService.getMovimentacoes());
        return "gestao/produtos/movimentacao";
    }

    @GetMapping("/produto/{codigo}")
    @ResponseBody
    public ResponseEntity<Produto> buscarProduto(@PathVariable String codigo) {
        Produto produto = produtoService.getProduto(codigo);
        return (produto != null) ? ResponseEntity.ok(produto) : ResponseEntity.notFound().build();
    }


    @PostMapping("/processar")
    public String processarMovimentacao(@ModelAttribute("movimentacao") Movimentacao movimentacao, Model model, HttpServletRequest request) {
        String retorno = movimentacaoService.processar(movimentacao, request);
        boolean confirmacao = Boolean.parseBoolean(retorno.split("%")[0]);
        String mensagem = retorno.split("%")[1];
        model.addAttribute("mensagem", mensagem);
        model.addAttribute("estado", confirmacao);
        return "redirect:/gestao-produtos/movimentacao";
    }


}
