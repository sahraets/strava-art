package com.sahra.strava_art.controller;

import com.sahra.strava_art.service.StravaAuthService;
import com.sahra.strava_art.store.StravaTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;



@RestController
public class StravaAuthController {

    private final StravaAuthService authService;
    private final StravaTokenStore tokenStore;

    public StravaAuthController(StravaAuthService authService, StravaTokenStore tokenStore) {
        this.authService = authService;
        this.tokenStore = tokenStore;
    }

    @GetMapping("/login")
    public RedirectView login() {
        return new RedirectView(authService.buildAuthorizationUrl());
    }

    @GetMapping("/exchange_token")
    public RedirectView exchangeToken(@RequestParam("code") String code) {
        authService.exchangeCodeForToken(code);
        return new RedirectView("/");
    }

    @GetMapping("/api/status")
    public Map<String, Boolean> status() {
        return Map.of("connected", tokenStore.isConnected());
    }
}
