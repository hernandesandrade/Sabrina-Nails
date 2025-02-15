package com.online.commerce.repositories;

import com.online.commerce.auth.models.User;
import com.online.commerce.models.Carrinho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CarrinhoRepository extends JpaRepository<Carrinho, Long> {
    Optional<Carrinho> findByCliente(User cliente);
}

