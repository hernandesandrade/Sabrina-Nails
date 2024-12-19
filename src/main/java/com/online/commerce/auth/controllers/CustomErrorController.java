package com.online.commerce.auth.controllers;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

public class CustomErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public String handleError(WebRequest webRequest, Model model) {
        var errorAttributes = this.errorAttributes.getErrorAttributes(webRequest, ErrorAttributeOptions.defaults());
        model.addAttribute("status", errorAttributes.get("status"));
        model.addAttribute("error", errorAttributes.get("error"));
        model.addAttribute("message", errorAttributes.get("message"));

        // Verifica se é erro 404 e redireciona
        if (Integer.parseInt(errorAttributes.get("status").toString()) == 404) {
            return "notFound";
        }

        // Para outros erros, crie uma página genérica ou específica
        return "error";
    }
}
