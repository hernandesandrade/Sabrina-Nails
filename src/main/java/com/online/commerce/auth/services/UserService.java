package com.online.commerce.auth.services;

import com.online.commerce.auth.infra.security.SecurityFilter;
import com.online.commerce.auth.infra.security.TokenService;
import com.online.commerce.auth.models.User;
import com.online.commerce.auth.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SecurityFilter securityFilter;

    public User getUser(HttpServletRequest request) {
        String token = securityFilter.extractTokenFromCookies(request);
        if (token != null) {
            String email = tokenService.validateToken(token);
            if (email != null) {
                return userRepository.findByEmail(email).orElse(null);
            }
        }
        return null;
    }
    public User getUser(String id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElse(null); // Retorna o usuário ou null se não existir
    }
}
