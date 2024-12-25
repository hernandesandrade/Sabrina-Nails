package com.online.commerce.controllers.gestao.produtos;

import com.online.commerce.auth.models.ImagemProduto;
import com.online.commerce.auth.models.Produto;
import com.online.commerce.auth.repositories.ImagemProdutoRepository;
import com.online.commerce.auth.repositories.ProdutoRepository;
import com.online.commerce.services.ProdutoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/gestao-produtos")
public class GestaoProdutosController {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ImagemProdutoRepository imagemProdutoRepository;

    private static final String UPLOAD_DIR = "src/main/resources/static/upload/";

    @GetMapping("")
    public String gestaoProdutos(@RequestParam(name = "pesquisa", required = false) String pesquisa, Model model, HttpServletRequest request){
        List<Produto> produtos;
        long i = 0;
        if (pesquisa != null && !pesquisa.trim().isEmpty()){
            try {
                i = Long.parseLong(pesquisa);
                produtos = produtoRepository.findAllById(i);
                System.out.println("id");
            }catch (Exception e){
                produtos = produtoRepository.findByNomeContainingIgnoreCase(pesquisa);
                System.out.println("nome");
            }
        }else {
            produtos = produtoRepository.findAll(); // Caso contrário, busca todos os produtos
        }
        model.addAttribute("produtos", produtos);
        model.addAttribute("pesquisa", pesquisa);
        return "gestao/produtos/gestaoProdutos";
    }

    @GetMapping("/novo")
    public String novoProduto(Model model, HttpServletRequest request){
        model.addAttribute("produto", new Produto());
        return "gestao/produtos/novoProduto";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@RequestParam("fotos") MultipartFile[] fotos, Produto produto, RedirectAttributes redirectAttributes) {
        try {
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
        String formattedDate = produto.getCriadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        model.addAttribute("formattedDate", formattedDate);
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
        produtoExistente.setPreco(produto.getPreco());
        produtoExistente.setPrecoDePor(produto.getPrecoDePor());

        // Salvar o produto atualizado
        produtoRepository.save(produtoExistente);

        // Processar as imagens para excluir
        if (!imagensExcluidas.isEmpty()) {
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






}
