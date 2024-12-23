package com.online.commerce.services;

import com.online.commerce.auth.models.ImagemProduto;
import com.online.commerce.auth.models.Produto;
import com.online.commerce.auth.repositories.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + id));
    }

    public void atualizarProduto(Long id, Produto produtoAtualizado, List<MultipartFile> novasImagens, List<Long> imagensExcluidas) throws IOException {
        Produto produtoExistente = buscarPorId(id);

        // Atualiza os campos principais
        produtoExistente.setNome(produtoAtualizado.getNome());
        produtoExistente.setDescricao(produtoAtualizado.getDescricao());
        produtoExistente.setPreco(produtoAtualizado.getPreco());
        produtoExistente.setPrecoDePor(produtoAtualizado.getPrecoDePor());
        produtoExistente.setEstoque(produtoAtualizado.getEstoque());
        produtoExistente.setAtualizadoEm(produtoAtualizado.getAtualizadoEm());

        // Gerencia as imagens a serem removidas
        if (imagensExcluidas != null && !imagensExcluidas.isEmpty()) {
            for (Long imagemId : imagensExcluidas) {
                removerImagemProduto(produtoExistente, imagemId);
            }
        }

        // Manter as imagens existentes e adicionar as novas
        if (novasImagens != null && !novasImagens.isEmpty()) {
            for (MultipartFile novaImagem : novasImagens) {
                salvarImagemProduto(produtoExistente, novaImagem);
            }
        }

        // Salva o produto atualizado no banco de dados
        produtoRepository.save(produtoExistente);
    }

    private void salvarImagemProduto(Produto produto, MultipartFile arquivo) throws IOException {
        if (!arquivo.isEmpty()) {
            // Criação do nome do arquivo no formato {id_produto}_{nome_original}
            String nomeArquivo = produto.getId() + "_" + arquivo.getOriginalFilename();
            nomeArquivo = nomeArquivo.replaceAll("\\s+", "_"); // Remove espaços para evitar problemas

            Path caminho = Paths.get(UPLOAD_DIR, nomeArquivo);
            Files.createDirectories(caminho.getParent());
            Files.write(caminho, arquivo.getBytes());

            ImagemProduto imagem = new ImagemProduto();
            imagem.setProduto(produto);
            imagem.setNomeArquivo(nomeArquivo);
            imagem.setTipoArquivo(Files.probeContentType(caminho));
            produto.getImagens().add(imagem);
        }
    }

    private static final String UPLOAD_DIR = "src/main/resources/static/upload/"; // Caminho para a pasta de uploads

    private void removerImagemProduto(Produto produto, Long imagemId) throws IOException {
        ImagemProduto imagem = produto.getImagens().stream()
                .filter(img -> img.getId().equals(imagemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Imagem não encontrada: " + imagemId));

        // Caminho absoluto para a imagem dentro da pasta de upload
        Path caminho = Paths.get(UPLOAD_DIR, imagem.getNomeArquivo());

        // Remove a imagem do sistema de arquivos
        if (Files.exists(caminho)) {
            Files.delete(caminho); // Remove o arquivo do sistema de arquivos
        } else {
            System.out.println("Arquivo não encontrado no diretório: " + caminho);
        }

        // Remove a imagem do produto (do banco de dados)
        produto.getImagens().remove(imagem); // Remove a imagem do produto
        produtoRepository.save(produto); // Salva a alteração no banco de dados
    }




}