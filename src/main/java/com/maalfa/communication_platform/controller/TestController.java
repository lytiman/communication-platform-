package com.maalfa.communication_platform.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/")
    public String home() {
        return "Mesob Communication Platform Backend is running!";
    }

    @GetMapping("/api/test")
    public String test() {
        return "API connection successful!";
    }
}