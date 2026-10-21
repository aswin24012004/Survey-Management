package com.healthcare.survey.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WebAppController {
    @GetMapping({"/", "/login", "/admin", "/admin/**", "/survey/**"})
    public String frontend() {
        return "forward:/index.html";
    }

    @GetMapping("/healthz")
    @ResponseBody
    public String health() {
        return "ok";
    }
}
