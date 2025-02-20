package com.online.commerce.services;

import com.online.commerce.models.ImagemProduto;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.ImagemProdutoRepository;
import com.online.commerce.repositories.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ImagemProdutoRepository imagemProdutoRepository;

    @Autowired
    private ImagemProdutoService imagemProdutoService;

    private static final String UPLOAD_DIR = "src/main/resources/static/upload/";

    public Produto getProduto(Long id) {
        return produtoRepository.findById(id).orElse(null);
    }

    public Produto getProduto(String codigo) {
        return produtoRepository.findByCodigoBarras(codigo);

    }

    public List<ImagemProduto> getImagens(Produto produto) {
        return imagemProdutoService.getImagens(produto);
    }

    public Page<Produto> listarProdutos(int pagina,
                                        String pesquisa) {
        int paginaCorrigida = Math.max(pagina - 1, 0);
        Pageable pageable = PageRequest.of(paginaCorrigida, 9);
        if (pesquisa != null && !pesquisa.trim().isEmpty()) {
            if (pesquisa.matches("\\d+")) {
                if (isValidEAN13(pesquisa)) {
                    return produtoRepository.findByCodigoBarras(pesquisa, pageable);
                } else {
                    return produtoRepository.findById(Long.parseLong(pesquisa), pageable);
                }
            } else {
                return produtoRepository.findByNomeContainingIgnoreCase(pesquisa, pageable);
            }
        } else {
            return produtoRepository.findAll(pageable);
        }
    }

    public void salvarProduto(Produto produto) {
        produtoRepository.save(produto);
    }

    public String salvarProduto(MultipartFile[] fotos, Produto produto) {
        return salvarProduto(fotos, produto, "");
    }

    public String salvarProduto(MultipartFile[] fotos, Produto produto, String imagensExcluidas) {
        if (!isValidEAN13(produto.getCodigoBarras())) {
            return "Esse nao é um codigo de barras valido.";
        }
        if (produtoRepository.existsByCodigoBarras(produto.getCodigoBarras())) {
            if (!getProduto(produto.getCodigoBarras()).getId().equals(produto.getId())) {
                return "Ja existe um produto com esse codigo de barras.";
            }
        }
        produtoRepository.save(produto);
        List<String> listImagesFails = imagemProdutoService.salvarImagens(produto, fotos, imagensExcluidas);
        if (!listImagesFails.isEmpty()) {
            return "Não foi possível salvar as seguintes imagens: " + String.join(", ", listImagesFails);
        }
        return "Produto salvo com sucesso!";
    }

    public String deletar(Long id) {
        produtoRepository.deleteById(id);
        List<ImagemProduto> imagens = imagemProdutoService.getImagens(id);
        if (!imagens.isEmpty()) {
            for (ImagemProduto img : imagens) {
                try {
                    Path path = Paths.get(UPLOAD_DIR + img.getNomeArquivo());
                    Files.deleteIfExists(path);
                } catch (Exception e) {
                    return "Erro ao deletar a imagem: " + img.getId();
                }
            }
        }
        return "produto excluido com sucesso.";
    }

    public void adicionarEstoque(Produto produto, int quantidade) {
        if (produto != null && quantidade > 0) {
            produto.setEstoque(produto.getEstoque() + quantidade);
            produtoRepository.save(produto);  // Salva o produto com a nova quantidade no banco de dados
        } else {
            throw new IllegalArgumentException("Produto ou quantidade inválidos");
        }
    }

    // Método para remover estoque
    public void removerEstoque(Produto produto, int quantidade) {
        if (produto != null && quantidade > 0) {
            // Verifica se há quantidade suficiente no estoque
            if (produto.getEstoque() >= quantidade) {
                produto.setEstoque(produto.getEstoque() - quantidade);
                produtoRepository.save(produto);  // Salva o produto com a quantidade atualizada
            } else {
                throw new IllegalArgumentException("Estoque insuficiente para a operação");
            }
        } else {
            throw new IllegalArgumentException("Produto ou quantidade inválidos");
        }
    }

    private boolean isValidEAN13(String codigoBarras) {
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
}
