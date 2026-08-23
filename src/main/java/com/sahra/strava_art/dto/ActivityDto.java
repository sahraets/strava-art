package com.sahra.strava_art.dto;

import java.util.List;

public record ActivityDto(
        long id,
        String name,
        String type,
        String date,
        double distanceMeters,
        double elevationGainMeters,
        List<RoutePoint> points,
        WeatherInfo weatherInfo
) {
}
