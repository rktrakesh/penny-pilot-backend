package com.pennypilot.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/healthCheck")
    public String healthCheck() {
        return "Penny Pilot application running.....";
    }

    @GetMapping("/test")
    public String test() {
        return "Penny Pilot application testing successfully.....";
    }

}
