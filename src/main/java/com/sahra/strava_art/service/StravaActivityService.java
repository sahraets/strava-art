package com.sahra.strava_art.service;

import com.sahra.strava_art.dto.ActivityDto;
import com.sahra.strava_art.dto.RoutePoint;
import com.sahra.strava_art.dto.WeatherInfo;
import com.sahra.strava_art.dto.TokenData;
import com.sahra.strava_art.util.PolylineDecoder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class StravaActivityService {

    private final RestClient restClient = RestClient.create();
    private final StravaAuthService authService;
    private final WeatherService weatherService;

    public StravaActivityService(StravaAuthService authService, WeatherService weatherService) {
        this.authService = authService;
        this.weatherService = weatherService;
    }

    public List<ActivityDto> getActivities() {
        TokenData token = authService.getValidAccessToken();

        List<Map<String, Object>> rawActivities = restClient.get()
                .uri("https://www.strava.com/api/v3/athlete/activities?per_page=30")
                .header("Authorization", "Bearer " + token.accessToken())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {});

        if (rawActivities == null) {
            return List.of();
        }

        return rawActivities.stream()
                .map(this::toActivityDto)
                .filter(Objects::nonNull)
                .toList();
    }
    @SuppressWarnings("unchecked")
    private ActivityDto toActivityDto(Map<String, Object> raw) {
        Map<String, Object> map = (Map<String, Object>) raw.get("map");
        if (map == null) return null;

        String polyline = (String) map.get("summary_polyline");
        if (polyline == null || polyline.isBlank()) return null;

        long id = ((Number) raw.get("id")).longValue();
        String name = (String) raw.get("name");
        String type = (String) raw.get("type");
        String date = (String) raw.get("start_date_local");
        double distance = ((Number) raw.get("distance")).doubleValue();
        double elevation = raw.get("total_elevation_gain") != null
                ? ((Number) raw.get("total_elevation_gain")).doubleValue()
                : 0.0;

        List<RoutePoint> points = PolylineDecoder.decode(polyline);
        WeatherInfo weather = fetchWeather(points, date);

        return new ActivityDto(id, name, type, date, distance, elevation, points, weather);
    }

    private WeatherInfo fetchWeather(List<RoutePoint> points, String date) {
        if (points.isEmpty() || date == null) {
            return WeatherInfo.unknown();
        }
        double centroidLat = points.stream().mapToDouble(RoutePoint::lat).average().orElse(0);
        double centroidLng = points.stream().mapToDouble(RoutePoint::lng).average().orElse(0);
        try {
            Instant activityStart = Instant.parse(date);
            return weatherService.getWeatherForActivity(centroidLat, centroidLng, activityStart);
        } catch (Exception e) {
            return WeatherInfo.unknown();
        }
    }

}
