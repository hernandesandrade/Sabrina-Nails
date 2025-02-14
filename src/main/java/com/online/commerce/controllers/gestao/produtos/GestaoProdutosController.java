package com.online.commerce.controllers.gestao.produtos;

import com.online.commerce.models.ImagemProduto;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.ImagemProdutoRepository;
import com.online.commerce.repositories.ProdutoRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomNumberEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
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

    private static final String UPLOAD_DIR = "src/main/resources/static/upload/";

    @GetMapping("")
    public String gestaoProdutos(@RequestParam(name = "pesquisa", required = false) String pesquisa, Model model, @RequestParam(defaultValue = "1") int pagina) {
        // Se pesquisa não for nula ou vazia, realiza a busca com paginação
        if (pesquisa != null && !pesquisa.trim().isEmpty()) {
            listarProdutos(pagina, pesquisa, model); // Usa a paginação com pesquisa
        } else {
            listarProdutos(pagina, null, model); // Chama o listarProdutos sem filtro de pesquisa
        }
        return "gestao/produtos/gestaoProdutos";
    }
    public void listarProdutos(int pagina,
                               String pesquisa, Model model) {
        // Verifica se a página solicitada é menor que 1 (não pode ser 0 ou negativa)
        int paginaCorrigida = Math.max(pagina - 1, 0);  // Subtrai 1 para ajustar com a lógica de Pageable

        Pageable pageable = PageRequest.of(paginaCorrigida, 10); // 10 produtos por página
        Page<Produto> produtosPage;
        if (pesquisa != null && !pesquisa.trim().isEmpty()) {
            if (pesquisa.matches("\\d+")){
                if (isValidEAN13(pesquisa)){
                    produtosPage = produtoRepository.findAllByCodigoBarras(pesquisa, pageable);
                }else{
                    produtosPage = produtoRepository.findAllById(Long.parseLong(pesquisa), pageable);
                }
            }else{
                produtosPage = produtoRepository.findByNomeContainingIgnoreCase(pesquisa, pageable);
            }
        } else {
            produtosPage = produtoRepository.findAll(pageable); // Caso não haja pesquisa, carrega todos os produtos com paginação
        }

        // Calcula o total de páginas de forma segura (não permite 0)
        int totalPaginas = produtosPage.getTotalPages();
        if (totalPaginas == 0) {
            totalPaginas = 1; // Caso não haja nenhum resultado, tratamos como uma única página
        }
        int paginasExibidas = 3;
        int startPage = Math.max(pagina - 1, 1); // Página inicial (página atual - 1)
        int endPage = Math.min(startPage + paginasExibidas - 1, totalPaginas);

        model.addAttribute("produtos", produtosPage.getContent());
        model.addAttribute("paginaAtual", pagina);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("pesquisa", pesquisa);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
    }

    @GetMapping("/novo")
    public String novoProduto(Model model, HttpServletRequest request){
        model.addAttribute("produto", new Produto());
        return "gestao/produtos/novoProduto";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@RequestParam("fotos") MultipartFile[] fotos, Produto produto, RedirectAttributes redirectAttributes) {
        try {
            if (!isValidEAN13(produto.getCodigoBarras())){
                System.out.println("Esse nao é um codigo de barras valido.");
                return "redirect:/gestao-produtos/novo";
            }
            if (produtoRepository.existsByCodigoBarras(produto.getCodigoBarras())){
                System.out.println("Ja existe um produto com esse codigo de barras.");
                return "redirect:/gestao-produtos/novo";
            }
            // Salvar o produto no banco de dados
            produtoRepository.save(produto);

            // Processar as imagens enviadas
            for (MultipartFile foto : fotos) {
                if (!foto.isEmpty()) {
                    // Gerar um UUID para o nome da imagem
                    String uuidFileName = produto.getId() + "_" + UUID.randomUUID().toString() + "." + Objects.requireNonNull(foto.getContentType()).split("/")[1];
                    Path filePath = Paths.get(UPLOAD_DIR, uuidFileName);

                    // Criar diretórios, se necessário
                    Files.createDirectories(filePath.getParent());

                    // Salvar o arquivo no sistema de arquivos
                    foto.transferTo(filePath);

                    // Criar a referência da imagem no banco de dados
                    ImagemProduto imagem = new ImagemProduto();
                    imagem.setProduto(produto);
                    imagem.setNomeArquivo(uuidFileName); // Salvar o nome UUID gerado
                    imagem.setTipoArquivo(foto.getContentType());

                    // Salvar a imagem no banco de dados
                    imagemProdutoRepository.save(imagem);
                }
            }

            redirectAttributes.addFlashAttribute("message", "Produto salvo com sucesso!");
            return "redirect:/gestao-produtos";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("message", "Erro ao salvar imagens: " + e.getMessage());
            return "redirect:/gestao-produtos";
        }
    }



    @GetMapping("/produto/{id}")
    public String exibirDetalhesProduto(@PathVariable("id") Long id, Model model) {
        Produto produto = produtoRepository.findById(id).orElse(null);

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
    public String editarProduto(@PathVariable("id") Long id, Model model){
        Produto produto = produtoRepository.findById(id).orElse(null);

        if (produto == null) {
            return "erroProdutoNaoEncontrado";  // Página de erro caso o produto não seja encontrado
        }

        // Buscar as imagens associadas ao produto
        List<ImagemProduto> imagens = imagemProdutoRepository.findByProdutoId(id);

        // Adicionar as imagens ao modelo
        model.addAttribute("produto", produto);
        model.addAttribute("imagens", imagens);  // Passa as imagens para o formulário

        return "gestao/produtos/editarProduto";
    }

    @PostMapping("/editar/{id}")
    public String editarProduto(@PathVariable("id") Long id,
                                @RequestParam("fotos") MultipartFile[] fotos,
                                @RequestParam("imagensExcluidas") String imagensExcluidas,
                                Produto produto, RedirectAttributes redirectAttributes) {




        // Buscar o produto existente
        Produto produtoExistente = produtoRepository.findById(id).orElse(null);
        if (produtoExistente == null) {
            redirectAttributes.addFlashAttribute("message", "Produto não encontrado!");
            return "redirect:/gestao-produtos";
        }
        // Atualizar as informações do produto (nome, descrição, preço, etc.)
        produtoExistente.setNome(produto.getNome());
        produtoExistente.setDescricao(produto.getDescricao());
        System.out.println(formatarPreco(produto.getPreco()));
        System.out.println(produtoExistente.getPreco());
        produtoExistente.setPreco(formatarPreco(produto.getPreco()));
        produtoExistente.setPrecoDePor(formatarPreco(produto.getPrecoDePor()));
        produtoExistente.setAtualizadoEm(LocalDateTime.now());

        // Salvar o produto atualizado
        produtoRepository.save(produtoExistente);

        // Processar as imagens para excluir
        if (!imagensExcluidas.isEmpty()) {
            System.out.println(imagensExcluidas);
            List<Long> idsImagensExcluidas = Arrays.stream(imagensExcluidas.split(","))
                    .map(Long::parseLong)
                    .toList();
            for (Long imagemId : idsImagensExcluidas) {
                ImagemProduto imagemProduto = imagemProdutoRepository.findById(imagemId).orElse(null);
                if (imagemProduto != null) {
                    // Remover a imagem do banco
                    imagemProdutoRepository.delete(imagemProduto);

                    // Remover o arquivo físico da imagem
                    Path filePath = Paths.get(UPLOAD_DIR, imagemProduto.getNomeArquivo());
                    try {
                        Files.deleteIfExists(filePath);
                    } catch (IOException e) {
                        redirectAttributes.addFlashAttribute("message", "Erro ao excluir imagem: " + e.getMessage());
                    }
                }
            }
        }

        // Processar as novas imagens
        for (MultipartFile foto : fotos) {
            try {
                if (!foto.isEmpty()) {
                    // Gerar o nome do arquivo
                    String uuidFileName = produto.getId() + "_" + UUID.randomUUID().toString() + "." + Objects.requireNonNull(foto.getContentType()).split("/")[1];
                    Path filePath = Paths.get(UPLOAD_DIR, uuidFileName);
                    System.out.println(uuidFileName);
                    // Criar diretórios, se necessário
                    Files.createDirectories(filePath.getParent());
                    foto.transferTo(filePath);
                    // Criar a referência da imagem no banco de dados
                    ImagemProduto imagem = new ImagemProduto();
                    imagem.setProduto(produtoExistente);
                    imagem.setNomeArquivo(uuidFileName); // Aqui salva o nome do arquivo (caminho relativo)
                    imagem.setTipoArquivo(foto.getContentType());

                    // Salvar no banco de dados
                    imagemProdutoRepository.save(imagem);
                }

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("message", "Erro ao salvar imagem: " + e.getMessage());
                return "redirect:/gestao-produtos/editar/" + produtoExistente.getId();
            }
        }

        // Redirecionar para a página de edição do produto
        return "redirect:/gestao-produtos/produto/" + produtoExistente.getId();
    }

    @GetMapping("/deletar/{id}")
    public String deletarProduto(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Produto p = produtoRepository.findById(id).orElse(null);
            if (p != null && !p.getImagens().isEmpty()) {
                for (ImagemProduto img : p.getImagens()) {
                    Path path = Paths.get(UPLOAD_DIR + img.getNomeArquivo());
                    Files.deleteIfExists(path);
                    System.out.println("deletando: " + img.getNomeArquivo());
                }
            }
            produtoRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Produto deletado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Erro ao deletar o produto.");
        }
        return "redirect:/gestao-produtos";
    }

    public boolean isValidEAN13(String codigoBarras) {
        if (codigoBarras == null || codigoBarras.length() != 13) {
            return false;
        }

        try {
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                int digit = Character.getNumericValue(codigoBarras.charAt(i));
                sum += (i % 2 == 0) ? digit : digit * 3;
            }
            int checkDigit = 10 - (sum % 10);
            if (checkDigit == 10) checkDigit = 0;

            return checkDigit == Character.getNumericValue(codigoBarras.charAt(12));
        } catch (NumberFormatException e) {
            return false; // Não é numérico.
        }
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
