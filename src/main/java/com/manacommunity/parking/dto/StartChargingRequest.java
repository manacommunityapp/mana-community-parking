package com.manacommunity.parking.dto;

import jakarta.validation.constraints.NotNull;

public record StartChargingRequest(
    @NotNull Long chargerId,
    Long vehicleId,
    Double currentMeterReading
) {}
