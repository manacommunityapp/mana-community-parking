package com.manacommunity.parking.dto;

import com.manacommunity.parking.enums.ChargerStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EvLiveTelemetryResponse(
    Long chargerId,
    String deviceId,
    ChargerStatus status,
    BigDecimal currentPowerKw,
    BigDecimal voltage,
    BigDecimal currentAmps,
    BigDecimal temperatureCelsius,
    Double meterReadingKwh,
    BigDecimal stateOfChargePercent,
    LocalDateTime timestamp
) {}
