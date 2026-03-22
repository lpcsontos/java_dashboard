package dev.lpcsontos.dashboard.modules.user.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api1/me")
public class MeController {

    @GetMapping
    public String me(Authentication auth) {
        return "Hello " + auth.getName();
    }
}
