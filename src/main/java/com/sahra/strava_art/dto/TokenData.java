package com.sahra.strava_art.dto;

public record TokenData(String accessToken, String refreshToken, long expiresAt) {

    public boolean isExpired() {
        long now = System.currentTimeMillis() / 1000;
        return expiresAt <= now;
    }
}
