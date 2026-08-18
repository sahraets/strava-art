package com.sahra.strava_art.controller;

import com.sahra.strava_art.dto.ActivityDto;
import com.sahra.strava_art.service.StravaActivityService;
import com.sahra.strava_art.service.StravaAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StravaActivityController {

    private final StravaActivityService activityService;

    public StravaActivityController(StravaActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping(value = "/api/activities", produces = "application/jon; charset=UTF-8")
    public List<ActivityDto> activities() {
        return activityService.getActivities();
    }
}
