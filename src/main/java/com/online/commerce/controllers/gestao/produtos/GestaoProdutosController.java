package com.online.commerce.controllers.gestao.produtos;

import com.online.commerce.models.ImagemProduto;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.ImagemProdutoRepository;
import com.online.commerce.repositories.ProdutoRepository;
import com.online.commerce.services.ProdutoService;
import com.online.commerce.services.ImagemProdutoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/gestao-produtos")
public class GestaoProdutosController {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ImagemProdutoRepository imagemProdutoRepository;

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ImagemProdutoService imagemProdutoService;

    private static final String UPLOAD_DIR = "src/main/resources/static/upload/";

    @GetMapping("")
    public String gestaoProdutos(@RequestParam(name = "pesquisa", required = false) String pesquisa, Model model, @RequestParam(defaultValue = "1") int pagina) {
        Page<Produto> produtosPage = produtoService.listarProdutos(pagina, pesquisa);
        int totalPaginas = produtosPage.getTotalPages();
        if (totalPaginas == 0) {
            totalPaginas = 1;
        }
        int paginasExibidas = 3;
        int startPage = Math.max(pagina - 1, 1);
        int endPage = Math.min(startPage + paginasExibidas - 1, totalPaginas);

        model.addAttribute("produtos", produtosPage.getContent());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("pesquisa", pesquisa);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        return "gestao/produtos/gestaoProdutos";
    }

    @GetMapping("/novo")
    public String novoProduto(Model model) {
        model.addAttribute("produto", new Produto());
        return "gestao/produtos/novoProduto";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@RequestParam("fotos") MultipartFile[] fotos, Produto produto) {
        String msg = produtoService.salvarProduto(fotos, produto);
        System.out.println("=================");
        System.out.println(msg);
        System.out.println();
        return "redirect:/gestao-produtos";
    }

    @GetMapping("/produto/{id}")
    public String exibirDetalhesProduto(@PathVariable("id") Long id, Model model) {
        Produto produto = produtoService.getProduto(id);
        if (produto == null) {
            return "erroProdutoNaoEncontrado";  // Página de erro caso o produto não seja encontrado
        }
        String dataCriacao = produto.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String dataAtualizacao = produto.getAtualizadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        model.addAttribute("dataCriacao", dataCriacao);
        model.addAttribute("dataAtualizacao", dataAtualizacao);
        model.addAttribute("produto", produto);
        return "gestao/produtos/detalhesProduto";  // Página que exibe os detalhes do produto
    }

    @GetMapping("/editar/{id}")
    public String editarProduto(@PathVariable("id") Long id, Model model) {
        Produto produto = produtoService.getProduto(id);
        if (produto == null) {
            return "erroProdutoNaoEncontrado";
        }
        List<ImagemProduto> imagens = produtoService.getImagens(produto);
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);
        return "gestao/produtos/editarProduto";
    }

    @PostMapping("/editar/{id}")
    public String editarProduto(@PathVariable("id") Long id,
                                @RequestParam("fotos") MultipartFile[] fotos,
                                @RequestParam("imagensExcluidas") String imagensExcluidas,
                                Produto produto, RedirectAttributes redirectAttributes) {

        Produto produtoExistente = produtoService.getProduto(id);
        if (produtoExistente == null) {
            redirectAttributes.addFlashAttribute("message", "Produto não encontrado!");
            return "redirect:/gestao-produtos";
        }
        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricao(produto.getDescricao());
        produtoExistente.setPreco(formatarPreco(produto.getPreco()));
        produtoExistente.setPrecoDePor(formatarPreco(produto.getPrecoDePor()));
        produtoExistente.setAtualizadoEm(LocalDateTime.now());
        String msg = produtoService.salvarProduto(fotos, produtoExistente, imagensExcluidas);
        System.out.println(msg);
        return "redirect:/gestao-produtos/produto/" + produtoExistente.getId();
    }

    @GetMapping("/deletar/{id}")
    public String deletarProduto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        Produto p = produtoService.getProduto(id);
        if (p != null) {
            String msg = produtoService.deletar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", msg);
        }else{
            return "erroProdutoNaoEncontrado";
        }
        return "redirect:/gestao-produtos";
    }

    private BigDecimal formatarPreco(BigDecimal precoString) {
        // Remove todos os caracteres não numéricos
        String preco = precoString.toString().replaceAll("\\D", "");

        // Garante que o valor tem pelo menos 2 dígitos
        if (preco.length() <= 2) {
            preco = "0" + preco;
        }

        // Insere o ponto antes dos dois últimos dígitos
        preco = preco.substring(0, preco.length() - 2) + "." + preco.substring(preco.length() - 2);

        return new BigDecimal(preco);
    }


}
