package com.manacommunity.parking.dto;

public record StopChargingRequest(
    Double finalMeterReading,
    String stopReason
) {}
