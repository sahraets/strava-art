package com.sahra.strava_art.service;

import com.sahra.strava_art.dto.TokenData;
import com.sahra.strava_art.store.StravaTokenStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.core.ParameterizedTypeReference;

import java.util.Map;

@Service
public class StravaAuthService {

    @Value("${strava.client-id}")
    private String clientId;

    @Value("${strava.client-secret}")
    private String clientSecret;

    @Value("${strava.redirect-uri}")
    private String redirectUri;

    private final RestClient restClient = RestClient.create();
    private final StravaTokenStore tokenStore;

    public StravaAuthService(StravaTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    public String buildAuthorizationUrl() {
        return "https://www.strava.com/oauth/authorize"
                + "?client_id=" + clientId
                + "&response_type=code"
                + "&redirect_uri=" + redirectUri
                + "&approval_prompt=auto"
                + "&scope=activity:read_all";
    }

    public void exchangeCodeForToken(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("code", code);
        formData.add("grant_type", "authorization_code");

        Map<String, Object> response = restClient.post()
                .uri("https://www.strava.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Fikk tomt svar fra Strava ved token-bytte. Koden kan allerede være brukt — prøv å logge inn på nytt via /login.");
        }

        String accessToken = (String) response.get("access_token");
        String refreshToken = (String) response.get("refresh_token");
        long expiresAt = ((Number) response.get("expires_at")).longValue();

        tokenStore.save(new TokenData(accessToken, refreshToken, expiresAt));
    }

    public TokenData getValidAccessToken() {
        TokenData token = tokenStore.get();
        if (token == null) {
            throw new IllegalStateException("Ikke logget inn ennå. Besøk /login først");
        }
        if (token.isExpired()) {
            token = refreshAccessToken(token.refreshToken());
        }
        return token;
    }

    private TokenData refreshAccessToken(String refreshToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);
        formData.add("grant_type", "refresh_token");

        Map<String, Object> response = restClient.post()
                .uri("https://www.strava.com/oauth/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Fikk tomt svar fra Strava ved fornying av token");
        }

        String accessToken = (String) response.get("access_token");
        String newRefreshToken = (String) response.get("refresh_token");
        long expiresAt = ((Number) response.get("expires_at")).longValue();

        TokenData fresh = new TokenData(accessToken, newRefreshToken, expiresAt);
        tokenStore.save(fresh);
        return fresh;
    }



}


