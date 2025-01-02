package com.online.commerce.repositories;

import com.online.commerce.models.ImagemProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagemProdutoRepository extends JpaRepository<ImagemProduto, Long> {
    List<ImagemProduto> findByProdutoId(Long produtoId);
    void deleteByProdutoId(Long produtoId);

}

