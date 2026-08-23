package com.sahra.strava_art.dto;

public record WeatherInfo(double temperatureCelsius, double precipitationMm, int weatherCode) {

    public static WeatherInfo unknown() {
        return new WeatherInfo(Double.NaN, Double.NaN, -1);
    }
}
