package com.devcast.fleetmanagement.features.vehicle.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleRoute {
    private Long vehicleId;
    private List<GpsPoint> points;
    private Double totalDistance;
    private Long totalDuration;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GpsPoint {
        private Double latitude;
        private Double longitude;
        private Long timestamp;
    }
}
