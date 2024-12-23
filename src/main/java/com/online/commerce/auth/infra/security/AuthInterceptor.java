package com.online.commerce.auth.infra.security;

import com.online.commerce.auth.models.User;
import com.online.commerce.auth.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SecurityFilter securityFilter;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView mv) throws Exception {
        if (mv != null) {
            checkAuth(mv, request);
        }
    }

    private void checkAuth(ModelAndView model, HttpServletRequest request) {
        String token = securityFilter.extractTokenFromCookies(request);
        if (token != null) {
            String email = tokenService.validateToken(token);
            if (email != null) {
                Optional<User> userOptional = userRepository.findByEmail(email);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();
                    model.addObject("userName", user.getName().split(" ")[0]);
                    model.addObject("user", user);
                    model.addObject("rule", user.getRule());
                }
            }
        }
    }
}

