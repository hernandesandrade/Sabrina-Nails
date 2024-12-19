package com.online.commerce.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GeralController {

    @GetMapping("")
    public String inicio(){
        return "inicio";
    }

    @GetMapping("entrar")
    public String login(){
        return "entrar";
    }

    @GetMapping("cadastrar")
    public String registrar(){
        return "cadastrar";
    }

}
