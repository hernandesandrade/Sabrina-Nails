package com.online.commerce.auth.controllers;

import com.online.commerce.auth.dto.LoginRequestDTO;
import com.online.commerce.auth.dto.RegisterRequestDTO;
import com.online.commerce.auth.infra.security.SecurityFilter;
import com.online.commerce.auth.infra.security.TokenService;
import com.online.commerce.auth.models.User;
import com.online.commerce.auth.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private SecurityFilter securityFilter;

    @Autowired
    private HttpSessionRequestCache requestCache;

    // Exibe a página de login
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        return "login";
    }

    @GetMapping("/cadastrar")
    public String reg(Model model, HttpServletRequest request){
        return "cadastrar";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute LoginRequestDTO loginRequestDTO, Model model, HttpServletResponse response, HttpServletRequest request) {
        User user = userRepository.findByEmail(loginRequestDTO.email())
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));
        if (!passwordEncoder.matches(loginRequestDTO.password(), user.getPassword())) {
            model.addAttribute("error", "Credenciais inválidas");
            return "login"; // Se as credenciais forem inválidas, retorna para a página de login
        }

        String token = tokenService.generateToken(user);

        Cookie cookie = new Cookie("auth_token", token);
        cookie.setHttpOnly(true); // Aumenta a segurança
        cookie.setPath("/"); // Define o caminho do cookie
        response.addCookie(cookie);

        // Salva o request original, para redirecionar após o login
        SavedRequest savedRequest = requestCache.getRequest(request, response);
        if (savedRequest != null) {
            return "redirect:" + savedRequest.getRedirectUrl();
        } else {
            return "redirect:/"; // Se não houver página anterior, redireciona para o home
        }
    }

    @PostMapping("/cadastrar")
    public String register(@ModelAttribute RegisterRequestDTO registerRequestDTO, Model model) {
        if (userRepository.findByEmail(registerRequestDTO.email()).isPresent()) {
            model.addAttribute("error", "Email já cadastrado");
            return "cadastrar";
        }
        User user = new User();
        user.setName(registerRequestDTO.name());
        user.setEmail(registerRequestDTO.email());
        user.setPassword(passwordEncoder.encode(registerRequestDTO.password()));
        userRepository.save(user);
        model.addAttribute("message", "Cadastro realizado com sucesso!");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(){
        return "redirect:inicio";
    }


}
