package com.online.commerce.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

public class GeralController {

    @GetMapping("")
    public String inicio(){
        return "inicio";
    }

    @GetMapping("login")
    public String login(){
        return "login";
    }

    @GetMapping("cadastrar")
    public String registrar(){
        return "cadastrar";
    }

}
