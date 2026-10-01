package com.manacommunity.parking.dto;

import com.manacommunity.parking.model.EvChargingSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EvChargingSessionResponse(
    Long id,
    Long communityId,
    Long chargerId,
    String chargerDeviceId,
    Long residentId,
    Long vehicleId,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    Double startMeterKwh,
    Double endMeterKwh,
    Double totalKwh,
    BigDecimal totalCost,
    String status,
    Long cfbosWalletTransactionId,
    String stopReason
) {
    public static EvChargingSessionResponse from(EvChargingSession s) {
        return new EvChargingSessionResponse(
            s.getId(),
            s.getCommunityId(),
            s.getCharger() != null ? s.getCharger().getId() : null,
            s.getCharger() != null ? s.getCharger().getDeviceId() : null,
            s.getResidentId(),
            s.getVehicleId(),
            s.getStartedAt(),
            s.getEndedAt(),
            s.getStartMeterKwh(),
            s.getEndMeterKwh(),
            s.getTotalKwh(),
            s.getTotalCost(),
            s.getStatus(),
            s.getCfbosWalletTransactionId(),
            s.getStopReason()
        );
    }
}
