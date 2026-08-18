package com.sahra.strava_art.store;

import com.sahra.strava_art.dto.TokenData;
import org.springframework.stereotype.Component;

@Component
public class StravaTokenStore {

    private TokenData currentToken;

    public void save(TokenData token) {
        this.currentToken = token;
    }

    public TokenData get() {
        return currentToken;
    }

    public boolean isConnected() {
        return currentToken != null;
    }
}
