package com.manacommunity.parking.dto;

import com.manacommunity.parking.enums.ChargerStatus;
import com.manacommunity.parking.model.EvCharger;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EvChargerResponse(
    Long id,
    Long communityId,
    Long parkingSlotId,
    String deviceId,
    String connectorType,
    Double maxKwRating,
    ChargerStatus status,
    BigDecimal currentPowerKw,
    Double latestMeterKwh,
    String lastFaultCode,
    LocalDateTime lastHeartbeatAt
) {
    public static EvChargerResponse from(EvCharger c) {
        return new EvChargerResponse(
            c.getId(),
            c.getCommunityId(),
            c.getParkingSlotId(),
            c.getDeviceId(),
            c.getConnectorType(),
            c.getMaxKwRating(),
            c.getStatus(),
            c.getCurrentPowerKw(),
            c.getLatestMeterKwh(),
            c.getLastFaultCode(),
            c.getLastHeartbeatAt()
        );
    }
}
