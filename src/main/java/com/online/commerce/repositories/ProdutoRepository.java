package com.online.commerce.repositories;

import com.online.commerce.models.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    Page<Produto> findByNomeContainingIgnoreCase(String nome, Pageable pageable);

    Page<Produto> findAllById(long l, Pageable pageable);

    Page<Produto> findAllByCodigoBarras(String codigoBarras, Pageable pageable);

    boolean existsByCodigoBarras(String codigoBarras);
}
