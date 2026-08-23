package com.sahra.strava_art.controller;
import com.sahra.strava_art.dto.WeatherInfo;
import com.sahra.strava_art.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class WeatherTestController {
    private final WeatherService weatherService;

    public WeatherTestController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    @GetMapping(value = "/api/weather-test", produces = "application/json; charset=UTF-8")
    public WeatherInfo test(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam String date
    ) {
        return weatherService.getWeatherForActivity(lat, lng, Instant.parse(date));
    }
}
