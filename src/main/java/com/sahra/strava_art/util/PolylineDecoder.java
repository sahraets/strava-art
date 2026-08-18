package com.sahra.strava_art.util;

import com.sahra.strava_art.dto.RoutePoint;

import java.util.ArrayList;
import java.util.List;

/// Google´s encoded polyline algorithm
///
public class PolylineDecoder {
    private PolylineDecoder() {
    }

    public static List<RoutePoint> decode(String encoded) {
        List<RoutePoint> points = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) {
            return points;
        }

        int index = 0, lat = 0, lng = 0;

        while (index < encoded.length()) {
            int shift = 0, result = 0, b;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = (result & 1) != 0 ? ~(result >> 1) : (result >> 1);
            lng += dlng;

            points.add(new RoutePoint(lat / 1e5, lng / 1e5));
        }
        return points;
    }
}


