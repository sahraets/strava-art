package com.sahra.strava_art.service;

import com.sahra.strava_art.dto.WeatherInfo;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    private final RestClient restClient;

    public WeatherService(RestClient restClient) {
        this.restClient = restClient;
    }

    public WeatherInfo getWeatherForActivity(double lat, double lng, Instant activityStart) {
        String date = activityStart.atOffset(ZoneOffset.UTC).toLocalDate().toString();

        Map<String, Object> response = restClient.get()
                .uri("https://archive-api.open-meteo.com/v1/archive"
                        + "?latitude=" + lat
                        + "&longitude=" + lng
                        + "&start_date=" + date
                        + "&end_date=" + date
                        + "&hourly=temperature_2m,precipitation,weathercode"
                        + "&timezone=UTC")
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {
                });
        if (response == null) return WeatherInfo.unknown();

        @SuppressWarnings("unchecked")
        Map<String, Object> hourly = (Map<String, Object>) response.get("hourly");
        if (hourly == null) return WeatherInfo.unknown();

        @SuppressWarnings("unchecked")
        List<String> times = (List<String>) hourly.get("time");
        @SuppressWarnings("unchecked")
        List<Number> temps = (List<Number>) hourly.get("temperature_2m");
        @SuppressWarnings("unchecked")
        List<Number> precs = (List<Number>) hourly.get("precipitation");
        @SuppressWarnings("unchecked")
        List<Number> codes = (List<Number>) hourly.get("weathercode");

        int index = findClosestHourIndex(times, activityStart);
        if (index < 0) return WeatherInfo.unknown();

        return new WeatherInfo(
                temps.get(index).doubleValue(),
                precs.get(index).doubleValue(),
                codes.get(index).intValue()
        );

    }

    private int findClosestHourIndex(List<String> times, Instant activityStart) {
        long bestDiff = Long.MAX_VALUE;
        int bestIndex = -1;
        for (int i = 0; i < times.size(); i++) {
            Instant hourInstant = OffsetDateTime.parse(times.get(i) + ":00Z").toInstant();
            long diff = Math.abs(ChronoUnit.SECONDS.between(activityStart, hourInstant));
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

}