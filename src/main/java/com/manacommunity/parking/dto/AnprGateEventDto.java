package com.manacommunity.parking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AnprGateEventDto(
    Long id,
    Long communityId,
    @NotBlank String gateId,
    @NotBlank String rawLicensePlate,
    String normalizedLicensePlate,
    @NotBlank String eventType,
    @NotNull Double confidence,
    Boolean barrierOpened,
    String matchType,
    Long matchedRefId,
    String snapshotUrl,
    LocalDateTime timestamp
) {}
