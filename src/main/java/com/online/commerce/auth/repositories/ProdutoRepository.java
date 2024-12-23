package com.online.commerce.auth.repositories;

import com.online.commerce.auth.models.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findAll();

    List<Produto> findByNomeContainingIgnoreCase(String nome);

    List<Produto> findAllById(Long longf);

}
