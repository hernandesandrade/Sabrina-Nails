package com.online.commerce.services;

import com.online.commerce.models.ImagemProduto;
import com.online.commerce.models.Produto;
import com.online.commerce.repositories.ImagemProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ImagemProdutoService {

    private static final String UPLOAD_DIR = "src/main/resources/static/upload/";

    @Autowired
    private ImagemProdutoRepository imagemProdutoRepository;

    public List<ImagemProduto> getImagens(Produto produto) {
        return getImagens(produto.getId());
    }
    List<ImagemProduto> getImagens(Long id) {
        return imagemProdutoRepository.findByProdutoId(id);
    }

    public List<String> salvarImagens(Produto produto, MultipartFile[] fotos, String imagensExcluidas) {
        List<String> listFails = new ArrayList<>();
        if (!imagensExcluidas.isEmpty()) {
            List<String> removeFail = processarImagensExcluidas(imagensExcluidas);
            listFails.addAll(removeFail);
        }
        List<String> saveFail = salvarImagens(produto, fotos);
        listFails.addAll(saveFail);
        return listFails;
    }

    public List<String> salvarImagens(Produto produto, MultipartFile[] fotos) {
        List<String> listFails = new ArrayList<>();
        for (MultipartFile foto : fotos) {
            try {
                salvarImagem(produto, foto);
            } catch (Exception e) {
                listFails.add(foto.getName());
            }
        }
        return listFails;
    }

    private void salvarImagem(Produto produto, MultipartFile foto) throws IOException {
        if (!foto.isEmpty()) {
            String uuidFileName = produto.getId() + "_" + UUID.randomUUID().toString() + "." + Objects.requireNonNull(foto.getContentType()).split("/")[1];
            Path filePath = Paths.get(UPLOAD_DIR, uuidFileName);
            Files.createDirectories(filePath.getParent());
            foto.transferTo(filePath);
            ImagemProduto imagem = new ImagemProduto();
            imagem.setProduto(produto);
            imagem.setNomeArquivo(uuidFileName); // Salvar o nome UUID gerado
            imagem.setTipoArquivo(foto.getContentType());
            imagemProdutoRepository.save(imagem);
        }
    }

    private void deletarImagem(ImagemProduto imagem) throws IOException {
        imagemProdutoRepository.delete(imagem);
        Path filePath = Paths.get(UPLOAD_DIR, imagem.getNomeArquivo());
        Files.deleteIfExists(filePath);
    }

    private List<String> processarImagensExcluidas(String imagensExcluidas) {
        List<String> listFails = new ArrayList<>();
        List<Long> idsImagensExcluidas = Arrays.stream(imagensExcluidas.split(","))
                .map(Long::parseLong)
                .toList();
        for (Long imagemId : idsImagensExcluidas) {
            ImagemProduto imagemProduto = imagemProdutoRepository.findById(imagemId).orElse(null);
            if (imagemProduto != null) {
                try {
                    deletarImagem(imagemProduto);
                    System.out.println("Imagem deletada: " + imagemProduto.getNomeArquivo());
                } catch (IOException e) {
                    listFails.add(imagemId + "");
                    System.out.println("Erro ao deletar a imagem.");
                }
            }
        }
        return listFails;
    }

}
