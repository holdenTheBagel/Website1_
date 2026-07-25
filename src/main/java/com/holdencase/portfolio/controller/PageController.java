package com.holdencase.portfolio.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/services")
    public String services() {
        return "services";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}
